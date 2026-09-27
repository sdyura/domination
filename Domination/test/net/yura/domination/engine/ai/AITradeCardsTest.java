package net.yura.domination.engine.ai;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import junit.framework.TestCase;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.core.Card;
import net.yura.domination.engine.core.Country;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.engine.core.RiskGameTest;
import net.yura.domination.test.TestUtil;

/**
 * Makes sure that for every card mode and every combination of cards in the hand
 * the AI only ever sends trade commands that the game engine would accept.
 */
public class AITradeCardsTest extends TestCase {

    private static final String[] CARD_TYPES = { Card.INFANTRY, Card.CAVALRY, Card.CANNON, Card.WILDCARD };

    /** biggest hand we test, you can get big hands in italian mode or after eliminating a player */
    private static final int MAX_HAND_SIZE = 9;

    private static final int NO_COUNTRIES = 3 * (2 * MAX_HAND_SIZE); // enough owned AND not owned cards of each type
    private static final int NO_WILDCARDS = MAX_HAND_SIZE;

    /** how the cards in the hand relate to the countries the AI owns */
    private static final int OWN_ALL = 0, OWN_NONE = 1, OWN_RANDOM = 2;

    public void testIncreasingCards() throws Exception {
        testCardMode(RiskGame.CARD_INCREASING_SET);
    }

    public void testFixedCards() throws Exception {
        testCardMode(RiskGame.CARD_FIXED_SET);
    }

    public void testItalianCards() throws Exception {
        testCardMode(RiskGame.CARD_ITALIANLIKE_SET);
    }

    /**
     * sanity check of the test itself, the engine must reject the known illegal combinations
     */
    public void testEngineRejectsIllegalTrades() throws Exception {
        RiskGame game = TestUtil.newRiskGame();
        // fixed: 2 same + 1 different is not a set
        assertEquals(0, game.getTradeAbsValue(Card.INFANTRY, Card.INFANTRY, Card.CAVALRY, RiskGame.CARD_FIXED_SET));
        assertEquals(0, game.getTradeAbsValue(Card.INFANTRY, Card.INFANTRY, Card.CAVALRY, RiskGame.CARD_INCREASING_SET));
        assertEquals(0, game.getTradeAbsValue(Card.INFANTRY, Card.INFANTRY, Card.CAVALRY, RiskGame.CARD_ITALIANLIKE_SET));
        // italian: 3 wildcards, 2 wildcards, or 1 wildcard with 2 different cards are not sets
        assertEquals(0, game.getTradeAbsValue(Card.WILDCARD, Card.WILDCARD, Card.WILDCARD, RiskGame.CARD_ITALIANLIKE_SET));
        assertEquals(0, game.getTradeAbsValue(Card.WILDCARD, Card.WILDCARD, Card.CANNON, RiskGame.CARD_ITALIANLIKE_SET));
        assertEquals(0, game.getTradeAbsValue(Card.WILDCARD, Card.INFANTRY, Card.CANNON, RiskGame.CARD_ITALIANLIKE_SET));
    }

    private void testCardMode(int cardMode) throws Exception {
        List<String> failures = new ArrayList<String>();
        int scenarios = 0;

        for (boolean maxFiveCards : new boolean[] {true, false}) {
            for (AI ai : newAIs()) {
                RiskGame game = newGame(cardMode, maxFiveCards);
                EngineParser engine = new EngineParser(game);
                Player player = game.getCurrentPlayer();
                Random random = new Random(cardMode * 1000 + (maxFiveCards ? 1 : 0) * 100 + ai.getType());

                for (int[] hand : allHands()) {
                    for (int ownership : new int[] {OWN_ALL, OWN_NONE, OWN_RANDOM}) {

                        resetHand(game, player);
                        dealHand(game, player, hand, ownership, random);

                        // the engine only ever puts a player into the trade state when he can trade
                        if (!game.canTrade()) {
                            continue;
                        }

                        String description = describe(ai, game, maxFiveCards, player);

                        // after eliminating a player and taking his cards, the player is forced to trade
                        boolean tradeCap = player.getCards().size() > game.getMaxCardsPerPlayer();
                        setField(game, "gameState", RiskGame.STATE_TRADE_CARDS);
                        setField(game, "tradeCap", tradeCap);

                        scenarios++;
                        String failure = runTradePhase(ai, engine, game, player);
                        if (failure != null) {
                            failures.add(description + (tradeCap ? " tradeCap" : "") + " -> " + failure);
                        }
                    }
                }
            }
        }

        System.out.println(modeName(cardMode) + ": tested " + scenarios + " scenarios, " + failures.size() + " illegal AI trades");

        if (!failures.isEmpty()) {
            StringBuilder message = new StringBuilder();
            message.append(failures.size()).append(" illegal AI trades out of ").append(scenarios).append(" scenarios:");
            for (int c = 0; c < Math.min(50, failures.size()); c++) {
                message.append('\n').append(failures.get(c));
            }
            fail(message.toString());
        }
    }

    /**
     * lets the AI trade until it ends the trade phase or the game moves on,
     * every command the AI gives is run through the real game command parser
     * @return null if everything the AI did was legal, otherwise a description of what went wrong
     */
    private static String runTradePhase(AI ai, EngineParser engine, RiskGame game, Player player) {
        List<String> commands = new ArrayList<String>();
        // every trade removes 3 cards, so the loop can never need more then this
        int maxSteps = MAX_HAND_SIZE / 3 + 2;

        for (int step = 0; step < maxSteps && game.getState() == RiskGame.STATE_TRADE_CARDS; step++) {

            ai.setGame(game);
            String hand = String.valueOf(player.getCards());
            String command;
            try {
                command = ai.getTrade();
            }
            catch (RuntimeException ex) {
                return commands + " AI threw " + ex;
            }
            commands.add(command);

            try {
                engine.parse(player.getAddress() + " " + command);
            }
            catch (RuntimeException ex) {
                return commands + " engine rejected command: " + ex.getMessage() + " hand was " + hand;
            }
        }

        if (game.getState() == RiskGame.STATE_TRADE_CARDS) {
            return commands + " AI never finished trading, hand " + player.getCards();
        }
        if (game.getState() != RiskGame.STATE_PLACE_ARMIES) {
            return commands + " game ended up in unexpected state " + game.getState();
        }
        return null;
    }

