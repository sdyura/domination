package net.yura.domination.engine;

import junit.framework.TestCase;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.RiskGameTest;
import net.yura.domination.test.TestUtil;

public class RiskTest extends TestCase {

    Risk risk = TestUtil.newRisk();

    public RiskTest(String testName) throws Exception {
        super(testName);
    }

    @Override
    protected void tearDown() throws Exception {
        risk.kill();
    }

    public void testRisk() throws Exception {

        risk.setReplay(true);

        int noPlayers = 3;
        int noCountries = 6;

        RiskGame myMap = TestUtil.createBasicMap(noCountries);
        risk.newMemoryGame(myMap);

        assertEquals(RiskGame.STATE_NEW_GAME, risk.getGame().getState());

        RiskGameTest.addPlayers(risk.getGame(), noPlayers);

        risk.inGameParser("local startgame domination italianlike recycle");
        risk.inGameParser("PLAYER 0");

        for (int i = 0; i < noCountries; i++) {

            assertEquals(RiskGame.STATE_PLACE_ARMIES, risk.getGame().getState());

            risk.inGameParser("local placearmies " + (i + 1) + " 1");

            assertEquals(RiskGame.STATE_END_TURN, risk.getGame().getState());

            risk.inGameParser("local endgo");
            risk.inGameParser("CARD");
        }

        assertEquals(RiskGame.STATE_PLACE_ARMIES, risk.getGame().getState());
    }
}
