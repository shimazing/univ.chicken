package uc;

/**
 * Created by keltp on 2017-06-01.
 */
public class UCReward {
    private double netReward;
    private int netScore;

    public double netReward() {
        return netReward;
    }

    public int netScore() {
        return netScore;
    }

    public static class Builder {
        private UCReward previousReward;

        public Builder init() {
            previousReward = null;
            return this;
        }

        public UCReward build(int currentScore) {
            UCReward reward = new UCReward();
            double netReward;
            int netScore;

            if(previousReward == null) {
                netScore = currentScore;
                netReward =  -10000 + netScore;
            } else {
                netScore = currentScore - previousReward.netScore;
                netReward = -10000 + netScore;
            }
            reward.netReward = netReward;
            reward.netScore = netScore;
            previousReward = reward;

            return reward;
        }
    }
}
