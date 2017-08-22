package com.apex.icrf;

import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONObject;

/**
 * Created by WASPVamsi on 02/01/16.
 */
public class VerifyRegistrationOTPActivity extends AppCompatActivity {

    Button btnSubmit;
    SharedPreferences prefs;
    Profile mProfile;
    ProgressDialog progressDialog;

    //EditText editTextOTP;
    TextView mTextViewResendOTP;

    //String member_id, name, user_id, mobile, email, memberid_type, profile_image;
    //boolean from_verify_petition = false;

    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;
    int timer_count = 0;

    EditText editTextOtp1, editTextOtp2, editTextOtp3, editTextOtp4;

    String country, phone, email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_verify_otp);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mProfile = new Profile(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable().setColorFilter(
                Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.GONE);

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            country = bundle.getString("country");
            phone = bundle.getString("phone");
            email = bundle.getString("email");
        }

        editTextOtp1 = (EditText) findViewById(R.id.editText_otp_1);
        editTextOtp2 = (EditText) findViewById(R.id.editText_otp_2);
        editTextOtp3 = (EditText) findViewById(R.id.editText_otp_3);
        editTextOtp4 = (EditText) findViewById(R.id.editText_otp_4);

        editTextOtp1.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editTextOtp1.getText().length() >= 1)
                    editTextOtp2.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextOtp2.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editTextOtp2.getText().length() >= 1)
                    editTextOtp3.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        editTextOtp3.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                if (editTextOtp3.getText().length() >= 1)
                    editTextOtp4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mTextViewResendOTP = (TextView) findViewById(R.id.textView_resend_otp);
        mTextViewResendOTP.setText(Html.fromHtml("<u>Resend OTP?</u>"));
        mTextViewResendOTP.setEnabled(false);
        mTextViewResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTextViewResendOTP.setEnabled(false);
                sendRegistrationOTPRequest();
            }
        });

        btnSubmit = (Button) findViewById(R.id.button_submit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //String otp = editTextOTP.getText().toString();


                String otp1 = editTextOtp1.getText().toString();
                String otp2 = editTextOtp2.getText().toString();
                String otp3 = editTextOtp3.getText().toString();
                String otp4 = editTextOtp4.getText().toString();

                String otp = otp1 + otp2 + otp3 + otp4;

                if (otp.equalsIgnoreCase("") || otp.length() < 4)
                    Toast.makeText(VerifyRegistrationOTPActivity.this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show();
                else
                    validateRegistrationOTP(otp);



                //startActivity(new Intent(VerifyRegistrationOTPActivity.this, RegistrationFinalStep1Activity.class));
            }
        });


        mCountDownTimer = new CountDownTimer(40000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                timer_count++;
                mProgressBar.setProgress(timer_count);

                mTextViewResendOTP.setText("Resend OTP in " + (40 - timer_count) + " secs");
            }

            @Override
            public void onFinish() {
                timer_count++;
                mProgressBar.setProgress(timer_count);
                mProgressBar.setVisibility(View.GONE);

                mTextViewResendOTP.setText(Html.fromHtml("<u>Click here to resend OTP</u>"));
                mTextViewResendOTP.setEnabled(true);
            }
        };

        sendRegistrationOTPRequest();
    }

    public void sendRegistrationOTPRequest() {

        showProgressDialog("Sending OTP...");

        String url = Const.FINAL_URL + Const.URLs.SEND_REGISTRATION_OTP;
        url = url + "mobileno=" + phone;
        url = url + "&emailto=" + email;
        //url = url + "&try_with_email=true";

        if (prefs.getString("registration_current_country", "IN").equalsIgnoreCase("IN"))
            url = url + "&try_with_email=false";
        else
            url = url + "&try_with_email=true";

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Send Registration OTP Url = " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {
                            dismissProgressDialog();
                            try {

                                if (response.getString("responce").equalsIgnoreCase("success")) {
                                    Toast.makeText(VerifyRegistrationOTPActivity.this, response.getString("status"), Toast.LENGTH_LONG).show();

                                    if (mProgressBar.getVisibility() == View.GONE)
                                        mProgressBar.setVisibility(View.VISIBLE);

                                    timer_count = 0;
                                    mCountDownTimer.start();
                                } else
                                    Toast.makeText(VerifyRegistrationOTPActivity.this, response.getString("status"), Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(VerifyRegistrationOTPActivity.this, "Cannot send OTP at this time. Please try again later", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dismissProgressDialog();

                Toast.makeText(VerifyRegistrationOTPActivity.this, "Cannot contact server at this point in time. Please try again later", Toast.LENGTH_SHORT).show();
            }
        });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);
    }


    public void validateRegistrationOTP(String otp) {

        showProgressDialog("Validating OTP...");

        String url = Const.FINAL_URL + Const.URLs.READ_REGISTRATION_OTP;
        url = url + "mobileno=" + phone;
        url = url + "&otp=" + otp;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Read Registration OTP Url = " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {
                            dismissProgressDialog();
                            try {

                                if (response.getString("responce").equalsIgnoreCase("success")) {
                                    //prefs.edit().putBoolean(Const.Prefs.REGISTRATION_OTP_VERIFIED, true).apply();

                                    startActivity(new Intent(VerifyRegistrationOTPActivity.this, RegistrationFinalStep1Activity.class)
                                            .putExtra("country", country)
                                            .putExtra("phone", phone)
                                            .putExtra("email", email)
                                    );

                                    VerifyRegistrationOTPActivity.this.finish();

                                } else {
                                    Toast.makeText(VerifyRegistrationOTPActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                    //prefs.edit().putBoolean(Const.Prefs.REGISTRATION_OTP_VERIFIED, false).apply();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(VerifyRegistrationOTPActivity.this, "Cannot verify OTP at this time. Please try again later", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dismissProgressDialog();
                Toast.makeText(VerifyRegistrationOTPActivity.this, "Cannot contact server at this point in time. Please try again later", Toast.LENGTH_SHORT).show();
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
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    protected void onResume() {
        LocalBroadcastManager.getInstance(this).registerReceiver(localReceiver, new IntentFilter("received_otp_sms"));
        super.onResume();
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localReceiver);
        super.onPause();
    }


    private BroadcastReceiver localReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (intent.getExtras().containsKey("otp_code")) {
                setOtp(intent.getExtras().getString("otp_code"));
            }
        }
    };

    private void setOtp(String message) {
        message = message.replace(".", "").replace(" ", "");

        editTextOtp1.setText(String.valueOf(message.charAt(0)));
        editTextOtp2.setText(String.valueOf(message.charAt(1)));
        editTextOtp3.setText(String.valueOf(message.charAt(2)));
        editTextOtp4.setText(String.valueOf(message.charAt(3)));

        mCountDownTimer.cancel();
        mProgressBar.setVisibility(View.GONE);
        mTextViewResendOTP.setVisibility(View.GONE);

        validateRegistrationOTP(message);
    }
}
