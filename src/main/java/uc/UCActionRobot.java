package uc;

import ab.demo.other.LoadLevelSchema;
import ab.demo.other.RestartLevelSchema;
import ab.demo.other.ShootingSchema;
import ab.demo.other.Shot;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.ABObject;
import ab.vision.GameStateExtractor;
import org.nd4j.linalg.api.ndarray.INDArray;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keltp on 2017-06-09.
 */
public class UCActionRobot {
    private static Proxy proxy;
    private LoadLevelSchema loadLevelSchema;
    private RestartLevelSchema restartLevelSchema;
    private ShootingSchema shootingSchema;
    private GameStateExtractor stateExtractor;


    public UCActionRobot() {
        if(proxy == null) {
            try {
                proxy = new Proxy(9000) {
                    @Override
                    public void onOpen() {
                        super.onOpen();
                        UCLog.i("Proxy client connected.");
                    }

                    @Override
                    public void onClose() {
                        super.onClose();
                        UCLog.i("Proxy client disconnected.");
                    }
                };
                proxy.start();

                UCLog.i("Server started on port: " + proxy.getPort());
                proxy.waitForClients(1);
            } catch (Exception e) {
                UCLog.e("Proxy connection failed", e);
            }
        }

        loadLevelSchema = new LoadLevelSchema(proxy);
        restartLevelSchema = new RestartLevelSchema(proxy);
        shootingSchema = new ShootingSchema();
        stateExtractor = new GameStateExtractor();
    }

    public void restartLevel() {
        restartLevelSchema.restartLevel();
    }

    public void loadLevel(int i) throws Exception {
        if(i < 1 || i > 21) {
            throw new Exception("Playable level is from 1 to 21.");
        }
        loadLevelSchema.loadLevel(i);
    }

    public BufferedImage doScreenShot() {
        byte[] imageBytes = proxy.send(new ProxyScreenshotMessage());
        BufferedImage image = null;
        try {
            image = ImageIO.read(new ByteArrayInputStream(imageBytes));
        } catch (IOException e) {
            UCLog.e(e.getMessage(), e);
        }
        return image;
    }

    public void fullyZoomIn() {
        for(int i = 0;i < 15;i++) {
            proxy.send(new ProxyMouseWheelMessage(1));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }

    public void fullyZoomOut() {
        for(int i = 0;i < 15;i++) {
            proxy.send(new ProxyMouseWheelMessage(-1));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }

    public GameStateExtractor.GameState getGameState() {
        BufferedImage image = doScreenShot();
        if(image != null) {
            return stateExtractor.getGameState(image);
        } else {
            return GameStateExtractor.GameState.UNKNOWN;
        }
    }

    public void goFromMainMenuToLevelSelection() {
        while (getGameState() == GameStateExtractor.GameState.MAIN_MENU) {
            UCLog.i("Go to the Episode menu.");
            proxy.send(new ProxyClickMessage(305, 277));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }

        while (getGameState() == GameStateExtractor.GameState.EPISODE_MENU) {
            UCLog.i("Select the Poached Eggs episode.");
            proxy.send(new ProxyClickMessage(150, 300));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }
    }

    public void shoot(Shot shot, long waitTime) {
        UCLog.i(String.format("Trying to shoot: Dragging from (%s, %s) to (%s, %s) and tapping at %s", shot.getX(), shot.getY(), shot.getDx(), shot.getDx(), shot.getT_tap()));
        List<Shot> shots = new ArrayList<>();
        shots.add(shot);
        shootingSchema.shoot(proxy, shots);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }



}
