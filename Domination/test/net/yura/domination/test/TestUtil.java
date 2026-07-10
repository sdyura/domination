package net.yura.domination.test;

import java.io.File;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Continent;
import net.yura.domination.engine.core.Country;
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

    public static RiskGame createBasicMap(int noCountries) throws Exception {

        RiskGame map = TestUtil.newRiskGame();
        map.setupNewMap();

        Continent continent = new Continent("meow", "Meow", 5, 0);
        map.setContinents(new Continent[] { continent });

        Country[] countries = new Country[noCountries];
        for (int c = 0; c < noCountries; c++) {
            countries[c] = new Country(c + 1, "meow" + c, "Meow " + c, continent, 50 + 50 * c, 50);
            continent.addTerritoriesContained(countries[c]);
            if (c != 0) {
                countries[c].addNeighbour(countries[c - 1]);
                countries[c - 1].addNeighbour(countries[c]);
            }
            map.getCards().add(new Card(((c%3)==0) ? Card.CAVALRY : (  ((c%3)==1) ? Card.INFANTRY : Card.CANNON  ), countries[c]));
        }
        map.setCountries(countries);
        map.getCards().add(new Card(Card.WILDCARD, null));
        map.getCards().add(new Card(Card.WILDCARD, null));

        return map;
    }

    public static File getScriptFile(String mode) {
        File resFolder = new File("../res/test_scripts");
        return new File(resFolder, mode.replace(' ', '-') + ".risk"); // RiskFileFilter.RISK_SCRIPT_FILES
    }
}
