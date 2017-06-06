package uc;

import ab.demo.other.ActionRobot;
import ab.vision.Vision;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;

/**
 * Created by keltp on 2017-05-16.
 */
public class Test {
    public static void main(String args[]) throws Exception {
/*
        ActionRobot ar = new ActionRobot();
        BufferedImage image = ActionRobot.doScreenShot();
        Vision vision = new Vision(image);
        Rectangle slingshot = vision.findSlingshotMBR();
        while(slingshot == null)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("no slingshot detected. Please remove pop up or zoom out");
            image = ActionRobot.doScreenShot();
            vision = new Vision(image);
            slingshot = vision.findSlingshotMBR();
        }

        ImageIO.write(image, "png", new File("d:/users/desktop/aibirds/test.png"));
*/
        INDArray a1 = Nd4j.zeros(10).addi(15);
        INDArray a2 = Nd4j.zeros(10).addi(14);
        System.out.println(a1.equalsWithEps(a2, Nd4j.EPS_THRESHOLD));
        int index = 0;
        int test = 2;
        index = test;
        test++;
        System.out.println(index);
    }
}
