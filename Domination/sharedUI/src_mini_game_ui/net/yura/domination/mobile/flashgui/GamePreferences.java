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
import java.util.prefs.BackingStoreException;

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

            boolean show_toasts = Boolean.parseBoolean(results.get("show_toasts"));
            boolean color_blind = Boolean.parseBoolean(results.get("color_blind"));
            boolean show_dice = Boolean.parseBoolean(results.get("show_dice"));
            int ai_wait = Integer.parseInt(results.get("ai_wait"));

            Risk.setShowDice(show_dice);
            AIManager.setWait(ai_wait);

            DominationMain.appPreferences.putBoolean("show_toasts", show_toasts);
            DominationMain.appPreferences.putBoolean("color_blind", color_blind);
            DominationMain.appPreferences.putBoolean("show_dice", show_dice);
            DominationMain.appPreferences.putInt("ai_wait", ai_wait);

            try {
                DominationMain.appPreferences.flush();
            } catch (BackingStoreException e) {
                e.printStackTrace();
            }
        }
    }

    public static void showGamePreferences() {
        GamePreferences gp = new GamePreferences();
        gp.loader.setFormData(CoreUtil.asHashtable(DominationMain.appPreferences));
        OptionPane.showOptionDialog(gp, gp.loader.getRoot(), gp.resBundle.getProperty("swing.menu.options"), OptionPane.OK_CANCEL_OPTION, OptionPane.PLAIN_MESSAGE, null, null, null);
    }
}
