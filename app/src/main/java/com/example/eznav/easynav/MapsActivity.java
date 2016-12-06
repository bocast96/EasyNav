package com.example.eznav.easynav;

import android.*;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.SearchView;
import android.util.Log;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MapsActivity extends FragmentActivity implements PopupMenu.OnMenuItemClickListener, OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final long LOCATION_REFRESH_TIME = 100;
    private static final float LOCATION_REFRESH_DISTANCE = .01f;
    private GoogleMap mMap;
    private double home_long;
    private double home_lat;
    private LatLng latLng, clickPos;
    private String addressText;
    private MarkerOptions markerOptions;
    private GoogleMap.OnMapClickListener onMapClickListener;
    private Marker searchPin;
    private Location myLocation;
    ArrayList<LatLng> MarkerPoints;
    private SearchView searchView;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);

        MarkerPoints = new ArrayList<>();


        //for search functionality
        searchView = (SearchView) findViewById(R.id.search_button);
        searchView.setIconifiedByDefault(false);
        searchView.clearFocus();
        searchView.setQueryHint("Enter location");
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                String g = query;

                Geocoder geocoder = new Geocoder(getBaseContext());
                List<Address> addresses = null;

                try {
                    // Getting a maximum of 3 Address that matches the input
                    // text
                    addresses = geocoder.getFromLocationName(g, 3);
                    if (addresses != null && !addresses.equals("")) {
                        search(addresses);
                    }
                    searchView.clearFocus();

                } catch (Exception e) {

                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (myLocation == null) {
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            String provider = locationManager.getBestProvider(crit, true);
            // Use the provider to get the last known location
            myLocation = locationManager.getLastKnownLocation(provider);
        }

        ImageButton directionsButton = (ImageButton) findViewById(R.id.imageButtonDir);
        directionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(MapsActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()), 13));

                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(myLocation.getLatitude(), myLocation.getLongitude()))      // Sets the center of the map to location user
                        .zoom(17)                   // Sets the zoom
                        .build();                   // Creates a CameraPosition from the builder
                mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                mMap.getUiSettings().setMyLocationButtonEnabled(false);
                //mMap.addMarker(myLocation);
                EditText etOrigin = (EditText) findViewById(R.id.etOrigin);
                findViewById(R.id.directionsGrid).setVisibility(View.VISIBLE);

                searchView.setVisibility(View.GONE);
                findViewById(R.id.imageButtonDir).setVisibility(View.GONE);
                findViewById(R.id.imageButtonReport).setVisibility(View.GONE);
                //etOrigin.setVisibility(View.VISIBLE);
                etOrigin.setText("Current Location");
                //findViewById(R.id.backButton).setVisibility(View.VISIBLE);
                //ViewFlipper vf = (ViewFlipper)findViewById(R.id.viewFlipper);
                //vf.showNext();
            }
        });

        ImageButton backButton = (ImageButton)findViewById(R.id.backButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mMap.getUiSettings().setMyLocationButtonEnabled(true);
                //mMap.addMarker(myLocation);

                findViewById(R.id.directionsGrid).setVisibility(View.GONE);

                searchView.setVisibility(View.VISIBLE);
                findViewById(R.id.imageButtonDir).setVisibility(View.VISIBLE);
                findViewById(R.id.imageButtonReport).setVisibility(View.VISIBLE);
                //etOrigin.setVisibility(View.VISIBLE);
            }
        });



        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();

    }

    /*
    public void onBackPressed(){
        // do something here and don't write super.onBackPressed()
    }*/

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
        Log.i("TEST", "TEST");
        mMap = googleMap;

        onMapClickListener = new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                mMap.addMarker(new MarkerOptions().position(latLng).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
                mMap.setOnMapClickListener(null);
            }
        };

        OnMapLongClickListener mapLongClickListener = new OnMapLongClickListener() {
            @Override
            public void onMapLongClick(LatLng point) {
                Log.i("TEST2","TEST2");
                clickPos = point;
                longClickReport();
            }
        };

        mMap.setOnMapLongClickListener(mapLongClickListener);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);

        // Add a marker in Sydney and move the camera
        //LatLng sydney = new LatLng(-34, 151);
        //mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));


        LatLng collegePark = new LatLng(38.9907439, -76.9362396);
        LatLng avwilliams_bike_rack_front = new LatLng(38.9906623, -76.9364820);
        LatLng avwilliams_bike_rack_side = new LatLng(38.9901492, -76.9365354);
        mMap.addMarker(new MarkerOptions().position(collegePark).title("Marker in CP"));

        mMap.addMarker(new MarkerOptions().position(avwilliams_bike_rack_front).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        mMap.addMarker(new MarkerOptions().position(avwilliams_bike_rack_side).icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
        //mMap.addMarker(new MarkerOptions().position(avwilliams_bike_rack_side).icon(BitmapDescriptorFactory.fromResource(R.drawable.bikerackpin)));

        //mMap.moveCamera(CameraUpdateFactory.newLatLng(collegePark));
        float zoomLevel = 17.0f; //This goes up to 21
        //mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(collegePark, zoomLevel));
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Criteria criteria = new Criteria();

        Location location = locationManager.getLastKnownLocation(locationManager.getBestProvider(criteria, false));
        if (location == null){
            Criteria crit = new Criteria();
            crit.setAccuracy(Criteria.ACCURACY_COARSE);
            // Finds a provider that matches the criteria
            String provider = locationManager.getBestProvider(crit, true);
            // Use the provider to get the last known location
            location = locationManager.getLastKnownLocation(provider);
        }
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(location.getLatitude(), location.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(location.getLatitude(), location.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    protected void search(List<Address> addresses) {

        if(searchPin != null){
            searchPin.remove();
        }

        Address address = addresses.get(0);
        home_long = address.getLongitude();
        home_lat = address.getLatitude();
        latLng = new LatLng(address.getLatitude(), address.getLongitude());

        addressText = String.format(
                "%s, %s",
                address.getMaxAddressLineIndex() > 0 ? address
                        .getAddressLine(0) : "", address.getCountryName());

        markerOptions = new MarkerOptions();

        markerOptions.position(latLng);
        markerOptions.title(addressText);

        //mMap.clear();
        searchPin =  mMap.addMarker(markerOptions);
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(address.getLatitude(), address.getLongitude()), 13));

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(new LatLng(address.getLatitude(), address.getLongitude()))      // Sets the center of the map to location user
                .zoom(17)                   // Sets the zoom
                //.bearing(90)                // Sets the orientation of the camera to east
                //.tilt(40)                   // Sets the tilt of the camera to 30 degrees
                .build();                   // Creates a CameraPosition from the builder
        mMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
        //mMap.animateCamera(CameraUpdateFactory.zoomTo(17));
        //locationTv.setText("Latitude:" + address.getLatitude() + ", Longitude:"
        //       + address.getLongitude());


    }

    private void longClickReport() {
        View v = findViewById(R.id.imageButtonReport);
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    public void onButtonClickDirections(View v) {

    }

    public void onButtonClickReport(View v) {
        PopupMenu popupMenu = new PopupMenu(this, v);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.popup_menu);
        popupMenu.show();
    }

    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuGap:
                // TODO: 12/4/2016 set icon
                break;
            case R.id.menuConstruct:
                // TODO: 12/4/2016 set icon
                break;
            case R.id.menuEntrance:
                // TODO: 12/4/2016 set icon
                break;
            case R.id.menuBike:
                // TODO: 12/4/2016 set icon
                break;
        }

        if (clickPos == null) {
            Toast.makeText(this, "Tap to Drop Pin", Toast.LENGTH_SHORT).show();
            mMap.setOnMapClickListener(onMapClickListener);
        } else {
            mMap.addMarker(new MarkerOptions().position(clickPos).title("Custom location").icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED)));
            clickPos = null;
        }

        return true;
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.eznav.easynav/http/host/path")
        );
        AppIndex.AppIndexApi.start(client, viewAction);
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        Action viewAction = Action.newAction(
                Action.TYPE_VIEW, // TODO: choose an action type.
                "Maps Page", // TODO: Define a title for the content shown.
                // TODO: If you have web page content that matches this app activity's content,
                // make sure this auto-generated web page URL is correct.
                // Otherwise, set the URL to null.
                Uri.parse("http://host/path"),
                // TODO: Make sure this auto-generated app URL is correct.
                Uri.parse("android-app://com.example.eznav.easynav/http/host/path")
        );
        AppIndex.AppIndexApi.end(client, viewAction);
        client.disconnect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }
}
