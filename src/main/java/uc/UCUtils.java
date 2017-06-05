package uc;

import org.nd4j.linalg.api.ndarray.INDArray;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;
/**
 * Created by keltp on 2017-06-03.
 */
public class UCUtils {
    private static final String IMG_FORMAT = "png";
    private static final int SCREEN_WIDTH = 840;
    private static final int SCREEN_HEIGHT = 480;

    public static void drawImage(BufferedImage image, File f) {
        try {
            ImageIO.write(image, IMG_FORMAT, f);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void drawImages(Point refPoint, List<Point> trajPoints, int xOffset, int yOffset, INDArray background, File f) {
            BufferedImage img = new BufferedImage(SCREEN_WIDTH, SCREEN_HEIGHT, BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = img.createGraphics();
          /*  g2d.setBackground(new Color(WHITE));


            g2d.drawLine(refPoint.x, refPoint.y, refPoint.x, refPoint.y);
            g2d.drawPo

            for(int x = 0; x < WIDTH; ++x) {
                for(int y = 0; y < HEIGHT; ++y) {
                    img.setRGB(x, y, mScreenArray.getInt(x, y));
                }
            }
            ImageIO.write(img, IMG_FORMAT, preprocessedImage);
        }*/
    }
}
