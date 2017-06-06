package uc.balltree;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by keltp on 2017-06-05.
 */
public class BallTree {
    private final int maxInstancesInLeaf = 40;
    private final double  maxRelativeLeafRadius = 0.001;

    private BallNode rootNode;

    private INDArray indicies;
    private DistanceFunction<Integer, Double> func;
    private INDArray data;

    private int nNodes;
    private int nLeaves;
    private int maxDepth;


    public BallTree(DistanceFunction<Integer, Double> func, INDArray data) {
        this.func = func;
        this.data = data;
    }

    public void buildTree() throws Exception {
        if(data == null) {
            throw new Exception("No data is supplied.");
        }

        nNodes = 0;
        maxDepth = 0;
        nLeaves = 1;
        indicies = Nd4j.arange(0, data.rows());
        rootNode = new BallNode(0, indicies.length() - 1, 0);
        rootNode.pivot = BallNode.calculateCentroid(indicies, data);
        rootNode.radius = BallNode.calculateRadius(indicies, data, rootNode.pivot, func);
        splitNodes(rootNode, maxDepth + 1, rootNode.radius);
    }

    private void splitNodes(BallNode node, int depth, double rootRadius) throws Exception {
        if(node.n <= maxInstancesInLeaf || rootRadius == 0 || (node.radius / rootRadius) < maxRelativeLeafRadius) {
            return;
        }

        nLeaves--;
        splitNodes(node, nNodes);
        nNodes += 2;
        nLeaves += 2;

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
        INDArray furthest1 = null;
        INDArray furthest2 = null;
        INDArray pivot = node.pivot;

        Pair<Integer, Double> p1 = func.maxDistance(data, pivot);
        furthest1 = data.getRow(p1.getFirst()).dup();

        INDArray distList = func.distances(data.get(NDArrayIndex.interval(node.start, node.start + node.n, false)), furthest1);
        furthest2 = data.getRow(Nd4j.getExecutioner().execAndReturn(new IMax(distList)).getFinalResult()).dup();

        int nRight = 0;
        double curDist = 0.0;
        for(int i = 0;i < node.n - nRight;i++) {
            INDArray rowDatum = data.getRow(i + node.start);
            curDist = func.distance(furthest2, rowDatum);
            double _dist = distList.getDouble(i);
            if(curDist < _dist) {
                swap(indicies, node.end - nRight, node.start + i);
                swap(distList, distList.length() - 1 - nRight, i);
                nRight++;
                i--;
            }
        }
        if ( nRight <= 0 || nRight >= node.n) {
            throw new Exception("Illegal value for nRight: " + nRight);
        }

        node.left = new BallNode(node.start, node.end - nRight, numNodesCreated + 1,
                BallNode.calculateCentroid(node.start, node.end - nRight, indicies, data),
                BallNode.calculateRadius(node.start, node.end - nRight, indicies, data, pivot, func));

        node.right = new BallNode(node.end - nRight + 1, node.end, numNodesCreated + 2,
                BallNode.calculateCentroid(node.end - nRight + 1, node.end, indicies, data),
                BallNode.calculateRadius(node.end - nRight + 1, node.end, indicies, data, pivot, func));
    }


    public List<Pair<Double, INDArray>> knn(INDArray target, int k) throws Exception {
        Heap heap = new Heap(k);
        knn(heap, rootNode, target, k);
        List<Pair<Double, INDArray>> neighbors = new ArrayList<>();

        while(heap.noOfKthNearest() > 0) {
            HeapElement h = heap.getKthNearest();
            neighbors.add(new Pair<>(h.distance, data.getRow(h.index)));
        }
        while(heap.size() > 0) {
            HeapElement h = heap.get();
            neighbors.add(new Pair<>(h.distance, data.getRow(h.index)));
        }
        return neighbors;
    }


    public List<Pair<Double, INDArray>> nn(INDArray target) throws Exception {
        return knn(target, 1);
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
                int index = indicies.getInt(i);
                if(target.neq(data.getRow(index)).sumNumber().doubleValue() == 0) {
                    continue;
                }
                if(heap.totalSize() < k) {
                    dist = func.distance(target, data.getRow(index));
                    heap.put(indicies.getInt(i), dist);
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
