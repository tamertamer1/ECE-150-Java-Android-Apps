package edu.ucsb.ece150.locationplus;
import androidx.core.content.ContextCompat;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.app.AlertDialog;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import android.content.pm.PackageManager;
import android.Manifest;
import android.widget.ImageButton;
import java.util.ArrayList;
import android.widget.ArrayAdapter;
import android.location.GnssStatus;
import androidx.annotation.NonNull;
import android.widget.Toast;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import com.google.android.gms.maps.model.LatLng;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingClient;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;

public class MapsActivity extends AppCompatActivity implements LocationListener, OnMapReadyCallback {

    private Geofence mGeofence;
    private GeofencingClient mGeofencingClient;
    private PendingIntent mPendingIntent = null;
    private boolean mapReady = false;
    private GnssStatus.Callback mGnssStatusCallback;
    private GoogleMap mMap;
    private LocationManager mLocationManager;
    private boolean isAutoCenteringEnabled = false;
    private Toolbar mToolbar;
    private boolean isMapLocked = false;
    private ArrayList<Satellite> satelliteList = new ArrayList<>();
    private ArrayAdapter<Satellite> adapter;
    private static final float GEOFENCE_RADIUS = 100;


    private String getConstellationName(int type) {
        switch (type) {
            case GnssStatus.CONSTELLATION_GPS:
                return "GPS";
            case GnssStatus.CONSTELLATION_GLONASS:
                return "GLONASS";
            case GnssStatus.CONSTELLATION_BEIDOU:
                return "BEIDOU";
            case GnssStatus.CONSTELLATION_GALILEO:
                return "GALILEO";
            case GnssStatus.CONSTELLATION_QZSS:
                return "QZSS";
            case GnssStatus.CONSTELLATION_IRNSS:
                return "IRNSS";
            default:
                return "UNKNOWN";
        }
    }
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 101;
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        // Set up Google Maps
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // Set up Geofencing Client
        mGeofencingClient = LocationServices.getGeofencingClient(MapsActivity.this);

