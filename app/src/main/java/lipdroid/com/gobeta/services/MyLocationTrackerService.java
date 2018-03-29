package lipdroid.com.gobeta.services;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Created by mdmunirhossain on 3/21/18.
 */

public class MyLocationTrackerService extends Service {

    private static final String TAG = MyLocationTrackerService.class.getSimpleName();
    private FusedLocationProviderClient client = null;
    public final static String MY_LOCATION = "MY_LOCATION";

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        requestLocationUpdates();

    }

    protected BroadcastReceiver stopReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "received stop broadcast");
            // Stop the service when the notification is tapped
            unregisterReceiver(stopReceiver);
            stopSelf();
        }
    };

    /**
     * Update location to firebase database
     */
    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(1000);
        request.setFastestInterval(500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        client = LocationServices.getFusedLocationProviderClient(this);
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, store the location in Firebase
            client.requestLocationUpdates(request, locationUpdateListener, null);
        }
    }

    public LocationCallback locationUpdateListener = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location location = locationResult.getLastLocation();
            if (location != null) {
                Log.d(TAG, "location update " + location);
                double current_latitude = location.getLatitude();
                double current_longitude = location.getLongitude();
                Intent intent = new Intent();
                intent.setAction(MY_LOCATION);
                intent.putExtra("latitude", current_latitude);
                intent.putExtra("longitude", current_longitude);
                sendBroadcast(intent);
            }
        }
    };

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationUpdateListener != null)
            client.removeLocationUpdates(locationUpdateListener);
    }
}
