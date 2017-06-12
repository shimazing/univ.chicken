package uc.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uc.UCConfiguration;
import uc.distance.DistanceFunction;

import java.io.*;
import java.lang.reflect.Type;
import java.util.TreeMap;

/**
 * Created by WoohyeokChoi on 2017-06-06.
 */
public class QecTable {
    private static final String STATES_FILE_NAME = "states";
    private static final String LRU_FILE_NAME = "lru";
    private static final String Q_FILE_NAME = "qvalues";

    private KNNLRUCache[] buffers;

    private QecTable() {

    }

    public QecTable(UCConfiguration conf) throws Exception {
        buffers = new KNNLRUCache[conf.nActions()];

        for(int i = 0;i < conf.nActions();i++) {
            buffers[i] = new KNNLRUCache(conf.maxStateCapacity(), conf.stateDimension(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue());
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

    public void serialize(File fn, File dir) throws IOException {
        if(!dir.exists()) {
            dir.mkdirs();
        }
        TreeMap<Integer, MetaData> data = new TreeMap<>();

        for(int i = 0;i < buffers.length;i++) {
            KNNLRUCache buffer = buffers[i];
            String statesPath = new File(dir, STATES_FILE_NAME + "_action_" + i).getCanonicalPath();
            Nd4j.writeTxt(buffer.states, statesPath, 10);

            String lruPath = new File(dir, LRU_FILE_NAME + "_action_" + i).getCanonicalPath();
            Nd4j.writeTxt(buffer.lruValues, lruPath, 10);

            String qValuesPath = new File(dir, Q_FILE_NAME + "_action_" + i).getCanonicalPath();
            Nd4j.writeTxt(buffer.qValues, qValuesPath, 10);

            MetaData meta = new MetaData();
            meta.statesPath = statesPath;
            meta.lruPath = lruPath;
            meta.qValuesPath = qValuesPath;
            meta.timer = buffer.timer;
            meta.curCapcity = buffer.curCapacity;

            data.put(i, meta);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter(fn);
        gson.toJson(data, writer);
        writer.close();
    }

    public static QecTable deserialize(File f, UCConfiguration conf) throws Exception {
        QecTable table = new QecTable();
        table.buffers = new KNNLRUCache[conf.nActions()];

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Type type = new TypeToken<TreeMap<Integer, MetaData>>() {}.getType();
        TreeMap<Integer, MetaData> data = gson.fromJson(reader, type);

        for(Integer index : data.keySet()) {
            MetaData meta = data.get(index);
            INDArray states = Nd4j.readTxt(meta.statesPath);
            INDArray lru = Nd4j.readTxt(meta.lruPath);
            INDArray qvalue = Nd4j.readTxt(meta.qValuesPath);
            double timer = meta.timer;
            int curCapcity = meta.curCapcity;

            table.buffers[index] = new KNNLRUCache(conf.maxStateCapacity(), conf.stateDimension(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue(),
                    states, qvalue, lru, timer, curCapcity);
        }
        return table;
    }

    private static class MetaData {
        String statesPath;
        String lruPath;
        String qValuesPath;
        double timer;
        int curCapcity;
    }
}
