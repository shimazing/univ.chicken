package uc;

import ab.demo.other.ActionRobot;
import ab.planner.TrajectoryPlanner;
import ab.vision.ABType;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

/**
 * Created by keltp on 2017-05-18.
 */
public class UCAgent implements Runnable {
    private ActionRobot mRobot;
    private TrajectoryPlanner mPlanner;
    private int mCurLevel = 1;
    private Point mPrevTarget;

    public UCAgent() {
        mRobot = new ActionRobot();
        mPlanner = new TrajectoryPlanner();
        mPrevTarget = null;
    }

    @Override
    public void run() {

    }

    public UCState getState() {
        BufferedImage image = ActionRobot.doScreenShot();
        Vision vision = new Vision(image);
        Rectangle sling = vision.findSlingshotMBR();
        ABType birdOnSling = mRobot.getBirdTypeOnSling();

        while( (sling == null || birdOnSling == ABType.Unknown) &&
                mRobot.getState() == GameStateExtractor.GameState.PLAYING) {
            image = ActionRobot.doScreenShot();
            vision = new Vision(image);
            sling = vision.findSlingshotMBR();
            birdOnSling = mRobot.getBirdTypeOnSling();
        }

        if(sling != null) {

        }
        return null;

    }
}
