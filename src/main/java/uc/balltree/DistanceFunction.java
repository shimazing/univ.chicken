package uc.balltree;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.indexing.INDArrayIndex;

/**
 * Created by keltp on 2017-06-05.
 */
public interface DistanceFunction<K extends Integer, V extends Double> {
    double distance(INDArray a, INDArray b);
    INDArray distanceArray(INDArray fullData, INDArray b);
    INDArray distanceArray(int start, int end, INDArray fullData, INDArray b);
    Pair<K, V> maxDistance(INDArray fullData, INDArray b);
    Pair<K, V> minDistance(INDArray fullData, INDArray b);
    Pair<K, V> maxDistance(int start, int end, INDArray fullData, INDArray b);
    Pair<K, V> minDistance(int start, int end, INDArray fullData, INDArray b);

}
