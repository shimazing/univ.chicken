package uc;

import ab.demo.other.Shot;
import ab.vision.*;
import org.nd4j.linalg.api.ndarray.INDArray;
import uc.data.QecTable;

import javax.imageio.ImageIO;
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
    private int stepsPerEpisode;
    private long timePerEpisode;
    private boolean isTraining = true;
    private int prevEpoch;

    private final File saveDir = new File("./autosave");
    private final File imgDir = new File("./imgs");

    public UCAgent(UCConfiguration conf, QecTable table, UCStatistics stat) throws Exception {
        robot = new UCActionRobot();
        saveDir.mkdirs();
        imgDir.mkdirs();

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

    public void serialize(File dir, String confFileName,
                          String qecSettingFileName, String statesFileName, String qValuesFileName,
                          String lruValuesFileName, String statsFileName) throws IOException {
        if(!dir.exists()) {
            dir.mkdirs();
        }

        configuration.serialize(new File(dir, confFileName));
        qecTable.serialize(new File(dir, qecSettingFileName), new File(dir, statesFileName), new File(dir, qValuesFileName), new File(dir, lruValuesFileName));
        stats.serialize(new File(dir, statsFileName));
    }

    public static UCAgent deserialize(File dir, String confFileName,
                                      String qecSettingFileName, String statesFileName, String qValuesFileName,
                                      String lruValuesFileName, String statsFileName) throws Exception {
        UCConfiguration configuration = UCConfiguration.deserializeFromJson(new File(dir, confFileName));
        QecTable qecTable = QecTable.deserialize(new File(dir, qecSettingFileName),
                new File(dir, statesFileName), new File(dir, qValuesFileName), new File(dir, lruValuesFileName),
                configuration);
        UCStatistics stats = UCStatistics.deserialize(new File(dir, statsFileName));

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
        UCLog.i("UCAgent started with this configuration: \r\n" + configuration.toString());
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
                        robot.goFromMainMenuToLevelSelection();
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    break;
                case EPISODE_MENU:
                    UCLog.w("Unexpected main menu page. Go th the lastly played level.");
                    try {
                        robot.goFromMainMenuToLevelSelection();
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
                    UCLog.i(String.format("The %s-th level clear!", curLevel));
                    endEpisode();
                    isStageStarted = false;
                    curLevel++;
                    if(curLevel > 21) {
                        curLevel = 1;
                    }
                    try {
                        robot.loadLevel(curLevel);
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    break;
                case LOST:
                    UCLog.w(String.format("Failed to beat stage %s. Retry it", curLevel));
                    endEpisode();
                    isStageStarted = false;
                    robot.restartLevel();
                    break;
            }
        }
    }

    private void startEpisode() {
        UCLog.i(String.format("Starts the %s-th EPISODE in the level %s", stats.nTotalEpisodes() + 1, curLevel));

        traces.clear();

        prevEpoch = stats.nTotalSteps() / configuration.nStepsPerEpoch();
        rewardBuilder = rewardBuilder.init();
        stepsPerEpisode = 0;
        timePerEpisode = System.currentTimeMillis();
        rewardsPerEpsiode = 0;
    }

    private void step() throws Exception {
        UCLog.i(String.format("The %s-th steps of the %s-th EPISODE in the level %s", stepsPerEpisode + 1, stats.nTotalEpisodes() + 1, curLevel));

        Rectangle sling1 = robot.getSling();
        if(sling1 == null) {
            UCLog.w("The first sling is not found. Retry this step.");
            return;
        }

        BufferedImage image = robot.doScreenShot();
        final UCObservation observation = observationBuilder.build(image);
        final INDArray state = observation.state();
        double epsilon = Math.max(configuration.epsilonMin(), configuration.epsilonStart() - stats.nTotalSteps() * configuration.epsilonRate());
        UCAction action;
        if(isTraining && random.nextDouble() < epsilon) {
            int actionId = random.nextInt(configuration.nActions());
            UCLog.i(String.format("Current EPS = %s and select RANDOM ACTION %s ", epsilon, actionId));
            action = actionBuilder.build(sling1, observation, actionId);
        } else {
            double maxQ = Double.NEGATIVE_INFINITY;
            int actionId = -1;
            for(int i = 0;i < configuration.nActions();i++) {
                double q = qecTable.estimateQValue(state, i);
                if(!Double.isNaN(q) && q > maxQ) {
                    maxQ = q;
                    actionId = i;
                }
            }
            if(actionId == -1) {
                actionId = random.nextInt(configuration.nActions());
            }
            UCLog.i(String.format("Current EPS = %s and select SUBOPTIMAL ACTION %s ", epsilon, actionId));
            action = actionBuilder.build(sling1, observation, actionId);
        }
        Rectangle sling2 = robot.getSling();
        if(sling2 == null) {
            UCLog.w("The second sling is not found. Retry this step.");
            return;
        }

        final Shot shot = action.shot();
        if(UCActionRobot.isSameScale(sling1, sling2)) {
            robot.shoot(shot, 0);
        } else {
            UCLog.w("Two slings are not same scale. Retry this step.");
            return;
        }

        if(isTraining) {
            new Thread() {
                @Override
                public void run() {
                    BufferedImage canvas = UCVisionUtils.copy(observation.originalImage());
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        UCLog.e(e.getMessage(), e);
                    }
                    try {
                        BufferedImage trajImage = robot.doScreenShot();
                        Vision vision = new Vision(trajImage);
                        List<Point> actualTrajectories = vision.findEstimatedTrajPoints();
                        canvas = UCVisionUtils.drawTrajectories(canvas, actualTrajectories, Color.RED, 3);
                    } catch (NullPointerException e) {

                    }

                    try {
                        List<Point> predictTrajectories = action.predictTrajectories();
                        canvas = UCVisionUtils.drawTrajectories(canvas, predictTrajectories, Color.BLACK, 5);
                    } catch (NullPointerException e) {

                    }

                    try {
                        ImageIO.write(observation.preprocessedImage(), "png", new File(imgDir, stats.nTotalSteps() + "_preprocess.png"));
                        ImageIO.write(canvas, "png", new File(imgDir, stats.nTotalSteps() + "_trajectories.png"));
                    } catch (Exception e) {
                        UCLog.e(e.getMessage(), e);
                    }
                }
            }.start();
        }

        int score = robot.getScoreInGame();
        final UCReward reward = rewardBuilder.build(score);
        traces.add(new UCTrace(observation, state, action, reward));
        stepsPerEpisode ++;
        rewardsPerEpsiode += reward.netReward();
        stats.updatePerSteps();

        UCLog.i(String.format("The agent did ACTION %s (angle %s in degrees, tapping time %s in millis, or tapping percentile %s), and receive REWARD %s (%s in scores).",
                action.id(), action.angleInDegrees(), shot.getT_tap(), action.tapTimeInPercentile(), reward.netReward(), reward.netScore()));
    }

    private void endEpisode() {
        int score = Math.max(robot.getScoreEndGame(), 0);
        stats.updatePerEpisode(curLevel, rewardsPerEpsiode, score);
        timePerEpisode = System.currentTimeMillis() - timePerEpisode;
        UCLog.i(String.format("The %s-th episode and %s-th steps completes: %s steps, %s seconds, %s rewards, and %s scores.",
                stats.nTotalEpisodes(), stats.nTotalSteps(), stepsPerEpisode, timePerEpisode / 1000, rewardsPerEpsiode, score));

        double qReturn = 0;
        for(int i = traces.size() - 1;i >= 0; i--) {
            UCTrace trace = traces.get(i);
            qReturn = qReturn * configuration.discountFactor() + trace.reward.netReward();
            try {
                qecTable.update(trace.state, trace.action.id(), qReturn);
            } catch (Exception e) {
                UCLog.e(e.getMessage(), e);
            }
        }

        int curEpoch = stats.nTotalSteps() / configuration.nStepsPerEpoch();
        if(prevEpoch != curEpoch) {
            UCLog.i(String.format("Reach to the %s-th epoch. Save the agent information.", curEpoch));
            try {
                serialize(saveDir, "conf.json", "qec.json", "states.bin", "qvalues.bin", "lruvalues.bin", "stats.json");
            } catch (IOException e) {
                UCLog.e(e.getMessage(), e);
            }
        }
    }
}
