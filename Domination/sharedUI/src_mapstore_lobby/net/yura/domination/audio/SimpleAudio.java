package net.yura.domination.audio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;

public class SimpleAudio implements AudioSystem, PlayerListener {

    private static final Logger LOGGER = Logger.getLogger(SimpleAudio.class.getName());
    
    Map<String, Player> currentPlayers = new HashMap(); // filename -> player

    private Player getPlayer(String fileName) throws IOException, MediaException {

        // TODO we need some sort of cache, as each time java seems to load it from scratch
        // i guess it makes sense as the file or url destination may have changed from last time
        // so we need to keep the last X audio samples used

        //Player player = Manager.createPlayer(RiskUtil.openStream(fileName), "audio/basic");
        Player player = Manager.createPlayer("file:///android_asset/" + fileName);
        player.addPlayerListener(this); // TODO do we need this?
        return player;
    }

    public void play(String fileName) {
        try {
            Player player = getPlayer(fileName);
            player.start();
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, "unable to play " + fileName, ex);
        }
    }

    @Override
    public void start(String fileName) {
        try {
            Player player = getPlayer(fileName);
            player.setLoopCount(-1);
            currentPlayers.put(fileName, player);
            player.start();
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, "unable to play " + fileName, ex);
        }
    }

    @Override
    public void playerUpdate(Player player, String event, Object eventData) {
        // TODO do i need to do something about this?
        // do i need to close the player?
    }

    @Override
    public void stop(String audioFile) {
        try {
            Player player = currentPlayers.remove(audioFile);
            if (player != null) {
                player.stop();
            }
            else {
                LOGGER.log(Level.INFO, "unable to find " + audioFile);
            }
        }
        catch (Exception ex) {
            LOGGER.log(Level.WARNING, "unable to stop " + audioFile, ex);
        }
    }
}
