package uc;

import ab.demo.other.LoadLevelSchema;
import ab.demo.other.RestartLevelSchema;
import ab.server.Proxy;

/**
 * Created by keltp on 2017-06-09.
 */
public class UCActionRobot {
    private static Proxy proxy;
    private LoadLevelSchema loadLevelSchema;
    private RestartLevelSchema restartLevelSchema;

    static {
        if(proxy == null) {
            try {
                proxy = new Proxy(9000) {
                    @Override
                    public void onOpen() {

                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
