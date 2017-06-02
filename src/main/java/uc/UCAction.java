package uc;

import ab.demo.other.Shot;
import ab.planner.TrajectoryPlanner;
import ab.vision.real.shape.Rect;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by keltp on 2017-05-31.
 */
public class UCAction {
    private static final double[] ANGLES = new double[30];
    private static final double[] TAP_TIMES = new double[20];

    static {
        for(int i = 0;i < ANGLES.length;i++) {
            ANGLES[i] = Math.toRadians(-10 + i * 3);
        }
        for (int i = 0;i < TAP_TIMES;i++) {
        }
    }

    private final int mId;
    private final double mAngle;
    private final int mTapTime;

    private UCAction(int id, double angle, int tapTime) {
        mId = id;
        mAngle = angle;
        mTapTime = tapTime;
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

    public static List<UCAction> getActions() {
        int id = 0;
        List<UCAction> actions = new ArrayList<>();
        for(double angle : ANGLES) {
            for(int tapTime : TAP_TIMES) {
                actions.add(new UCAction(id++, angle, tapTime));
            }
        }
        return actions;
    }

    public Shot generateShot(Rectangle sling, TrajectoryPlanner planner) {
        Point refPoint = planner.getReferencePoint(sling);
        Point relPoint = planner.findReleasePoint(sling, planner.actualToLaunch(mAngle));
        int dx = relPoint.x - refPoint.x;
        int dy = relPoint.y - refPoint.y;

        return new Shot(refPoint.x, refPoint.y, dx, dy, 0, mTapTime);
    }

    private Point findCollisionPoint(UCObservation observation, ) {


    }
}
