/*****************************************************************************
 ** ANGRYBIRDS AI AGENT FRAMEWORK
 ** Copyright (c) 2014,  XiaoYu (Gary) Ge, Stephen Gould, Jochen Renz
 ** Sahan Abeyasinghe , Jim Keys,  Andrew Wang, Peng Zhang
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

/**
 * Schema for loading level
 */
public class LoadLevelSchema {
    private Proxy proxy;
    private UCActionRobot robot;
    private boolean pageSwitch = false;

    public LoadLevelSchema(Proxy proxy, UCActionRobot robot) {
        this.proxy = proxy;
        this.robot = robot;
    }

    public boolean loadLevel(int i) {
        if (i > 21) {
            if (i == 22 || i == 43)
                pageSwitch = true;
            i = ((i % 21) == 0) ? 21 : i % 21;
        }

        loadLevel(StateUtil.getGameState(proxy), i);
        GameState state = robot.getGameState(robot.doScreenShot());

        while (state != GameState.PLAYING) {
            UCLog.i(" In state:   " + state + " Try reloading...");
            loadLevel(state, i);
            try {
                Thread.sleep(12000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            state = robot.getGameState(robot.doScreenShot());
        }
        return true;
    }

    private boolean loadLevel(GameState state, int i) {
        robot.goFromMainMenuToLevelSelection();
        if (state == GameState.WON || state == GameState.LOST) {
            proxy.send(new ProxyClickMessage(342, 382));//Click the left most button at the end page
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            if (pageSwitch) {
                proxy.send(new ProxyClickMessage(378, 451));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    UCLog.e(e.getMessage(), e);
                }
                pageSwitch = false;
            }
            proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }

            if (i == 1) {
                proxy.send(new ProxyClickMessage(1176, 704));
            }
        } else if (state == GameState.PLAYING) {
            proxy.send(new ProxyClickMessage(48, 44));//Click the left most button, pause
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            proxy.send(new ProxyClickMessage(168, 28));//Click the left most button, pause
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }

            if (pageSwitch) {
                proxy.send(new ProxyClickMessage(378, 451));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    UCLog.e(e.getMessage(), e);
                }
                pageSwitch = false;
            }

            proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }

            if (i == 1) {
                proxy.send(new ProxyClickMessage(1176, 704));
            }
        } else {
            if (pageSwitch) {
                proxy.send(new ProxyClickMessage(378, 451));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    UCLog.e(e.getMessage(), e);
                }
                pageSwitch = false;
            }

            proxy.send(new ProxyClickMessage(54 + ((i - 1) % 7) * 86, 110 + ((i - 1) / 7) * 100));
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            if (i == 1) {
                proxy.send(new ProxyClickMessage(1176, 704));
            }
        }

        //Wait 9000 seconds for loading the level
        GameState _state = robot.getGameState(robot.doScreenShot());
        int count = 0; // at most wait 10 seconds
        while (_state != GameState.PLAYING && count < 3) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
            count++;
            _state = StateUtil.getGameState(proxy);
        }

        if (_state == GameState.PLAYING) {
            for (int k = 0; k < 15; k++) {
                proxy.send(new ProxyMouseWheelMessage(-1));
            }

            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                UCLog.e(e.getMessage(), e);
            }
        }
        return true;
    }
}
