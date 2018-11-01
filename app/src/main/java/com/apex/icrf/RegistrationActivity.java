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
import android.util.Log;
import android.util.Patterns;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.classes.Country;
import com.apex.icrf.diskcache.RequestManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by WASPVamsi on 26/04/16.
 */
public class RegistrationActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView title, txtViewCountryCode;
    Spinner mSpinner;
    String country_code;
    RegistrationCountryAdapter mCountryAdapter;

    protected SparseArray<ArrayList<Country>> mCountriesMap = new SparseArray<ArrayList<Country>>();
    public String current_country = "India";
    public String current_country_code = "+91";

    SharedPreferences prefs;
    EditText editTextPhoneNumber, editTextEmailID;
    ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.edit().putString("registration_current_country", current_country).apply();

        txtViewCountryCode = (TextView) findViewById(R.id.textView_country_code);
        editTextPhoneNumber = (EditText) findViewById(R.id.editText_mobile_number);
        editTextEmailID = (EditText) findViewById(R.id.edittext_registration_email);

        mSpinner = (Spinner) findViewById(R.id.spinner_country);

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

        mCountryAdapter = new RegistrationCountryAdapter(this, data);
        mSpinner.setAdapter(mCountryAdapter);
        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Country country = data.get(position);
                current_country = country.getCountryISO().toLowerCase(Locale.ENGLISH);
                prefs.edit().putString("registration_current_country", current_country).apply();
                country_code=country.getCountryCodeStr();
                txtViewCountryCode.setText(country.getCountryCodeStr());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        txtViewCountryCode.setText(current_country_code);
    }


    public class RegistrationCountryAdapter extends BaseAdapter {

        private LayoutInflater mLayoutInflater;

        private ArrayList<Country> data = new ArrayList<>();

        public RegistrationCountryAdapter(Context context, ArrayList<Country> data) {
            mLayoutInflater = LayoutInflater.from(context);
            this.data = data;
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return data.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {

            Country country = data.get(position);

            final ViewHolder holder;
            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_country_drop, parent, false);
                holder = new ViewHolder();
                //holder.mImageView = (ImageView) convertView.findViewById(R.id.image);
                holder.mNameView = (TextView) convertView.findViewById(R.id.country_name);
                holder.mCodeView = (TextView) convertView.findViewById(R.id.country_code);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            if (country != null) {
                holder.mNameView.setText(country.getName() + "(" + country.getCountryISO().toUpperCase(Locale.ENGLISH) + ")");
                holder.mCodeView.setText(country.getCountryCodeStr());
                //holder.mImageView.setImageResource(country.getResId());
            }
            return convertView;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            Country country = data.get(position);


            if (convertView == null) {
                convertView = mLayoutInflater.inflate(R.layout.item_registration_country, parent, false);
            }
//        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);
//        imageView.setImageResource(country.getResId());

            TextView textView = (TextView) convertView.findViewById(R.id.country_code);
            textView.setText(country.getName().substring(0, 1).toUpperCase(Locale.ENGLISH) + country.getName().substring(1));

            return convertView;
        }

        private class ViewHolder {
            //public ImageView mImageView;
            public TextView mNameView;
            public TextView mCodeView;
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

        if (toolbar != null && title != null) {
            title.setText(getResources().getString(R.string.title_activity_registration_final_step_1));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_registration, menu);

        MenuItem item = menu.findItem(R.id.action_next);
        if (item != null) {
            MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    // Make Api call
                    // If registered or verified
                    // display error
                    // Else
                    // redirect to VerifyRegistrationActivity

                    String country = current_country;
                    String phone_number = editTextPhoneNumber.getText().toString();
                    String email_id = editTextEmailID.getText().toString();

                    // login validation disabled for testing before release should uncomment this one
                    if (isValid(country, phone_number, email_id)) {

                        if(country.equalsIgnoreCase("IN")) {
                            checkCanRegister(country, phone_number, email_id);
                        }else{
                            String countrycode=country_code.replace("+","");
                            phone_number=countrycode+editTextPhoneNumber.getText().toString();
                            checkCanRegister(country, phone_number, email_id);
                            Toast.makeText(RegistrationActivity.this, "country:"+current_country+"  phone_number:"+phone_number, Toast.LENGTH_LONG).show();



                        }
                    }

                    // press "next" to go to registartion screen
//                    startActivity(new Intent(RegistrationActivity.this, RegistrationFinalStep1Activity.class)
//                            .putExtra("country", current_country)
//                            .putExtra("phone", "+919491191338")
//                            .putExtra("email", "saigopi49@gmail.com"));

                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }


    public boolean isValid(String country, String phone_number, String email) {

        if (country.equalsIgnoreCase("IN")) {

            if (phone_number.isEmpty() || phone_number.length() < 10) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid phone number", Toast.LENGTH_LONG).show();
                return false;
            } else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid email id", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }

        } else {

            if (phone_number.isEmpty()) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid phone number", Toast.LENGTH_LONG).show();
                return false;
            } else if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Toast.makeText(RegistrationActivity.this, "Please enter a valid email id", Toast.LENGTH_LONG).show();
                return false;
            } else {
                return true;
            }
        }

    }


    public void checkCanRegister(final String country, final String phone_number, final String email) {

        showProgressDialog("Validating...");

        String url = Const.FINAL_URL + Const.URLs.NEW_REGISTRATION_CHECKING;
        url = url + "mobile_no=" + phone_number;
        url = url + "&country_code=" + country;
        url = url + "&is_verification_checking=true";

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);


        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        dismissProgressDialog();
                        parseResponse(response, country, phone_number, email);
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
                                    RegistrationActivity.this,
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public void parseResponse(JSONObject response, String country, String phone_number, String email) {

        if (response != null) {

            /* @formatter:off
            {
                status: "User ID already existed.",
                responce: "failure",
                is_done_mobile_verified: "A",
                country_code: "other"
            } @formatter:on
             */

            try {

                if (response.getString("responce").equalsIgnoreCase("success")) {

                    if(response.getString("is_done_mobile_verified").equalsIgnoreCase("A")) {
                        // Verified mobile number
                        // go to registration screen

                        startActivity(new Intent(RegistrationActivity.this, RegistrationFinalStep1Activity.class)
                                .putExtra("country", current_country)
                                .putExtra("phone", phone_number)
                                .putExtra("email", email));
                    } else {
                        // Mobile not verified
                        // go to verification screen

                        startActivity(new Intent(RegistrationActivity.this, VerifyRegistrationActivity.class)
                                .putExtra("country", current_country)
                                .putExtra("phone", phone_number)
                                .putExtra("email", email));

//                        if(current_country.equalsIgnoreCase("IN")) {
//                            // move to otp screen
//                            startActivity(new Intent(RegistrationActivity.this, VerifyRegistrationOTPActivity.class)
//                                    .putExtra("country", current_country)
//                                    .putExtra("phone", phone_number)
//                                    .putExtra("email", email));
//                        } else {
//                            // move to verify phone number screen
//                            startActivity(new Intent(RegistrationActivity.this, VerifyRegistrationActivity.class)
//                                .putExtra("country", current_country)
//                                .putExtra("phone", phone_number)
//                                .putExtra("email", email));
//                    }
                    }

                } else if (response.getString("responce").equalsIgnoreCase("failure")) {

                    String message = response.getString("status");
                    Toast.makeText(RegistrationActivity.this, message, Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing response");
            }


        }
    }

}
