package net.yura.domination.test;

import java.io.File;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.lobby.server.ServerGameRisk;

public class TestUtil {

    public static void setupMapsForTest() throws Exception {
        // we must set the maps folder for map loading to work
        //RiskUIUtil.mapsdir = new File("game/Domination/maps").toURI().toURL();

        File test = new File(".");
        System.out.println("running tests in " + test.getCanonicalFile());

        // we must change the current folder for map loading to work
        String testRoot = System.getProperty("user.dir");
        if ("/".equals(testRoot)) {
            throw new RuntimeException("current dir set incorrectly! /");
        }
        System.setProperty("user.dir", testRoot + File.separator + "game");

        // force static init to run that setups map access
        Class.forName(ServerGameRisk.class.getName());

        // after we have done the ServerGameRisk init, we can revert to the standard value
        // this will allow this method to run multiple tiles without messing anything up
        System.setProperty("user.dir", testRoot);
    }

    public static Risk newRisk() throws Exception {
        setupMapsForTest();

        // this may or may not reload the ai wait time from game.ini file
        Risk risk = new Risk();

        // we want test to run quickly
        AIManager.setWait(0);
        Risk.setShowDice(false);

        return risk;
    }

    public static RiskGame newRiskGame() throws Exception {
        setupMapsForTest();

        return new RiskGame();
    }

    public static File getScriptFile(String mode) {
        File resFolder = new File("../res/test_scripts");
        return new File(resFolder, mode.replace(' ', '-') + ".risk"); // RiskFileFilter.RISK_SCRIPT_FILES
    }
}
