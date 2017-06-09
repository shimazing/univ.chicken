package uc;

/**
 * Created by keltp on 2017-06-01.
 */
public class UCReward {
    private double reward;

    public static class Builder {
        private UCReward previousReward;
        private int currentScore;

        public Builder previousReward (UCReward reward) {
            previousReward = reward;
            return this;
        }

        public Builder currentScore (int score) {
            currentScore = score;
            return this;
        }

        public UCReward build(int score) {
            UCReward reward = new UCReward();
            reward.reward = -10000 + (currentScore - previousReward.reward);
            return reward;
        }
    }
}
