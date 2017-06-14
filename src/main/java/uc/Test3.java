package uc;

import org.nd4j.linalg.factory.Nd4j;
import uc.UCConfiguration;
import uc.data.KNNLRUCache;
import uc.data.QecTable;

import java.io.File;

/**
 * Created by keltp on 2017-06-14.
 */
public class Test3 {
    public static void main(String[] args) throws Exception {
        UCConfiguration conf = new UCConfiguration.Builder().nAngles(2).nTapTimes(2).maxStateCapacity(500).build();
        QecTable table = new QecTable(conf);
        table.update(Nd4j.randn(1, 5, 11), 0, 100);
        table.update(Nd4j.randn(1, 5, 22), 1, 200);
        table.update(Nd4j.randn(1, 5, 33), 1, 300);
        table.update(Nd4j.randn(1, 5, 33), 3, 300);
        KNNLRUCache[] buffers = table.getBuffers();



        File f = new File("d:/desktop/test.json");
        File s = new File("d:/desktop/states");
        File q = new File("d:/desktop/qvalues");
        File l = new File("d:/desktop/lru");
        long start = System.currentTimeMillis();
        table.serialize(f, s, q, l);
        System.out.println("Serialize: " + (System.currentTimeMillis() - start) + "ms");
        start = System.currentTimeMillis();
        QecTable table2 = QecTable.deserialize(f,s, q, l, conf);
        System.out.println("Deserialize: " + (System.currentTimeMillis() - start) + "ms");
        KNNLRUCache[] buffers2 = table.getBuffers();

        for(int i = 0;i < buffers.length;i++) {
            System.out.println(i);
            System.out.println(buffers[i].states());
            System.out.println(buffers2[i].states());
            System.out.println(buffers[i].qValues().equalsWithEps(buffers2[i].qValues(), 0.0));
            System.out.println(buffers[i].lruValues().equalsWithEps(buffers2[i].lruValues(), 0.0));
        }


    }
}
