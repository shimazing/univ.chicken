
package uc.balltree;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;
import uc.distance.DistanceFunction;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keltp on 2017-06-05.
 */
public class BallTree {
    private final int maxInstancesInLeaf = 40;
    private final double  maxRelativeLeafRadius = 0.001;

    private BallNode rootNode;

    private INDArray indices;
    private DistanceFunction<Integer, Double> func;
    private INDArray data;

    private int nNodes;
    private int maxDepth;

    private BallTree(DistanceFunction<Integer, Double> func, INDArray data) {
        this.func = func;
        this.data = data;
    }

    public static BallTree buildTree(DistanceFunction<Integer, Double> func, INDArray data) throws Exception {
        BallTree tree = new BallTree(func, data);
        tree.buildTree();
        return tree;
    }

    public void buildTree() throws Exception {

        if(data == null) {
            throw new Exception("No data is supplied.");
        }

        nNodes = 0;
        maxDepth = 0;
        indices = Nd4j.arange(0, data.rows());
        rootNode = new BallNode(0, indices.length() - 1, 0);
        rootNode.pivot = BallNode.calculateCentroid(indices, data);
        rootNode.radius = BallNode.calculateRadius(indices, data, rootNode.pivot, func);
        splitNodes(rootNode, maxDepth + 1, rootNode.radius);
    }

    private void splitNodes(BallNode node, int depth, double rootRadius) throws Exception {
        if(node.n <= maxInstancesInLeaf || rootRadius == 0 || (node.radius / rootRadius) < maxRelativeLeafRadius) {
            return;
        }

        splitNodes(node, nNodes);
        nNodes += 2;

        if(maxDepth < depth) {
            maxDepth = depth;
        }

        splitNodes(node.left, depth + 1, rootRadius);
        splitNodes(node.right, depth + 1, rootRadius);
    }

    private void swap(INDArray array, int n1, int n2) {
        double temp = array.getDouble(n1);
        array.putScalar(n1, array.getDouble(n2));
        array.putScalar(n2, temp);
    }

    private void splitNodes(BallNode node, int numNodesCreated) throws Exception {
        INDArray furthest1;
        INDArray furthest2;
        INDArray pivot = node.pivot;
        INDArray _tempIndex;
        SpecifiedIndex index;

        _tempIndex = indices.get(NDArrayIndex.interval(node.start, node.end, true));
        index = new SpecifiedIndex(_tempIndex.data().asInt());

        Pair<Integer, Double> p1 = func.maxDistance(data.get(index, NDArrayIndex.all()), pivot);
        furthest1 = data.getRow(index.getIndexes()[p1.getFirst()]).dup();

        _tempIndex = indices.get(NDArrayIndex.interval(node.start, node.start + node.n));
        index = new SpecifiedIndex(_tempIndex.data().asInt());

        INDArray distList = func.distances(data.get(index, NDArrayIndex.all()), furthest1);
        int maxDistIndex = Nd4j.getExecutioner().execAndReturn(new IMax(distList)).getFinalResult();
        furthest2 = data.getRow(index.getIndexes()[maxDistIndex]).dup();

        int nRight = 0;
        double curDist = 0.0;
        for(int i = 0;i < node.n - nRight;i++) {
            INDArray rowDatum = data.getRow(indices.getInt(i + node.start));
            curDist = func.distance(furthest2, rowDatum);
            double _dist = distList.getDouble(i);
            if(curDist < _dist) {
                swap(indices, node.end - nRight, node.start + i);
                swap(distList, distList.length() - 1 - nRight, i);
                nRight++;
                i--;
            }
        }
        if ( nRight <= 0 || nRight >= node.n) {
            throw new Exception("Illegal value for nRight: " + nRight);
        }

        node.left = new BallNode(node.start, node.end - nRight, numNodesCreated + 1,
                BallNode.calculateCentroid(node.start, node.end - nRight, indices, data),
                BallNode.calculateRadius(node.start, node.end - nRight, indices, data, pivot, func));

        node.right = new BallNode(node.end - nRight + 1, node.end, numNodesCreated + 2,
                BallNode.calculateCentroid(node.end - nRight + 1, node.end, indices, data),
                BallNode.calculateRadius(node.end - nRight + 1, node.end, indices, data, pivot, func));
    }


    public List<Pair<Double, Integer>> knn(INDArray target, int k) throws Exception {
        Heap heap = new Heap(k);
        knn(heap, rootNode, target, k);
        List<Pair<Double, Integer>> neighbors = new ArrayList<>();

        while(heap.noOfKthNearest() > 0) {
            HeapElement h = heap.getKthNearest();
            neighbors.add(new Pair<>(h.distance, h.index));
        }
        while(heap.size() > 0) {
            HeapElement h = heap.get();
            neighbors.add(new Pair<>(h.distance, h.index));
        }
        return neighbors;
    }

    public boolean contains(INDArray target) {
        if(data != null) {

        }
    }

    public Pair<Double, Integer> nn(INDArray target) throws Exception {
        List<Pair<Double, Integer>> nn = knn(target, 1);
        if(nn != null && nn.size() > 0) {
            return nn.get(0);
        }
        return null;
    }

    private void knn(Heap heap, BallNode node, INDArray target, int k) throws Exception {
        double dist = Double.NEGATIVE_INFINITY;
        if (heap.totalSize() >= k) {
            dist = func.distance(target, node.pivot);
        }

        if(dist > -0.00000001 && Math.sqrt(heap.peek().distance) < dist - node.radius) {
            return;
        }

        if (node.left != null && node.right != null) {
            double leftDist = Math.sqrt(func.distance(target, node.left.pivot));
            double rightDist = Math.sqrt(func.distance(target, node.right.pivot));
            double leftBallDist = leftDist - node.left.radius;
            double rightBallDist = rightDist - node.right.radius;

            if(leftBallDist < 0 && rightBallDist < 0) {
                if(leftDist < rightDist) {
                    knn(heap, node.left, target, k);
                    knn(heap, node.right, target, k);
                } else {
                    knn(heap, node.right, target, k);
                    knn(heap, node.left, target, k);
                }
            } else {
                if(leftBallDist < rightBallDist) {
                    knn(heap, node.left, target, k);
                    knn(heap, node.right, target, k);
                } else {
                    knn(heap, node.right, target, k);
                    knn(heap, node.left, target, k);
                }
            }
        } else if (node.left != null || node.right != null) {
            throw new Exception("Only one leaf is assigned");
        } else {
            for(int i = node.start; i <= node.end; i++) {
                int index = indices.getInt(i);
                if(target.neq(data.getRow(index)).sumNumber().doubleValue() == 0) {
                    continue;
                }
                if(heap.totalSize() < k) {
                    dist = func.distance(target, data.getRow(index));
                    heap.put(index, dist);
                } else {
                    HeapElement head = heap.peek();
                    dist = func.distance(target, data.getRow(index));
                    if(dist < head.distance) {
                        heap.putBySubstitute(index, dist);
                    } else if (dist == head.distance) {
                        heap.putKthNearest(index, dist);
                    }
                }
            }
        }
    }
}
