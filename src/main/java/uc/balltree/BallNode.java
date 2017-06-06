package uc.balltree;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Mean;
import org.nd4j.linalg.api.ops.impl.accum.distances.*;
import org.nd4j.linalg.api.ops.impl.accum.distances.EuclideanDistance;
import org.nd4j.linalg.factory.NDArrayFactory;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;

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

    public static INDArray calculateCentroid(INDArray indices, INDArray data) {
        SpecifiedIndex index = new SpecifiedIndex(indices.data().asInt());
        INDArray subArray = data.get(index, NDArrayIndex.all());
        return Nd4j.getExecutioner().exec(new Mean(subArray), 0);
    }

    public static INDArray calculateCentroid(int start, int end, INDArray indices, INDArray data) {
        INDArray subIndicesArray = indices.get(NDArrayIndex.interval(start, end, true));
        SpecifiedIndex index = new SpecifiedIndex(subIndicesArray.data().asInt());
        INDArray subArray = data.get(index, NDArrayIndex.all());
        return Nd4j.getExecutioner().exec(new Mean(subArray), 0);
    }

    public static double calculateRadius(int start, int end, INDArray indices, INDArray data, INDArray pivot, DistanceFunction func) {
        INDArray subIndicesArray = indices.get(NDArrayIndex.interval(start, end, true));
        SpecifiedIndex index = new SpecifiedIndex(subIndicesArray.data().asInt());
        INDArray subArray = data.get(index, NDArrayIndex.all());
        double radius = (double) func.maxDistance(subArray, pivot).getSecond();
        return Math.sqrt(radius);
    }

    public static double calculateRadius(INDArray indices, INDArray data, INDArray pivot, DistanceFunction func) {
        SpecifiedIndex index = new SpecifiedIndex(indices.data().asInt());
        INDArray subArray = data.get(index, NDArrayIndex.all());
        double radius = (double) func.maxDistance(subArray, pivot).getSecond();
        return Math.sqrt(radius);
    }
}
