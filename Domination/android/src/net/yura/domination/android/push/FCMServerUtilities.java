package net.yura.domination.android.push;

import net.yura.android.AndroidMeApp;
import net.yura.domination.mobile.flashgui.DominationMain;
import net.yura.domination.mobile.flashgui.MiniFlashRiskAdapter;
import net.yura.lobby.client.AndroidLobbyClient;
import net.yura.lobby.client.Connection;
import net.yura.lobby.mini.MiniLobbyClient;

public class FCMServerUtilities implements AndroidLobbyClient {

    public static void register(String registrationId) {
        Connection con = getLobbyConnection();
        if (con != null) {
            con.addAndroidEventListener(new FCMServerUtilities(registrationId));
            con.androidRegister(registrationId);
        }
    }

    public static void unregister() {
        Connection con = getLobbyConnection();
        if (con != null) {
            con.addAndroidEventListener(new FCMServerUtilities(null));
            con.androidUnregister(null);
        }
    }

    static Connection getLobbyConnection() {
        DominationMain main = (DominationMain)AndroidMeApp.getMIDlet();
        if (main != null) {
            MiniFlashRiskAdapter gui = main.adapter;
            if (gui != null) {
                MiniLobbyClient lobby = gui.lobby;
                if (lobby != null) {
                    return lobby.mycom;
                }
            }
        }
        return null;
    }

    String token;
    public FCMServerUtilities(String token) {
        this.token = token;
    }

    @Override
    public void registerDone() {
        FCMRegistrar.setRegisteredOnServer(token);
    }

    @Override
    public void unregisterDone() {
        FCMRegistrar.setRegisteredOnServer(null);
    }
}
