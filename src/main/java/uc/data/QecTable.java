package uc.data;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uc.UCConfiguration;
import uc.UCLog;
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
        UCLog.i(String.format("QecTable initialization with %s actions.", conf.nActions()));
        buffers = new KNNLRUCache[conf.nActions()];
        for(int i = 0;i < conf.nActions();i++) {
            buffers[i] = new KNNLRUCache(conf.maxStateCapacity(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue());
            UCLog.i(String.format("%s-th KNNLRUCache initialization - maxCapacity: %s, stateDimension: %s, kNearestNeighbor: %s, distantFunction: %s, initialQValue: %s",
                    i, conf.maxAngle(), conf.stateDimension(), conf.kNearestNeighbor(), conf.distantFunction().getClass().getSimpleName(), conf.initialQValue()));
        }
    }

    public double estimateQValue(INDArray state, int action) throws Exception {
        UCLog.i(String.format("Estimate Q Value for %s-th KNNLRUCache.", action));
        double q = buffers[action].getQValue(state);
        if(Double.isNaN(q)) {
            UCLog.i("There is no matched STATE-ACTION pair. Estimate Q Value with K-Nearest Neighbor.");
            q = buffers[action].getKNNValue(state);
        }
        UCLog.i(String.format("Estimated Q Values for %s-th KNNLRUCache: %s", action, q));
        return q;
    }

    public double update(INDArray state, int action, double reward) throws Exception {
        UCLog.i(String.format("Update Q Value for %s-th KNNLRUCache with REWARD %s.", action, reward));
        double q = buffers[action].update(state, reward);
        if(Double.isNaN(q)) {
            UCLog.i("There is no matched STATE-ACTION pair. BallTree will be rebuilt.");
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

            DataOutputStream output;

            File statesFile = new File(dir, STATES_FILE_NAME + "_action_" + i);
            output = new DataOutputStream(new FileOutputStream(statesFile));
            if(buffer.states != null) {
                Nd4j.write(buffer.states, output);
            }
            output.close();

            File lruFile = new File(dir, LRU_FILE_NAME + "_action_" + i);
            output = new DataOutputStream(new FileOutputStream(lruFile));
            if(buffer.lruValues != null) {
                Nd4j.write(buffer.lruValues, output);
            }

            output.close();

            File qFile = new File(dir, Q_FILE_NAME + "_action_" + i);
            output = new DataOutputStream(new FileOutputStream(qFile));
            if(buffer.qValues != null) {
                Nd4j.write(buffer.qValues, output);
            }
            output.close();

            MetaData meta = new MetaData();
            meta.statesPath = statesFile.getCanonicalPath();
            meta.lruPath = lruFile.getCanonicalPath();
            meta.qValuesPath = qFile.getCanonicalPath();
            meta.timer = buffer.timer;
            meta.curCapcity = buffer.curCapacity;

            data.put(i, meta);
        }

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        FileWriter writer = new FileWriter(fn);
        gson.toJson(data, writer);
        writer.close();
    }

    public static QecTable deserialize(File f, UCConfiguration conf) throws Exception {
        QecTable table = new QecTable();
        table.buffers = new KNNLRUCache[conf.nActions()];

        Gson gson = new GsonBuilder().setPrettyPrinting().serializeSpecialFloatingPointValues().create();
        BufferedReader reader = new BufferedReader(new FileReader(f));
        Type type = new TypeToken<TreeMap<Integer, MetaData>>() {}.getType();
        TreeMap<Integer, MetaData> data = gson.fromJson(reader, type);

        for(Integer index : data.keySet()) {
            MetaData meta = data.get(index);
            DataInputStream input;

            File statesFile = new File(meta.statesPath);
            INDArray states = null;
            if(statesFile.exists()) {
                input = new DataInputStream(new FileInputStream(statesFile));
                states = Nd4j.read(input);
                input.close();
            }

            File lruFile = new File(meta.lruPath);
            INDArray lru = null;
            if(lruFile.exists()) {
                input = new DataInputStream(new FileInputStream(lruFile));
                lru = Nd4j.read(input);
                input.close();
            }


            File qValueFile = new File(meta.qValuesPath);
            INDArray qvalue = null;
            if(qValueFile.exists()) {
                input = new DataInputStream(new FileInputStream(new File(meta.qValuesPath)));
                qvalue = Nd4j.read(input);
                input.close();
            }
            double timer = meta.timer;
            int curCapcity = meta.curCapcity;

            table.buffers[index] = new KNNLRUCache(conf.maxStateCapacity(), conf.kNearestNeighbor(), conf.distantFunction(), conf.initialQValue(),
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