    /**
     * gives access to the game engine command parser.
     * in replay mode the parser throws an exception for any command it rejects,
     * and does not ask anyone for the next command
     */
    private static class EngineParser extends Risk {
        EngineParser(RiskGame game) {
            setReplay(true);
            setGame(game);
        }
        void parse(String message) {
            inGameParser(message);
        }
    }

    private static List<AI> newAIs() {
        return Arrays.asList(new AISubmissive(), new AIEasy(), new AIAverage(), new AIHard());
    }

    /**
     * @return every possible hand from 3 to MAX_HAND_SIZE cards,
     * as a count of each of the CARD_TYPES, e.g. {1,1,1,0} is 3 different cards, {0,0,0,3} is 3 wildcards
     */
    private static List<int[]> allHands() {
        List<int[]> hands = new ArrayList<int[]>();
        for (int inf = 0; inf <= MAX_HAND_SIZE; inf++) {
            for (int cav = 0; inf + cav <= MAX_HAND_SIZE; cav++) {
                for (int can = 0; inf + cav + can <= MAX_HAND_SIZE; can++) {
                    for (int wild = 0; inf + cav + can + wild <= MAX_HAND_SIZE; wild++) {
                        if (inf + cav + can + wild >= 3) {
                            hands.add(new int[] {inf, cav, can, wild});
                        }
                    }
                }
            }
        }
        return hands;
    }

    private static RiskGame newGame(int cardMode, boolean maxFiveCards) throws Exception {
        RiskGame game = TestUtil.createBasicMap(NO_COUNTRIES);
        for (int c = 2; c < NO_WILDCARDS; c++) { // createBasicMap already adds 2
            game.getCards().add(new Card(Card.WILDCARD, null));
        }
        RiskGameTest.addPlayers(game, 2);
        game.startGame(RiskGame.MODE_DOMINATION, cardMode, true, maxFiveCards, 3, true);
        game.setCurrentPlayer(0);

        // setup, players take turns in claiming countries, so each owns every other country
        // and both own countries with cards of all types
        for (int c = 0; c < NO_COUNTRIES; c++) {
            assertEquals(1, game.placeArmy(game.getCountryInt(c + 1), 1));
            assertNotNull(game.endGo());
        }
        while (!game.getSetupDone()) {
            Player player = game.getCurrentPlayer();
            assertEquals(1, game.placeArmy((Country) player.getTerritoriesOwned().get(0), 1));
            assertNotNull(game.endGo());
        }
        // this is now the normal start of a turn
        game.setCurrentPlayer(0);
        return game;
    }

    private static void resetHand(RiskGame game, Player player) {
        while (!player.getCards().isEmpty()) {
            game.getCards().add(player.takeCard());
        }
        // cards traded in with recycle on go back into the deck, but make sure
        game.getCards().addAll(game.getUsedCards());
        game.getUsedCards().clear();
    }

    private static void dealHand(RiskGame game, Player player, int[] hand, int ownership, Random random) {
        List<Card> deck = new ArrayList<Card>(game.getCards());
        Collections.shuffle(deck, random);

        for (int t = 0; t < CARD_TYPES.length; t++) {
            for (int n = 0; n < hand[t]; n++) {
                boolean wantOwned = ownership == OWN_ALL || (ownership == OWN_RANDOM && random.nextBoolean());
                Card card = findCard(deck, CARD_TYPES[t], player, wantOwned);
                if (card == null) {
                    card = findCard(deck, CARD_TYPES[t], player, !wantOwned);
                }
                assertNotNull("not enough " + CARD_TYPES[t] + " cards in the deck", card);
                deck.remove(card);
                game.getCards().remove(card);
                player.giveCard(card);
            }
        }
    }

    private static Card findCard(List<Card> deck, String type, Player player, boolean owned) {
        for (Card card : deck) {
            if (card.getName().equals(type)) {
                Country country = card.getCountry();
                if (country == null || (country.getOwner() == player) == owned) {
                    return card;
                }
            }
        }
        return null;
    }

    private static void setField(RiskGame game, String name, Object value) throws Exception {
        Field field = RiskGame.class.getDeclaredField(name);
        field.setAccessible(true);
        field.set(game, value);
    }

    private static String describe(AI ai, RiskGame game, boolean maxFiveCards, Player player) {
        StringBuilder hand = new StringBuilder();
        for (Card card : (List<Card>) player.getCards()) {
            if (hand.length() > 0) {
                hand.append(',');
            }
            hand.append(card.getName());
            if (card.getCountry() != null) {
                hand.append(card.getCountry().getOwner() == player ? "(own)" : "(not own)");
            }
        }
        return modeName(game.getCardMode()) + " " + ai.getCommand() + " maxFiveCards=" + maxFiveCards + " [" + hand + "]";
    }

    private static String modeName(int cardMode) {
        switch (cardMode) {
            case RiskGame.CARD_INCREASING_SET: return "increasing";
            case RiskGame.CARD_FIXED_SET: return "fixed";
            case RiskGame.CARD_ITALIANLIKE_SET: return "italianlike";
            default: return "unknown " + cardMode;
        }
    }
}
