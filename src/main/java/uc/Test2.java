package uc;


import java.io.File;

/**
 * Created by keltp on 2017-06-07.
 */
public class Test2 {
    public static void main(String args[]) throws Exception {
        /*UCConfiguration conf = new UCConfiguration.Builder().nStepsForEpsilonDecay(1500).kNearestNeighbor(7).build();
        UCAgent agent = new UCAgent(conf);
        agent.run();*/

        UCAgent agent = UCAgent.deserialize(new File("./autosave"), "conf.json", "qec.json", "states.bin", "qvalues.bin", "lruvalues.bin", "stats.json");
        agent.run();
    }
}
