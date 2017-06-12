package uc;

import org.nd4j.linalg.api.ndarray.INDArray;

/**
 * Created by keltp on 2017-06-08.
 */
public class UCTrace {
    protected UCObservation observation;
    protected INDArray state;
    protected UCAction action;
    protected UCReward reward;

    public UCTrace(UCObservation observation, INDArray state, UCAction action, UCReward reward) {
        this.observation = observation;
        this.state = state;
        this.action = action;
        this.reward = reward;
    }
}
