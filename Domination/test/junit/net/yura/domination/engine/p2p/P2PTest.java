package net.yura.domination.engine.p2p;

import java.io.File;
import junit.framework.TestCase;
import static junit.framework.TestCase.assertEquals;
import net.yura.domination.engine.Risk;
import net.yura.domination.engine.RiskAdapter;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.core.Player;
import net.yura.domination.engine.core.RiskGame;
import net.yura.domination.guishared.RiskUIUtil;

public class P2PTest extends TestCase {
    
    private Risk myrisk;
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // we must set the maps folder for map loading to work
        RiskUIUtil.mapsdir = new File("game" + File.separator + RiskUtil.GAME_NAME + File.separator + "maps").toURI().toURL();
        
        myrisk = new Risk();

        // we want test to run quickly
        AIManager.setWait(0);
        Risk.setShowDice(false);

        myrisk.addRiskListener(new RiskAdapter() {
            AIManager fakeHuman = new AIManager();
            
            @Override
            public void sendMessage(String output, boolean redrawNeeded, boolean repaintNeeded) {
                System.out.println(output);
            }

            @Override
            public void needInput(int state) {
                RiskGame game = myrisk.getGame();
                if (game == null) return;
                Player player = game.getCurrentPlayer();
                if (player == null) return;

                if (state != RiskGame.STATE_GAME_OVER) {
                    myrisk.parser(fakeHuman.getOutput(game, Player.PLAYER_AI_HARD));
                }
            }
        });
    }
    
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        myrisk.parser("closegame");
        myrisk.parser("killserver");
    }
    
    public void testP2PGame() throws Exception {

        myrisk.parser("startserver");
        myrisk.parser("join localhost");
        myrisk.parser("autosetup"); // sends newplayer commands into the network
        
        Thread.sleep(1000); // wait for all players to be created
        
        assertEquals(6, myrisk.getGame().getPlayers().size());
        
        myrisk.parser("startgame domination increasing recycle");
        
        while(myrisk.getWinner() == null) {
            Thread.sleep(1000);
            Thread.yield();
        }
        
        assertEquals(RiskGame.STATE_GAME_OVER, myrisk.getGame().getState());
    }
}
