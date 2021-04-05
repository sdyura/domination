package net.yura.domination.android.push;

import android.content.Context;
import net.yura.domination.mobile.flashgui.DominationMain;

/**
 * @see com.google.android.gcm.GCMRegistrar
 */
public class FCMRegistrar {

    private static final String KEY = "onServerToken";

    /**
     * @see com.google.android.gcm.GCMRegistrar#isRegisteredOnServer(Context)
     */
    public static boolean isRegisteredOnServer(String token) {
        String dbtoken = DominationMain.appPreferences.get(KEY, null);
        return token == null ? dbtoken != null : token.equals(dbtoken);
    }

    /**
     * @see com.google.android.gcm.GCMRegistrar#setRegisteredOnServer(Context, boolean)
     */
    public static void setRegisteredOnServer(String token) {
        if (token == null) {
            DominationMain.appPreferences.remove(KEY);
        }
        else {
            DominationMain.appPreferences.put(KEY, token);
        }
        try {
            DominationMain.appPreferences.flush();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
