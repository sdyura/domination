package net.yura.domination.android.push;

import java.util.logging.Level;
import java.util.logging.Logger;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.messaging.FirebaseMessaging;
import android.app.Activity;
import android.os.Bundle;
import android.widget.Toast;
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
            // can not use this check as sometimes even if this is not SUCCESS, push still works fine
            //if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS) {

            setup();
            //unregister();
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

                            Exception exception = task.getException();
                            Level level = Level.WARNING;
                            // for some strange reason this comes back as IOException
                            if (exception != null && "MISSING_INSTANCEID_SERVICE".equals(exception.getMessage())) {
                                level = Level.INFO;
                            }

                            if (exception != null && "TOO_MANY_REGISTRATIONS".equals(exception.getMessage())) {
                                if (net.yura.android.AndroidMeActivity.DEFAULT_ACTIVITY != null) {
                                    javax.microedition.midlet.MIDlet.showToast("FCM Error: TOO_MANY_REGISTRATIONS, try uninstalling some apps for notifications to work", Toast.LENGTH_LONG);
                                }
                            }

                            logger.log(level, "Fetching FCM registration token failed", exception);
                            return;
                        }

                        // Get new Instance ID token
                        String token = task.getResult();

                        if (FCMRegistrar.isRegisteredOnServer(token)) {
                            logger.info("FCM Already registered");
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

                       logger.info("FCM Device unregistered");
                       if (FCMRegistrar.isRegisteredOnServer(null)) {
                           FCMServerUtilities.unregister();
                       }
                       else {
                           // This callback results from the call to unregister made on
                           // ServerUtilities when the registration to the server failed.
                           logger.info("FCM Ignoring unregister callback");
                       }
                   }
               });
    }
}
