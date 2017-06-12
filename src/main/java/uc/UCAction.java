package uc;

import ab.demo.other.Shot;
import ab.vision.Vision;
import org.deeplearning4j.berkeley.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keltp on 2017-06-09.
 */
public class UCAction {
    private int id;
    private double angleInDegrees;
    private double inGameAngleInDegrees;
    private double tapTimeInPercentile;
    private double velocity;
    private int tapTimeInMillis;
    private double scale;
    private Point refPoint;
    private Point relPoint;
    private Point impactPoint;
    private Shot shot;

    private List<Point> predictTrajectories() {
        double inGameAngleInRadians = Math.toRadians(inGameAngleInDegrees);
        double a = Math.tan(inGameAngleInRadians);
        double b = - 1 / (2 * velocity * velocity * Math.cos(inGameAngleInRadians) * Math.cos(inGameAngleInRadians));
        List<Point> points = new ArrayList<>();

        for(int x = refPoint.x; x < impactPoint.x; x++) {
            double nx = (double) x / scale;
            double ny = nx * a + nx * nx * b;
            int y = (int) (ny * scale);
            points.add(new Point(x, y));
        }
        return points;
    }

    public int id() {
        return id;
    }

    public double angleInDegrees() {
        return angleInDegrees;
    }

    public double inGameAngleInDegrees() {
        return inGameAngleInDegrees;
    }

    public double tapTimeInPercentile() {
        return tapTimeInPercentile;
    }

    public double velocity() {
        return velocity;
    }

    public int tapTimeInMillis() {
        return tapTimeInMillis;
    }

    public double scale() {
        return scale;
    }

    public Point refPoint() {
        return refPoint;
    }

    public Point relPoint() {
        return relPoint;
    }

    public Point impactPoint() {
        return impactPoint;
    }

    public Shot shot() {
        return shot;
    }

    public static class Builder {
        private List<Pair<Double, Double>> availableActions;
        private int screenWidth;

        public Builder(UCConfiguration conf) {
            availableActions = new ArrayList<>();
            for(int i = 0;i < conf.nAngles();i++) {
                double angle = conf.minAngle() + (double) (conf.maxAngle() - conf.maxAngle()) / conf.nActions() * i;
                for(int j = 0;j < conf.nTapTimes();j++) {
                    double tapTime = conf.minPercentileTapTime() + (conf.maxPercentileTapTime()
                            - conf.minPercentileTapTime()) / conf.nTapTimes() * j;
                    availableActions.add(Pair.makePair(angle, tapTime));
                }
            }
            screenWidth = conf.screenWidth();
        }

        public UCAction build(BufferedImage image, UCObservation observation, int id) {
            if(id == -1 || availableActions == null || availableActions.size() == 0) {
                return null;
            }
            Pair<Double, Double> actionSetting = availableActions.get(id);

            UCAction action = new UCAction();
            Vision vision = new Vision(image);
            Rectangle sling = vision.findSlingshotMBR();

            int actionId = id;
            int scale = (int) (sling.getHeight() + sling.getWidth());
            double angleInDegrees = actionSetting.getFirst();
            double tapTimeInPercentile = actionSetting.getSecond();
            double inGameAngleInDegrees = getInGameAngle(angleInDegrees);
            double velocity = getVelocity(angleInDegrees);
            Point refPoint = getReferencePoint(sling);
            Point relPoint = getReleasePoint(sling, refPoint, angleInDegrees);
            Point impactPoint = predictImpactPoint(scale, refPoint, velocity, inGameAngleInDegrees, observation);
            int tapTimeInMillis = predictTapTime(scale, refPoint, impactPoint, velocity, inGameAngleInDegrees, tapTimeInPercentile);
            Shot shot = new Shot(refPoint.x, refPoint.y, (int) (relPoint.getX() - refPoint.getX()), (int) (relPoint.getY() - refPoint.getY()), 0, tapTimeInMillis);

            action.id = actionId;
            action.scale = scale;
            action.angleInDegrees = angleInDegrees;
            action.velocity = velocity;
            action.tapTimeInPercentile = tapTimeInPercentile;
            action.inGameAngleInDegrees = inGameAngleInDegrees;
            action.refPoint = refPoint;
            action.relPoint = relPoint;
            action.impactPoint = impactPoint;
            action.tapTimeInMillis = tapTimeInMillis;
            action.shot = shot;

            return action;
        }

        /**
         * @TODO degree to velocity
         */
        private double getVelocity(double degrees) {
            double radians = Math.toRadians(degrees);
            return 1;
        }

        /**
         * @TODO degree to in game angle
         */
        private double getInGameAngle(double degrees) {
            double radians = Math.toRadians(degrees);
            return 1;
        }

        private Point getReferencePoint(Rectangle sling) {
            return new Point((int) (sling.x + 0.188 * sling.width), (int) (sling.y + 0.156 * sling.height));
        }

        private Point getReleasePoint(Rectangle sling, Point ref, double degrees) {
            double radians = Math.toRadians(degrees);
            double mag = sling.height * 10;
            return new Point((int)(ref.x - mag * Math.cos(radians)), (int)(ref.y + mag * Math.sin(radians)));
        }

        private Point predictImpactPoint(int scale, Point ref, double velocity, double inGameAngleInDegrees, UCObservation obs) {
            double inGameAngleRadians = Math.toRadians(inGameAngleInDegrees);
            double a = Math.tan(inGameAngleRadians);
            double b = - 1 / (2 * velocity * velocity * Math.cos(inGameAngleRadians) * Math.cos(inGameAngleRadians));

            Point impactPoint = null;
            for(int x = ref.x; x <= screenWidth; x++) {
                double nx = (double) x / scale;
                double ny = nx * a + nx * nx * b;
                int y = (int) (ny * scale);

                impactPoint = new Point(x, y);

                if(obs.isNotEmpty(x, y)) {
                    break;
                }
            }
            return impactPoint;
        }



        private int predictTapTime(int scale, Point ref, Point impact, double velocity, double inGameAngleInDegrees, double tapTimeInPercentile) {
            double inGameAngleInRadians = Math.toRadians(inGameAngleInDegrees);
            double xVelocity = velocity * Math.cos(inGameAngleInRadians);
            double distance = (impact.x - ref.x + xVelocity * 0.4 * scale) * tapTimeInPercentile / scale;
            return (int) (distance / xVelocity * 815);
        }
    }
}
