package uc;

import ab.demo.other.Shot;
import ab.vision.Vision;
import com.sun.corba.se.impl.oa.toa.TOA;
import org.deeplearning4j.berkeley.Pair;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

/**
 * Created by keltp on 2017-06-09.
 */
public class UCAction {
    private int id;
    private UCObservation observation;
    private int angleInDegrees;
    private double inGameAngleInDegrees;
    private double tapTimeInPercentile;
    private double velocity;
    private Rectangle sling;

    public UCObservation observation() {
        return observation;
    }

    public int angleInDegrees() {
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

    public List<Point> predictTrajectories() {
        double inGameAngleRadians = Math.toRadians(inGameAngleInDegrees);
        double a = Math.tan(inGameAngleRadians);
        double ux = velocity * Math.cos(inGameAngleRadians);
        double b = - 0.5 / (ux * ux);

        Point refPoint = getReferencePoint();
        List<Point> points = new ArrayList<>();
        int scale = sling.height + sling.width;

        for(double x = sling.getCenterX(); x <= 840; x++) {
            double nx = (x - sling.getCenterX()) / scale;
            double ny = nx * a + nx * nx * b;
            int y = refPoint.y - (int) (ny * scale);

            points.add(new Point((int)x, y));

            if(observation.isNotEmpty((int) x, y) || y > observation.yOffset()) {
                break;
            }
        }
        return points;
    }

    private Point getReleasePoint(Point ref) {
        double radians = Math.toRadians(angleInDegrees);
        double mag = sling.height * 10;
        return new Point((int)(ref.x - mag * Math.cos(radians)), (int)(ref.y + mag * Math.sin(radians)));
    }

    private Point getReferencePoint() {
        return new Point((int) (sling.x + 0.188 * sling.width), (int) (sling.y + 0.156 * sling.height));
    }

    private Point predictImpactPoint(Point refPoint) {
        double inGameAngleRadians = Math.toRadians(inGameAngleInDegrees);
        double a = Math.tan(inGameAngleRadians);
        double ux = velocity * Math.cos(inGameAngleRadians);
        double b = - 0.5 / (ux * ux);

        Point impactPoint = null;

        int scale = sling.height + sling.width;
        for(double x = sling.getCenterX(); x <= 840; x++) {
            double nx = (x - sling.getCenterX()) / scale;
            double ny = nx * a + nx * nx * b;
            int y = refPoint.y - (int) (ny * scale);

            impactPoint = new Point((int)x, y);

            if(observation.isNotEmpty((int) x, y) || y > observation.yOffset()) {
                break;
            }
        }
        return impactPoint;
    }

    private int predictTapTime(Point impactPoint) {
        double inGameAngleInRadians = Math.toRadians(inGameAngleInDegrees);
        double cos = Math.cos(inGameAngleInRadians);
        double xVelocity = velocity * cos;
        int scale = sling.height + sling.width;
        double tapPoint = sling.x + (impactPoint.x - sling.x) * tapTimeInPercentile;
        double pullback = scale * 0.4 * cos;
        double distance = (tapPoint - getReferencePoint().x + pullback) / scale;
        return (int) (distance / xVelocity * 815);
    }

    public int id() {
        return id;
    }

    public Shot shot() {
        Point refPoint = getReferencePoint();
        Point relPoint = getReleasePoint(refPoint);
        Point impactPoint = predictImpactPoint(refPoint);
        int tapTimeInMillis = predictTapTime(impactPoint);
        return new Shot(refPoint.x, refPoint.y, (int) (relPoint.getX() - refPoint.getX()), (int) (relPoint.getY() - refPoint.getY()), 0, tapTimeInMillis);
    }

    public static class Builder {
        private List<Pair<Integer, Double>> availableActions;
        private static final int FROM_ANGLE = 8;
        private static final int TO_ANGLE = 77;
        private static final double[] VELOCITIES = {2.996719, 2.862938, 2.826255, 2.982086,
                2.865554, 2.863649, 2.865673, 2.888111, 2.917939, 2.865227, 2.855169, 2.874725,
                2.866073, 2.853022, 2.859230, 2.849410, 2.840482, 2.838203, 2.846641, 2.833310,
                2.832710, 2.849923, 2.831579, 2.829733, 2.846028, 2.834989, 2.837869, 2.822843,
                2.822735, 2.684189, 2.808665, 2.820365, 2.810520, 2.826538, 2.801513, 2.800633,
                2.800818, 2.800852, 2.796766, 2.790987, 2.792198, 2.796711, 2.792550, 2.787254,
                2.785502, 2.786492, 2.780270, 2.781517, 2.783312, 2.782071, 2.772840, 2.780222,
                2.773210, 2.769689, 2.765933, 2.770550, 2.764448, 2.771498, 2.773206, 2.764881,
                2.769128, 2.769936, 2.768649, 2.765417, 2.764695, 2.758847, 2.759056, 2.759704,
                2.735524, 2.734722};

        public Builder(UCConfiguration conf) {
            availableActions = new ArrayList<>();
            Set<Integer> angles = new TreeSet<>();
            int minAngle = Math.max(conf.minAngle(), FROM_ANGLE);
            int maxAngle = Math.min(conf.maxAngle(), TO_ANGLE);

            for(int i = 0;i < conf.nActions();i++) {
                double angle = minAngle + (double) (maxAngle - minAngle) / conf.nActions() * i;
                angles.add((int) angle);
            }

            for(int angle : angles) {
                for(int j = 0;j < conf.nTapTimes();j++) {
                    double tapTime = conf.minPercentileTapTime() + (conf.maxPercentileTapTime()
                            - conf.minPercentileTapTime()) / conf.nTapTimes() * j;
                    availableActions.add(Pair.makePair(angle, tapTime));
                }
            }
        }

        public UCAction buildTest(Rectangle sling, UCObservation observation, int angle) {
            UCAction action = new UCAction();
            int angleInDegrees = angle;
            double inGameAngleInDegrees =  Math.toDegrees(getInGameAngle(angleInDegrees));
            double velocity = getVelocity(angleInDegrees);

            action.sling = sling;
            action.angleInDegrees = angleInDegrees;
            action.velocity = velocity;
            action.inGameAngleInDegrees = inGameAngleInDegrees;
            action.observation = observation;

            return action;
        }

        public UCAction build(Rectangle sling, UCObservation observation, int id) {
            if(id == -1 || availableActions == null || availableActions.size() == 0) {
                return null;
            }
            Pair<Integer, Double> actionSetting = availableActions.get(id);
            UCAction action = new UCAction();

            int actionId = id;
            int angleInDegrees = actionSetting.getFirst();
            double tapTimeInPercentile = actionSetting.getSecond();
            double inGameAngleInDegrees = Math.toDegrees(getInGameAngle(angleInDegrees));
            double velocity = getVelocity(angleInDegrees);


            action.id = actionId;
            action.observation = observation;
            action.sling = sling;
            action.angleInDegrees = angleInDegrees;
            action.velocity = velocity;
            action.tapTimeInPercentile = tapTimeInPercentile;
            action.inGameAngleInDegrees = inGameAngleInDegrees;

            return action;
        }

        private double getVelocity(int degrees) {
            int index = degrees - FROM_ANGLE;
            if(index < 0) {
                index = 0;
            } else if (index >= VELOCITIES.length) {
                index = VELOCITIES.length - 1;
            }
            return VELOCITIES[index];
        }

        private double getInGameAngle(int degrees) {
            double radians = Math.toRadians(degrees);
            return -0.04669618 +
                    0.93734937 * radians +
                    0.09900396 * radians * radians +
                    -0.03185295 * radians * radians * radians;
        }
   }
}
