package edu.ucsb.ece150.locationplus;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.google.android.gms.location.GeofenceStatusCodes;

public class GeofenceBroadcastReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("Geofence", "User has entered the geofence area");
        GeofenceTransitionJobIntentService.enqueueWork(context, intent);
    }
}
