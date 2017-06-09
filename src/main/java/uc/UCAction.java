package uc;

import ab.demo.other.Shot;
import org.deeplearning4j.berkeley.Pair;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by keltp on 2017-06-09.
 */
public class UCAction {
    private int id;
    private double angleInDegree;
    private double tapTime;
    private Shot shot;



    public static class Builder {
        private List<Pair<Double, Double>> availableActions;

        public Builder(double minAngleInDegrees, double maxAngleInDegress, int nAngles,
                       double minTapTimeInPercentile, double maxTapTimeInPercentile, int nTapTimes) {

            availableActions = new ArrayList<>();

            for(int i = 0;i < nAngles;i++) {
                double angle = minAngleInDegrees + (maxAngleInDegress - minAngleInDegrees) / nAngles * i;
                for(int j = 0;j < nTapTimes;j++) {
                    double tapTime = minTapTimeInPercentile + (maxTapTimeInPercentile - minTapTimeInPercentile) / nTapTimes * j;

                    availableActions.add(Pair.makePair(angle, tapTime));
                }
            }
        }

        public UCAction build(int id, Rectangle sling) {
            if(id == -1 || availableActions == null || availableActions.size() == 0) {
                return null;
            }
            return new UCAction();
            //return availableActions.get(id);
        }



    }
}
