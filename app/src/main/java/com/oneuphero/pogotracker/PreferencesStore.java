package com.oneuphero.pogotracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by dan on 26/9/2016.
 */

class PreferencesStore {

    final static public String LAST_LOCATION="LAST_LOCATION";

    public static boolean setLastLocation(Context context, Location location) {
        try {
            JSONObject locationJson = new JSONObject();

            locationJson.put("latitude", location.getLatitude());
            locationJson.put("longitude", location.getLongitude());
            //other location data
            SharedPreferences.Editor edit = PreferenceManager.getDefaultSharedPreferences(context).edit();
            edit.putString(LAST_LOCATION, locationJson.toString());
            edit.commit();
        } catch (JSONException e) {
            Log.e("JSON Exception", e.getMessage());
            return false;
        }
        return true;
    }

    public static Location getLastLocation(Context context) {
        try {
            String json = PreferenceManager.getDefaultSharedPreferences(context).getString(LAST_LOCATION, null);

            if (json != null) {
                JSONObject locationJson = new JSONObject(json);
                Location location = new Location("");
                location.setLatitude(locationJson.getInt("latitude"));
                location.setLongitude(locationJson.getInt("longitude"));
                return location;
            }
        } catch (JSONException e) {
            Log.e("JSON Exception", e.getMessage());
        }

        //or throw exception depending on your logic
        return null;
    }
}
