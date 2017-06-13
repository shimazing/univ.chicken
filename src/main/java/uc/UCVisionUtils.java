package uc;

import ab.vision.Vision;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

/**
 * Created by keltp on 2017-06-12.
 */
public class UCVisionUtils {
    public static double rgbToNormalizedGreyScale(int r, int g, int b) {
        return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
    }

    public static BufferedImage copy(BufferedImage original) {
        BufferedImage image = new BufferedImage(original.getWidth(), original.getHeight(), BufferedImage.TYPE_INT_RGB);
        Graphics g = image.getGraphics();
        g.drawImage(original, 0, 0, null);
        g.dispose();
        return image;
    }

    public static BufferedImage drawTrajectories(BufferedImage canvas, List<Point> trajectories, Color color, int width) {
        if(trajectories != null && trajectories.size() > 0) {
            Graphics2D g = canvas.createGraphics();
            int[] pX = new int[trajectories.size()];
            int[] pY = new int[trajectories.size()];
            int pN = trajectories.size();
            for(int i = 0;i < trajectories.size();i++) {
                Point point = trajectories.get(i);
                pX[i] = point.x;
                pY[i] = point.y;
            }

            g.setColor(color);
            g.setStroke(new BasicStroke(width));
            g.drawPolyline(pX, pY, pN);
            g.dispose();
        }
        return canvas;
    }



}
