package com.example.waybus;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.example.waybus.databinding.ActivityMaps2Binding;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MapsActivity2 extends FragmentActivity implements OnMapReadyCallback{


    private GoogleMap mMap;


    private static final String TAG = "MapsActivity2";
    private static final int REQUEST_CHECK_SETTINGS = 0x1 ;
    private Geocoder geocoder;
    private static final int ACCESS_LOCATION_REQUEST_CODE = 1001;
    FusedLocationProviderClient fusedLocationProviderClient;
    LocationRequest locationRequest;
    FirebaseAuth mAuth;
    FirebaseDatabase firebaseDatabase;
    DatabaseReference databaseReference;
    DatabaseReference myref;
    DatabaseReference pref;
    FirebaseUser firebaseUser;
    LatLng mlatLng;
    private Polyline currentPolyline;


    Marker userLocationMarker;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        geocoder = new Geocoder(this);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);

        locationRequest = LocationRequest.create();
        locationRequest.setInterval(5000);
        locationRequest.setFastestInterval(2000);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        checklocationsettings();


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
    @SuppressLint("PotentialBehaviorOverride")
    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);

        firebaseDatabase = FirebaseDatabase.getInstance();
        mAuth = FirebaseAuth.getInstance();
        databaseReference = firebaseDatabase.getReference("Location");
        myref = databaseReference.child("Drivers");
         firebaseUser = mAuth.getCurrentUser();

        myref.addChildEventListener(markerUpdateListener);

