package com.apex.icrf;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.util.Log;
import android.util.SparseArray;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.classes.Country;
import com.apex.icrf.classes.CountryAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.PermissionUtils;
import com.apex.icrf.utils.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.apex.icrf.MainPostPetitionMapsFragment.REQUEST_CHECK_SETTINGS;

/**
 * Created by WASPVamsi on 02/01/16.
 */
public class LoginActivity extends AppCompatActivity implements PermissionUtils.PermissionResultCallback, ActivityCompat.OnRequestPermissionsResultCallback, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private String forgotPassword = "<u>Forgot Password?</u>";
    private String cancel = "<u>Cancel</u>";

    private static final int SMS = 0;
    private static final int EMAIL = 1;

    private final static int PLAY_SERVICES_REQUEST = 1000;

    //Toolbar toolbar;
    //TextView title, icrf_login_label;
    TextView txtForgotPassword, txtCancel;
    EditText edtxtMobileNo, edtxtPassword;
    Button btnLogin, btnRequestPassword, btnRegister;
    RadioGroup radioGroup;
    RadioButton rbSms, rbEmail;
    ProgressDialog progressDialog;

    Spinner mSpinner;
    LocationListener locationListenerGPS;
    CountryAdapter mCountryAdapter;

    SharedPreferences prefs;
    Profile mProfile;

    protected SparseArray<ArrayList<Country>> mCountriesMap = new SparseArray<ArrayList<Country>>();
    public String current_country = "IN";

    Typeface font_roboto_thin;
    double latitude;
    double longitude;
    Address locationAddress;
    gpsTracker gps;
    int MAPPERMISSION = 012;
    PermissionUtils permissionUtils;
    ArrayList<String> permissions = new ArrayList<>();
    Location mLastLocation;

    // Google client to interact with Google API

    private GoogleApiClient mGoogleApiClient;

    boolean isPermissionGranted = false;


    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        gps = new gpsTracker(LoginActivity.this);

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }

            if (Build.VERSION.SDK_INT >= 22) {
                if(!(Build.VERSION.SDK_INT == 22)) {
                    getPermission();
                }
            }

        permissionUtils = new PermissionUtils(LoginActivity.this);
       /* try {
            Location();
        } catch (Exception e) {
            e.printStackTrace();
        }*/

        font_roboto_thin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");
        //icrf_login_label = (TextView) findViewById(R.id.login_icrf_label);


        txtForgotPassword = (TextView) findViewById(R.id.login_textview_forgot_password);
        txtForgotPassword.setText(Html.fromHtml(forgotPassword));

        txtCancel = (TextView) findViewById(R.id.login_textview_cancel);
        txtCancel.setText(Html.fromHtml(cancel));
        txtCancel.setVisibility(View.GONE);

        edtxtMobileNo = (EditText) findViewById(R.id.login_edittext_mobile_number);
        edtxtPassword = (EditText) findViewById(R.id.login_edittext_password);

        btnLogin = (Button) findViewById(R.id.login_button);
        btnLogin.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                validateLogin();
            }
        });

        btnRegister = (Button) findViewById(R.id.register_button);
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
                // removing OTP
                startActivity(new Intent(LoginActivity.this, RegistrationActivity.class));
            }
        });

        btnRequestPassword = (Button) findViewById(R.id.login_request_password);
        btnRequestPassword.setVisibility(View.GONE);
        btnRequestPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                submitRequest();
            }
        });

        radioGroup = (RadioGroup) findViewById(R.id.radioGroup);

        txtForgotPassword.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (edtxtPassword.getVisibility() == View.VISIBLE)
                    edtxtPassword.setVisibility(View.GONE);

                if (btnLogin.getVisibility() == View.VISIBLE)
                    btnLogin.setVisibility(View.GONE);

                if (btnRegister.getVisibility() == View.VISIBLE)
                    btnRegister.setVisibility(View.GONE);


                if (btnRequestPassword.getVisibility() == View.GONE)
                    btnRequestPassword.setVisibility(View.VISIBLE);

                if (radioGroup.getVisibility() == View.GONE)
                    radioGroup.setVisibility(View.VISIBLE);

                v.setVisibility(View.GONE);

                if (txtCancel.getVisibility() == View.GONE)
                    txtCancel.setVisibility(View.VISIBLE);
            }
        });

        txtCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (edtxtPassword.getVisibility() == View.GONE)
                    edtxtPassword.setVisibility(View.VISIBLE);

                if (btnLogin.getVisibility() == View.GONE)
                    btnLogin.setVisibility(View.VISIBLE);

                if (btnRegister.getVisibility() == View.GONE)
                    btnRegister.setVisibility(View.VISIBLE);

                if (btnRequestPassword.getVisibility() == View.VISIBLE)
                    btnRequestPassword.setVisibility(View.GONE);

                if (radioGroup.getVisibility() == View.VISIBLE)
                    radioGroup.setVisibility(View.GONE);

                v.setVisibility(View.GONE);

                if (txtForgotPassword.getVisibility() == View.GONE)
                    txtForgotPassword.setVisibility(View.VISIBLE);

            }
        });


        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("current_country", current_country).apply();

        mProfile = new Profile(this);

        mSpinner = (Spinner) findViewById(R.id.spinner);

        final ArrayList<Country> data = new ArrayList<Country>();
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(this.getApplicationContext().getAssets().open("countries.dat"), "UTF-8"));

            // do reading, usually loop until end of file reading
            String line;
            int i = 0;
            while ((line = reader.readLine()) != null) {

                Country c = new Country(this, line, i);
                data.add(c);
                ArrayList<Country> list = mCountriesMap.get(c.getCountryCode());
                if (list == null) {
                    list = new ArrayList<Country>();
                    mCountriesMap.put(c.getCountryCode(), list);
                }
                list.add(c);
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception while reading countries.dat");
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();

                    if (Const.DEBUGGING)
                        Log.d(Const.DEBUG, "Exception while closing BufferReader");
                }
            }
        }

        mCountryAdapter = new CountryAdapter(this, data);
        mSpinner.setAdapter(mCountryAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Country country = data.get(position);
                current_country = country.getCountryISO().toLowerCase(Locale.ENGLISH);
                prefs.edit().putString("current_country", current_country).apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        //checkPlayServices();

    }

    protected void validateLogin() {


        int errorCode = -1;

        errorCode = checkInput();

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Error Code: " + errorCode);

        if (errorCode == 1) {
            Toast.makeText(this, "Mobile Number cannot be Empty", Toast.LENGTH_SHORT)
                    .show();
        } else if (errorCode == 2) {
            Toast.makeText(this, "Password cannot be Empty", Toast.LENGTH_SHORT)
                    .show();
        } else if (errorCode == 3) {
            Toast.makeText(this, "Mobile Number should be a valid 10 digit number", Toast.LENGTH_SHORT).show();
        } else if (errorCode == -1) {

            showProgressDialog("Authenticating...");
            performLogin();
        }

    }

    private int checkInput() {

        if (edtxtMobileNo.getText().toString().equalsIgnoreCase(""))
            return 1;
        else if (current_country.equalsIgnoreCase("IN") && edtxtMobileNo.length() < 10)
            return 3;
        else if (edtxtPassword.getText().toString().equalsIgnoreCase(""))
            return 2;
        else
            return -1;
    }

    private void performLogin() {

        String username = edtxtMobileNo.getText().toString();
        String password = edtxtPassword.getText().toString();

        String url = Const.FINAL_URL + Const.URLs.LOGIN_DETAILS;
        url = url + "userid=" + current_country + "-" + username;
        url = url + "&";
        url = url + "pwd=" + password;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        if (response.length() == 0) {
                            Toast.makeText(LoginActivity.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                        } else {
                            parseResponse(response);
                        }


                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Cannot login at this point in time. Please try again after sometime.",
                                    Toast.LENGTH_LONG).show();
                        }

                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    private void parseResponse(JSONArray response) {

        dismissProgressDialog();


        try {

            JSONObject jsonObject = response.getJSONObject(0);

            JSONArray loginDetailsArray = jsonObject.getJSONArray("Login_Details");

            if (loginDetailsArray.length() > 0) {

                JSONObject loginDetailsObject = loginDetailsArray.getJSONObject(0);

                String isValidResponse = loginDetailsObject.getString("responce");

                if (isValidResponse.equalsIgnoreCase("success")) {


                    if (loginDetailsObject.getString("country").equalsIgnoreCase(current_country)) {

                        String member_id = loginDetailsObject.getString("memberid");
                        String memberid_type = loginDetailsObject.getString("memberid_type");
                        String name = loginDetailsObject.getString("name");
                        String user_id = loginDetailsObject.getString("uname");
                        String mobile = loginDetailsObject.getString("mobile");
                        String email = loginDetailsObject.getString("email");
                        String ID_Type = loginDetailsObject.getString("ID Type");
                        String  Valid_Upto = loginDetailsObject.getString("Valid Upto");
                        prefs.edit().putString("ID_Type", ID_Type).apply();
                        prefs.edit().putString("Valid_Upto", Valid_Upto).apply();
                        JSONArray profileDetailsArray = jsonObject.getJSONArray("Profile_Details");
                        JSONObject profileDetailsObject = profileDetailsArray.getJSONObject(0);

                        String profile_image = profileDetailsObject.getString("profile_image");


                        Bundle login_bundle = new Bundle();
                        login_bundle.putString("member_id", member_id);
                        login_bundle.putString("name", name);
                        login_bundle.putString("user_id", user_id);
                        login_bundle.putString("mobile", mobile);
                        login_bundle.putString("email", email);
                        login_bundle.putString("memberid_type", memberid_type);
                        login_bundle.putString("profile_image", profile_image);

                        mProfile.setPreferences(member_id, name, user_id, mobile, email, memberid_type, profile_image);

                        //mProfile.setPreferences(member_id, name, user_id, mobile, email, memberid_type, profile_image);

//                        Bundle bundle = getIntent().getExtras();
//                        if (bundle != null && bundle.getBoolean("from_verify_petition"))
//                            startActivity(new Intent(this, VerifyPetitionActivity.class).putExtra("from_login", true));
//                        else
//                            startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                        try {
                            prefs.edit().putString("mobile", mobile).apply();
                        }catch (Exception e){
                            e.printStackTrace();
                        }


                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null && bundle.getBoolean("from_verify_petition")) {
                            //startActivity(new Intent(this, VerifyPetitionActivity.class).putExtra("from_login", true));
                            login_bundle.putBoolean("from_login", true);
                        } else {
                            login_bundle.putBoolean("from_login", false);
                            //startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                        }

                        // skipping OTP Activity
                        //startActivity(new Intent(LoginActivity.this, OTPActivity.class).putExtra("login_bundle", login_bundle));

                        // sending to introduction page after login success


                        startActivity(new Intent(LoginActivity.this, IntroductionActivity.class));


                        LoginActivity.this.finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
                    }

                }
            } else {
                Toast.makeText(LoginActivity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        // service.removeUpdates(LoginActivity.this);
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void submitRequest() {

        int id = radioGroup.getCheckedRadioButtonId();

        String mobile = edtxtMobileNo.getText().toString();
        if (mobile.length() == 0) {
            Toast.makeText(LoginActivity.this, "Mobile number cannot be empty", Toast.LENGTH_LONG).show();
        } else {

            if (current_country.equalsIgnoreCase("IN")) {

                if (mobile.length() == 10) {
                    if (id == R.id.radioButton_sms)
                        sendRequest(mobile, SMS);
                    else if (id == R.id.radioButton_email)
                        sendRequest(mobile, EMAIL);
                } else {
                    Toast.makeText(LoginActivity.this, "Please enter a valid Mobile number", Toast.LENGTH_LONG).show();
                }
            } else {

                if (id == R.id.radioButton_sms)
                    sendRequest(mobile, SMS);
                else if (id == R.id.radioButton_email)
                    sendRequest(mobile, EMAIL);
            }


        }

    }

    private void sendRequest(String value, int type) {

        showProgressDialog("Sending Request...");

        String url = Const.FINAL_URL + Const.URLs.FORGOT_PASSWORD;
        url = url + "userid=" + current_country + "-" + value;

        if (type == SMS)
            url = url + "&mode_of_sending=SMS";
        else if (type == EMAIL)
            url = url + "&mode_of_sending=EMAIL";


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        dismissProgressDialog();
                        try {
                            Toast.makeText(getApplicationContext(), "" + response.getString("status"), Toast.LENGTH_SHORT).show();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        dismissProgressDialog();
                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    LoginActivity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);

    }

    private void parseArrayResponse(JSONObject response) {

        try {

            String strResponse = response.getString("responce");
            String status = response.getString("status");

            Toast.makeText(LoginActivity.this, status, Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Cannot parse Forgot Password response");
        }
    }

    private void dismissProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showProgressDialog(String message) {

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }

    public Address getAddress(double latitude, double longitude) {
        Geocoder geocoder;
        List<Address> addresses;
        geocoder = new Geocoder(this, Locale.getDefault());

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses.get(0);

        } catch (IOException e) {
            e.printStackTrace();
        }

        return null;

    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    void getPermission() {
        String[] strings = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
        requestPermissions(strings, MAPPERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        permissionUtils.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MAPPERMISSION && grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                // Toast.makeText(this, "map read permission granted", Toast.LENGTH_LONG).show();
                if(gps.canGetLocation) {
                    mLastLocation = gps.getLocation();
                }
                Log.d("location",""+mLastLocation);
            } else {
                Toast.makeText(this, "map read permission not granted", Toast.LENGTH_LONG).show();
            }
        }
    }


    public void postLocation(final String mobile, final double latitude, final double longitude, String location) {
        String url;
        url = "http://www.icrf.org.in/apex_icrf_android_api.asmx/GetAppLocation?Mobile=" + mobile + "&Latitude=" + latitude + "&Longitude=" + longitude + "&Addr=" +location;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        dismissProgressDialog();
                        try {
                            String errorMessage = error.getClass().toString();
                            if (errorMessage
                                    .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                                Toast.makeText(
                                        LoginActivity.this,
                                        "Cannot detect active internet connection. "
                                                + "Please check your network connection.",
                                        Toast.LENGTH_LONG).show();
                            }
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }
                });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);
    }

    @Override
    public void PermissionGranted(int request_code) {
        Log.i("PERMISSION", "GRANTED");
        isPermissionGranted = true;
    }

    @Override
    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
        Log.i("PERMISSION PARTIALLY", "GRANTED");
    }

    @Override
    public void PermissionDenied(int request_code) {
        Log.i("PERMISSION", "DENIED");
    }

    @Override
    public void NeverAskAgain(int request_code) {

    }

    private boolean checkPlayServices() {

        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int resultCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(this, resultCode,
                        PLAY_SERVICES_REQUEST).show();
            } else {
                Toast.makeText(getApplicationContext(),
                        "This device is not supported.", Toast.LENGTH_LONG)
                        .show();
                finish();
            }
            return false;
        }
        return true;
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API).build();

        mGoogleApiClient.connect();

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(10000);
        mLocationRequest.setFastestInterval(5000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest);

        PendingResult<LocationSettingsResult> result =
                LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient, builder.build());

        result.setResultCallback(new ResultCallback<LocationSettingsResult>() {
            @Override
            public void onResult(LocationSettingsResult locationSettingsResult) {

                final Status status = locationSettingsResult.getStatus();

                switch (status.getStatusCode()) {
                    case LocationSettingsStatusCodes.SUCCESS:
                        // All location settings are satisfied. The client can initialize location requests here
                        if(gps.canGetLocation) {
                            mLastLocation = gps.getLocation();
                        }
                        // Toast.makeText(getApplicationContext(),""+ mLastLocation, Toast.LENGTH_SHORT).show();
                        break;
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            status.startResolutionForResult(LoginActivity.this, REQUEST_CHECK_SETTINGS);

                        } catch (IntentSender.SendIntentException e) {
                            // Ignore the error.
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        break;
                }
            }
        });


    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        try {
            mLastLocation = gps.getLocation();
            //Toast.makeText(getApplicationContext(), ""+mLastLocation+"api succcess", Toast.LENGTH_SHORT).show();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onConnectionSuspended(int i) {
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void Location() {
        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();
            try {
                locationAddress = getAddress(latitude, longitude);
            } catch (Exception e) {
                e.printStackTrace();
            }
            // \n is for new line
            //  Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
        } else {
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            //gps.showSettingsAlert();
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);
        }


        if (checkPlayServices()) {
            // Building the GoogleApi client
            buildGoogleApiClient();
        }
    }

    private void getLocation() {
        try {
            //  Toast.makeText(getApplicationContext(), "GPS on", Toast.LENGTH_SHORT).show();
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
            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                    mGoogleApiClient);
            Log.d("location",""+mLastLocation);
            if (mLastLocation != null) {
                // Toast.makeText(getApplicationContext(),"GPS on",Toast.LENGTH_SHORT).show();
                latitude = mLastLocation.getLatitude();
                longitude = mLastLocation.getLongitude();
            }else{
                // Toast.makeText(getApplicationContext(),"GPS off",Toast.LENGTH_SHORT).show();
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        //  mGoogleApiClient.connect();
    }
}
