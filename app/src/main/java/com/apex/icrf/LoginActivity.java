package com.apex.icrf;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by WASPVamsi on 02/01/16.
 */
public class LoginActivity extends AppCompatActivity {

    private String forgotPassword = "<u>Forgot Password?</u>";
    private String cancel = "<u>Cancel</u>";

    private static final int SMS = 0;
    private static final int EMAIL = 1;

    //Toolbar toolbar;
    //TextView title, icrf_login_label;
    TextView txtForgotPassword, txtCancel;
    EditText edtxtMobileNo, edtxtPassword;
    Button btnLogin, btnRequestPassword, btnRegister;
    RadioGroup radioGroup;
    RadioButton rbSms, rbEmail;
    ProgressDialog progressDialog;

    Spinner mSpinner;
    CountryAdapter mCountryAdapter;

    SharedPreferences prefs;
    Profile mProfile;

    protected SparseArray<ArrayList<Country>> mCountriesMap = new SparseArray<ArrayList<Country>>();
    public String current_country = "IN";

    Typeface font_roboto_thin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        toolbar = (Toolbar) findViewById(R.id.toolbar);
//        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
//
//        setSupportActionBar(toolbar);
//        getSupportActionBar().setTitle("");

        if (Build.VERSION.SDK_INT >= 21) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS,
                    WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }


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

//        if (toolbar != null && title != null) {
//            title.setText(getResources().getString(R.string.title_activity_login));
//        }
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

                        //mProfile.setPreferences(member_id, name, user_id, mobile, email, memberid_type, profile_image);

//                        Bundle bundle = getIntent().getExtras();
//                        if (bundle != null && bundle.getBoolean("from_verify_petition"))
//                            startActivity(new Intent(this, VerifyPetitionActivity.class).putExtra("from_login", true));
//                        else
//                            startActivity(new Intent(LoginActivity.this, OTPActivity.class));


                        Bundle bundle = getIntent().getExtras();
                        if (bundle != null && bundle.getBoolean("from_verify_petition")) {
                            //startActivity(new Intent(this, VerifyPetitionActivity.class).putExtra("from_login", true));
                            login_bundle.putBoolean("from_login", true);
                        } else {
                            login_bundle.putBoolean("from_login", false);
                            //startActivity(new Intent(LoginActivity.this, OTPActivity.class));
                        }

                        startActivity(new Intent(LoginActivity.this, OTPActivity.class).putExtra("login_bundle", login_bundle));


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
                        parseArrayResponse(response);
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
}
