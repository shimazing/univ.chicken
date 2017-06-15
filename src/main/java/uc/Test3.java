package uc;

import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;
import uc.UCConfiguration;
import uc.balltree.BallNode;
import uc.balltree.BallTree;
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
        INDArray array = null;
        for(int i = 0;i < 50; i++) {
            if(array == null) {
                array = Nd4j.zeros(2).addi(i);
            } else {
                array = Nd4j.vstack(array, Nd4j.zeros(2).addi(i));
            }
        }

        BallTree tree = BallTree.buildTree(new HammingDistance(), array);
        tree.nn(Nd4j.zeros(2).addi(5));

    }
}
