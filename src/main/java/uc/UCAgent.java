package uc;

import ab.demo.NaiveAgent;
import ab.demo.other.ActionRobot;
import ab.planner.TrajectoryPlanner;
import ab.vision.*;
import ab.vision.real.shape.Rect;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

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
        mRobot.loadLevel(mCurLevel);

        while(true) {
            GameStateExtractor.GameState state = mRobot.getState();
            BufferedImage image = ActionRobot.doScreenShot();
            System.out.println(image.getWidth() + "," + image.getHeight() + "," + image.getTileWidth() + "," + image.getTileHeight());
            switch (state) {
                case LOADING:
                case PLAYING:
                case UNKNOWN:
                case WON:
                    mRobot.loadLevel(++mCurLevel);
                    break;
                case LOST:
                    mRobot.restartLevel();
                    break;
                case LEVEL_SELECTION:
                    mRobot.loadLevel(mCurLevel);
                    break;
                case MAIN_MENU:
                case EPISODE_MENU:
                    ActionRobot.GoFromMainMenuToLevelSelection();
                    mRobot.loadLevel(mCurLevel);
                    break;
            }
        }
    }



    public static void main(String args[]) throws IOException {
        INDArray array = Nd4j.create(2 ,2);
        array.putScalar(0, 0,2);
        array.putScalar(0, 1, 2);
        INDArray array2 = Nd4j.randn(5, 4);
       or(int i = 1;i < _launchAngle.length;i++) {
            System.out.println(_launchAngle[i] - _launchAngle[i-1]);
        }

        for(int i = 1;i < _launchVelocity.length;i++) {
            System.out.println(_launchVelocity[i] - _launchVelocity[i-1]);
        }

        System.out.println(_launchAngle.length + "," + _launchVelocity.length);

    }
}
