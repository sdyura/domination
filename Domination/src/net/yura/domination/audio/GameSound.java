package net.yura.domination.audio;

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

    public final static GameSound INSTANCE = new GameSound();
    
    public static final String MENU_MUSIC = "menu_music";
    public static final String START_GAME = "start_game";
    
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
        if (soundEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.play(audioFile);
            }
        }
    }

    public void playMusic(String audioId) {
        if (musicEnabled && audioSystem != null && currentTheme != null) {
            String audioFile = currentTheme.get(audioId);
            if (audioFile != null) {
                audioSystem.start(audioFile);
            }
        }
    }

    public void stopMusic(String audioId) {
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
