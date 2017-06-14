package uc.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import uc.UCConfiguration;
import uc.UCLog;

import java.io.*;
import java.lang.reflect.Type;
import java.util.TreeMap;

/**
 * Created by WoohyeokChoi on 2017-06-06.
 */
public class QecTable {
    private KNNLRUCache[] buffers;

    private QecTable() {

    }

    public KNNLRUCache[] getBuffers() {
        return buffers;
    }

    public QecTable(UCConfiguration conf) throws Exception {
        UCLog.i(String.format("QecTable initialization with %s actions.", conf.nActions()));
        buffers = new KNNLRUCache[conf.nActions()];
        for(int i = 0;i < conf.nActions();i++) {
            buffers[i] = new KNNLRUCache(conf.maxStateCapacity(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue());
        }
    }

    public double estimateQValue(INDArray state, int action) throws Exception {
        //UCLog.i(String.format("Estimate Q Value for %s-th KNNLRUCache.", action));
        INDArray _state = state.rows() != 1 ? state.transposei() : state;
        double q = buffers[action].getQValue(_state.rows() != 1 ? state.transposei() : state);
        if(Double.isNaN(q)) {
          //  UCLog.i("There is no matched STATE-ACTION pair. Estimate Q Value with K-Nearest Neighbor.");
            q = buffers[action].getKNNValue(_state);
        }
        //UCLog.i(String.format("Estimated Q Values for %s-th KNNLRUCache: %s", action, q));
        return q;
    }

    public double update(INDArray state, int action, double reward) throws Exception {
        //UCLog.i(String.format("Update Q Value for %s-th KNNLRUCache with REWARD %s.", action, reward));
        INDArray _state = state.rows() != 1 ? state.transposei() : state;
        double q = buffers[action].update(_state, reward);
        if(Double.isNaN(q)) {
            //UCLog.i("There is no matched STATE-ACTION pair. BallTree will be rebuilt.");
            buffers[action].add(_state, reward);
            q = reward;
        }
        return q;
    }

    public void serialize(File fn, File statesFile, File qFile, File lruFile) throws IOException {

        TreeMap<Integer, MetaData> data = new TreeMap<>();

        int qValueColumn = 0;
        int qValueRows = 0;
        int lruValueColumn = 0;
        int lruValueRows = 0;
        int statesColumn = 0;
        int statesRows = 0;

        for(int i = 0;i < buffers.length;i++) {
            MetaData meta = new MetaData();
            KNNLRUCache buffer = buffers[i];

            if(buffer.states == null) {
                meta.statesRowIndexFrom = -1;
                meta.statesRowIndexTo = -1;
            } else {
                statesColumn = buffer.states.columns();
                meta.statesRowIndexFrom = statesRows;
                statesRows += buffer.states.rows();
                meta.statesRowIndexTo = statesRows;
            }

            if(buffer.qValues == null) {
                meta.qValueRowIndex = -1;
            } else {
                qValueColumn = buffer.qValues.columns();
                qValueRows++;
                meta.qValueRowIndex = i;
            }

            if(buffer.lruValues == null) {
                meta.lruValueRowIndex = -1;
            } else {
                lruValueColumn = buffer.lruValues.columns();
                lruValueRows++;
                meta.lruValueRowIndex = i;
            }

            meta.timer = buffer.timer;
            meta.curCapacity = buffer.curCapacity;
            data.put(i, meta);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        FileWriter writer = new FileWriter(fn);
        gson.toJson(data, writer);
        writer.close();

        INDArray states = null;
        if(statesRows > 0 && statesColumn > 0) {
            states = Nd4j.create(statesRows, statesColumn);
        }

        INDArray qValues = null;
        if(qValueRows > 0 && qValueColumn > 0) {
            qValues = Nd4j.create(qValueRows, qValueColumn);
        }

        INDArray lruValues = null;
        if(lruValueRows > 0 && lruValueColumn > 0) {
            lruValues = Nd4j.create(lruValueRows, lruValueColumn);
        }

        for(int i = 0;i < buffers.length;i++) {
            MetaData meta = data.get(i);
            KNNLRUCache buffer = buffers[i];
            if(states != null && meta.statesRowIndexFrom != -1 && meta.statesRowIndexTo != -1) {
                for(int j = 0; j < buffer.states.rows(); j++) {
                    states.putRow(meta.statesRowIndexFrom + j, buffer.states.getRow(j));
                }
            }
            if(qValues != null && meta.qValueRowIndex != -1) {
                qValues.putRow(meta.qValueRowIndex, buffer.qValues);
            }
            if(lruValues != null && meta.lruValueRowIndex != -1) {
                lruValues.putRow(meta.lruValueRowIndex, buffer.lruValues);
            }
        }

        if(states != null) {
            Nd4j.saveBinary(states, statesFile);
        }

        if(qValues != null) {
            Nd4j.saveBinary(qValues, qFile);
        }

        if(lruValues != null) {
            Nd4j.saveBinary(lruValues, lruFile);
        }


    }

    public static QecTable deserialize(File metaFile, File statesFile, File qFile, File lruFile, UCConfiguration conf) throws Exception {
        QecTable table = new QecTable();
        table.buffers = new KNNLRUCache[conf.nActions()];

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        BufferedReader reader = new BufferedReader(new FileReader(metaFile));
        Type type = new TypeToken<TreeMap<Integer, MetaData>>() {}.getType();
        TreeMap<Integer, MetaData> data = gson.fromJson(reader, type);

        INDArray states = null;
        if(statesFile.exists()) {
            states = Nd4j.readBinary(statesFile);
        }

        INDArray qValues = null;
        if(qFile.exists()) {
            qValues = Nd4j.readBinary(qFile);
        }

        INDArray lruValues = null;
        if(lruFile.exists()) {
            lruValues = Nd4j.readBinary(lruFile);
        }

        for(Integer index : data.keySet()) {
            MetaData meta = data.get(index);
            INDArray subStates = null;
            INDArray subQValues = null;
            INDArray subLRUValues = null;

            if(states != null) {
                if(meta.statesRowIndexTo != -1 && meta.statesRowIndexFrom != -1) {
                    subStates = states.get(NDArrayIndex.interval(meta.statesRowIndexFrom, meta.statesRowIndexTo), NDArrayIndex.all()).dup();
                }
            }

            if(qValues != null) {
                if(meta.qValueRowIndex != -1) {
                    subQValues = qValues.getRow(meta.qValueRowIndex).dup();
                }
            }

            if(lruValues != null) {
                if(meta.lruValueRowIndex != -1) {
                    subLRUValues = lruValues.getRow(meta.lruValueRowIndex).dup();
                }
            }

            double timer = meta.timer;
            int curCapacity = meta.curCapacity;

            table.buffers[index] = new KNNLRUCache(conf.maxStateCapacity(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue(),
                    subStates, subQValues, subLRUValues, timer, curCapacity);
        }
        return table;
    }

    private static class MetaData {
        int statesRowIndexFrom;
        int statesRowIndexTo;
        int lruValueRowIndex;
        int qValueRowIndex;
        double timer;
        int curCapacity;
    }
}
