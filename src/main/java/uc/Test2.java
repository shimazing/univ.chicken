package uc;

        import org.nd4j.linalg.api.ndarray.INDArray;
        import org.nd4j.linalg.factory.Nd4j;
        import uc.data.QecTable;

        import javax.imageio.ImageIO;

        import java.awt.*;
        import java.awt.image.BufferedImage;
        import java.io.*;

/**
 * Created by keltp on 2017-06-07.
 */
public class Test2 {
    public static void main(String args[]) throws Exception {
        UCConfiguration conf = new UCConfiguration.Builder().maxStateCapacity(1000).nTapTimes(1).nAngles(4).nStepsPerEpoch(5).epsilonStart(0.0005).build();
        UCAgent agent = new UCAgent(conf);
        agent.run(2);

        /*UCConfiguration conf = new UCConfiguration.Builder().maxStateCapacity(1000).nTapTimes(1).nAngles(4).stateDimension(2).kNearestNeighbor(5).build();
        QecTable table = new QecTable(conf);*/

    }
}
