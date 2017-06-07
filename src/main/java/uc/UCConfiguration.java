package uc;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;
import uc.distance.DistanceFunction;
import uc.distance.HammingDistance;

import java.io.*;

/**
 * Created by keltp on 2017-06-03.
 */
public class UCConfiguration {
    /**
     * Observation
     */
    private int screenWidth;
    private int screenHeight;
    private int observationXOffset;
    private int observationYOffset;
    private int observationWidth;
    private int observationHeight;
    private int observationBirdWidth;
    private int observationBirdHeight;
    private int observationDimension;

    /**
     * State
     */
    private int stateDimension;
    private transient INDArray randomProjection;
    private int maxStateCapacity;
    private int maxStateCapacityPerAction;

    /**
     * Actions
     */
    private int minAngle;
    private int maxAngle;
    private int nAngles;
    private double minPercentileTapTime;
    private double maxPercentileTapTime;
    private int nTapTimes;
    private int nActions;

    /**
     * Parameter
     */
    private double epsilonStart;
    private double epsilonMin;
    private double nStepsForEpsilonDecay;
    private double discountFactor;
    private int nEpochs;
    private int nStepsPerEpoch;
    private long seed;
    private int kNearestNeighbor;

    /**
     * Distance function
     */
    private String classNameDistanceFunction;
    private transient DistanceFunction<Integer, Double> distFunc;


    public int screenWidth() {
        return screenWidth;
    }

    public int screenHeight() {
        return screenHeight;
    }

    public int observationXOffset() {
        return observationXOffset;
    }

    public int observationYOffset() {
        return observationYOffset;
    }

    public int observationWidth() {
        return observationWidth;
    }

    public int observationHeight() {
        return observationHeight;
    }

    public int observationBirdWidth() {
        return observationBirdWidth;
    }

    public int observationBirdHeight() {
        return observationBirdHeight;
    }

    public int observationDimension() {
        return observationDimension;
    }

    public int stateDimension() {
        return stateDimension;
    }

    public INDArray randomProjection() {
        return randomProjection;
    }

    public int maxStateCapacity() {
        return maxStateCapacity;
    }

    public int maxStateCapacityPerAction() {
        return maxStateCapacityPerAction;
    }

    public int minAngle() {
        return minAngle;
    }

    public int maxAngle() {
        return maxAngle;
    }

    public int nAngles() {
        return nAngles;
    }

    public int nActions() {
        return nActions;
    }

    public double minPercentileTapTime() {
        return minPercentileTapTime;
    }

    public double maxPercentileTapTime() {
        return maxPercentileTapTime;
    }

    public int nTapTimes() {
        return nTapTimes;
    }

    public double epsilonStart() {
        return epsilonStart;
    }

    public double epsilonMin() {
        return epsilonMin;
    }

    public double nStepsForEpsilonDecay() {
        return nStepsForEpsilonDecay;
    }

    public double discountFactor() {
        return discountFactor;
    }

    public int nEpochs() {
        return nEpochs;
    }

    public int nStepsPerEpoch() {
        return nStepsPerEpoch;
    }

    public long seed() {
        return seed;
    }

    public int kNearestNeighbor() {
        return kNearestNeighbor;
    }

    public DistanceFunction<Integer, Double> distantFunction() {
        return distFunc;
    }

    private UCConfiguration() {}

