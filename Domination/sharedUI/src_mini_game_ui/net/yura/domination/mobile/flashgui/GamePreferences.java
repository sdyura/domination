package net.yura.domination.mobile.flashgui;

import net.yura.domination.engine.Risk;
import net.yura.domination.engine.ai.AIManager;
import net.yura.domination.engine.translation.TranslationBundle;
import net.yura.mobile.gui.ActionListener;
import net.yura.mobile.gui.Application;
import net.yura.mobile.gui.components.OptionPane;
import net.yura.mobile.gui.layout.XULLoader;
import net.yura.mobile.util.Properties;
import net.yura.swingme.core.CoreUtil;
import java.util.Map;
import net.yura.domination.engine.RiskSettings;

public class GamePreferences implements ActionListener {

    private Properties resBundle = CoreUtil.wrap(TranslationBundle.getBundle());
    XULLoader loader;

    public GamePreferences() {
        try {
            loader = XULLoader.load(Application.getResourceAsStream("/preferences.xml") , this, resBundle);
        }
        catch(Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void actionPerformed(String actionCommand) {
        if ("ok".equals(actionCommand)) {
            Map<String, String> results = loader.getFormData();

            // TODO do we want to support ability to put the game into fullscreen mode?
            //new CheckBox(resb.getString("game.menu.fullscreen"));
            //fullscreen.setKey("fullscreen");

            boolean show_toasts = Boolean.parseBoolean(results.get(RiskSettings.SHOW_TOASTS_KEY));
            boolean color_blind = Boolean.parseBoolean(results.get(RiskSettings.COLOR_BLIND_KEY));
            boolean showDice = Boolean.parseBoolean(results.get(RiskSettings.SHOW_DICE_KEY));
            int aiWait = Integer.parseInt(results.get(RiskSettings.AI_WAIT_KEY));

            Risk.setShowDice(showDice);
            AIManager.setWait(aiWait);

            DominationMain.appPreferences.putBoolean(RiskSettings.SHOW_TOASTS_KEY, show_toasts);
            DominationMain.appPreferences.putBoolean(RiskSettings.COLOR_BLIND_KEY, color_blind);
            
            RiskSettings.saveSettingsToPrefs(DominationMain.appPreferences);
        }
    }

    public static void showGamePreferences() {
        GamePreferences gp = new GamePreferences();
        gp.loader.setFormData(CoreUtil.asHashtable(DominationMain.appPreferences));
        OptionPane.showOptionDialog(gp, gp.loader.getRoot(), gp.resBundle.getProperty("swing.menu.options"), OptionPane.OK_CANCEL_OPTION, OptionPane.PLAIN_MESSAGE, null, null, null);
    }
}
