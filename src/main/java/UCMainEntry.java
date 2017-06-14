import uc.UCAgent;
import uc.UCConfiguration;

import java.io.File;

/**
 * Created by keltp on 2017-06-13.
 */
public class UCMainEntry {
    public static void main(String args[]) throws Exception {
        if(args == null || args.length == 0) {
            UCConfiguration conf = new UCConfiguration.Builder().build();
            UCAgent agent = new UCAgent(conf);
            agent.run();
        } else if (args.length % 2 != 0){
            throw new Exception("Commands should be key-value pairs");
        } else {
            int playedLevel = 1;
            boolean isTraining = false;
            String confFn = null;
            String qecFn = null;
            String statsFn = null;
            String statesFn = null;
            String qValueFn = null;
            String lruFn = null;

            for(int i = 0; i < args.length - 1;i+=2) {
                String key = args[i];
                String value = args[i+1];

                if(key.equalsIgnoreCase("-level")) {
                    playedLevel = Integer.parseInt(value);
                } else if (key.equalsIgnoreCase("-training")) {
                    isTraining = Boolean.parseBoolean(value);
                } else if (key.equalsIgnoreCase("-conf")) {
                    confFn = value;
                } else if (key.equalsIgnoreCase("-stats")) {
                    statsFn = value;
                } else if (key.equalsIgnoreCase("-qec")) {
                    qecFn = value;
                } else if (key.equalsIgnoreCase("-states")) {
                    statesFn = value;
                } else if( key.equalsIgnoreCase("-qvalues")) {
                    qValueFn = value;
                } else if (key.equalsIgnoreCase("-lru")) {
                    lruFn = value;
                }
            }

            if(confFn == null) {
                UCConfiguration conf = new UCConfiguration.Builder().build();
                UCAgent agent = new UCAgent(conf);
                agent.setIsTraining(isTraining);
                agent.run(playedLevel);
            } else {
                boolean isAllFileNotNull = qecFn != null && statsFn != null && statesFn != null && qValueFn != null && lruFn != null;
                if(isAllFileNotNull) {
                    File dir = new File("./");
                    UCAgent agent = UCAgent.deserialize(dir, confFn, qecFn, statesFn, qValueFn, lruFn, statsFn);
                    agent.setIsTraining(isTraining);
                    agent.run(playedLevel);
                }
            }
        }
    }
}
