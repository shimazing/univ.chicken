package uc;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by keltp on 2017-05-18.
 */
public class UCObservation {
    private INDArray observationMatrix;
    private BufferedImage originalImage;
    private double pixelPerScaleX;
    private double pixelPerScaleY;
    private double xOffset;
    private double yOffset;
    private INDArray randomProjection;

    public boolean isNotEmpty(int xPixel, int yPixel) {
        int nx = (int) ((xPixel - xOffset) / pixelPerScaleX);
        int ny = (int) (observationMatrix.rows() + (yPixel - yOffset) / pixelPerScaleY);

        if (nx >= 0 && nx < observationMatrix.columns() && ny >= 6 && ny < observationMatrix.rows()) {
            return observationMatrix.getDouble(ny, nx) > 0;
        }
        return false;
    }

    public INDArray observation() {
        return observationMatrix;
    }

    public INDArray state() {
        return Nd4j.toFlattened(observationMatrix).mmul(randomProjection);
    }

    public BufferedImage originalImage() {
        return originalImage;
    }

    public BufferedImage preprocessedImage() {
        BufferedImage image = new BufferedImage(observationMatrix.columns(), observationMatrix.rows(), BufferedImage.TYPE_INT_ARGB);
        for(int nx = 0; nx < observationMatrix.columns(); nx++) {
            for (int ny = 0; ny < observationMatrix.rows(); ny++) {
                int v = (int) (observationMatrix.getDouble(ny, nx) * 255);
                int rgb = new Color(v, v, v).getRGB();
                image.setRGB(nx, ny, rgb);
            }
        }
        return image;
    }

    public static class Builder {
        private int observationImageXOffset;
        private int observationImageWidth;
        private int observationImageHeight;
        private int observationMatrixColumns;
        private int observationMatrixRows;
        private INDArray randomProjection;

        public Builder (UCConfiguration conf) {
            observationImageXOffset = conf.observationImageXOffset();
            observationImageWidth = conf.observationImageWidth();
            observationImageHeight = conf.observationImageHeight();
            observationMatrixColumns = conf.observationMatrixColumns();
            observationMatrixRows = conf.observationMatrixRows();
            randomProjection = conf.randomProjection();
        }

        public UCObservation build(BufferedImage image) throws IOException {
            Vision vision = new Vision(image);
            Rectangle sling = vision.findSlingshotMBR();
            double pixelPerScale = sling.getHeight() + sling.getWidth();

            List<ABObject> birds = vision.findBirdsMBR();
            List<ABObject> allObjects = new ArrayList<>();

            allObjects.addAll(vision.findBlocksRealShape());
            allObjects.addAll(vision.findHills());
            allObjects.addAll(vision.findPigsRealShape());

            UCObservation obs = new UCObservation();
            obs.observationMatrix = Nd4j.zeros(observationMatrixRows, observationMatrixColumns);
            obs.pixelPerScaleX = (double) observationImageWidth / pixelPerScale;
            obs.pixelPerScaleY = (double) observationImageHeight / pixelPerScale;
            obs.xOffset = observationImageXOffset;
            obs.yOffset = vision.findGroundLevel();
            obs.originalImage = image;
            obs.randomProjection = randomProjection;

            for(int nx = 0; nx < observationMatrixColumns; nx++) {
                for (int ny = 0; ny < observationMatrixRows; ny++) {
                    double x = obs.xOffset + obs.pixelPerScaleX * nx;
                    double y = obs.yOffset - obs.pixelPerScaleY * (observationMatrixRows - ny);
                    for(ABObject object : allObjects) {
                        boolean contains;
                        if (object instanceof Poly) {
                            contains = ((Poly) object).polygon.contains(x, y);
                        } else if (object instanceof Rect) {
                            contains = ((Rect) object).p.contains(x, y);
                        } else{
                            contains = object.contains(x, y);
                        }

                        if(contains) {
                            int[] rgb = toRGB(object.type);
                            double value = rgbToNormalizedGreyScale(rgb[0], rgb[1], rgb[2]);
                            obs.observationMatrix.putScalar(ny, nx, value);
                        }
                    }
                }
            }

            Collections.sort(birds, new Comparator<ABObject>() {
                @Override
                public int compare(ABObject o1, ABObject o2) {
                    return ((Integer)(o1.y)).compareTo((Integer)(o2.y));
                }
            });
            ABObject birdOnSling = birds.get(0);

            Collections.sort(birds, new Comparator<ABObject>() {
                @Override
                public int compare(ABObject o1, ABObject o2) {
                    return ((Integer)(o1.x)).compareTo((Integer)(o2.x));
                }
            });
            ABObject firstBird = birds.get(0);

            if(!birdOnSling.equals(firstBird)) {
                Collections.sort(birds, new Comparator<ABObject>() {
                    @Override
                    public int compare(ABObject o1, ABObject o2) {
                        return ((Integer)(o2.x)).compareTo((Integer)(o1.x));
                    }
                });
            }

            for(int i = 0;i < birds.size();i++) {
                ABObject bird = birds.get(i);
                for(int nx = 0; nx < 6; nx++) {
                    for(int ny = 0; ny < 6; ny++) {
                        int[] rgb = toRGB(bird.getType());
                        double v = rgbToNormalizedGreyScale(rgb[0], rgb[1], rgb[2]);
                        obs.observationMatrix.putScalar(ny, i * 10 + nx, v);
                    }
                }
            }

            return obs;
        }

        private double rgbToNormalizedGreyScale(int r, int g, int b) {
            return (0.299 * r + 0.587 * g + 0.114 * b) / 255;
        }

        private int[] toRGB(ABType type) {
            switch (type) {
                case Wood:
                    return new int[]{226, 245, 38};
                case Stone:
                    return new int[]{160, 160, 160};
                case Hill:
                    return new int[]{65, 42, 24};
                case Pig:
                    return new int[]{109, 226, 73};
                case Ice:
                    return new int[]{146, 217, 250};
                case RedBird:
                    return new int[]{214, 0, 45};
                case YellowBird:
                    return new int[]{241, 219, 32};
                case BlueBird:
                    return new int[]{99, 170, 197};
                default:
                    return new int[]{255, 255, 255};
            }
        }
    }
}
