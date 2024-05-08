package net.yura.domination.audio;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;

public class SimpleAudio implements AudioSystem, ThreadFactory {

    private static final Logger LOGGER = Logger.getLogger(SimpleAudio.class.getName());

    Map<String, Player> currentPlayers = new HashMap(); // filename -> player

    private boolean outOfMemoryError;

    /**
     * we need a single thread for starting and stopping music
     * otherwise if one thread starts it and another thread stops it
     * the stop may never happen as it may never find the player
     */
    private final Executor singleThread = Executors.newSingleThreadExecutor(this);
    
    @Override
    public Thread newThread(Runnable r) {
        Thread th = Executors.defaultThreadFactory().newThread(r);
        th.setName("SimpleAudioStartStopThread");
        return th;
    }

    private Player getPlayer(String fileName) throws IOException, MediaException {

        // TODO we need some sort of cache, as each time java seems to load it from scratch
        // i guess it makes sense as the file or url destination may have changed from last time
        // so we need to keep the last X audio samples used

        //Player player = Manager.createPlayer(RiskUtil.openStream(fileName), "audio/basic");
        Player player = Manager.createPlayer("file:///android_asset/" + fileName);
        return player;
    }

    public void play(String fileName) {
        if (outOfMemoryError) return;
        
        Player player = null;
        try {
            player = getPlayer(fileName);
            player.start(); // can throw oom
        }
        catch (Exception ex) {
            startError(fileName, player, ex);
        }
        catch (Error oom) { // OutOfMemoryError and NoClassDefFoundError
            outOfMemoryError = true;
            startError(fileName, player, oom);
        }
    }

    @Override
    public void start(final String fileName) {
        if (outOfMemoryError) return;
        
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                Player player = null;
                try {
                    player = getPlayer(fileName);
                    player.setLoopCount(-1);
                    currentPlayers.put(fileName, player);
                    player.start(); // can throw oom
                }
                catch (Exception ex) {
                    startError(fileName, player, ex);
                }
                catch (Error oom) { // OutOfMemoryError and NoClassDefFoundError
                    outOfMemoryError = true;
                    startError(fileName, player, oom);
                }
            }
        });
    }
    
    private void startError(String fileName, Player player, Throwable ex) {
        LOGGER.log(Level.WARNING, "unable to play " + fileName, ex);
        try {
            currentPlayers.remove(fileName);
            if (player != null) {
                player.close();
            }
        }
        catch (Throwable th) {}
    }

    @Override
    public void stop(final String audioFile) {
        singleThread.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Player player = currentPlayers.remove(audioFile);
                    if (player != null) {
                        // this is not needed as we only start and stop from a single thread
                        //if (player.getState() != Player.STARTED) {
                        //    LOGGER.log(Level.INFO, "player not started yet, will stop with listener: " + audioFile);
                        //    player.addPlayerListener(SimpleAudio.this);
                        //}
                        player.stop();
                    }
                    else {
                        // this really should never happen
                        LOGGER.log(Level.INFO, "unable to stop, not found: " + audioFile);
                    }
                }
                catch (Exception ex) {
                    LOGGER.log(Level.WARNING, "unable to stop " + audioFile, ex);
                }
            }
        });
    }
}
