package net.yura.domination.audio;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import net.yura.domination.engine.RiskUtil;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class GameSound {
    
    private static final Logger LOGGER = Logger.getLogger(GameSound.class.getName());

    public final static GameSound INSTANCE = new GameSound();

    public static final String MUSIC_MENU = "music_menu";
    public static final String MUSIC_GAME = "music_game";
    public static final String MUSIC_VICTORY = "music_victory";
    public static final String MUSIC_DEFEAT = "music_defeat";

    public static final String MENU_BUTTON = "menu_button";
    public static final String BUTTON = "button";
    public static final String BACK_BUTTON = "back_button";
    public static final String BUTTON_START_GAME = "button_start_game";

    public static final String CARDS_RECEIVE = "cards_receive";
    public static final String CARDS_TRADE = "cards_trade";

    public static final String PLACE_ARMY = "place_army";
    public static final String PLACE_ARMIES = "place_armies";

    public static final String ATTACK = "attack";
    public static final String BATTLE_RETREAT = "battle_retreat";

    public static final String DICE_ROLL = "dice_roll";
    public static final String DICE_WIN = "dice_win";
    public static final String DICE_LOSE = "dice_lose";
    public static final String DICE_DRAW = "dice_draw";
    
    public static final String BATTLE_WIN = "battle_win";
    public static final String BATTLE_DEFEAT = "battle_defeat";

    public static final String BATTLE_DEFENSE_WIN = "battle_defense_win";
    public static final String BATTLE_DEFENSE_DEFEAT = "battle_defense_defeat";

    public static final String MOVE_ARMIES = "move_armies";
    public static final String MOVE_TACTICAL = "move_tactical";

    public static final String LOBBY_START = "lobby_start";
    public static final String LOBBY_JOIN = "lobby_join";
    public static final String LOBBY_PLAY = "lobby_play";
    public static final String LOBBY_WATCH = "lobby_watch";
    public static final String LOBBY_SET_NICK = "lobby_set_nick";
    public static final String LOBBY_CLOSE_GAME = "lobby_close_game";
    
    
    private boolean soundEnabled = true;
    private boolean musicEnabled = true;
    private AudioSystem audioSystem;
    private Map<String, String> currentTheme; // ID -> filename

    public void setAudioSystem(AudioSystem audio) {
        audioSystem = audio;
    }

    public void load(String theme) {

        try {
            final String folder = "sound";

            InputStream in = RiskUtil.openStream(folder + "/" + theme + ".xml");

            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            currentTheme = new HashMap();
            parser.parse(in, new DefaultHandler() {
                @Override
                public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
                    if ("sound".equals(qName)) {
                        String key = attributes.getValue("name");
                        String filename = attributes.getValue("file");
                        
                        if (!new File("sound", filename).exists()) {
                            System.out.println("[WARNING!!!!!] File not found: " + filename);
                        }
                        
                        currentTheme.put(key, folder + "/" + filename);
                    }
                }
            });
        }
        catch (Exception ex) {
            Logger.getLogger(GameSound.class.getName()).log(Level.WARNING, "unable to load theme: " + theme, ex);
        }
    }
    
    public void setSoundEnabled(boolean on) {
        soundEnabled = on;
    }
    public void setMusicEnabled(boolean on) {
        musicEnabled = on;
    }

    public void playSound(String audioId) {
        
        LOGGER.info("Playing sound with id: " + audioId);
        
        if (soundEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.play(audioFile);
            }
        }
    }

    public void playMusic(String audioId) {
        
        LOGGER.info("Playing music with id: " + audioId);
        
        if (musicEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.start(audioFile);
            }
        }
    }

    public void stopMusic(String audioId) {

        LOGGER.info("Stopping music with id: " + audioId);
        
        if (audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.stop(audioFile);
            }
        }
    }

    public boolean isSoundEnabled() {
        return soundEnabled;
    }

    public boolean isMusicEnabled() {
        return musicEnabled;
    }
}
