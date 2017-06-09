package uc;

import ab.vision.ABObject;
import ab.vision.ABType;
import ab.vision.Vision;
import ab.vision.VisionRealShape;
import ab.vision.real.shape.Rect;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.awt.image.BufferedImage;
import java.awt.Rectangle;
import java.util.List;

/**
 * Created by keltp on 2017-05-18.
 */
public class UCState {
    private INDArray state;

    public INDArray state () {
        return state.dup();
    }

    public static class Builder {
        private INDArray projection;

        public Builder(int nObsDimension, int nStateDimension, long seed) {
            projection = Nd4j.randn(nObsDimension, nStateDimension, seed);
        }

        public UCState build(BufferedImage observation) {
            return new UCState();
        }
    }


}
