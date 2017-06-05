package uc.balltree;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.api.ops.impl.accum.Max;
import org.nd4j.linalg.api.ops.impl.accum.Min;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.conditions.ConditionEquals;
import org.nd4j.linalg.indexing.conditions.EqualsCondition;
import org.nd4j.linalg.indexing.conditions.NotEqualsCondition;

/**
 * Created by keltp on 2017-06-05.
 */
public class HammingDistance implements DistanceFunction<Integer, Double> {

    @Override
    public double distance(INDArray a, INDArray b) {
        return a.sub(b).neq(0).sumNumber().doubleValue();
    }

    @Override
    public INDArray distanceArray(INDArray fullData, INDArray b) {
        return fullData.subRowVector(b).neq(0).sum(1);
    }

    @Override
    public INDArray distanceArray(int start, int end, INDArray fullData, INDArray b) {
        return fullData.get(NDArrayIndex.interval(start, end, true), NDArrayIndex.all()).subRowVector(b).neq(0).sum(1);
    }

    @Override
    public Pair<Integer, Double> maxDistance(INDArray fullData, INDArray b) {
        INDArray array = fullData.subRowVector(b).neq(0).sum(1);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMax(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Max(array)).getFinalResult().doubleValue());
    }

    @Override
    public Pair<Integer, Double> minDistance(INDArray fullData, INDArray b) {
        INDArray array = fullData.subRowVector(b).neq(0).sum(1);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMin(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Min(array)).getFinalResult().doubleValue());
    }

    @Override
    public Pair<Integer, Double> maxDistance(int start, int end, INDArray fullData, INDArray b) {
        INDArray array = fullData.get(NDArrayIndex.interval(start, end, true), NDArrayIndex.all()).subRowVector(b).neq(0).sum(1);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMax(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Max(array)).getFinalResult().doubleValue());    }

    @Override
    public Pair<Integer, Double> minDistance(int start, int end, INDArray fullData, INDArray b) {
        INDArray array = fullData.get(NDArrayIndex.interval(start, end, true), NDArrayIndex.all()).subRowVector(b).neq(0).sum(1);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMin(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Min(array)).getFinalResult().doubleValue());
    }
}
