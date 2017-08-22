package com.apex.icrf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by WASPVamsi on 27/04/16.
 */
public class RegistrationFinalStep3Activity extends AppCompatActivity {

    Toolbar toolbar;
    TextView title;

    SharedPreferences prefs;
    Profile mProfile;

    EditText editTextLandmark, editTextStreet, editTextDoorNo, editTextReferral;
    String landmark, street, door, referral;
    String country, phone, email, name, gender, dob, pincode, state, district, city;
    String referral_name, referral_memberid = "0", referral_memberid_type = "I";

    CheckBox checkBox;
    ProgressDialog progressDialog;
    String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_final_step_3);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        editTextLandmark = (EditText) findViewById(R.id.edittext_registration_final_step_3_landmark);
        editTextStreet = (EditText) findViewById(R.id.edittext_registration_final_step_3_street);
        editTextDoorNo = (EditText) findViewById(R.id.edittext_registration_final_step_3_door);
        editTextReferral = (EditText) findViewById(R.id.edittext_registration_final_step_3_referral);

        checkBox = (CheckBox) findViewById(R.id.checkBox);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            country = bundle.getString("country");
            phone = bundle.getString("phone");
            email = bundle.getString("email");
            name = bundle.getString("name");
            gender = bundle.getString("gender");
            dob = bundle.getString("dob");
            pincode = bundle.getString("pincode");
            state = bundle.getString("state");
            district = bundle.getString("district");
            city = bundle.getString("city");
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mProfile = new Profile(this);

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (toolbar != null && title != null) {
            title.setText(getResources().getString(R.string.title_activity_registration_final_step_3));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_registration_final_step_3, menu);

        MenuItem item = menu.findItem(R.id.action_next);
        if (item != null) {
            MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (checkBox.isChecked()) {
                        validateInput();
                    } else {
                        Toast.makeText(RegistrationFinalStep3Activity.this, "Please accept the terms and conditions", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    public void validateInput() {

        landmark = editTextLandmark.getText().toString();
        street = editTextStreet.getText().toString();
        door = editTextDoorNo.getText().toString();
        referral = editTextReferral.getText().toString();

        if (door.length() == 0) {
            Toast.makeText(RegistrationFinalStep3Activity.this, "Door No. / H.No. cannot be empty", Toast.LENGTH_LONG).show();
        } else {

            if (referral.length() > 0) {
                validateReferral(referral);
            } else {
                performRegistration();
            }
        }

    }

    public void validateReferral(String referral) {

        showProgressDialog("Validating...");

        String url = Const.FINAL_URL + Const.URLs.CHECK_REFERRAL_ID;
        url = url + "mobile_no=" + referral;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);


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
                        parseResponse(response);
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
                                    RegistrationFinalStep3Activity.this,
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

    public void performRegistration() {

        /*
        userid:
        emailid:
        reffmmeid:
        reffmmeid_type:
        name:
        gender:
        dob:
        hno:
        landmark:
        street_or_village:
        city_or_village:
        dist:
        state:
        pincode:
        */

        showProgressDialog("Registering...");

        try {

            String url = Const.FINAL_URL + Const.URLs.NEW_REGISTRATION;
            url = url + "userid=" + phone;
            url = url + "&emailid=" + email;
            url = url + "&reffmmeid=" + referral_memberid;
            url = url + "&reffmmeid_type=" + referral_memberid_type;
            url = url + "&name=" + URLEncoder.encode(name, "UTF-8");

            if (gender.equalsIgnoreCase("male"))
                url = url + "&gender=M";
            else
                url = url + "&gender=F";

            url = url + "&dob=" + URLEncoder.encode(dob, "UTF-8");
            url = url + "&hno=" + URLEncoder.encode(door, "UTF-8");
            url = url + "&landmark=" + URLEncoder.encode(landmark, "UTF-8");
            url = url + "&street_or_village=" + URLEncoder.encode(street, "UTF-8");
            url = url + "&city_or_village=" + URLEncoder.encode(city, "UTF-8");
            url = url + "&dist=" + URLEncoder.encode(district, "UTF-8");
            url = url + "&state=" + URLEncoder.encode(state, "UTF-8");
            url = url + "&pincode=" + Integer.parseInt(pincode);

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Url = " + url);


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
                            parseRegistrationResponse(response);
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
                                        RegistrationFinalStep3Activity.this,
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

        } catch (Exception e) {
            e.printStackTrace();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception while registering");
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

    private void parseResponse(JSONObject response) {

        if (response != null) {

            try {

                if (response.getString("responce").equalsIgnoreCase("success")) {

                    referral_name = response.getString("ref_name");
                    referral_memberid = response.getString("ref_memid");
                    referral_memberid_type = response.getString("ref_memid_type");

                    performRegistration();
                } else {

                    String message = response.getString("status");
                    Toast.makeText(RegistrationFinalStep3Activity.this, message, Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing response");
            }
        }
    }

    private void parseRegistrationResponse(JSONObject response) {

        if (response != null) {

            try {

                if (response.getString("responce").equalsIgnoreCase("success")) {

                    Toast.makeText(RegistrationFinalStep3Activity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                    password = response.getString("pwd");

                    performLogin();

                } else {
                    Toast.makeText(RegistrationFinalStep3Activity.this, "Registration failed. Please contact ICRF", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(RegistrationFinalStep3Activity.this, "Registration failed. Please try again later.", Toast.LENGTH_LONG).show();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing registration response");
            }
        }
    }


    private void performLogin() {

        showProgressDialog("Logging in...");

        String url = Const.FINAL_URL + Const.URLs.LOGIN_DETAILS;
        url = url + "userid=" + country + "-" + phone;
        url = url + "&";
        url = url + "pwd=" + password;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        if (response.length() == 0) {
                            Toast.makeText(RegistrationFinalStep3Activity.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
                        } else {
                            parseLoginResponse(response);
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
                                    RegistrationFinalStep3Activity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                        if (progressDialog != null && progressDialog.isShowing())
                            progressDialog.dismiss();
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    private void parseLoginResponse(JSONArray response) {

        dismissProgressDialog();

        try {

            JSONObject jsonObject = response.getJSONObject(0);

            JSONArray loginDetailsArray = jsonObject.getJSONArray("Login_Details");

            if (loginDetailsArray.length() > 0) {

                JSONObject loginDetailsObject = loginDetailsArray.getJSONObject(0);

                String isValidResponse = loginDetailsObject.getString("responce");

                if (isValidResponse.equalsIgnoreCase("success")) {

                    if (loginDetailsObject.getString("country").equalsIgnoreCase(country)) {

                        String member_id = loginDetailsObject.getString("memberid");
                        String memberid_type = loginDetailsObject.getString("memberid_type");
                        String name = loginDetailsObject.getString("name");
                        String user_id = loginDetailsObject.getString("uname");
                        String mobile = loginDetailsObject.getString("mobile");
                        String email = loginDetailsObject.getString("email");

                        JSONArray profileDetailsArray = jsonObject.getJSONArray("Profile_Details");
                        JSONObject profileDetailsObject = profileDetailsArray.getJSONObject(0);

                        String profile_image = profileDetailsObject.getString("profile_image");

                        mProfile.setPreferences(member_id, name, user_id, mobile, email, memberid_type, profile_image);

                        startActivity(new Intent(RegistrationFinalStep3Activity.this, IntroductionActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        RegistrationFinalStep3Activity.this.finish();
                    } else {
                        Toast.makeText(RegistrationFinalStep3Activity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
                    }

                }
            } else {
                Toast.makeText(RegistrationFinalStep3Activity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
