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
import java.util.*;
import java.util.List;

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
        FileWriter writer = new FileWriter("d:/desktop/aibirds/aibirds.csv", true);
        TreeMap<Integer, Integer> map = new TreeMap<>();
        map.put(8, 4);
        map.put(9, 4);
        map.put(11, 54);
        map.put(12, 4);
        map.put(13, 4);
        map.put(14, 4);
        map.put(15, 4);
        map.put(16, 4);
        map.put(17, 4);
        map.put(18, 4);
        map.put(19, 4);
        map.put(20, 4);
        map.put(21, 4);

        for(Integer level : map.keySet()) {
            int angle = map.get(level);
            playLevel(writer, level, angle);
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

    private void playLevel(FileWriter writer, int level, int from) throws Exception{
        robot.loadLevel(level);

        while(robot.getState() != GameStateExtractor.GameState.PLAYING) {
            Thread.sleep(2000);
            robot.loadLevel(level);
        }
        for(int angle = from;angle <= 77;angle++) {
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
                        //System.out.println(String.format("%s,%s,%s,%s,%s,%s",level,angle,sling.getWidth(),sling.getHeight(),traj.getX(),traj.getY()) + System.lineSeparator());
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
