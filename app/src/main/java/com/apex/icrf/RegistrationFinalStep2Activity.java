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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apex.icrf.classes.Address;
import com.apex.icrf.diskcache.RequestManager;

import org.json.JSONArray;
import org.json.JSONObject;

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

    List<Address> mAddressList = new ArrayList<>();
    List<String> pin_codes = new ArrayList<>();
    //List<String> states = new ArrayList<>();
    //List<String> districts = new ArrayList<>();
    List<String> cities = new ArrayList<>();


    //LocationsTableDbAdapter mLocationsTableDbAdapter;
    //StatesAdapter states_adapter;
    //DistrictsAdapter districts_adapter;
    CitiesAdapter cities_adapter;

    String country, phone, email, name, gender, dob, pincode, state, district, city;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_final_step_2);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

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

                    if (mSpinnerCity.getSelectedItemPosition() == 0) {
                        Toast.makeText(RegistrationFinalStep2Activity.this, "Please select your city", Toast.LENGTH_LONG).show();
                    } else {

                        pincode = editTextPincode.getText().toString();
                        state = editTextState.getText().toString();
                        district = editTextDistrict.getText().toString();
                        int position = mSpinnerCity.getSelectedItemPosition();
                        city = cities.get(position);

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Pincode: " + pincode);
                            Log.d(Const.DEBUG, "State: " + state);
                            Log.d(Const.DEBUG, "District: " + district);
                            Log.d(Const.DEBUG, "City: " + city);
                        }

                        startActivity(new Intent(RegistrationFinalStep2Activity.this, RegistrationFinalStep3Activity.class)
                                .putExtra("country", country)
                                .putExtra("phone", phone)
                                .putExtra("email", email)
                                .putExtra("name", name)
                                .putExtra("dob", dob)
                                .putExtra("gender", gender)
                                .putExtra("pincode", pincode)
                                .putExtra("state", state)
                                .putExtra("district", district)
                                .putExtra("city", city)
                        );
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
