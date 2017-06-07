package uc;

import ab.demo.other.ActionRobot;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by keltp on 2017-06-07.
 */
public class Test2 {
    public static void main(String args[]) {
        UCConfiguration conf = new UCConfiguration.Builder().build();
        File f = new File("d:/desktop/aibirds/conf.json");
        try {
            conf.serializeToJson(f);
            UCConfiguration conf2 = UCConfiguration.deserializeFromJson(f);
            INDArray a1 = Nd4j.zeros(10).addi(15);
            INDArray a2 = Nd4j.zeros(10).addi(10);
            System.out.println(conf2.distantFunction().distance(a1, a2));
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }
}
