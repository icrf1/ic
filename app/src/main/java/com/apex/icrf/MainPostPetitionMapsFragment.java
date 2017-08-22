package com.apex.icrf;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.apex.icrf.classes.IMainPostPetitionMapsListener;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStates;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceSelectionListener;
import com.google.android.gms.location.places.ui.SupportPlaceAutocompleteFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;
import java.util.Locale;

/**
 * Created by WASPVamsi on 15/05/16.
 */
public class MainPostPetitionMapsFragment extends Fragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener, LocationListener {

    Activity activity;
    private GoogleMap mMap;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation, mLastLocation;
    private LocationRequest mLocationRequest;

    private double mLatitude = 0.0, mLongitude = 0.0;
    private double mFinalLatitude = 0.0, mFinalLongitude = 0.0;

    private ImageView imageviewCurrentLocation;
    private Button buttonContinue;
    private boolean isLocationSelected = false;
    private boolean isSearchSelected = false;

    TextView textViewLocateArea, textViewCurrentLocation, textViewAddress;

    private IMainPostPetitionMapsListener mIMainPostPetitionMapsListener;

    Typeface font_robotoslab_bold, font_robotoslab_regular, font_robot_regular,
            font_roboto_light, font_roboto_medium, font_roboto_bold, font_roboto_thin;

    protected static final int REQUEST_CHECK_SETTINGS = 0x1;

    public static final int MAPPERMISSION = 0001;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;


