package uc;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;
import org.apache.commons.lang3.exception.ExceptionUtils;
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

    public INDArray observationMatrix() {
        return observationMatrix;
    }

    public double pixelPerScaleX() {
        return pixelPerScaleX;
    }

    public double pixelPerScaleY() {
        return pixelPerScaleY;
    }

    public double xOffset() {
        return xOffset;
    }

    public double yOffset() {
        return yOffset;
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
            observationMatrixColumns = conf.observationPreprocessedWidth();
            observationMatrixRows = conf.observationPreprocessedHeight();
            randomProjection = conf.randomProjection();
        }

        public UCObservation build(BufferedImage zoomOutImage, BufferedImage zoomInImage) throws Exception {
            if(zoomOutImage == null || zoomInImage == null) {
                throw new Exception("Image is null. Retry it.");
            }
            Vision zoomOutVision = new Vision(zoomOutImage);
            Rectangle sling = zoomOutVision.findSlingshotMBR();
            if(sling == null) {
                throw new Exception("Cannot find sling. Maybe the game state is not equal to PLAYING.");
            }
            double pixelPerScale = sling.getHeight() + sling.getWidth();


            Vision zoomInVision = new Vision(zoomInImage);
            List<ABObject> birds = zoomInVision.findBirdsMBR();
            if(birds == null || birds.size() == 0) {
                throw new Exception("Cannot find any birds. Maybe the game state is not equal to PLAYING.");
            }

            UCLog.i(String.format("Find %s birds.", birds.size()));

            List<ABObject> allObjects = new ArrayList<>();
            allObjects.addAll(zoomOutVision.findBlocksRealShape());
            allObjects.addAll(zoomOutVision.findHills());
            allObjects.addAll(zoomOutVision.findPigsRealShape());
            if(allObjects.size() == 0) {
                throw new Exception("Cannot find any objects (e.g., blocks, hills, pigs). Maybe the game state is not equal to PLAYING.");
            }
            UCLog.i(String.format("Find %s objects.", allObjects.size()));

            UCObservation obs = new UCObservation();
            obs.observationMatrix = Nd4j.zeros(observationMatrixRows, observationMatrixColumns);
            obs.pixelPerScaleX = (double) observationImageWidth / pixelPerScale;
            obs.pixelPerScaleY = (double) observationImageHeight / pixelPerScale;
            obs.xOffset = observationImageXOffset;
            obs.yOffset = zoomOutVision.findGroundLevel();
            obs.originalImage = zoomOutImage;
            obs.randomProjection = randomProjection;

            if(obs.yOffset() <= 0) {
                throw new Exception("Cannot find correct ground level. Maybe the game state is not equal to PLAYING.");
            }

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
                            double value = UCVisionUtils.rgbToNormalizedGreyScale(rgb[0], rgb[1], rgb[2]);
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
                        double v = UCVisionUtils.rgbToNormalizedGreyScale(rgb[0], rgb[1], rgb[2]);
                        obs.observationMatrix.putScalar(ny, i * 10 + nx, v);
                    }
                }
            }

            return obs;
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
