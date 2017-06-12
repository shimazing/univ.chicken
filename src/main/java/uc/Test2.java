package uc;

import ab.vision.Vision;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

/**
 * Created by keltp on 2017-06-07.
 */
public class Test2 {
    public static void main(String args[]) throws Exception {
        UCConfiguration conf = new UCConfiguration.Builder().nStepsPerEpoch(100).build();
        UCAgent agent = new UCAgent(conf);
        File dir = new File("./autosave");
        dir.mkdirs();
        agent.serialize(dir, "conf.json", "qec.json", "stats.json");
        UCAgent agent2 = UCAgent.deserialize(dir, "conf.json", "qec.json", "stats.json");
        agent2.serialize(dir, "conf2.json", "qec2.json", "stats2.json");
    }
}