        // Set up Satellite List
        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);



        mGnssStatusCallback = new GnssStatus.Callback() {
            @Override
            public void onSatelliteStatusChanged(GnssStatus status) {
                // Clear the existing satellite list
                satelliteList.clear();
                Satellite.resetCounter();

                // Populate the satellite list with new status information
                for (int i = 0; i < status.getSatelliteCount(); i++) {
                    String constellationName = getConstellationName(status.getConstellationType(i));
                    int svid = status.getSvid(i);
                    double azimuth = status.getAzimuthDegrees(i);
                    double elevation = status.getElevationDegrees(i);
                    double carrierFrequency = 0; // Default to 0 or some other default value
                    double cno = 0; // Default to 0 or some other default value

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        carrierFrequency = status.getCarrierFrequencyHz(i);
                        cno = status.getCn0DbHz(i);
                    }
                    boolean usedInFix = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        usedInFix = status.usedInFix(i);
                    }
                    // Create a new Satellite object and add it to the list
                    Satellite satellite = new Satellite(
                            "Satellite " + svid,
                            azimuth,
                            elevation,
                            carrierFrequency,
                            cno,
                            constellationName,
                            svid,
                            usedInFix
                    );
                    satelliteList.add(satellite);
                }

                // Notify the adapter of the data change
                if(adapter != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.notifyDataSetChanged();
                        }
                    });
                }
            }
        };




        // [TODO] Additional setup for viewing satellite information (lists, adapters, etc.)

        // Set up Toolbar
        mToolbar = (Toolbar) findViewById(R.id.appToolbar);
        setSupportActionBar(mToolbar);
        ImageButton locationButton = findViewById(R.id.locationButton);
        locationButton.setOnClickListener(v -> {
            if (mMap != null) {
                centerMapOnMyLocation();
            } else {
                Toast.makeText(this, "Map is not ready", Toast.LENGTH_SHORT).show();
            }
        });
        ImageButton infoButton = findViewById(R.id.infoButton);
        infoButton.setOnClickListener(v -> {
            Intent intent = new Intent(MapsActivity.this, ListActivity.class);
            intent.putExtra("SatelliteList", satelliteList);
            startActivity(intent);
        });

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        } else {

        }


    }

    private void centerMapOnMyLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
            return;
        }

        mMap.setMyLocationEnabled(true);
        Location currentLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (currentLocation != null) {
            updateMapLocation(currentLocation);
        } else {
            // If no last known location, request a single update
            mLocationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateMapLocation(location);
                    mLocationManager.removeUpdates(this);
                }
            }, null);
        }
    }



    private void updateMapLocation(Location location) {
        LatLng userLatLng = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15));

        if (!isMapLocked) {
            isMapLocked = true;
            disableMapMovement();
        } else {
            isMapLocked = false;
            enableMapMovement();
        }
    }




    private void disableMapMovement() {
        mMap.getUiSettings().setAllGesturesEnabled(false);
    }

    private void enableMapMovement() {
        mMap.getUiSettings().setAllGesturesEnabled(true);
    }



    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Fine location permission granted
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    // Request background location permission for Android 10 and above
                    ActivityCompat.requestPermissions(this,
                            new String[]{Manifest.permission.ACCESS_BACKGROUND_LOCATION},
                            102);
                } else {
                    // For Android 9 and below, proceed with setting up geofencing
                }
            } else {
                // Permission was denied
                Toast.makeText(this, "Fine location permission is needed to use geofencing.", Toast.LENGTH_SHORT).show();
            }
        } else if (requestCode == 102) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Background location permission granted

            } else {
                // Permission was denied
                Toast.makeText(this, "Background location access is needed for geofencing to work properly.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        enableMapMovement();
        mMap.setOnMapLongClickListener(latLng -> {
            mMap.clear();
            // Prompt the user to confirm the destination
            new AlertDialog.Builder(MapsActivity.this)
                    .setTitle("Confirm Destination")
                    .setMessage("Set position (" + latLng.latitude + ", " + latLng.longitude + ") as your destination?")
                    .setPositiveButton("YES", (dialogInterface, i) -> addMarkerAtLocation(latLng))
                    .setNegativeButton("NO", (dialogInterface, i) -> dialogInterface.dismiss())
                    .show();
        });
    }

    private void addMarkerAtLocation(LatLng latLng) {
        // Add a red location marker
        mMap.addMarker(new MarkerOptions()
                .position(latLng)
                .title("Destination")
                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));

        // Add a Geofence at the selected location
        addGeofence(latLng, GEOFENCE_RADIUS);
    }

    private void addGeofence(LatLng latLng, float radius) {
        Geofence geofence = new Geofence.Builder()
                .setRequestId("destination_geofence")
                .setCircularRegion(latLng.latitude, latLng.longitude, radius)
                .setExpirationDuration(Geofence.NEVER_EXPIRE)
                .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                .build();

        GeofencingRequest geofencingRequest = new GeofencingRequest.Builder()
                .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
                .addGeofence(geofence)
                .build();

        PendingIntent geofencePendingIntent = getGeofencePendingIntent();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mGeofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
                    .addOnSuccessListener(this, aVoid -> {
                        // Handle success
                        Toast.makeText(this, "Geofence added", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(this, e -> {

                        Toast.makeText(this, "Failed to add geofence: " ,Toast.LENGTH_SHORT).show();
                    });
        }
    }


    @Override
    public void onLocationChanged(Location location) {

    }
    /*
     * The following three methods onProviderDisabled(), onProviderEnabled(), and onStatusChanged()
     * do not need to be implemented -- they must be here because this Activity implements
     * LocationListener.
     *
     * You may use them if you need to.
     */
    @Override
    public void onProviderDisabled(String provider) {}

    @Override
    public void onProviderEnabled(String provider) {}

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {}

    private GeofencingRequest getGeofenceRequest() {
        // [TODO] Set the initial trigger (i.e. what should be triggered if the user is already
        // inside the Geofence when it is created)

        return new GeofencingRequest.Builder()
                //.setInitialTrigger()  <--  Add triggers here
                .addGeofence(mGeofence)
                .build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if(mPendingIntent != null)
            return mPendingIntent;

        Intent intent = new Intent(MapsActivity.this, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(MapsActivity.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStart() {
        super.onStart();
        // Check and request necessary permissions here
        // Register GNSS status callback
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);
            mLocationManager.registerGnssStatusCallback(mGnssStatusCallback);
        } else {
            // Request permissions from the user
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // [TODO] Data recovery
    }

    @Override
    protected void onPause() {
        super.onPause();

        // [TODO] Data saving
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onStop() {
        super.onStop();

        mLocationManager.removeUpdates(this);
        mLocationManager.unregisterGnssStatusCallback(mGnssStatusCallback);
    }
}
