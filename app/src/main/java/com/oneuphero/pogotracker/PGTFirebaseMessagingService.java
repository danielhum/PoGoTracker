package com.oneuphero.pogotracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;

import java.util.Map;

/**
 * Created by dan on 25/9/2016.
 */

public class PGTFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "PGTFirebaseMsgService";

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .build();

            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onDestroy() {
        if (mGoogleApiClient != null) {
            mGoogleApiClient.disconnect();
        }
        super.onDestroy();
    }
    /**
     * Called when message is received.
     *
     * @param remoteMessage Object representing the message received from Firebase Cloud Messaging.
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        // [START_EXCLUDE]
        // There are two types of messages data messages and notification messages. Data messages are handled
        // here in onMessageReceived whether the app is in the foreground or background. Data messages are the type
        // traditionally used with GCM. Notification messages are only received here in onMessageReceived when the app
        // is in the foreground. When the app is in the background an automatically generated notification is displayed.
        // When the user taps on the notification they are returned to the app. Messages containing both notification
        // and data payloads are treated as notification messages. The Firebase console always sends notification
        // messages. For more see: https://firebase.google.com/docs/cloud-messaging/concept-options
        // [END_EXCLUDE]

        // TODO(developer): Handle FCM messages here.
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
//        Log.d(TAG, "From: " + remoteMessage.getFrom());

        // Check if message contains a data payload.
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> data = remoteMessage.getData();
            Gson gson = new Gson();
            PokemonSpawn spawn = gson.fromJson(data.get("pokemon_spawn"), PokemonSpawn.class);
//            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            //if (Helper.haveLocationPermissions(getApplicationContext())) { TODO: fix this
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location == null) location = PreferencesStore.getLastLocation(this);

                if (location != null) {
                    int distance = (int) spawn.distanceTo(location);
                    if (distance <= 1000) {
                        Intent intent = new Intent(this, MapsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                                PendingIntent.FLAG_ONE_SHOT);

                        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        float bearing = spawn.bearingTo(location);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this)
                                .setSmallIcon(R.mipmap.ic_launcher)
                                .setContentTitle(String.format("%s %d meters away!", spawn.getPokemonName(), distance))
                                .setContentText(String.format("bearing:%.2f°", bearing))
                                .setAutoCancel(false) //TODO: false for debug, change this later
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                        NotificationManager notificationManager =
                                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(0 /* ID of notification */, notificationBuilder.build());
                    } else {
                        Log.d(TAG, String.format("ignoring %s spawn %d meters away", spawn.getPokemonName(), distance));
                    }
                } else {
                    FirebaseCrash.logcat(Log.DEBUG, TAG, "location null, notification ignored");
                    Log.w(TAG, "location null, notification ignored");
                }
            //}
        }

        // Check if message contains a notification payload.
        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "Message Notification Body: " + remoteMessage.getNotification().getBody());
        }

        // Also if you intend on generating your own notifications as a result of a received FCM
        // message, here is where that should be initiated. See sendNotification method below.
    }
    // [END receive_message]
}
