package com.apex.icrf;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.classes.Address;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by WASPVamsi on 27/04/16.
 */
public class RegistrationFinalStep2Activity extends AppCompatActivity {

    Toolbar toolbar;
    TextView title;

    SharedPreferences prefs;

    ProgressDialog progressDialog;
    TextView textViewCountry, textViewCityLabel;
    EditText editTextPincode, editTextState, editTextDistrict, editTextCity;
    Spinner mSpinnerCity;
    ImageView imageViewPincode;
    //AutoCompleteTextView autoCompleteTextViewPincode;
    Profile mProfile;

    List<Address> mAddressList = new ArrayList<>();
    List<String> pin_codes = new ArrayList<>();
    //List<String> states = new ArrayList<>();
    //List<String> districts = new ArrayList<>();
    List<String> cities = new ArrayList<>();


    //LocationsTableDbAdapter mLocationsTableDbAdapter;
    //StatesAdapter states_adapter;
    //DistrictsAdapter districts_adapter;
    CitiesAdapter cities_adapter;

    CheckBox checkBox;

    String country, phone, email, name, gender, dob, pincode, state, district, city,cityText;

    String door = "N.A.",street = "N.A.",landmark = "N.A.",referral_memberid_type = "I",password;

    // default member id is 0 and type is "I"
    float referral_memberid = 0;

