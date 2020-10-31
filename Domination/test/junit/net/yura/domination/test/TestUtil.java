package net.yura.domination.test;

import java.io.File;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.guishared.RiskUIUtil;

public class TestUtil {
    
    public static void setupForTest() throws Exception {
            // we must set the maps folder for map loading to work
            RiskUIUtil.mapsdir = new File("game" + File.separator + RiskUtil.GAME_NAME + File.separator + "maps").toURI().toURL();

            // we want test to run quickly
            AIManager.setWait(0);
            Risk.setShowDice(false);
    }
}
