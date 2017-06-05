package uc.balltree;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Mean;
import org.nd4j.linalg.api.ops.impl.accum.distances.*;
import org.nd4j.linalg.api.ops.impl.accum.distances.EuclideanDistance;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static INDArray calculateCentroid(INDArray data) {
        return Nd4j.getExecutioner().exec(new Mean(data), 0);
    }

    public static INDArray calculateCentroid(int start, int end, INDArray data) {
        return Nd4j.getExecutioner().exec(new Mean(data.get(NDArrayIndex.interval(start, end, true), NDArrayIndex.all())), 0);
    }

    public static double calculateRadius(int start, int end, INDArray data, INDArray pivot, DistanceFunction func) {
        double radius = (double) func.maxDistance(start, end, data, pivot).getSecond();
        return Math.sqrt(radius);
    }

    public static double calculateRadius(INDArray data, INDArray pivot, DistanceFunction func) {
        double radius = (double) func.maxDistance(data, pivot).getSecond();
        return Math.sqrt(radius);
    }

    public static double calculateRadius(BallNode child1, BallNode child2, INDArray pivot, DistanceFunction func) {
        INDArray p1 = child1.pivot;
        INDArray p2 = child2.pivot;

        double radius = child1.radius + child2.radius + func.distance(p1, p2);

        return radius / 2;
    }
}