    Utility utility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_final_step_2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);
        checkBox = (CheckBox) findViewById(R.id.checkBox);
        utility = new Utility(this);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mProfile = new Profile(this);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            country = bundle.getString("country");
            phone = bundle.getString("phone");
            email = bundle.getString("email");
            name = bundle.getString("name");
            gender = bundle.getString("gender");
            dob = bundle.getString("dob");
        }

        textViewCountry = (TextView) findViewById(R.id.textview_registration_final_step_2_country);
        if (country != null)
            textViewCountry.setText(country.toUpperCase(Locale.ENGLISH));

        textViewCityLabel = (TextView) findViewById(R.id.textview_registration_final_step_2_city_label);
        textViewCityLabel.setText(Html.fromHtml(getResources().getString(R.string.city)));

        editTextPincode = (EditText) findViewById(R.id.edittext_registration_final_step_2_pincode);
        editTextState = (EditText) findViewById(R.id.edittext_registration_final_step_2_state);
        editTextDistrict = (EditText) findViewById(R.id.edittext_registration_final_step_2_district);
        editTextCity = (EditText) findViewById(R.id.edittext_registration_final_step_2_city);

        imageViewPincode = (ImageView) findViewById(R.id.imageview_registration_final_step_2_pincode);
        imageViewPincode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (editTextPincode.length() == 0)
                    Toast.makeText(RegistrationFinalStep2Activity.this, "Pincode cannot be empty", Toast.LENGTH_LONG).show();
                else if (editTextPincode.length() < 6 && country.equalsIgnoreCase("IN"))
                    Toast.makeText(RegistrationFinalStep2Activity.this, "Please enter a valid pincode", Toast.LENGTH_LONG).show();
                else
                    getDetailsForPincode();
            }
        });


        mSpinnerCity = (Spinner) findViewById(R.id.spinner_city);
        cities.clear();
        cities.add("Select City");
        cities_adapter = new CitiesAdapter(this);
        mSpinnerCity.setAdapter(cities_adapter);

        if(country != null) {

            if(country.equalsIgnoreCase("IN")) {
                editTextCity.setVisibility(View.GONE);

                textViewCityLabel.setVisibility(View.VISIBLE);
                mSpinnerCity.setVisibility(View.VISIBLE);
                imageViewPincode.setVisibility(View.VISIBLE);

                editTextDistrict.setEnabled(false);
                editTextState.setEnabled(false);
            } else {
                editTextCity.setVisibility(View.VISIBLE);

                textViewCityLabel.setVisibility(View.GONE);
                mSpinnerCity.setVisibility(View.GONE);
                imageViewPincode.setVisibility(View.GONE);

                editTextDistrict.setEnabled(true);
                editTextState.setEnabled(true);
            }
        }


        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(false);

    }


    public void getDetailsForPincode() {

        showProgressDialog("Validating...");

        String url = Const.FINAL_URL + Const.URLs.GET_DATA_BY_PINCODE;
        url = url + "pincode=" + editTextPincode.getText().toString();

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

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    RegistrationFinalStep2Activity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    public void parseResponse(JSONArray response) {

        if (response != null && response.length() > 0) {

            try {

                JSONObject object = response.getJSONObject(0);

                String pincode = object.getString("pincode");
                String state = object.getString("state");
                String district = object.getString("district");

                cities.clear();
                cities.add("Select City");

                JSONArray citiesJSONArray = object.getJSONArray("cities");
                for (int i = 0; i < citiesJSONArray.length(); i++) {
                    cities.add(citiesJSONArray.getString(i));
                }

                if (state == null || district == null) {
                    Toast.makeText(RegistrationFinalStep2Activity.this, "Pincode is not valid", Toast.LENGTH_LONG).show();
                } else {

                    editTextState.setText(state);
                    editTextDistrict.setText(district);

                    cities_adapter.notifyDataSetChanged();
                }


            } catch (Exception e) {
                e.printStackTrace();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing response");
            }
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        if (toolbar != null && title != null) {
            title.setText(getResources().getString(R.string.title_activity_registration_final_step_2));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_registration_final_step_2, menu);

        MenuItem item = menu.findItem(R.id.action_next);
        if (item != null) {
            MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    pincode = editTextPincode.getText().toString();
                    state = editTextState.getText().toString();
                    district = editTextDistrict.getText().toString();
                    int position = mSpinnerCity.getSelectedItemPosition();
                    city = cities.get(position);
                    cityText = editTextCity.getText().toString();

                    if (TextUtils.isEmpty(pincode)) {
                        utility.toast("Pincode Should not be Empty");
                    } else if (TextUtils.isEmpty(state)) {
                        utility.toast("State Should not be Empty");
                    } else if (TextUtils.isEmpty(district)) {
                        utility.toast("District Should not be Empty");
                    } else if (country.equalsIgnoreCase("IN") && mSpinnerCity.getSelectedItemPosition() == 0) {
                        Toast.makeText(RegistrationFinalStep2Activity.this, "Please select your city", Toast.LENGTH_LONG).show();
                    }
                    else if (!country.equalsIgnoreCase("IN") && TextUtils.isEmpty(cityText)) {
                            Toast.makeText(RegistrationFinalStep2Activity.this, "City Should not be Empty", Toast.LENGTH_LONG).show();
                    } else if (!checkBox.isChecked()) {
                            Toast.makeText(RegistrationFinalStep2Activity.this, "We must accept terms & conditions to register here", Toast.LENGTH_LONG).show();

                    } else {
                            if (Const.DEBUGGING) {
                                Log.d(Const.DEBUG, "Pincode: " + pincode);
                                Log.d(Const.DEBUG, "State: " + state);
                                Log.d(Const.DEBUG, "District: " + district);
                                Log.d(Const.DEBUG, "City: " + city);
                            }

                        //utility.toast("Registration success");

                            performRegistration();
                            // skipped registration screen 3 and directly registering user at screen 2 itself

//                        startActivity(new Intent(RegistrationFinalStep2Activity.this, RegistrationFinalStep3Activity.class)
//                                .putExtra("country", country)
//                                .putExtra("phone", phone)
//                                .putExtra("email", email)
//                                .putExtra("name", name)
//                                .putExtra("dob", dob)
//                                .putExtra("gender", gender)
//                                .putExtra("pincode", pincode)
//                                .putExtra("state", state)
//                                .putExtra("district", district)
//                                .putExtra("city", city)
//                        );
                        }


                    }

            });
        }
        return super.onCreateOptionsMenu(menu);
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
            if (country.equalsIgnoreCase("IN"))
            {
                url = url + "&city_or_village=" + URLEncoder.encode(city, "UTF-8");
            }
            else
            {
                url = url + "&city_or_village=" + URLEncoder.encode(cityText, "UTF-8");
            }

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
                                        RegistrationFinalStep2Activity.this,
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
    private void parseRegistrationResponse(JSONObject response) {

        if (response != null) {

            try {

                if (response.getString("responce").equalsIgnoreCase("success")) {

                    Toast.makeText(RegistrationFinalStep2Activity.this, "Registration Successful", Toast.LENGTH_SHORT).show();

                    password = response.getString("pwd");

                    performLogin();

                } else {
                    Toast.makeText(RegistrationFinalStep2Activity.this, "Registration failed. Please contact ICRF", Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(RegistrationFinalStep2Activity.this, "Registration failed. Please try again later.", Toast.LENGTH_LONG).show();

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
                            Toast.makeText(RegistrationFinalStep2Activity.this, "Invalid Credentials", Toast.LENGTH_LONG).show();
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
                                    RegistrationFinalStep2Activity.this,
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

                        startActivity(new Intent(RegistrationFinalStep2Activity.this, IntroductionActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));

                        RegistrationFinalStep2Activity.this.finish();
                    } else {
                        Toast.makeText(RegistrationFinalStep2Activity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
                    }

                }
            } else {
                Toast.makeText(RegistrationFinalStep2Activity.this, "Invalid Credentials.", Toast.LENGTH_LONG).show();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
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

    public class CitiesAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;
        TextView textView;

        public CitiesAdapter(Context context) {
            mLayoutInflater = LayoutInflater.from(context);
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return cities.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            String state = cities.get(position);

            if (convertView == null)
                convertView = mLayoutInflater.inflate(R.layout.item_registration_spinner_dropdown_text, parent, false);

            textView = (TextView) convertView.findViewById(R.id.textview);
            textView.setText(state);

            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            String state = cities.get(position);


            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_registration_spinner_text, parent, false);
            }

            TextView textView = (TextView) convertView.findViewById(R.id.textview);
            textView.setText(state);

            return convertView;
        }
    }

}
