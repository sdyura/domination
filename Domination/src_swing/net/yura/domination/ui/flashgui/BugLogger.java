package net.yura.domination.ui.flashgui;

import java.util.Map;
import java.util.logging.LogRecord;
import net.yura.domination.engine.RiskUtil;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.domination.guishared.RiskUIUtil;

public class BugLogger {

    public static void initGrasshopper() {
        if (RiskUIUtil.checkForNoSandbox()) {
            try {
                // Could not open/create prefs root node Software\JavaSoft\Prefs at root 0x80000002. Windows RegCreateKeyEx(...) returned error code 5.
                // HACK this will print any problems loading the Preferences before we start grasshopper
                java.util.prefs.Preferences.userRoot(); // returns java.util.prefs.WindowsPreferences
            }
            catch (Throwable th) { }

            // catch everything in my PrintStream
            try {
                net.yura.grasshopper.PopupBug.initSimple(RiskUtil.GAME_NAME,
                        RiskUtil.RISK_VERSION+" FlashGUI" // "(save: " + RiskGame.SAVE_VERSION + " network: "+RiskGame.NETWORK_VERSION+")"
                        , TranslationBundle.getBundle().getLocale().toString());

                net.yura.grasshopper.BugSubmitter.setApplicationInfoProvider(new net.yura.grasshopper.ApplicationInfoProvider() {
                    public void addInfoForSubmit(Map map) {
                        // TODO SwingGUI should also prob send this
                        map.put("lobbyID", net.yura.lobby.mini.MiniLobbyClient.getMyUUID());
                    }
                    public boolean ignoreError(LogRecord record) {
                        if (RiskUtil.isOldVersion()) {
                            return true;
                        }
                        if ("Trying to recreate Windows registry node Software\\JavaSoft\\Prefs\\net\\yura\\domination\\ui\\flashgui at root 0xffffffff80000001.".equals(record.getMessage())) {
                            return true;
                        }
                        return false;
                    }
                } );
            }
            catch(Throwable th) {
                RiskUtil.printStackTrace(th);
            }

            try {
                net.yura.swingme.core.CoreUtil.setupLogging();
            }
            catch (Throwable th) {
                RiskUtil.printStackTrace(th);
            }
        }
    }
}
