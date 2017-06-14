package uc;

        import org.nd4j.linalg.api.ndarray.INDArray;
        import org.nd4j.linalg.factory.Nd4j;
        import org.nd4j.linalg.indexing.INDArrayIndex;
        import org.nd4j.linalg.indexing.NDArrayIndex;
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
        UCConfiguration conf = new UCConfiguration.Builder().nStepsForEpsilonDecay(1500).build();
        UCAgent agent = new UCAgent(conf);
        agent.run();
    }
}
