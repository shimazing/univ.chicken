package uc.data;

import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import uc.balltree.BallTree;
import uc.distance.DistanceFunction;

import javax.print.attribute.standard.MediaSize;
import java.util.List;

/**
 * Created by keltp on 2017-06-01.
 */
public class KNNLRUCache {
    private final int maxCapacity;
    private DistanceFunction<Integer, Double> distFunc;
    private int curCapacity;
    private INDArray states;
    private INDArray qValues;
    private INDArray lruValues;
    private double timer;
    private BallTree tree;
    private int k;

    public KNNLRUCache(int maxCapacity, int dimension, int k, DistanceFunction<Integer, Double> distFunc) {
        this.maxCapacity = maxCapacity;
        this.k = k;
        this.distFunc = distFunc;
        states = Nd4j.create(maxCapacity, dimension);
        qValues = Nd4j.create(maxCapacity);
        lruValues = Nd4j.create(maxCapacity);
        timer = 0.0;
        curCapacity = 0;
    }

    private int find(INDArray state) throws Exception {
        if(curCapacity == 0) {
            return -1;
        }

        Pair<Double, Integer> nn = tree.nn(state);
        int index = nn.getSecond();
        INDArray stateStored = states.getRow(index);

        if(state.equalsWithEps(stateStored, Nd4j.EPS_THRESHOLD)) {
            lruValues.putScalar(index, timer);
            timer += 0.01;
            return index;
        }
        return -1;
    }

    public double getQValue(INDArray state) throws Exception {
        int index = find(state);
        if(index != -1) {
            return qValues.getDouble(index);
        }
        return Double.NaN;
    }

    public double update(INDArray state, double reward) throws Exception {
        int index = find(state);
        if(index != -1) {
            double newQ = Math.max(qValues.getDouble(index), reward);
            qValues.putScalar(index, newQ);
            return newQ;
        }
        return Double.NaN;
    }

    public double getKNNValue(INDArray state) throws Exception {
        if(curCapacity == 0) {
            return Double.NaN;
        }

        List<Pair<Double, Integer>> knn = tree.knn(state, k);
        double q = 0.0;
        for(Pair<Double, Integer> pair : knn) {
            int index = pair.getSecond();
            q += qValues.getDouble(index);
            lruValues.putScalar(index, timer);
            timer += 0.01;
        }
        return q / k;
    }

    public void add(INDArray state, double reward) throws Exception {
        int index;
        if(curCapacity < maxCapacity) {
            index = curCapacity;
            curCapacity++;
        } else {
            IMin argMin = new IMin(lruValues);
            Nd4j.getExecutioner().exec(argMin);
            index = argMin.getFinalResult();
        }

        if(index > -1) {
            states.put(index, state);
            qValues.putScalar(index, reward);
            lruValues.putScalar(index, timer);
            timer += 0.01;
            tree = BallTree.buildTree(distFunc, states);
        }
    }
}
