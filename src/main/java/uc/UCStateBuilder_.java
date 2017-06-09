package uc;

import ab.demo.other.ActionRobot;
import ab.vision.*;
import ab.vision.real.shape.Circle;
import ab.vision.real.shape.Poly;
import ab.vision.real.shape.Rect;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;
/**
 * Created by keltp on 2017-05-31.
 */
public class UCStateBuilder_ {
    private static final int WIDTH = 390;
    private static final int HEIGHT = 390;
    private static final int X_OFFSET = 390;
    private static final int Y_OFFSET = 0;
    private static final int BIRD_COL = 10;
    private static final int DIMENSION_OBSERVATION = WIDTH * HEIGHT + BIRD_COL;
    private static final int DIMENSION_STATE = 128;

    private static final INDArray PROJECTION_MATRIX = Nd4j.randn(DIMENSION_STATE, DIMENSION_OBSERVATION, System.currentTimeMillis());

    private final String IMG_FORMAT = "png";

    private INDArray mScreenArray;
    private INDArray mBirdArray;


    public static UCStateBuilder_ generateObservation (ActionRobot robot, File originalImage, File preprocessedImage) {
        try {
            return new UCStateBuilder_(robot, originalImage, preprocessedImage);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static INDArray buildState(BufferedImage image) {
        return null;
    }

    public static UCStateBuilder_ generateObservation (ActionRobot robot, File originalImage) {
        return generateObservation(robot, originalImage, null);
    }

    public static UCStateBuilder_ generateObservation (ActionRobot robot) {
        return generateObservation(robot, null, null);
    }

    public INDArray getScreenArray() {
        return mScreenArray;
    }

    public INDArray getBirdArray() {
        return mBirdArray;
    }

    public INDArray getFlattenArray() {
        return Nd4j.toFlattened(mBirdArray, mScreenArray);
    }

    public INDArray getState() {
        return PROJECTION_MATRIX.mmul(Nd4j.toFlattened(mBirdArray, mScreenArray).transposei());
    }

    public boolean isEmptyPixelAtScreen(int pixelX, int pixelY) {
        return mScreenArray.getInt(pixelX - X_OFFSET, pixelY - Y_OFFSET) == 16777215;
    }


    private UCStateBuilder_(ActionRobot mRobot, File originalImage, File preprocessedImage) throws Exception {
        Vision vision = null;
        Rectangle sling = null;
        ABType birdOnSling = ABType.Unknown;
        BufferedImage image = null;
        while ( (sling == null || birdOnSling == ABType.Unknown) && mRobot.getState() == GameStateExtractor.GameState.PLAYING) {
            image = ActionRobot.doScreenShot();
            vision = new Vision(image);
            sling = vision.findSlingshotMBR();
            birdOnSling = mRobot.getBirdTypeOnSling();
        }

        if (sling != null) {
            mScreenArray = Nd4j.zeros(WIDTH, HEIGHT).addi(16777215);
            mBirdArray = Nd4j.zeros(1, BIRD_COL).addi(16777215);

            List<ABObject> birds = vision.findBirdsMBR();
            List<ABObject> pigs = vision.findPigsRealShape();
            List<ABObject> tnts = vision.findTNTs();
            List<ABObject> blocks = vision.findBlocksRealShape();
            List<ABObject> hills = vision.findHills();

            for (int i = 0; i < birds.size(); ++i) {
                mBirdArray.putScalar(0, i, birds.get(i).getType().id);
            }

            fillArray(hills);
            fillArray(blocks);
            fillArray(tnts);
            fillArray(pigs);



            if(image != null) {
                ImageIO.write(image, IMG_FORMAT, originalImage);
            }

            if(preprocessedImage != null) {
                BufferedImage img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_RGB);
                for(int x = 0; x < WIDTH; ++x) {
                    for(int y = 0; y < HEIGHT; ++y) {
                        img.setRGB(x, y, mScreenArray.getInt(x, y));
                    }
                }
                ImageIO.write(img, IMG_FORMAT, preprocessedImage);
            }
        } else {
            throw new Exception("The agent failed to find a slingshot.");
        }
    }

    private void fillArray(List<ABObject> objects) {
        for(ABObject obj : objects) {

            ABType type = obj.getType();
            if(obj instanceof Poly) {
                fillPolygon((Poly) obj, toSimpleRGB(type.id));
            } else if ( obj instanceof Rect) {
                fillRectangle((Rect) obj, toSimpleRGB(type.id));
            } else if (obj instanceof Circle) {
                System.out.println(type.name());
                fillCircle((Circle) obj, toSimpleRGB(type.id));
            } else {
                System.out.println("There is unknown type of shape");
            }


        }
    }

    private void fillCircle(Circle  obj, double value) {
        Circle newCircle = new Circle(obj.centerX, obj.centerY, obj.r - 0.5 , obj.type);

        Rectangle rect = newCircle.getBounds();
        double startX = rect.getX();
        double startY = rect.getY();
        double endX = startX + rect.getWidth();
        double endY = startY + rect.getHeight();

        double centerX = newCircle.centerX;
        double centerY = newCircle.centerY;
        double radius = newCircle.r;
        double radius2 = radius * radius;
        for(double x = startX; x <= endX && x < WIDTH + X_OFFSET; ++x) {
            for(double y = startY; y <= endY && y < HEIGHT + Y_OFFSET; ++y) {
                double dx = x - centerX;
                double dy = y - centerY;
                double dist = dx * dx + dy * dy;
                if(dist <= radius2) {
                    mScreenArray.putScalar((int) (x - X_OFFSET) , (int) (y - Y_OFFSET) , value);
                }
            }
        }
    }

    private void fillRectangle(Rect obj, double value) {
        Rect newRect = new Rect(obj.centerX, obj.centerY, obj.getpWidth() - 1.5, obj.getpLength() - 1.5, obj.angle, obj.type);

        Rectangle rect = newRect.getBounds();
        double startX = rect.getX();
        double startY = rect.getY();
        double endX = startX + rect.getWidth();
        double endY = startY + rect.getHeight();
        Polygon p = newRect.p;

        for(double x = startX; x <= endX && x < WIDTH + X_OFFSET; ++x) {
            for(double y = startY; y <= endY && y < HEIGHT + Y_OFFSET; ++y) {
                if (p.contains(x, y)) {
                    mScreenArray.putScalar((int) (x - X_OFFSET), (int) (y - Y_OFFSET), value);
                }
            }
        }
    }

    private void fillPolygon(Poly obj, double value) {
        Rectangle rect = obj.getBounds();
        double startX = rect.getX();
        double startY = rect.getY();
        double endX = startX + rect.getWidth();
        double endY = startY + rect.getHeight();
        Polygon p = obj.polygon;

        for(double x = startX; x <= endX && x < WIDTH + X_OFFSET; ++x) {
            for(double y = startY; y <= endY && y < HEIGHT + Y_OFFSET; ++y) {
                if (p.contains(x, y)) {
                    mScreenArray.putScalar((int) (x - X_OFFSET), (int) (y - Y_OFFSET), value);
                }
            }
        }
    }

    private int toSimpleRGB (int abTypeId) {
        switch (abTypeId) {
            case 10:
                return 6999032;
            case 9:
                return 7201353;
            case 2:
                return 4270616;
            case 11:
                return 14848294;
            case 12:
                return 10526880;
            case 4:
                return 12648489;
            case 6:
                return 6531781;
            case 5:
                return 15850272;
            default:
                return 16777215;
        }
    }
}