//        mMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
//            @Override
//            public boolean onMarkerClick(@NonNull Marker marker) {
//                new FetchURL(MapsActivity2.this).execute(getUrl(mlatLng,marker.getPosition(), "driving"), "driving");
//                return false;
//            }
//        });


        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            enableUserLocation();
            zoomToUserLocation();
        }else{
            requestPermission();
            return;
        }
    }



    LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult locationResult) {
            super.onLocationResult(locationResult);
            Log.d(TAG, "onLocationResult: " + locationResult.getLastLocation());
            if (mMap != null) {
                setUserLocationMarker(locationResult.getLastLocation());
            }
        }
    };

    private void setUserLocationMarker(Location location) {


         mlatLng = new LatLng(location.getLatitude(),location.getLongitude());

        LocationHelper helper = new LocationHelper(location.getLatitude(),location.getLongitude());
        databaseReference.child("Users").child(firebaseUser.getUid()).setValue(helper);


        if (userLocationMarker == null) {
            //Create a new marker
            MarkerOptions markerOptions = new MarkerOptions();
            markerOptions.position(mlatLng);
            markerOptions.icon(BitmapDescriptorFactory.fromResource(R.drawable.myloc));
//            markerOptions.rotation(location.getBearing());
            markerOptions.anchor((float) 0.5, (float) 0.5);
            userLocationMarker = mMap.addMarker(markerOptions);
            try {
                List<Address> addresses = geocoder.getFromLocation(mlatLng.latitude, mlatLng.longitude, 1);
                if (addresses.size() > 0) {
                    Address address = addresses.get(0);
                    String streetAddress = address.getAddressLine(0);
                    userLocationMarker.setTitle(streetAddress);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
//            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        } else {
            //use the previously created marker
            userLocationMarker.setPosition(mlatLng);
//            userLocationMarker.setRotation(location.getBearing());
//            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17));
        }
    }

    private void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

        }else {
            requestPermission();
        }

    }

    private void stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback);
    }

    @Override
    protected void onStart() {
        super.onStart();
        startLocationUpdates();

    }

    @Override
    protected void onStop() {
        super.onStop();
        stopLocationUpdates();
    }

    private void enableUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ) {
            mMap.setMyLocationEnabled(true);
        }else{
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
        }

    }

    private void zoomToUserLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            requestPermission();
            return;
        }
        Task<Location> locationTask = fusedLocationProviderClient.getLastLocation();
        locationTask.addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                if(location != null){
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng , 18));
                }else{
                    startLocationUpdates();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ACCESS_LOCATION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                enableUserLocation();
                zoomToUserLocation();
            }
            else{
                Toast.makeText(this ,"user denied",Toast.LENGTH_SHORT).show();
            }
        }
    }
    public void requestPermission() {
        ActivityCompat.requestPermissions(this, new String[]{ Manifest.permission.ACCESS_FINE_LOCATION}, ACCESS_LOCATION_REQUEST_CODE);
    }

    public  void checklocationsettings(){
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(locationRequest);
        builder.setAlwaysShow(true);

        Task<LocationSettingsResponse> result =
                LocationServices.getSettingsClient(this).checkLocationSettings(builder.build());
        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response = task.getResult(ApiException.class);
                    // All location settings are satisfied. The client can initialize location
                    // requests here.
                    zoomToUserLocation();

                } catch (ApiException exception) {
                    switch (exception.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the
                            // user a dialog.
                            try {
                                // Cast to a resolvable exception.
                                ResolvableApiException resolvable = (ResolvableApiException) exception;
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                resolvable.startResolutionForResult(
                                        MapsActivity2.this,
                                        REQUEST_CHECK_SETTINGS);
                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            } catch (ClassCastException e) {
                                // Ignore, should be an impossible error.
                            }
                            break;

                    }
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made

                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        Toast.makeText(this ,"permission denied",Toast.LENGTH_SHORT).show();
                        break;
                    default:
                        break;
                }
                break;
        }
    }


    Map<String, Marker> mNamedMarkers = new HashMap<String,Marker>();

    ChildEventListener markerUpdateListener = new ChildEventListener() {

        /**
         * Adds each existing/new location of a marker.
         *
         * Will silently update any existing markers as needed.
         * @param dataSnapshot  The new location data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();
            String a;

            Log.d(TAG, "Adding location for '" + key + "'");

            Double lng = dataSnapshot.child("longitude").getValue(Double.class);
            Double lat = dataSnapshot.child("latitude").getValue(Double.class);
            LatLng location = new LatLng(lat, lng);



            pref = firebaseDatabase.getReference("Registered users");
            DatabaseReference titleref = pref.child("Drivers");
            DatabaseReference secref = titleref.child(key);
            secref.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Marker marker = mNamedMarkers.get(key);
                    String d = snapshot.child("name").getValue(String.class);
                    Log.d("jjh", d+"hi");
                    if (marker == null) {
                        MarkerOptions options = new MarkerOptions();
                        options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busic));
                        options.anchor((float) 0.5, (float) 0.5);
                        marker = mMap.addMarker(options.position(location));


                        marker.setTitle(d);


                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String streetAddress = address.getAddressLine(0);
                                marker.setSnippet(streetAddress);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        mNamedMarkers.put(key, marker);
                    } else {

                        marker.setPosition(location);

                        marker.setTitle(d);


                        try {
                            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                            if (addresses.size() > 0) {
                                Address address = addresses.get(0);
                                String streetAddress = address.getAddressLine(0);
                                marker.setSnippet(streetAddress);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });


        }

        /**
         * Updates the location of a previously loaded marker.
         *
         * Will silently create any missing markers as needed.
         * @param dataSnapshot  The new location data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String previousChildName) {
            String key = dataSnapshot.getKey();

            Log.d(TAG, "Location for '" + key + "' was updated.");


            Double lng = dataSnapshot.child("longitude").getValue(Double.class);
            Double lat = dataSnapshot.child("latitude").getValue(Double.class);

            LatLng location = new LatLng(lat, lng);

            Marker marker = mNamedMarkers.get(key);


            if (marker == null) {
                // This null-handling section should never be called in this listener's normal use, but is here to handle edge cases quietly.
                Log.d(TAG, "Expected existing marker for '" + key + "', but one was not found. Added now.");
                MarkerOptions options = new MarkerOptions();
                options.icon(BitmapDescriptorFactory.fromResource(R.drawable.busic));

                options.anchor((float) 0.5, (float) 0.5);




                try {
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (addresses.size() > 0) {
                        Address address = addresses.get(0);
                        String streetAddress = address.getAddressLine(0);
                        marker.setSnippet(streetAddress);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }

                marker = mMap.addMarker(options.position(location));



                mNamedMarkers.put(key, marker);
            } else {
                marker.setPosition(location);

            }
        }

        /**
         * Removes the marker from its GoogleMap instance
         * @param dataSnapshot  The removed data
         */
        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
            String key = dataSnapshot.getKey();
            Log.d(TAG, "Location for '" + key + "' was removed.");

            Marker marker = mNamedMarkers.get(key);
            if (marker != null)
                marker.remove();
        }

        /**
         * Ignored.
         * @param dataSnapshot  The moved data
         * @param previousChildName  The key of the previous child event
         */
        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String previousChildName) {
            // Unused
            Log.d(TAG, "Priority for '" + dataSnapshot.getKey() + " was changed.");
        }

        /**
         * Error handler when listener is canceled.
         * @param databaseError  The error object
         */
        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(TAG, "markerUpdateListener:onCancelled", databaseError.toException());

        }
    };
















}
