package uc;

import ab.demo.other.ActionRobot;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import ab.vision.real.shape.Circle;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uc.distance.DistanceFunction;
import uc.distance.EuclideanDistance;
import uc.distance.HammingDistance;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by keltp on 2017-06-07.
 */
public class Test2 {
    public static void main(String args[]) throws IOException {
        UCLog.i("INFO");
        UCLog.w("WARNING");
        UCLog.e("ERROR");
        UCLog.e("Error Find", new Exception("ASDF"));
    }
}
