package uc;

import ab.vision.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uc.data.QecTable;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by keltp on 2017-05-18.
 */
public class UCAgent implements Runnable {
    private UCActionRobot robot;

    private QecTable qecTable;
    private UCStatistics stats;
    private UCConfiguration configuration;
    private UCObservation.Builder observationBuilder;
    private UCAction.Builder actionBuilder;
    private UCReward.Builder rewardBuilder;

    private int curLevel = 1;

    private List<UCTrace> traces;
    private boolean isStageStarted;
    private Random random = new Random(System.currentTimeMillis());

    private double rewardsPerEpsiode;
    private int scoresPerEpsiode;
    private int stepsPerEpisode;
    private long timePerEpisode;
    private boolean isTraining = true;
    private int prevEpoch;

    public UCAgent(UCConfiguration conf, QecTable table, UCStatistics stat) throws Exception {
        robot = new UCActionRobot();

        if(conf == null) {
            configuration = new UCConfiguration.Builder().build();
        } else {
            configuration = conf;
        }

        if(table == null) {
            qecTable = new QecTable(configuration);
        } else {
            qecTable = table;
        }

        if(stat == null) {
            stats = new UCStatistics();
        } else {
            stats = stat;
        }

        observationBuilder = new UCObservation.Builder(conf);
        actionBuilder = new UCAction.Builder(conf);
        rewardBuilder = new UCReward.Builder();

        traces = new ArrayList<>();
        isStageStarted = false;
    }

    public UCAgent (UCConfiguration conf) throws Exception {
        this(conf, null, null);
    }

    public UCAgent (UCConfiguration conf, QecTable table) throws Exception {
        this(conf, table, null);
    }

    public void serialize(File dir, String confFileName, String qecFileName, String statsFileName) throws IOException {
        if(!dir.exists()) {
            dir.mkdirs();
        }

        configuration.serialize(new File(dir, confFileName));
        qecTable.serialize(new File(dir, qecFileName), new File(dir, "qec_table"));
        stats.serialize(new File(dir, statsFileName));
    }

    public static UCAgent deserialize(File dir, String confFileName, String qecFileName, String statesFileName) throws Exception {
        UCConfiguration configuration = UCConfiguration.deserializeFromJson(new File(dir, confFileName));
        QecTable qecTable = QecTable.deserialize(new File(dir, qecFileName),
                configuration);
        UCStatistics stats = UCStatistics.deserialize(new File(dir, statesFileName));

        return new UCAgent(configuration, qecTable, stats);
    }

    public void setIsTraining(boolean isTraining) {
        this.isTraining = isTraining;
    }

    public void run(int level) {
        curLevel = level;
        run();
    }

    @Override
    public void run() {
        robot.connect();
        try {
            robot.loadLevel(curLevel);
        } catch (Exception e) {
            UCLog.e(e.getMessage(), e);
        }

        while (true) {
            GameStateExtractor.GameState state = robot.getGameState(robot.doScreenShot());
            switch (state) {
                case UNKNOWN:
                    UCLog.w("Unknown game state...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case LOADING:
                    UCLog.i("Loading stage...");
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case LEVEL_SELECTION:
                    UCLog.w("Unexpected level selection page. Go to the lastly played level.");
                    try {
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case MAIN_MENU:
                    UCLog.w("Unexpected main menu page. Go th the lastly played level.");
                    try {
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case EPISODE_MENU:
                    UCLog.w("Unexpected main menu page. Go th the lastly played level.");
                    try {
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case PLAYING:
                    if(!isStageStarted) {
                        isStageStarted = true;
                        startEpisode();
                    }
                    try {
                        step();
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case WON:
                    try {
                        endEpisode();
                        isStageStarted = false;
                        UCLog.i(String.format("The %s-th level clear! Go to the next level.", curLevel));
                        curLevel++;
                        if(curLevel > 21) {
                            curLevel = 1;
                        }
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case LOST:
                    try {
                        endEpisode();
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    UCLog.w(String.format("Failed to beat stage %s. Retry it", curLevel));
                    isStageStarted = false;
                    robot.restartLevel();
                    break;
            }
        }
    }

    private void startEpisode() {
        UCLog.i(String.format("Starts %s-th episode in the level %s", stats.nTotalEpisodes() + 1, curLevel));

        traces.clear();
        prevEpoch = stats.nTotalSteps() / configuration.nStepsPerEpoch();
        rewardBuilder = rewardBuilder.init();
        stepsPerEpisode = 0;
        timePerEpisode = System.currentTimeMillis();
        rewardsPerEpsiode = 0;
        scoresPerEpsiode = 0;
    }

    private void step() throws Exception {
        UCLog.i(String.format("%s-th steps of the %s-th episode in the level %s", stepsPerEpisode + 1, stats.nTotalEpisodes() + 1, curLevel));

        Rectangle sling1 = robot.getSling();
        if(sling1 == null) {
            return;
        }

        BufferedImage image = robot.doScreenShot();
        UCObservation observation = observationBuilder.build(image);
        INDArray state = observation.state();
        double epsilon = Math.min(configuration.epsilonMin(), configuration.epsilonStart() - stats.nTotalSteps() * configuration.epsilonRate());
        UCAction action;

        if(isTraining && random.nextDouble() < epsilon) {
            action = actionBuilder.build(image, observation, random.nextInt(configuration.nActions()));
        } else {
            double maxQ = Double.NEGATIVE_INFINITY;
            int actionId = 0;
            for(int i = 0;i < configuration.nActions();i++) {
                double q = qecTable.estimateQValue(state, i);
                if(q > maxQ) {
                    maxQ = q;
                    actionId = i;
                }
            }
            action = actionBuilder.build(image, observation, actionId);
        }
        Rectangle sling2 = robot.getSling();

        if(UCActionRobot.isSameScale(sling1, sling2)) {
            robot.shoot(action.shot(), 10000);
        }

        int score = robot.getScoreInGame();
        UCReward reward = rewardBuilder.build(score);
        traces.add(new UCTrace(observation, state, action, reward));

        stepsPerEpisode ++;
        scoresPerEpsiode += score;
        rewardsPerEpsiode += reward.netReward();
        stats.updatePerSteps();
    }

    private void endEpisode() throws Exception {
        double qReturn = 0;
        for(int i = traces.size() - 1;i >= 0; i--) {
            UCTrace trace = traces.get(i);
            qReturn = qReturn * configuration.discountFactor() + trace.reward.netReward();
            qecTable.update(trace.state, trace.action.id(), qReturn);
        }
        int score = robot.getScoreEndGame();
        stats.updatePerEpisode(curLevel, rewardsPerEpsiode, score);
        timePerEpisode = System.currentTimeMillis() - timePerEpisode;
        UCLog.i(String.format("The %s-th episode completes: %s steps, %s seconds, %s rewards, and %s scores.",
                stats.nTotalEpisodes(), stepsPerEpisode, timePerEpisode / 1000, rewardsPerEpsiode, scoresPerEpsiode));

        int curEpoch = stats.nTotalSteps() / configuration.nStepsPerEpoch();
        if(prevEpoch != curEpoch) {
            UCLog.i(String.format("Reach to the %s-th epoch. Save the agent information."));
            serialize(new File("./autosave"), "conf.json", "qec.json", "stats.json");
        }
    }

    public static void main(String args[]) throws IOException {

    }
}
