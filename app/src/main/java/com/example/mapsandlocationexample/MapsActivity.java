package com.example.mapsandlocationexample;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import android.annotation.SuppressLint;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;

public class MapsActivity extends FragmentActivity implements OnMapReadyCallback, PermissionManager.IPermissionManager {

    private GoogleMap mMap;
    EditText etTopView;
    private PermissionManager permissionManager;
    private LocationRequest locationRequest;
    private LocationCallback mLocationCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        permissionManager = new PermissionManager(this);
        permissionManager.checkPermission();
        etTopView = findViewById(R.id.etTopView);
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
        mMap = googleMap;


        // Add a marker in Sydney and move the camera
        LatLng sydney = new LatLng(-34, 151);
        mMap.addMarker(new MarkerOptions().position(sydney).title("Marker in Sydney"));
        //mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        //  mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
        //mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        //mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
        //mMap.setMapType(GoogleMap.MAP_TYPE_NONE);


        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney));
    }

    public void onClick(View view) {
        switch (view.getId()){
            case R.id.btnGoToOffice:
                double lat = 33.909112;
                double lng = -84.478994;

                LatLng latLng = new LatLng(lat, lng);
                mMap.addMarker(new MarkerOptions().position(latLng).title("Mobile Apps Company"));
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng,17));
                //mMap.setMinZoomPreference(15);
                break;
            case R.id.btnGoToAddress:
                String searchAddress = etTopView.getText().toString();
                goToAddress(searchAddress);
                // same as above
//               LatLng searchLatLng =  goToAddress(searchAddress);
//               mMap.addMarker(new MarkerOptions().position(searchLatLng).title(searchAddress.toUpperCase(Locale.getDefault())));
//               mMap.animateCamera(CameraUpdateFactory.newLatLng(searchLatLng));




        }

    }

    public void moveToLocation(Location location) {
       String thisAddress =  getAddress(location); // posts actual street address
        LatLng latLongOfLocation = new LatLng(location.getLatitude(), location.getLongitude());
        mMap.addMarker(new MarkerOptions().position(latLongOfLocation).title(thisAddress));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLongOfLocation,18));
        //mMap.setMinZoomPreference(17);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionResult(boolean isGranted) {
        if (isGranted) {
            getLastKnownLocation();
           // locationChangeFusedSetup();
        }
    }


    @SuppressLint("MissingPermission")
    public void getLastKnownLocation() { //gets last known location
        FusedLocationProviderClient fusedLocationProviderClient = new FusedLocationProviderClient(this);
        fusedLocationProviderClient.getLastLocation().addOnSuccessListener(new OnSuccessListener<Location>() {
            @Override
            public void onSuccess(Location location) {
                Log.d("TAG", "onSuccess: ");
                moveToLocation(location);
            }
        });
    }


    @SuppressLint("MissingPermission")
    public void locationChangeFusedSetup() {
        locationRequest = new LocationRequest();
        locationRequest.setInterval(3000);
        locationRequest.setMaxWaitTime(5000);
        locationRequest.setFastestInterval(4000);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(locationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        SettingsClient settingsClient = new SettingsClient(this);
        settingsClient.checkLocationSettings(locationSettingsRequest);

        getFusedLocationProviderClient(this).requestLocationUpdates(
                locationRequest,
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult locationResult) {
                        super.onLocationResult(locationResult);
                        onLocationChanged(locationResult.getLocations().get(0));
                    }
                },
                Looper.myLooper());

    }

    public String getAddress(Location location) { // gets physical address given longitude latitude
        Geocoder geocoder;
        List<Address> addresses = new ArrayList<>();
        geocoder = new Geocoder(this);

        try {
            addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
        } catch (IOException e) {
            e.printStackTrace();
        }
        String address = "";
        address += addresses.get(0).getAddressLine(0) + " "; // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
//        address += addresses.get(0).getLocality() + " ";
      //  address += addresses.get(0).getAdminArea() + " ";
//        address += addresses.get(0).getCountryName() + " ";
//        address += addresses.get(0).getPostalCode() + " ";
        Log.d("TAG",  "da address "+ address);
        return address;
    }

    public void goToAddress(String address) {
        Geocoder geocoder = new Geocoder(this);
        List<Address> addressList = new ArrayList<>();
        try {
            addressList = geocoder.getFromLocationName(address, 1);

        } catch (IOException e) {
            e.printStackTrace();
        }

        LatLng searchLatLng = new LatLng( addressList.get(0).getLatitude(),addressList.get(0).getLongitude());
        mMap.addMarker(new MarkerOptions().position(searchLatLng).title(address.toUpperCase(Locale.getDefault())));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(searchLatLng,17));
      ;

    }
        public void onLocationChanged(Location location) {
        moveToLocation(location);

    }
}

