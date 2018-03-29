package lipdroid.com.gobeta;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.net.Uri;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import lipdroid.com.gobeta.services.MyLocationTrackerService;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_CODE = 1;
    private GoogleMap mMap;
    private LocationUpdateReceiver locationUpdateReceiver = null;
    private boolean isMapReady = false;
    private Marker player_marker = null;
    private double player_pre_lat = 0.0;
    private double player_pre_lng = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    @Override
    public void onMapReady(GoogleMap googleMap) {
        isMapReady = true;
        mMap = googleMap;
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomGesturesEnabled(false);
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(23.7834976, 90.3941726))
                .zoom(18)
                .tilt(67.5f)
                .bearing(314)
                .build();
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    /* Reciever for location updates*/
    private class LocationUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub
            double latitude = arg1.getDoubleExtra("latitude", 0);
            double longitude = arg1.getDoubleExtra("longitude", 0);
            Log.e("current latitude: ", latitude + "");
            Log.e("Current longitude: ", longitude + "");
            if (isMapReady) {
                if (player_marker != null) {
                    animateMarker(90, new LatLng(player_pre_lat, player_pre_lng), new LatLng(latitude, longitude), false, player_marker);
                } else {
                    addPlayerMarkerFirstTime(latitude, longitude);
                }
            }

            player_pre_lat = latitude;
            player_pre_lng = longitude;

        }
    }

    private void addPlayerMarkerFirstTime(double latitude, double longitude) {
        player_marker = mMap.addMarker(new MarkerOptions().position(new LatLng(latitude, longitude))
                .title("User")
                .anchor(0.5f, 0.5f)
                .rotation(0f));
        //Build camera position
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(latitude, longitude))
                .zoom(18)
                .tilt(67.5f)
                .bearing(314)
                .build();
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    @Override
    protected void onStop() {
        // TODO Auto-generated method stub

        if (checkLocationServiceRunning()) {
            stopLocationTrackingService();
        }

        super.onStop();
    }

    private void stopLocationTrackingService() {
        unregisterReceiver(locationUpdateReceiver);
        //stop our own service
        Intent intent = new Intent(MapsActivity.this,
                MyLocationTrackerService.class);
        stopService(intent);
    }

    public void go_to_my_location(View view) {
        if (player_pre_lat != 0.0 && player_pre_lng != 0.0) {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(player_pre_lat, player_pre_lng))
                    .zoom(18).build();
            mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    @Override
    protected void onStart() {
        if (!permissionsGranted()) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_CODE);
        } else startLocationTrackingService();
        super.onStart();
    }

    private void startLocationTrackingService() {
        //Register BroadcastReceiver
        //to receive event from our service
        locationUpdateReceiver = new LocationUpdateReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MyLocationTrackerService.MY_LOCATION);
        registerReceiver(locationUpdateReceiver, intentFilter);

        //Start our own service
        Intent intent = new Intent(MapsActivity.this,
                MyLocationTrackerService.class);
        startService(intent);
    }

    //This methos is used to move the marker of each car smoothly when there are any updates of their position
    public void animateMarker(final int position, final LatLng startPosition, final LatLng toPosition,
                              final boolean hideMarker, Marker animate_marker) {


        final Marker marker = animate_marker;


        final Handler handler = new Handler();
        final long start = SystemClock.uptimeMillis();

        final long duration = 1000;
        final LinearInterpolator interpolator = new LinearInterpolator();

        handler.post(new Runnable() {
            @Override
            public void run() {
                long elapsed = SystemClock.uptimeMillis() - start;
                float t = interpolator.getInterpolation((float) elapsed
                        / duration);
                double lng = t * toPosition.longitude + (1 - t)
                        * startPosition.longitude;
                double lat = t * toPosition.latitude + (1 - t)
                        * startPosition.latitude;

                marker.setPosition(new LatLng(lat, lng));
                // marker.setRotation(getRotation(startPosition, toPosition));

                if (t < 1.0) {
                    // Post again 16ms later.
                    handler.postDelayed(this, 16);
                } else {
                    if (hideMarker) {
                        marker.setVisible(false);
                    } else {
                        marker.setVisible(true);
                    }
                }
            }
        });
    }

    //rotate the car according to its location
    private float getRotation(LatLng startPosition, LatLng toPosition) {
        Location targetLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(toPosition.latitude);//your coords of course
        targetLocation.setLongitude(toPosition.longitude);

        Location startLocation = new Location("");//provider name is unnecessary
        targetLocation.setLatitude(startPosition.latitude);//your coords of course
        targetLocation.setLongitude(startPosition.longitude);
        Log.e("icon_rotation", startLocation.bearingTo(targetLocation) + "");
        return startLocation.bearingTo(targetLocation);
    }

    private Boolean permissionsGranted() {
        return ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(final int requestCode, @NonNull final String[] permissions, @NonNull final int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted.
                if (!checkLocationServiceRunning()) {
                    //start the service
                    startLocationTrackingService();
                }
            } else {
                // User refused to grant permission. You can add AlertDialog here
                Toast.makeText(this, "You didn't give permission to access device location", Toast.LENGTH_LONG).show();
                startInstalledAppDetailsActivity();
            }
        }
    }

    private void startInstalledAppDetailsActivity() {
        Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }

    public boolean checkLocationServiceRunning() {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if ("lipdroid.com.gobeta.services.TrackerService"
                    .equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
