package com.oneuphero.pogotracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.NonNull;
import android.util.Log;

import com.google.android.gms.awareness.Awareness;
import com.google.android.gms.awareness.fence.FenceState;
import com.google.android.gms.awareness.fence.FenceUpdateRequest;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallbacks;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationServices;

/**
 * Created by dan on 2/10/2016.
 */

class PGTFenceReceiver extends BroadcastReceiver {

    final private static String TAG="PGTFenceReceiver";
    private GoogleApiClient mGoogleApiClient;

    public PGTFenceReceiver() {}

    public PGTFenceReceiver(GoogleApiClient googleApiClient) {
        super();
        mGoogleApiClient = googleApiClient;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        FenceState fenceState = FenceState.extract(intent);

        switch (fenceState.getCurrentState()) {
            case FenceState.TRUE:
                Log.i(TAG, "fence state true");
                PokemonSpawn spawn = PokemonSpawn.findById(PokemonSpawn.class, Integer.valueOf(fenceState.getFenceKey()));

                Location location = null;
                Integer distance = null;
                if (Helper.haveLocationPermissions(context)) {
                    location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
                    if (location != null) {
                        distance = (int) spawn.distanceTo(location);
                    }
                }
                Helper.notifyAboutSpawn(context, spawn, distance, location);

                // Unregister Fence
                final String fenceKey = fenceState.getFenceKey();
                Awareness.FenceApi.updateFences(
                        mGoogleApiClient,
                        new FenceUpdateRequest.Builder()
                                .removeFence(fenceKey)
                                .build()).setResultCallback(new ResultCallbacks<Status>() {
                    @Override
                    public void onSuccess(@NonNull Status status) {
                        Log.i(TAG, "Fence " + fenceKey + " successfully removed.");
                    }

                    @Override
                    public void onFailure(@NonNull Status status) {
                        Log.e(TAG, "Fence " + fenceKey + " could NOT be removed.");
                    }
                });

                break;
            case FenceState.FALSE:
                Log.w(TAG, "fence state false");
                break;
            case FenceState.UNKNOWN:
                Log.w(TAG, "fence state unknown");
                break;
        }
    }
}
