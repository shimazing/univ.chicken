package uc.distance;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.Max;
import org.nd4j.linalg.api.ops.impl.accum.Min;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;

/**
 * Created by keltp on 2017-06-05.
 */
public class HammingDistance implements DistanceFunction<Integer, Double> {

    @Override
    public double distance(INDArray a, INDArray b) {
        return 1 - (a.sub(b).eps(0).sumNumber().doubleValue() / a.length());
    }

    @Override
    public INDArray distances(INDArray fullData, INDArray b) {
        return fullData.subRowVector(b).eps(0).sum(1).mul(-1).div(b.length()).add(1.0);

    }


    @Override
    public Pair<Integer, Double> maxDistance(INDArray fullData, INDArray b) {
        INDArray array = fullData.subRowVector(b).eps(0).sum(1).mul(-1).div(b.length()).add(1.0);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMax(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Max(array)).getFinalResult().doubleValue());
    }

    @Override
    public Pair<Integer, Double> minDistance(INDArray fullData, INDArray b) {
        INDArray array = fullData.subRowVector(b).eps(0).sum(1).mul(-1).div(b.length()).add(1.0);
        return new Pair<>(Nd4j.getExecutioner().execAndReturn(new IMin(array)).getFinalResult(),
                Nd4j.getExecutioner().execAndReturn(new Min(array)).getFinalResult().doubleValue());
    }

}
