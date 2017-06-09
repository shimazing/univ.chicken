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
        File dir = new File("d:/desktop/aibirds");
        for(File f : dir.listFiles()) {
            String fn = f.getName();
            if(fn.endsWith(".png")) {
                System.out.println(fn);
                BufferedImage image = ImageIO.read(f);
                Vision vision = new Vision(image);
                List<ABObject> objs = vision.findPigsRealShape();
                for(ABObject obj : objs) {
                    System.out.print( ((Circle) obj).r + ", ");
                }
                System.out.println();
            }
        }

    }
}
