package uc;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by keltp on 2017-05-31.
 */
public class UCAction_ {
    private static final double[] ANGLES = new double[30];
    private static final double[] TAP_TIMES = new double[20];
    private static final double[] LAUNCH_ANGLES  = {0.0698132, 0.0872665, 0.10472, 0.122173, 0.139626, 0.15708, 0.174533, 0.191986, 0.20944, 0.226893, 0.244346, 0.261799, 0.279253, 0.296706, 0.314159, 0.331613, 0.349066, 0.366519, 0.383972, 0.401426, 0.418879, 0.436332, 0.453786, 0.471239, 0.488692, 0.506145, 0.523599, 0.541052, 0.558505, 0.575959, 0.593412, 0.610865, 0.628319, 0.645772, 0.663225, 0.680678, 0.698132, 0.715585, 0.733038, 0.750492, 0.767945, 0.785398, 0.802851, 0.820305, 0.837758, 0.855211, 0.872665, 0.890118, 0.907571, 0.925025, 0.942478, 0.959931, 0.977384, 0.994838, 1.012290, 1.029740, 1.047200, 1.064650, 1.082100, 1.099560, 1.117010, 1.134460, 1.151920, 1.169370, 1.186820, 1.204280, 1.221730, 1.239180, 1.256640, 1.274090, 1.291540, 1.309000, 1.326450, 1.343900, 1.361360, 1.378810};
    private static final double[] LAUNCH_VELOCITIES = {2.78571, 2.78323, 2.77290, 2.72967, 2.74747, 2.74946, 2.74762, 2.74765, 2.75169, 2.75992, 2.77147, 2.78507, 2.79933, 2.81297, 2.82493, 2.83443, 2.84097, 2.83430, 2.82441, 2.81147, 2.80584, 2.79793, 2.78825, 2.77734, 2.76572, 2.78389, 2.77228, 2.76128, 2.75119, 2.74222, 2.73450, 2.72811, 2.72302, 2.71916, 2.71641, 2.71461, 2.71357, 2.71309, 2.71298, 2.71304, 2.71312, 2.71307, 2.71278, 2.71219, 2.71125, 2.70995, 2.70834, 2.70646, 2.70438, 2.70218, 2.69996, 2.69778, 2.69573, 2.69385, 2.69217, 2.69070, 2.68941, 2.68826, 2.68721, 2.68618, 2.68514, 2.68404, 2.68287, 2.68163, 2.68035, 2.67901, 2.67752, 2.67554, 2.67231, 2.66625, 2.65447, 2.61058, 2.55100, 2.47435, 2.40219, 2.31633};
    private static double X_OFFSET = 0.188;
    private static double Y_OFFSET = 0.156;
    private static final int X_MAX = 640;
    private static final int TIME_UNIT = 815;

    static {
        for(int i = 0;i < ANGLES.length;i++) {
            ANGLES[i] = Math.toRadians(4 + 2.533 * i);
        }
        for (int i = 0;i < TAP_TIMES.length;i++) {
            TAP_TIMES[i] = 0.6 + 0.0175 * i;
        }
    }

    private final int mId;
    private final double mAngle;
    private final double mTapTime;

    private UCAction_(int id, double angle, double tapTime) {
        mId = id;
        mAngle = angle;
        mTapTime = tapTime;
    }

    public static Shot getShot(INDArray state, int actionId) {
        return null;
    }

    public int getActionId() {
        return mId;
    }

    public double getAngle() {
        return mAngle;
    }

    public double getTapTimes() {
        return mTapTime;
    }

    public static List<UCAction_> createActions() {
        int id = 0;
        List<UCAction_> actions = new ArrayList<>();
        for(double angle : ANGLES) {
            for(double tapTime : TAP_TIMES) {
                actions.add(new UCAction_(id++, angle, tapTime));
            }
        }
        return actions;
    }

    public Shot generateShot(Rectangle sling, TrajectoryPlanner planner) {
        Point refPoint = planner.getReferencePoint(sling);
        Point relPoint = planner.findReleasePoint(sling, planner.actualToLaunch(mAngle));
        int dx = relPoint.x - refPoint.x;
        int dy = relPoint.y - refPoint.y;

        return null;
        //return new Shot(refPoint.x, refPoint.y, dx, dy, 0, mTapTime);
    }

    private double actualToLaunch(double angleInRadian) {
        return angleInRadian + (0.0386871 + 0.12898 * angleInRadian - 0.214323 * angleInRadian * angleInRadian + 0.0818703 * angleInRadian * angleInRadian * angleInRadian);
    }

    private double launchToActual(double angleInRadian) {
        return angleInRadian - (0.0386871 + 0.12898 * angleInRadian - 0.214323 * angleInRadian * angleInRadian + 0.0818703 * angleInRadian * angleInRadian * angleInRadian);
    }

    private double getVelocity(double radian) {
        double degree = Math.toDegrees(radian);
        int pos = (int)(degree) - 4;
        if(pos > LAUNCH_ANGLES.length - 1) {
            pos = LAUNCH_ANGLES.length - 1;
        } else if (pos < 0) {
            pos = 0;
        }
        return LAUNCH_VELOCITIES[pos];
    }

    private Point getReferencePoint(Rectangle sling) {
        return new Point((int) (sling.x + X_OFFSET * sling.width ), (int) (sling.y + Y_OFFSET * sling.height));
    }

    private Point getReleasePoint(Rectangle sling, double radian) {
        double mag = sling.height * 10;
        Point ref = getReferencePoint(sling);
        double launchRadian = actualToLaunch(radian);
        return new Point((int)(ref.x - mag * Math.cos(launchRadian)), (int)(ref.y + mag * Math.sin(launchRadian)));
    }

    private List<Point> predictTrajectory(Rectangle sling, double radian, UCStateBuilder_ observation) {
        Point refPoint = getReferencePoint(sling);
        List<Point> trajs = new ArrayList<>();
        double scale = sling.height + sling.width;
        double velocity = getVelocity(radian);
        double a = Math.tan(radian);
        double b = - 1 / (2 * velocity * velocity * Math.cos(radian) * Math.cos(radian) * scale);

        for(int x = 0; x < X_MAX; x++) {
            int y = refPoint.y - (int) (a * x + b * x * x);
            trajs.add(new Point(x, y));

            if(!observation.isEmptyPixelAtScreen(x, y)) {
                break;
            }
        }
        return trajs;
    }

    private int getTapTime(Rectangle sling, Point targetPoint, double radian, double percentile) {
        Point refPoint = getReferencePoint(sling);
        double velocity = getVelocity(radian);
        int distance = (int) ( (refPoint.x - targetPoint.x) * percentile / (sling.height + sling.width));
        return (int) (distance / (velocity * Math.cos(radian)) * TIME_UNIT);
    }
}
