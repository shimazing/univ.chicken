package uc;

import ab.demo.other.ActionRobot;
import ab.planner.TrajectoryPlanner;
import ab.vision.Vision;
import com.google.common.collect.ImmutableMap;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.api.ops.impl.accum.EqualsWithEps;
import org.nd4j.linalg.api.ops.impl.accum.Max;
import org.nd4j.linalg.api.ops.impl.accum.Mean;
import org.nd4j.linalg.api.ops.impl.accum.Sum;
import org.nd4j.linalg.api.ops.impl.accum.distances.EuclideanDistance;
import org.nd4j.linalg.api.ops.impl.indexaccum.IMax;
import org.nd4j.linalg.api.ops.impl.scalar.comparison.ScalarEquals;
import org.nd4j.linalg.api.ops.impl.transforms.comparison.EqualTo;
import org.nd4j.linalg.exception.ND4JException;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.INDArrayIndex;
import org.nd4j.linalg.indexing.NDArrayIndex;
import org.nd4j.linalg.indexing.SpecifiedIndex;
import org.nd4j.linalg.indexing.conditions.EqualsCondition;
import org.nd4j.linalg.indexing.conditions.NotEqualsCondition;
import uc.balltree.BallNode;
import uc.balltree.BallTree;
import uc.balltree.HammingDistance;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by keltp on 2017-05-16.
 */
public class Test {
    public static void main(String args[]) throws Exception {
        /*ActionRobot ar = new ActionRobot();
        Vision vision = new Vision(ActionRobot.doScreenShot());
        Rectangle slingshot = vision.findSlingshotMBR();
        while(slingshot == null)
        {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            System.out.println("no slingshot detected. Please remove pop up or zoom out");
            vision = new Vision(ActionRobot.doScreenShot());
            slingshot = vision.findSlingshotMBR();
        }
        UCObservation.generateObservation(ar, new File("d:/desktop/aibirds/test.png"), new File("d:/desktop/aibirds/test-1.png"));*/

        INDArray arrays = Nd4j.randn(50, 2, 12345);

        BallTree tree = new BallTree(new uc.balltree.EuclideanDistance(), arrays);
        INDArray point = Nd4j.create(new double[]{-.5, -2});
        tree.buildTree();
        List<Pair<Double, INDArray>> result = tree.knn(point, 5);

        FileWriter writer = new FileWriter(new File("d:/desktop/test2.csv"));
        writer.write("x,y,dist" + System.lineSeparator());
        writer.write(point.getDouble(0) + "," + point.getDouble(1) + "," + 1 + System.lineSeparator());

        for(int i = 0;i < arrays.rows();i++) {
            INDArray a = arrays.getRow(i);
            writer.write(a.getDouble(0) + "," + a.getDouble(1) + "," );

            for(Pair<Double, INDArray> r : result) {
                INDArray b = (INDArray) r.getSecond();
                if(b.neq(a).sumNumber().doubleValue() == 0) {
                    writer.write(2);
                }
            }

            writer.write(System.lineSeparator());
        }
        writer.flush();
        writer.close();

    }
}
