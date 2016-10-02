package com.oneuphero.pogotracker;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;

/**
 * Created by dan on 25/9/2016.
 */

class Helper {

    static boolean haveLocationPermissions(Context context) {
        return (ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED);
    }

    static void notifyAboutSpawn(Context context, PokemonSpawn spawn, Integer distance, Location userLocation) {
        Intent intent = new Intent(context, MapsActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setAutoCancel(false) //TODO: false for debug, change this later
                .setSound(defaultSoundUri)
                .setContentIntent(pendingIntent);

        if (distance != null && userLocation != null) {
            float bearing = spawn.bearingTo(userLocation);
            notificationBuilder
                    .setContentTitle(String.format("%s %d meters away!", spawn.getPokemonName(), distance))
                    .setContentText(String.format("bearing:%.2f°", bearing));
        } else {
            notificationBuilder.setContentTitle(String.format("%s nearby!", spawn.getPokemonName()));
        }

        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(spawn.getId().intValue(), notificationBuilder.build());
    }
}
