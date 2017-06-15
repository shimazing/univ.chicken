package uc.data;

import com.google.gson.Gson;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMin;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import uc.balltree.BallTree;
import uc.distance.DistanceFunction;


import java.util.List;

/**
 * Created by keltp on 2017-06-01.
 */
public class KNNLRUCache {
    private final int maxCapacity;
    private int k;

    private BallTree tree;
    private int dimension;
    protected INDArray states;
    protected INDArray qValues;
    protected INDArray lruValues;
    protected double timer;
    protected int curCapacity;

    public INDArray states() {
        return states;
    }

    public INDArray qValues() {
        return qValues;
    }

    public INDArray lruValues() {
        return lruValues;
    }

    public KNNLRUCache(int maxCapacity, int k, int dimension, double initQValue) throws Exception {
        this(maxCapacity, k, dimension, initQValue, null, null, null, 0.0, 0);
    }

    protected KNNLRUCache(int maxCapacity, int k, int dimension, double initQValue,
                       INDArray states, INDArray qValues, INDArray lruValues, double timer, int curCapacity) throws Exception {
        this.maxCapacity = maxCapacity;
        this.k = k;
        this.dimension = dimension;

        if(states == null) {
            this.states = null;
        } else {
            this.states = states;
        }

        if(qValues == null) {
            this.qValues = Nd4j.zeros(maxCapacity).addi(initQValue);
        } else {
            this.qValues = qValues;
        }

        if(lruValues == null) {
            this.lruValues = Nd4j.zeros(maxCapacity);
        } else {
            this.lruValues = lruValues;
        }
        this.timer = timer;
        this.curCapacity = curCapacity;

       // this.tree = new BallTree(this.dimension);

    }

    public int find(INDArray state) throws Exception {
        if(curCapacity == 0 || states == null) {
            return -1;
        }

        Pair<Double, Integer> point = tree.nn(state);
        if(point != null) {
            INDArray stateStored = this.states.getRow(point.getSecond());
            if(stateStored.equalsWithEps(state, Nd4j.EPS_THRESHOLD)) {
                lruValues.putScalar(point.getSecond(), timer);
                timer += 0.01;
                return point.getSecond();
            }
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

        if(knn == null || knn.size() == 0) {
            return Double.NaN;
        }

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
            if(states == null || states.rows() == 0) {
                states = state;
            } else {
                states = Nd4j.vstack(states, state);
            }
            qValues.putScalar(index, reward);
            lruValues.putScalar(index, timer);
            timer += 0.01;
            //tree.insert(state, index);
        }
    }
}
