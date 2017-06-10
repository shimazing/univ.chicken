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
        FileWriter writer = new FileWriter("d:/GoogleDrive/aibirds.csv", true);
        writer.write("level,angle,try,refX,refY,relX,relY,width,height,x,y,nx,ny" + System.lineSeparator());
        TreeMap<Integer, Integer> map = new TreeMap<>();
        map.put(1, 30);
        map.put(2, 4);
        map.put(3, 4);
        map.put(4, 4);
        map.put(5, 4);
        map.put(6, 4);
        map.put(7, 4);
        map.put(8, 4);
        map.put(9, 4);
        map.put(10, 4);
        map.put(11, 4);
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
            while(n < 3) {
                System.out.println(String.format("Try to %s-th shot with angle %s in level %s", (n+1), angle, level));
                Rectangle sling = getSling();
                double mag = sling.height * 10;
                double radian = Math.toRadians(angle);

                Point ref = new Point((int) (sling.x + X_OFFSET * sling.width ), (int) (sling.y + Y_OFFSET * sling.height));
                Point rel = new Point((int)(ref.x - mag * Math.cos(radian)), (int)(ref.y + mag * Math.sin(radian)));
                Shot shot = new Shot(ref.x, ref.y, (int) rel.getX() - ref.x, (int) rel.getY() - ref.y, 0, 0);

                Rectangle sling2 = getSling();

                double dw = sling.width - sling2.width;
                double dh = sling.height - sling2.height;
                double diff = dw * dw + dh * dh;

                if(diff > 5) {
                    System.err.println("Scene changed. Retry it");
                } else {
                    robot.cFastshoot(shot);
                    Thread.sleep(5000);

                    BufferedImage image = ActionRobot.doScreenShot();
                    Vision vision = new Vision(image);
                    List<Point> trajs = vision.findTrajPoints();

                    if(trajs == null) {
                        System.err.println("Failed to find trajectories.");
                        ImageIO.write(image, "png", new File("d:/GoogleDrive/aibirds/" + String.format("fail-traj-%s-%s-%s.png", level, angle, (n+1))));
                    } else {
                        System.out.println("Found trajectories.");
                        List<Point> clean = new ArrayList<>();

                        double prevX = 0;

                        for(Point traj : trajs) {
                            double curX = traj.getX();
                            double curY = traj.getY();
                            if (curX >= rel.getX() && curX >= prevX) {
                                clean.add(new Point((int) curX, (int) curY));
                                prevX = curX;
                            }
                        }

                        if(clean.size() > 10) {
                            System.out.println("Trajectories are well-founded.");
                            for(Point p : clean) {
                                String line = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s",
                                        level, angle, (n+1),
                                        ref.getX(), ref.getY(), rel.getX(), rel.getY(),
                                        sling.getWidth(), sling.getHeight(), p.getX(), p.getY(),
                                        (p.getX() / (sling.getWidth() + sling.getHeight())), (p.getY() / (sling.getWidth() + sling.getHeight())));
                                writer.write(line + System.lineSeparator());
                                writer.flush();
                            }
                            n++;
                        } else {
                            ImageIO.write(image, "png", new File("d:/GoogleDrive/aibirds/" + String.format("not-well-traj-%s-%s-%s.png", level, angle, (n+1))));
                            System.err.println("Trajectories are not well-founded. Retry it");

                        }
                    }
                }


                robot.restartLevel();
            }
        }
    }







}
