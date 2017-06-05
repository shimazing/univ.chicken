package uc;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by keltp on 2017-06-03.
 */
public class UCConfiguration {
    private int mScreenWidth = 840;
    private int mScreenHeight = 480;
    private int mObsXOffset = 390;
    private int mObsYOffset = 0;
    private int mObsWidth = 390;
    private int mObsHeight = 390;
    private int mObsDim = mObsWidth * mObsHeight;
    private int mStateDim = 128;

    private INDArray mRandomProjection;



}
