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
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.indexing.conditions.EqualsCondition;
import org.nd4j.linalg.indexing.conditions.NotEqualsCondition;
import uc.balltree.BallNode;
import uc.balltree.BallTree;
import uc.balltree.HammingDistance;

import java.io.File;
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
        /*INDArray arrays = Nd4j.randn(50, 2, 12345);
        List<INDArray> list = new ArrayList<>();
        for(int i = 0; i < arrays.rows();i++) {
            list.add(arrays.getRow(i));
        }

        for(INDArray array : list) {
            System.out.println(array.toString());
        }

        BallTree tree = new BallTree(new uc.balltree.EuclideanDistance(), list);
        tree.buildTree();
        List<Pair<Double, INDArray>> result = tree.knn(list.get(5), 5);
        for(Pair<Double, INDArray> pair : result) {
            System.out.println(pair.toString());
        }*/
        INDArray a1 = Nd4j.zeros(1, 10).addi(10);
        INDArray a2 = Nd4j.zeros(2, 10).addi(20);
        a2.putScalar(1, 5, 15);
        INDArray a3 = Nd4j.zeros(1, 10).addi(20);
        a3.putScalar(0, 9, 532);
        INDArray a4 = Nd4j.zeros(1, 10).addi(20);

        INDArray r1 = a2.getRow(0).dup();
        INDArray r2 = a2.getRow(1);
        a2.putRow(0, r2);
        a2.putRow(1, r1);
        System.out.println(a2.subRowVector(a1));
        System.out.println(a2.subRowVector(a1).norm2(1));
        System.out.println(Nd4j.getExecutioner().execAndReturn(new IMax(a2.subRowVector(a1).norm2(1))).getFinalResult());
        System.out.println(Nd4j.getExecutioner().execAndReturn(new Max(a2.subRowVector(a1).norm2(1))).getFinalResult().doubleValue());
        System.out.println(a1.sub(a3).norm2(1));

        double v2 = a3.sub(a4).neq(0).sumNumber().doubleValue();

        System.out.println(v2);
    }
}
