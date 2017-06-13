/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,XiaoYu (Gary) Ge, Stephen Gould,Jochen Renz
 **  Sahan Abeyasinghe, Jim Keys,   Andrew Wang, Peng Zhang
 ** All rights reserved.
 **This work is licensed under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
 **To view a copy of this license, visit http://www.gnu.org/licenses/
 *****************************************************************************/
package uc.schema;

import ab.demo.other.ActionRobot;
import ab.server.Proxy;
import ab.server.proxy.message.ProxyClickMessage;
import ab.server.proxy.message.ProxyMouseWheelMessage;
import ab.utils.StateUtil;
import ab.vision.GameStateExtractor.GameState;
import uc.UCAction;
import uc.UCActionRobot;
import uc.UCLog;

public class RestartLevelSchema {
    private Proxy proxy;
    private UCActionRobot robot;

    //This schema is used for automatically restarting levels in the standalone version.
    public RestartLevelSchema(Proxy proxy, UCActionRobot robot) {
        this.proxy = proxy;
        this.robot = robot;
    }

    public boolean restartLevel() {
        GameState state = robot.getGameState(robot.doScreenShot());
        if (state == GameState.WON || state == GameState.LOST) {
            proxy.send(new ProxyClickMessage(420, 380));//Click the left most button at the end page
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        } else if (state == GameState.PLAYING) {
            proxy.send(new ProxyClickMessage(100, 39));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }

        //Wait 4000 seconds for loading the level
        try {
            Thread.sleep(4000);
        } catch (InterruptedException e) {
            UCLog.e(e.getMessage(), e);
        }
        robot.fullyZoomOut();
        return true;

    }
}
