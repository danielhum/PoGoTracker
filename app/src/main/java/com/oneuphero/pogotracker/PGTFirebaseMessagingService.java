package com.oneuphero.pogotracker;

import android.app.PendingIntent;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.AwarenessFence;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.awareness.fence.LocationFence;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;
import com.google.firebase.crash.FirebaseCrash;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by dan on 25/9/2016.
 */

public class PGTFirebaseMessagingService extends FirebaseMessagingService {
    private static final String TAG = "PGTFirebaseMsgService";

    private GoogleApiClient mGoogleApiClient;
    private PendingIntent mPendingIntent;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create an instance of GoogleAPIClient.
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(this)
                    .addApi(LocationServices.API)
                    .addApi(Awareness.API)
                    .build();

            mGoogleApiClient.connect();
        }

        Intent intent = new Intent(MapsActivity.FENCE_RECEIVER_ACTION);
        mPendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
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
            List<PokemonSpawn> spawnList = new ArrayList<>();
            String spawnsJson = data.get("pokemon_spawns");
            if (spawnsJson != null) {
                spawnList.addAll((ArrayList<PokemonSpawn>)gson.fromJson(spawnsJson, new TypeToken<List<PokemonSpawn>>(){}.getType()));
            } else {
                PokemonSpawn spawn = gson.fromJson(data.get("pokemon_spawn"), PokemonSpawn.class);
                spawnList.add(spawn);
            }

//            Log.d(TAG, "Message data payload: " + remoteMessage.getData());

            //if (Helper.haveLocationPermissions(getApplicationContext())) { TODO: fix this
                Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);

                if (location == null) location = PreferencesStore.getLastLocation(this);

                if (location != null) {
                    for (PokemonSpawn spawn : spawnList) {
                        int distance = (int) spawn.distanceTo(location);
                        if (distance <= 600) {
                            Helper.notifyAboutSpawn(this, spawn, distance, location);
                        } else if (distance <= 1800) {
                            AwarenessFence spawnFence = LocationFence.entering(spawn.getLatitude(), spawn.getLongitude(), 600);
                            final String fenceKey = String.valueOf(spawn.getId());

                            // *** TODO: Unregister and delete spawn after it expires ***
                            spawn.save();
                            Awareness.FenceApi.updateFences(
                                    mGoogleApiClient, new FenceUpdateRequest.Builder()
                                        .addFence(fenceKey, spawnFence, mPendingIntent)
                                        .build())
                                    .setResultCallback(new ResultCallback<Status>() {
                                        @Override
                                        public void onResult(@NonNull Status status) {
                                            if(status.isSuccess()) {
                                                Log.i(TAG, "Fence was successfully registered.");
                                                //queryFence(fenceKey);
                                            } else {
                                                Log.e(TAG, "Fence could not be registered: " + status);
                                            }
                                        }
                                    });
                            Log.i(TAG, String.format("setup fence for %s spawn %d meters away", spawn.getPokemonName(), distance));
                        } else {
                            Log.d(TAG, String.format("ignoring %s spawn %d meters away", spawn.getPokemonName(), distance));
                        }
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
