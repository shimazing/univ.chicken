package uc;

import ab.demo.other.Shot;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.server.proxy.message.ProxyScreenshotMessage;
import ab.vision.GameStateExtractor;
import ab.vision.Vision;
import uc.schema.LoadLevelSchema;
import uc.schema.RestartLevelSchema;
import uc.schema.ShootingSchema;

import javax.imageio.ImageIO;
import java.awt.*;
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

    public void connect() {
        if (proxy == null) {
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

        loadLevelSchema = new LoadLevelSchema(proxy, this);
        restartLevelSchema = new RestartLevelSchema(proxy, this);
        shootingSchema = new ShootingSchema();
        stateExtractor = new GameStateExtractor();
    }

    public void restartLevel() {
        restartLevelSchema.restartLevel();
    }

    public void loadLevel(int i) throws Exception {
        if (i < 1 || i > 21) {
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
        for (int i = 0; i < 15; i++) {
            proxy.send(new ProxyMouseWheelMessage(1));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }

    public void fullyZoomOut() {
        for (int i = 0; i < 15; i++) {
            proxy.send(new ProxyMouseWheelMessage(-1));
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }

    public GameStateExtractor.GameState getGameState(BufferedImage image) {
        if (image != null) {
            return stateExtractor.getGameState(image);
        } else {
            return GameStateExtractor.GameState.UNKNOWN;
        }
    }

    public void goFromMainMenuToLevelSelection() {
        while (getGameState(doScreenShot()) == GameStateExtractor.GameState.MAIN_MENU) {
            UCLog.i("Go to the Episode menu.");
            proxy.send(new ProxyClickMessage(305, 277));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }

        while (getGameState(doScreenShot()) == GameStateExtractor.GameState.EPISODE_MENU) {
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
        List<Shot> shots = new ArrayList<>();
        shots.add(shot);
        shootingSchema.shoot(proxy, shots);
        try {
            Thread.sleep(waitTime);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
    }

    public Rectangle getSling() {
        BufferedImage image;
        Rectangle sling = null;
        while(true) {
            if(sling != null) {
                break;
            }
            image = doScreenShot();
            if(image == null) {
                break;
            }

            GameStateExtractor.GameState state = getGameState(image);
            if(state != GameStateExtractor.GameState.PLAYING) {
                break;
            }
            fullyZoomOut();
            Vision vision = new Vision(image);
            sling = vision.findSlingshotMBR();
        }
        return sling;
    }

    public static boolean isSameScale(Rectangle sling1, Rectangle sling2) {
        if (sling1 == null || sling2 == null) {
            return false;
        }

        double dw = sling1.width - sling2.width;
        double dh = sling1.height - sling2.height;
        double diff = dw * dw + dh * dh;

        return diff < 25;
    }

    public int getScoreInGame() {
        int prevScore = -1;
        int n = 9;
        for(int i = 0;i < 10;i++) {
            BufferedImage image = doScreenShot();
            if(image == null) {
                break;
            }

            GameStateExtractor.GameState state = stateExtractor.getGameState(image);
            if(state != GameStateExtractor.GameState.PLAYING) {
                break;
            }

            int score = stateExtractor.getScoreInGame(image);
            if(prevScore != -1 && score - prevScore >= 10000) {
                break;
            }
            prevScore = score;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            n--;
        }

        try {
            Thread.sleep(n * 1000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }

        return prevScore;
    }

    public int getScoreEndGame() {
        int prevScore = -1;
        while(true) {
            BufferedImage image = doScreenShot();
            if(image == null) {
                break;
            }

            GameStateExtractor.GameState state = stateExtractor.getGameState(image);
            if(state != GameStateExtractor.GameState.WON) {
                break;
            }

            int score = stateExtractor.getScoreEndGame(image);
            if(prevScore == -1) {
                prevScore = score;
             } else if (score == prevScore) {
                break;
             }

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }
        return prevScore;
    }
}
