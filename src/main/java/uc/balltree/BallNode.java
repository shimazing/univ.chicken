package uc.balltree;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Mean;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;
import org.nd4j.shade.jackson.databind.node.DoubleNode;
import uc.distance.DistanceFunction;

/**
 * Created by keltp on 2017-06-05.
 */
public class BallNode {
    protected int start;
    protected int end;
    protected int n;
    protected int id;
    protected BallNode left = null;
    protected BallNode right = null;
    protected double radius;

    protected INDArray pivot;

    public BallNode(int id) {
        this.id = id;
    }

    public BallNode(int start, int end, int id) {
        this.start = start;
        this.end = end;
        this.id = id;
        this.n = end - start + 1;
    }

    public BallNode(int start, int end, int id, INDArray pivot, double radius) {
        this.start = start;
        this.end = end;
        this.id = id;
        this.n = end - start + 1;
        this.pivot = pivot;
        this.radius = radius;
    }

    public String toString() {
        return String.format("start: %s / end: %s / id: %s / n: %s / pivot: %s / radius: %s", start, end, id, n, pivot.toString(), radius);
    }

    public boolean isLeaf() {
        return left == null && right == null;
    }

    public void setStartEndIndicies(int start, int end) {
        this.start = start;
        this.end = end;
        this.n = end - start + 1;
    }

    public int n() {
        return this.end - this.start + 1;
    }

    public static INDArray calculateCentroid(INDArray indices, INDArray data) {
        INDArray aggr = Nd4j.create(indices.rows(), data.columns());
        for(int i = 0;i < indices.rows();i++) {
            int index = indices.getInt(i);
            INDArray temp = data.getRow(index);
            aggr.putRow(i, temp);
        }
        return aggr.mean(0);
    }

    public static INDArray calculateCentroid(int start, int end, INDArray indices, INDArray data) {
        INDArray aggr = Nd4j.create(end - start + 1, data.columns());
        for(int i = start;i <= end;i++) {
            int index = indices.getInt(i);
            INDArray temp = data.getRow(index);
            aggr.putRow(i, temp);
        }
        return aggr.mean(0);
    }

    public static double calculateRadius(int start, int end, INDArray indices, INDArray data, INDArray pivot, DistanceFunction func) {
        double r = Double.NEGATIVE_INFINITY;
        for(int i = start; i <= end;i++) {
            int index = indices.getInt(i);
            INDArray temp = data.getRow(index);

            double distance = func.distance(pivot, temp);
            if(distance > r) {
                r = distance;
            }
        }
        return Math.sqrt(r);
    }

    public static double calculateRadius(INDArray indices, INDArray data, INDArray pivot, DistanceFunction func) {
       return calculateRadius(0, indices.length() - 1, indices, data, pivot, func);
    }
}