    public void serializeToJson(File file) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        FileWriter writer = new FileWriter(file, false);
        gson.toJson(this, writer);
        writer.flush();
        writer.close();
    }

    public static UCConfiguration deserializeFromJson(File file) throws IOException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        UCConfiguration conf = gson.fromJson(new BufferedReader(new FileReader(file)), UCConfiguration.class);
        Class<?> clazz = Class.forName(conf.classNameDistanceFunction);
        conf.distFunc = (DistanceFunction<Integer, Double>) clazz.newInstance();
        conf.randomProjection = Nd4j.randn(conf.observationDimension, conf.stateDimension, conf.seed);
        return conf;
    }

    public static class Builder {
        /**
         * Observation
         */
        private int screenWidth = 840;
        private int screenHeight = 480;
        private int observationXOffset = 435;
        private int observationYOffset = 180;
        private int observationWidth = 365;
        private int observationHeight = 205;
        private int observationBirdWidth = 15;
        private int observationBirdHeight = 15;

        /**
         * State
         */
        private int stateDimension = 128;
        private int maxStateCapacity = 10000000;

        /**
         * Actions
         */
        private int minAngle = 4;
        private int maxAngle = 83;
        private int nAngles = 30;
        private double minPercentileTapTime = 0.5;
        private double maxPercentileTapTime = 0.95;
        private int nTapTimes = 10;

        /**
         * Parameter
         */
        private double epsilonStart = 1.0;
        private double epsilonMin = 0.005;
        private double nStepsForEpsilonDecay = 10000;
        private double discountFactor = 0.99;
        private int nEpochs = 1000;
        private int nStepsPerEpoch = 1000;
        private long seed = 12345;
        private int kNearestNeighbor = 20;

        /**
         * Distance function
         */
        private DistanceFunction<Integer, Double> distFunc = new HammingDistance();

        public Builder screenWidth(int screenWidth) {
            this.screenWidth = screenWidth;
            return this;
        }

        public Builder screenHeight(int screenHeight) {
            this.screenHeight = screenHeight;
            return this;
        }

        public Builder observationXOffset(int observationXOffset) {
            this.observationXOffset = observationXOffset;
            return this;
        }

        public Builder observationYOffset(int observationYOffset) {
            this.observationYOffset = observationYOffset;
            return this;
        }

        public Builder observationWidth(int observationWidth) {
            this.observationWidth = observationWidth;
            return this;
        }

        public Builder observationHeight(int observationHeight) {
            this.observationHeight = observationHeight;
            return this;
        }

        public Builder observationBirdWidth(int observationBirdWidth) {
            this.observationBirdWidth = observationBirdWidth;
            return this;
        }

        public Builder observationBirdHeight(int observationBirdHeight) {
            this.observationBirdHeight = observationBirdHeight;
            return this;
        }

        public Builder stateDimension(int stateDimension) {
            this.stateDimension = stateDimension;
            return this;
        }

        public Builder maxStateCapacity(int maxStateCapacity) {
            this.maxStateCapacity = maxStateCapacity;
            return this;
        }

        public Builder minAngle(int minAngle) {
            this.minAngle = minAngle;
            return this;
        }

        public Builder maxAngle(int maxAngle) {
            this.maxAngle = maxAngle;
            return this;
        }

        public Builder nAngles(int nAngles) {
            this.nAngles = nAngles;
            return this;
        }

        public Builder minPercentileTapTime(double minPercentileTapTime) {
            this.minPercentileTapTime = minPercentileTapTime;
            return this;
        }

        public Builder maxPercentileTapTime(double maxPercentileTapTime) {
            this.maxPercentileTapTime = maxPercentileTapTime;
            return this;
        }

        public Builder nTapTimes(int nTapTimes) {
            this.nTapTimes = nTapTimes;
            return this;
        }

        public Builder epsilonStart(double epsilonStart) {
            this.epsilonStart = epsilonStart;
            return this;
        }

        public Builder epsilonMin(double epsilonMin) {
            this.epsilonMin = epsilonMin;
            return this;
        }

        public Builder nStepsForEpsilonDecay(double nStepsForEpsilonDecay) {
            this.nStepsForEpsilonDecay = nStepsForEpsilonDecay;
            return this;
        }

        public Builder discountFactor(double discountFactor) {
            this.discountFactor = discountFactor;
            return this;
        }

        public Builder nEpochs(int nEpochs) {
            this.nEpochs = nEpochs;
            return this;
        }

        public Builder nStepsPerEpoch(int nStepsPerEpoch) {
            this.nStepsPerEpoch = nStepsPerEpoch;
            return this;
        }

        public Builder seed(long seed) {
            this.seed = seed;
            return this;
        }

        public Builder kNearestNeighbor(int kNearestNeighbor) {
            this.kNearestNeighbor = kNearestNeighbor;
            return this;
        }

        public Builder distantFunction(DistanceFunction<Integer, Double> distFunc) {
            this.distFunc = distFunc;
            return this;
        }

        public UCConfiguration build() {
            UCConfiguration conf = new UCConfiguration();

            conf.screenWidth = this.screenWidth;
            conf.screenHeight = this.screenHeight;
            conf.observationXOffset = this.observationXOffset;
            conf.observationYOffset = this.observationYOffset;
            conf.observationWidth = this.observationWidth;
            conf.observationHeight = this.observationHeight;
            conf.observationBirdWidth = this.observationBirdWidth;
            conf.observationBirdHeight = this.observationBirdHeight;
            conf.observationDimension = this.observationWidth * this.observationHeight;
            conf.stateDimension = this.stateDimension;
            conf.randomProjection = Nd4j.randn(conf.observationDimension, conf.stateDimension, this.seed);
            conf.maxStateCapacity = this.maxStateCapacity;

            conf.minAngle = this.minAngle;
            conf.maxAngle = this.maxAngle;
            conf.nAngles = this.nAngles;
            conf.minPercentileTapTime = this.minPercentileTapTime;
            conf.maxPercentileTapTime = this.maxPercentileTapTime;
            conf.nTapTimes = this.nTapTimes;
            conf.nActions = this.nAngles * this.nTapTimes;
            conf.maxStateCapacityPerAction = this.maxStateCapacity / (conf.nActions);

            conf.epsilonStart = this.epsilonStart;
            conf.epsilonMin = this.epsilonMin;
            conf.nStepsForEpsilonDecay = this.nStepsForEpsilonDecay;
            conf.discountFactor = this.discountFactor;
            conf.nEpochs = this.nEpochs;
            conf.nStepsPerEpoch = this.nStepsPerEpoch;
            conf.seed = this.seed;
            conf.kNearestNeighbor = this.kNearestNeighbor;
            conf.discountFactor = this.discountFactor;
            conf.distFunc = this.distFunc;
            conf.classNameDistanceFunction = this.distFunc.getClass().getCanonicalName();
            return conf;
        }
    }
}
