package net.yura.domination.android.push;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.microedition.midlet.MIDlet;
import net.yura.domination.R;
import net.yura.lobby.client.AndroidLobbyClient;
import net.yura.lobby.mini.MiniLobbyClient;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

public class FCMIntentService extends FirebaseMessagingService {

    @Override
    public void onNewToken(String registrationId) {
        FCMActivity.displayMessage("Device registered: regId = "+registrationId);
        FCMRegistrar.setRegisteredOnServer(null);
        FCMServerUtilities.register(registrationId);
    }

    /**
     * @see MiniLobbyClient#notify(net.yura.lobby.model.Game, boolean)
     */
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage){
        String from = remoteMessage.getFrom();
        Map<String, String> data = remoteMessage.getData();

        String msg = data.get(AndroidLobbyClient.MESSAGE);
        String gameId = data.get(AndroidLobbyClient.GAME_ID);
        String options = data.get(AndroidLobbyClient.OPTIONS);

        String message = msg==null?"Received message":msg;
        FCMActivity.displayMessage(from + ": " + message);
        // notifies user
        Map<String, Object> extras = new HashMap();
        if (gameId != null) {
            extras.put(MiniLobbyClient.EXTRA_GAME_ID, gameId);
        }
        if (options != null) {
            extras.put(MiniLobbyClient.EXTRA_GAME_OPTIONS, options);
        }
        MIDlet.showNotification(this.getString(R.string.app_name), message, R.drawable.icon, -1, extras);
    }

    @Override
    public void onDeletedMessages() {
        String message = "Received deleted messages notification ";
        FCMActivity.displayMessage(message);
        // notifies user
        MIDlet.showNotification(this.getString(R.string.app_name), message, R.drawable.icon, -1, Collections.EMPTY_MAP);
    }

    @Override
    public void onSendError(String s, Exception e) {
        FCMActivity.displayMessage("Received error: " + s + " " + e);
    }

    @Override
    public void onMessageSent(String s) {
        FCMActivity.displayMessage("onMessageSent: "+s);
    }
}
