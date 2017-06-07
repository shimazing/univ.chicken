package uc;

import ab.demo.other.ActionRobot;
import ab.demo.other.Shot;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;
import ab.vision.VisionUtils;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Rect;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by keltp on 2017-05-16.
 */
public class Test {
    private static double X_OFFSET = 0.188;
    private static double Y_OFFSET = 0.156;

    public static void main(String args[]) throws Exception {
        new Test();
    }

    private ActionRobot robot;

    public Test() throws Exception {
        robot = new ActionRobot();
        FileWriter writer = new FileWriter("d:/desktop/aibirds/aibirds.csv");
        for(int i = 1;i <= 21;i++) {
            playLevel(writer, i);
        }
        writer.close();
    }

    private Rectangle getSling() throws Exception {
        BufferedImage image = null;
        Vision vision = null;
        Rectangle sling = null;

        while (sling == null) {
            ActionRobot.fullyZoomOut();
            Thread.sleep(2000);

            image = ActionRobot.doScreenShot();
            vision = new Vision(image);
            sling = vision.findSlingshotMBR();
        }

        return sling;
    }

    private void playLevel(FileWriter writer, int level) throws Exception{
        robot.loadLevel(level);

        while(robot.getState() != GameStateExtractor.GameState.PLAYING) {
            Thread.sleep(2000);
            robot.loadLevel(level);
        }

        for(int angle = 4;angle <= 84;angle++) {
            int n = 0;
            while(n < 5) {
                System.out.println(String.format("Try to %s-th shot with angle %s in level %s", (n+1), angle, level));
                Rectangle sling = getSling();
                List<Point> trajs = shot(sling, angle);
                if(trajs == null) {
                    System.out.println("Failed to find trajectories.");
                    continue;
                } else {
                    System.out.println("Found trajectories.");
                    for(Point traj : trajs) {
                        writer.write(String.format("%s,%s,%s,%s,%s,%s",level,angle,sling.getWidth(),sling.getHeight(),traj.getX(),traj.getY()) + System.lineSeparator());
                        writer.flush();
                    }
                    n++;
                }
                robot.restartLevel();
            }
        }
    }



    private List<Point> shot(Rectangle sling, int angle) throws Exception {
        Shot shot = makeShot(sling, Math.toRadians(angle));
        Rectangle sling2 = getSling();
        double dw = sling.width - sling2.width;
        double dh = sling.height - sling2.height;
        double diff = dw * dw + dh * dh;

        if(diff < 5) {
            robot.cFastshoot(shot);
            Thread.sleep(5000);
            BufferedImage image = ActionRobot.doScreenShot();
            Vision vision = new Vision(image);
            List<Point> traj = vision.findTrajPoints();

            return traj;
        } else {
            return null;
        }
    }

    private Shot makeShot(Rectangle sling, double radian) {
        double mag = sling.height * 10;
        Point ref = new Point((int) (sling.x + X_OFFSET * sling.width ), (int) (sling.y + Y_OFFSET * sling.height));;
        Point rel = new Point((int)(ref.x - mag * Math.cos(radian)), (int)(ref.y + mag * Math.sin(radian)));
        return new Shot(ref.x, ref.y, (int) rel.getX() - ref.x, (int) rel.getY() - ref.y, 0, 0);
    }

}