        if (activity instanceof IMainPostPetitionMapsListener) {
            mIMainPostPetitionMapsListener = (IMainPostPetitionMapsListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }

        font_robotoslab_bold = Typeface.createFromAsset(activity.getAssets(),
                "fonts/RobotoSlab-Bold.ttf");

        font_robotoslab_regular = Typeface.createFromAsset(activity.getAssets(),
                "fonts/RobotoSlab-Regular.ttf");

        font_robot_regular = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Regular.ttf");

        font_roboto_light = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Light.ttf");

        font_roboto_medium = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Medium.ttf");

        font_roboto_bold = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Bold.ttf");

        font_roboto_thin = Typeface.createFromAsset(activity.getAssets(),
                "fonts/Roboto-Thin.ttf");

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {
                            startLocationUpdates();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();

            createLocationRequest();

            LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest);

            PendingResult<LocationSettingsResult> result =
                    LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

            result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
                @Override
                public void onResult(LocationSettingsResult locationSettingsResult) {

                    final Status status = locationSettingsResult.getStatus();
                    final LocationSettingsStates LS_state = locationSettingsResult.getLocationSettingsStates();
                    switch (status.getStatusCode()) {
                        case LocationSettingsStatusCodes.SUCCESS:
                            // All location settings are satisfied. The client can initialize location
                            // requests here.
                            startLocationUpdates();
                            break;
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            // Location settings are not satisfied. But could be fixed by showing the user
                            // a dialog.
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                status.startResolutionForResult(activity, REQUEST_CHECK_SETTINGS);

                            } catch (IntentSender.SendIntentException e) {
                                // Ignore the error.
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            // Location settings are not satisfied. However, we have no way to fix the
                            // settings so we won't show the dialog.

                            break;
                    }
                }
            });


        }
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        final LocationSettingsStates states = LocationSettingsStates.fromIntent(data);
        switch (requestCode) {
            case REQUEST_CHECK_SETTINGS:
                switch (resultCode) {
                    case Activity.RESULT_OK:
                        // All required changes were successfully made
                        //GetUserLocation();//FINALLY YOUR OWN METHOD TO GET YOUR USER LOCATION HERE
                        startLocationUpdates();
                        break;
                    case Activity.RESULT_CANCELED:
                        // The user was asked to change settings, but chose not to
                        break;
                    default:
                        break;
                }


                break;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_post_petition_maps, container, false);

        textViewLocateArea = (TextView) view.findViewById(R.id.textView_locate_area);
        textViewLocateArea.setTypeface(font_robot_regular);

        final SupportPlaceAutocompleteFragment supportPlaceAutocompleteFragment = (SupportPlaceAutocompleteFragment)
                this.getChildFragmentManager().findFragmentById(R.id.place_autocomplete_fragment);

        textViewCurrentLocation = (TextView) view.findViewById(R.id.textView_current_location);
        textViewLocateArea.setTypeface(font_robot_regular);

        textViewAddress = (TextView) view.findViewById(R.id.textView_address);
        textViewLocateArea.setTypeface(font_robot_regular);

        imageviewCurrentLocation = (ImageView) view.findViewById(R.id.imageview_current_location);
        imageviewCurrentLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                startLocationUpdates();

                addMarker(new LatLng(mLatitude, mLongitude));
                getAddress(new LatLng(mLatitude, mLongitude));

                mFinalLatitude = mLatitude;
                mFinalLongitude = mLongitude;
            }
        });

        buttonContinue = (Button) view.findViewById(R.id.button_continue);
        buttonContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isLocationSelected) {
                    Bundle bundle = new Bundle();
                    bundle.putString("latitude", String.valueOf(mFinalLatitude));
                    bundle.putString("longitude", String.valueOf(mFinalLongitude));
                    mIMainPostPetitionMapsListener.onContinueClicked(bundle);
                } else {
                    Toast.makeText(activity, "Please locate area related to the petition", Toast.LENGTH_LONG).show();
                }
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);


        supportPlaceAutocompleteFragment.setOnPlaceSelectedListener(new PlaceSelectionListener() {
            @Override
            public void onPlaceSelected(Place place) {

                if (place != null) {

                    isSearchSelected = true;
                    isLocationSelected = true;

                    mFinalLatitude = place.getLatLng().latitude;
                    mFinalLongitude = place.getLatLng().longitude;

                    addMarker(new LatLng(mFinalLatitude, mFinalLongitude));
                    getAddress(new LatLng(mFinalLatitude, mFinalLongitude));
                }
            }

            @Override
            public void onError(Status status) {

            }
        });

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            getMapPermission();
        }



        return view;
    }


    private void addMarker(LatLng latLng) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "addMarker()");

        mFinalLatitude = latLng.latitude;
        mFinalLongitude = latLng.longitude;

        mMap.clear();
        mMap.addMarker(new MarkerOptions().position(latLng));
    }

    private void getAddress(LatLng latLng) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "getAddress()");

        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(activity, Locale.getDefault());

        try {

            addresses = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1);

            String address = null;

            for (int i = 0; i < addresses.get(0).getMaxAddressLineIndex(); i++) {

                if (i == 0)
                    address = addresses.get(0).getAddressLine(i);
                else
                    address = address + addresses.get(0).getAddressLine(i);

                if (i != addresses.get(0).getMaxAddressLineIndex() - 1) {
                    address = address + ", ";
                }
            }

            String city = addresses.get(0).getLocality();
            String state = addresses.get(0).getAdminArea();
            String country = addresses.get(0).getCountryName();
            String postalCode = addresses.get(0).getPostalCode();
            String knownName = addresses.get(0).getFeatureName();

            if (Const.DEBUGGING) {
                Log.d(Const.DEBUG, "Address: " + address);
                Log.d(Const.DEBUG, "City: " + city);
                Log.d(Const.DEBUG, "State: " + state);
                Log.d(Const.DEBUG, "Country: " + country);
                Log.d(Const.DEBUG, "Postal Code: " + postalCode);
                Log.d(Const.DEBUG, "Known Name: " + knownName);
            }

            textViewAddress.setText("Address: " + address);
        } catch (Exception e) {
            e.printStackTrace();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Unable to get Location Details");
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "onMapClick()");

        addMarker(latLng);
        getAddress(latLng);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "onMapReady()");

        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setAllGesturesEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
            mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
            mMap.getUiSettings().setMapToolbarEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);
            mMap.getUiSettings().setRotateGesturesEnabled(true);

            mMap.getUiSettings().setScrollGesturesEnabled(true);
            mMap.getUiSettings().setTiltGesturesEnabled(true);
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setZoomGesturesEnabled(true);

            mMap.setOnMapClickListener(this);
        }
        return;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }
    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0);
    }

    protected void stopLocationUpdates() {

        if (mGoogleApiClient.isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
    }


    protected void getMapPermission() {
        String[] permissions = new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        requestPermissions(permissions, MAPPERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MAPPERMISSION) {
            if (grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                //Toast.makeText(getActivity(), "Maps permission granted successfully", Toast.LENGTH_SHORT).show();
            } else {
                //Toast.makeText(getActivity(), "Please give permission manually", Toast.LENGTH_SHORT).show();
            }
        }
    }


    protected void startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //  ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
//               public void onRequestPermissionsResult(int requestCode, String[] permissions,
//                                                      int[] grantResults)
            //to handle the case where the user grants the permission. See the documentation
            //for ActivityCompat#requestPermissions for more details.
            getMapPermission();
        }
        else
        {

            LocationServices.FusedLocationApi.requestLocationUpdates(
                    mGoogleApiClient, mLocationRequest, this);
        }


    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "onLocationChanged()");

        mCurrentLocation = location;
        mLatitude = mCurrentLocation.getLatitude();
        mLongitude = mCurrentLocation.getLongitude();

        //mFinalLatitude = mCurrentLocation.getLatitude();
        //mFinalLongitude = mCurrentLocation.getLongitude();

        isLocationSelected = true;

        stopLocationUpdates();

        if (!isSearchSelected) {
            animateCamera();
        } else {
            animateToSearchArea();
        }
    }

    public void animateCamera() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "animateCamera()");

        LatLng marker = new LatLng(mLatitude, mLongitude);
        mMap.addMarker(new MarkerOptions().position(marker).title("Location"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));

        getAddress(marker);
    }

    public void animateToSearchArea() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "animateToSearchArea()");

        LatLng marker = new LatLng(mFinalLatitude, mFinalLongitude);
        mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));

        isSearchSelected = false;
    }
}
