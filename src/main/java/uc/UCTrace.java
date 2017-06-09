package uc;

/**
 * Created by keltp on 2017-06-08.
 */
public class UCTrace {
    private UCState state;
    private UCAction_ action;
    private UCReward reward;

    public UCTrace(UCState state, UCAction_ action, UCReward reward) {
        this.state = state;
        this.action = action;
        this.reward = reward;
    }
}
