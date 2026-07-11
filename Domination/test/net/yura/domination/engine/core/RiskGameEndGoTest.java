package net.yura.domination.engine.core;

import java.lang.reflect.Field;
import junit.framework.TestCase;
import net.yura.domination.test.TestUtil;

public class RiskGameEndGoTest extends TestCase {

    private void setPrivateField(Object target, String fieldName, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(target, value);
    }

    private RiskGame createAndStartGame(int noPlayers, int noCountries, int gameMode, boolean minimumThreeReinforcements) throws Exception {
        RiskGame instance = TestUtil.createBasicMap(noCountries);
        RiskGameTest.addPlayers(instance, noPlayers);
        instance.startGame(gameMode, RiskGame.CARD_FIXED_SET, true, true, 2, minimumThreeReinforcements);
        return instance;
    }

    private void setPlayerExtraArmies(Player player, int armies) {
        int current = player.getExtraArmies();
        if (current < armies) {
            player.addArmies(armies - current);
        } else if (current > armies) {
            player.loseExtraArmy(current - armies);
        }
    }

    public void testEndGo_WrongState() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        // Initially in STATE_PLACE_ARMIES
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());

        Player origPlayer = instance.getCurrentPlayer();
        Player result = instance.endGo();

        assertNull(result);
        assertEquals(origPlayer, instance.getCurrentPlayer());
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_SetupNotDone() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 0); // Setup not done (setup < players.size())

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        instance.setCurrentPlayer(0);
        assertEquals(p1, instance.getCurrentPlayer());

        int p2ArmiesBefore = p2.getExtraArmies();

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        assertEquals(p2, instance.getCurrentPlayer());

        // Setup not done, so nextTurn() should NOT be called on p2, nor should getExtraArmiesForPlayer be added.
        assertEquals(p2ArmiesBefore, p2.getExtraArmies());

        // Assert the correct gameState is transition to
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());

        // Assert reset flags
        assertFalse(instance.isCapturedCountry());
        assertFalse(instance.getTradeCap());
    }

    public void testEndGo_SetupDone_NextPlayerHasTerritories() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2); // Setup is done

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        // Ensure p2 has at least one territory
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);

        instance.setCurrentPlayer(0);

        int p2ExtraArmiesBefore = p2.getExtraArmies();

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        assertEquals(p2, instance.getCurrentPlayer());

        // Since setup is done, p2 gets extra armies (at least minimumNewArmies)
        assertTrue(p2.getExtraArmies() > p2ExtraArmiesBefore);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_SetupDone_NextPlayerHasNoTerritories() throws Exception {
        // 3 players
        RiskGame instance = createAndStartGame(3, 6, RiskGame.MODE_DOMINATION, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 3); // Setup is done

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1); // p2 has 0 territories
        Player p3 = (Player) instance.getPlayers().get(2);

        // p2's territories is empty
        p2.getTerritoriesOwned().clear();

        // p3 has at least one territory
        Country c = instance.getCountries()[0];
        c.setOwner(p3);
        p3.newCountry(c);

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        // Since p2 owns 0 territories, endGo() should skip p2 and select p3
        assertEquals(p3, nextPlayer);
        assertEquals(p3, instance.getCurrentPlayer());

        // Assert the correct gameState is transition to
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_CapitalMode_SetupNotFinished() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_CAPITAL, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2); // Setup is done

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        // p2 has territory but no capital set
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        p2.setCapital(null);

        instance.setCurrentPlayer(0);

        int p2ExtraArmiesBefore = p2.getExtraArmies();

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        // Since capital is null, nextTurn() and extra armies are NOT called/added, and gameState becomes STATE_SELECT_CAPITAL
        assertEquals(p2ExtraArmiesBefore, p2.getExtraArmies());
        assertEquals(RiskGame.STATE_SELECT_CAPITAL, instance.getState());
    }

    public void testEndGo_CanTrade() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2); // Setup is done

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        // Ensure p2 has territories so they are not skipped
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);

        // Give p2 three identical cards (e.g. Cavalry) to enable trade
        Card card1 = new Card(Card.CAVALRY, c1);
        Card card2 = new Card(Card.CAVALRY, c1);
        Card card3 = new Card(Card.CAVALRY, c1);
        p2.giveCard(card1);
        p2.giveCard(card2);
        p2.giveCard(card3);

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        // Because they can trade, gameState must become STATE_TRADE_CARDS
        assertEquals(RiskGame.STATE_TRADE_CARDS, instance.getState());
    }

    public void testEndGo_CannotTrade_ExtraArmies() throws Exception {
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, true);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2);

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);

        // p2 has some cards but not a tradeable set
        Card card1 = new Card(Card.CAVALRY, c1);
        p2.giveCard(card1);

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        // Cannot trade, has extra armies (due to minimum armies added at end of turn setup)
        assertTrue(p2.getExtraArmies() > 0);
        assertEquals(RiskGame.STATE_PLACE_ARMIES, instance.getState());
    }

    public void testEndGo_ItalianMode_CanAttack() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2);

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        p2.getTerritoriesOwned().clear();
        p1.getTerritoriesOwned().clear();

        // Set up next player p2 with 0 extra armies using helper method
        setPlayerExtraArmies(p2, 0);

        // p2 owns c1, has 2 armies (can attack)
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        c1.addArmies(2); // total 2 armies (1 left to attack)

        // Adjacent c2 owned by p1
        Country c2 = instance.getCountries()[1];
        c2.setOwner(p1);
        p1.newCountry(c2);
        c2.addArmies(1);

        // Ensure neighbors are connected: c1 and c2 are neighbors
        c1.getNeighbours().clear();
        c2.getNeighbours().clear();
        c1.addNeighbour(c2);
        c2.addNeighbour(c1);

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        assertEquals(RiskGame.STATE_ATTACKING, instance.getState());
    }

    public void testEndGo_ItalianMode_CanMove() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2);

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        p2.getTerritoriesOwned().clear();
        setPlayerExtraArmies(p2, 0);

        // p2 owns both connected countries c1 and c2
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);
        c1.addArmies(2); // >1 armies

        Country c2 = instance.getCountries()[1];
        c2.setOwner(p2);
        p2.newCountry(c2);
        c2.addArmies(1);

        // connect neighbors
        c1.getNeighbours().clear();
        c2.getNeighbours().clear();
        c1.addNeighbour(c2);
        c2.addNeighbour(c1);

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        // Can't attack (no enemy neighbor), but can move (c1 has 2 armies and friendly neighbor c2)
        assertEquals(RiskGame.STATE_FORTIFYING, instance.getState());
    }

    public void testEndGo_ItalianMode_NoMove() throws Exception {
        // Set minimumThreeReinforcements to false so next player gets 0 extra armies
        RiskGame instance = createAndStartGame(2, 6, RiskGame.MODE_DOMINATION, false);
        setPrivateField(instance, "gameState", RiskGame.STATE_END_TURN);
        setPrivateField(instance, "setup", 2);

        Player p1 = (Player) instance.getPlayers().get(0);
        Player p2 = (Player) instance.getPlayers().get(1);

        p2.getTerritoriesOwned().clear();
        setPlayerExtraArmies(p2, 0);

        // p2 owns c1, but it only has 1 army (cannot attack/move)
        Country c1 = instance.getCountries()[0];
        c1.setOwner(p2);
        p2.newCountry(c1);

        while (c1.getArmies() > 1) {
            c1.looseArmy();
        }
        while (c1.getArmies() < 1) {
            c1.addArmy();
        }

        instance.setCurrentPlayer(0);

        Player nextPlayer = instance.endGo();

        assertEquals(p2, nextPlayer);
        assertEquals(0, p2.getExtraArmies());
        // Cannot attack, cannot move.
        assertEquals(RiskGame.STATE_END_TURN, instance.getState());
    }
}
