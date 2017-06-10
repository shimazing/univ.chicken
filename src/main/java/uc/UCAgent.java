package uc;

import ab.demo.other.ActionRobot;
import ab.vision.*;
import uc.data.QecTable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

/**
 * Created by keltp on 2017-05-18.
 */
public class UCAgent implements Runnable {
    private ActionRobot mRobot;
    private QecTable qecTable;
    private List<UCTrace> traces;
    private UCConfiguration configuration;
    private boolean isStarting;
    private int nStepsPerEpsiode;
    private int nEpisodes;
    private double rewardsPerEpisode;
    private double totalRewards;
    private int mCurLevel = 1;
    private TreeMap<Integer, Double> scores;
    private Random random = new Random(System.currentTimeMillis());
    private double epsilon;

    public UCAgent(UCConfiguration conf) {
        mRobot = new ActionRobot();
        configuration = conf;
        qecTable = new QecTable(configuration.maxStateCapacityPerAction(),
                configuration.stateDimension(),
                configuration.nActions(),
                configuration.kNearestNeighbor(),
                configuration.distantFunction());
        isStarting = false;
        epsilon = conf.epsilonStart();
        scores = new TreeMap<>();

    }

    @Override
    public void run() {
        mRobot.loadLevel(mCurLevel);

        while(true) {
            GameStateExtractor.GameState state = mRobot.getState();
            switch (state) {
                case UNKNOWN:
                    System.out.println("Unknown game state...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;
                case LOADING:
                    System.out.println("Loading stage...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    break;

                case LEVEL_SELECTION:
                    System.out.println("Unexpected level selection page. Go to the lastly played level.");
                    mRobot.loadLevel(mCurLevel);
                    break;
                case MAIN_MENU:
                    System.out.println("Unexpected main menu page. Go th the lastly played level.");
                    ActionRobot.GoFromMainMenuToLevelSelection();
                    mRobot.loadLevel(mCurLevel);
                    break;
                case EPISODE_MENU:
                    System.out.println("Unexpected main menu page. Go th the lastly played level.");
                    ActionRobot.GoFromMainMenuToLevelSelection();
                    mRobot.loadLevel(mCurLevel);
                    break;
                case PLAYING:
                    if(!isStarting) {
                        isStarting = true;
                        startEpisode();
                    }
                    try {
                        step();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case WON:
                    mRobot.loadLevel(++mCurLevel);
                    endEpisode();
                    break;
                case LOST:
                    System.out.println("Failed to beat stage " + mCurLevel);
                    failEpisode();
                    mRobot.restartLevel();
                    break;
            }
        }
    }

    private void startEpisode() {
        traces = new ArrayList<>();
        nStepsPerEpsiode = 0;
        rewardsPerEpisode = 0;
    }

    private void step() throws Exception {
       /* UCAction_ action;
        UCState state;
        UCReward reward;

        Rectangle sling1 = getSling();
        if(sling1 == null) {
            return;
        }

        BufferedImage observation = ActionRobot.doScreenShot();
        //state = UCStateBuilder_.buildState(observation);

        if(state == null) {
            return;
        }

        if(random.nextDouble() < epsilon) {
            action = random.nextInt(configuration.nActions());
        } else {
            double maxQ = Double.NEGATIVE_INFINITY;
            for(int i = 0;i < configuration.nActions();i++) {
                double q = qecTable.estimateQValue(state, i);
                if(q > maxQ) {
                    maxQ = q;
                    action = i;
                }
            }
        }

        if(action == -1) {
            return;
        }*/






    }

    private void endEpisode() {

    }

    private void failEpisode() {

    }


    private Rectangle getSling() throws Exception {
        BufferedImage image;
        Vision vision;
        Rectangle sling = null;

        while (sling == null && mRobot.getState() == GameStateExtractor.GameState.PLAYING) {
            ActionRobot.fullyZoomOut();
            Thread.sleep(2000);
            image = ActionRobot.doScreenShot();
            vision = new Vision(image);
            sling = vision.findSlingshotMBR();
        }

        return sling;
    }

    private boolean isSameScene(Rectangle sling1, Rectangle sling2) throws Exception {
        if(sling1 == null || sling2 == null) {
            return false;
        }

        double dw = sling1.width - sling2.width;
        double dh = sling1.height - sling2.height;
        double diff = dw * dw + dh * dh;

        return diff < 25;
    }

    public static void main(String args[]) throws IOException {

    }
}
