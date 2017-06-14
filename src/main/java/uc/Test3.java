package uc;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;
import uc.UCConfiguration;
import uc.data.KNNLRUCache;
import uc.data.QecTable;
import uc.distance.EuclideanDistance;
import uc.distance.HammingDistance;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Random;

/**
 * Created by keltp on 2017-06-14.
 */
public class Test3 {
    public static void main(String[] args) throws Exception {
/*
        UCConfiguration conf = UCConfiguration.deserializeFromJson(new File("./autosave/conf.json"));
        QecTable table = QecTable.deserialize(new File("./autosave/qec.json"), new File("./autosave/states.bin"), new File("./autosave/qvalues.bin"), new File("lruvalues.bin"), conf);
        BufferedImage image = ImageIO.read(new File("./imgs/692_preprocess.png"));
        INDArray array = Nd4j.create(84, 84);
        for (int i = 0; i < array.rows(); i++) {
            for (int j = 0; j < array.columns(); j++) {
                int rgb = image.getRGB(j, i);
                Color color = new Color(rgb);
                array.put(j, i, (double) color.getBlue() / 255.0);
            }
        }

        INDArray state = Nd4j.toFlattened(array).mmul(conf.randomProjection());

*/

        KNNLRUCache cache = new KNNLRUCache(100, 3, new HammingDistance(), 0);
        INDArray state = Nd4j.randn(1, 64);

        cache.add(state, 1);
        for (int i = 0; i < 50; i++) {
            cache.add(Nd4j.randn(1, 64), 1);
        }



        int i = cache.find(state);
        System.out.println(i);
    }
}
