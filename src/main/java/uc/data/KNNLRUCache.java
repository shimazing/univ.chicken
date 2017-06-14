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

import javax.print.attribute.standard.MediaSize;
import java.io.File;
import java.util.List;

/**
 * Created by keltp on 2017-06-01.
 */
public class KNNLRUCache {
    private final int maxCapacity;
    private DistanceFunction<Integer, Double> distFunc;
    private int k;

    private BallTree tree;

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

    public KNNLRUCache(int maxCapacity, int k, DistanceFunction<Integer, Double> distFunc, double initQValue) throws Exception {
        this(maxCapacity, k, distFunc, initQValue, null, null, null, 0.0, 0);
    }

    protected KNNLRUCache(int maxCapacity, int k, DistanceFunction<Integer, Double> distFunc, double initQValue,
                       INDArray states, INDArray qValues, INDArray lruValues, double timer, int curCapacity) throws Exception {
        this.maxCapacity = maxCapacity;
        this.k = k;
        this.distFunc = distFunc;

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

        if(this.curCapacity > 0) {
            this.tree = BallTree.buildTree(distFunc, this.states);
        }
    }

    public int find(INDArray state) throws Exception {
        if(curCapacity == 0) {
            System.out.println("capacity");
            return -1;
        }

        Pair<Double, Integer> nn = tree.nn(state);
        if(nn == null) {
            System.out.println("no nn");
            return -1;
        }
        int index = nn.getSecond();

        if(states == null) {
            System.out.println("no states");
            return -1;
        }

        INDArray stateStored = states.getRow(index);

        if(state.equalsWithEps(stateStored, Nd4j.EPS_THRESHOLD)) {

            lruValues.putScalar(index, timer);
            timer += 0.01;
            return index;
        }
        System.out.println("not same");
        System.out.println(state);
        System.out.println(stateStored);
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
            tree = BallTree.buildTree(distFunc, states);
        }
    }
}
