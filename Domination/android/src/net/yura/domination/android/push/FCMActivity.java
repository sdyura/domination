package net.yura.domination.android.push;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.NonNull;

/**
 * this activity is started when we connect to the lobby server
 */
public class FCMActivity extends Activity {

    static final Logger logger = Logger.getLogger(FCMActivity.class.getName());

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	try {
            setup();
            //unregister();
	}
	catch (UnsupportedOperationException th) {
	    logger.log(Level.INFO, "FCM fail", th);
	}
	catch (Throwable th) {
	    logger.log(Level.WARNING, "FCM fail", th);
	}
        finish();
    }

    public static void displayMessage(String text) {
        logger.info(text);
    }

    public static void setup() {

        FirebaseMessaging.getInstance().getToken()
                .addOnCompleteListener(new OnCompleteListener<String>() {
                    @Override
                    public void onComplete(@NonNull Task<String> task) {
                        if (!task.isSuccessful()) {
                            // something went wrong, should we request to register? it should do this automatically
                            // FirebaseMessaging.getInstance().setAutoInitEnabled(true);
                            logger.log(Level.WARNING, "Fetching FCM registration token failed", task.getException());
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult();

                        if (FCMRegistrar.isRegisteredOnServer(token)) {
                            displayMessage("Already registered");
                        }
                        else {
                            FCMServerUtilities.register(token);
                        }
                    }
                });
    }

    public static void unregister() {

        // not really sure this is correct, but its not currently used, so who cares
        FirebaseMessaging.getInstance().deleteToken()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                   @Override
                   public void onComplete(@NonNull Task<Void> task) {
                       if (!task.isSuccessful()) {
                           logger.log(Level.WARNING, "FCM deleteToken failed", task.getException());
                           return;
                       }

                       displayMessage("Device unregistered");
                       if (FCMRegistrar.isRegisteredOnServer(null)) {
                           FCMServerUtilities.unregister();
                       }
                       else {
                           // This callback results from the call to unregister made on
                           // ServerUtilities when the registration to the server failed.
                           displayMessage("Ignoring unregister callback");
                       }
                   }
               });
    }
}
