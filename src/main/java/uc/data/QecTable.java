package uc.data;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import uc.distance.DistanceFunction;

/**
 * Created by WoohyeokChoi on 2017-06-06.
 */
public class QecTable {
    private DistanceFunction<Integer, Double> distFunc;

    private KNNLRUCache[] buffers;

    public QecTable(int maxCapacity, int stateDim, int nActions, int k, DistanceFunction<Integer, Double> distFunc) {
        buffers = new KNNLRUCache[nActions];
        for(int i = 0;i < nActions;i++) {
            buffers[i] = new KNNLRUCache(maxCapacity, stateDim, k, distFunc);
        }
    }

    public double estimateQValue(INDArray state, int action) throws Exception {
        double q = buffers[action].getQValue(state);
        if(Double.isNaN(q)) {
            q = buffers[action].getKNNValue(state);
        }
        return q;
    }

    public double update(INDArray state, int action, double reward) throws Exception {
        double q = buffers[action].update(state, reward);
        if(Double.isNaN(q)) {
            buffers[action].add(state, reward);
            q = reward;
        }
        return q;
    }
}
