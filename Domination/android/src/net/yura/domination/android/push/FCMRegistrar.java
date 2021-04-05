package net.yura.domination.android.push;

import android.content.Context;
import net.yura.domination.mobile.flashgui.DominationMain;

/**
 * @see com.google.android.gcm.GCMRegistrar
 */
public class FCMRegistrar {

    public static final long DEFAULT_ON_SERVER_LIFESPAN_MS = 604800000L; // 7 days

    private static final String KEY = "onServerToken";
    private static final String PROPERTY_ON_SERVER_EXPIRATION_TIME = "onServerExpirationTime";

    /**
     * @see com.google.android.gcm.GCMRegistrar#isRegisteredOnServer(Context)
     */
    public static boolean isRegisteredOnServer(String token) {
        String dbtoken = DominationMain.appPreferences.get(KEY, null);
        if (token == null) {
            // if token is null we want to find out if we are registered on the server at all
            return dbtoken != null;
        }
        boolean isRegistered = token.equals(dbtoken);
        if (isRegistered) {
            long expirationTime = DominationMain.appPreferences.getLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, -1L);
            if (System.currentTimeMillis() > expirationTime) {
                return false;
            }
        }
        return isRegistered;
    }

    /**
     * @see com.google.android.gcm.GCMRegistrar#setRegisteredOnServer(Context, boolean)
     */
    public static void setRegisteredOnServer(String token) {
        if (token == null) {
            DominationMain.appPreferences.remove(KEY);
            DominationMain.appPreferences.remove(PROPERTY_ON_SERVER_EXPIRATION_TIME);
        }
        else {
            DominationMain.appPreferences.put(KEY, token);
            DominationMain.appPreferences.putLong(PROPERTY_ON_SERVER_EXPIRATION_TIME, System.currentTimeMillis() + DEFAULT_ON_SERVER_LIFESPAN_MS);
        }
        try {
            DominationMain.appPreferences.flush();
        }
        catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
