package com.apex.icrf;

import android.*;
import android.Manifest;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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
import com.apex.icrf.Interfaces.SmsListner;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONObject;

import java.security.Permission;

/**
 * Created by WASPVamsi on 02/01/16.
 */
public class OTPActivity extends AppCompatActivity {

    Button btnSubmit;
    SharedPreferences prefs;
    Profile mProfile;
    ProgressDialog progressDialog;

    //EditText editTextOTP;
    TextView mTextViewResendOTP;

    String member_id, name, user_id, mobile, email, memberid_type, profile_image;
    boolean from_verify_petition = false;

    ProgressBar mProgressBar;
    CountDownTimer mCountDownTimer;
    int timer_count = 0;

    EditText editTextOtp1, editTextOtp2, editTextOtp3, editTextOtp4;

    public static final String TAG = "OTPActivity";
    public final int SMSREADPERMISSION = 0002;

    Utility utility;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        utility = new Utility(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mProfile = new Profile(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.getProgressDrawable().setColorFilter(Color.WHITE, android.graphics.PorterDuff.Mode.SRC_IN);
        mProgressBar.setVisibility(View.GONE);

        Bundle login_bundle = getIntent().getBundleExtra("login_bundle");
        if (login_bundle != null) {

            member_id = login_bundle.getString("member_id");
            name = login_bundle.getString("name");
            user_id = login_bundle.getString("user_id");
            mobile = login_bundle.getString("mobile");
            email = login_bundle.getString("email");
            memberid_type = login_bundle.getString("memberid_type");
            profile_image = login_bundle.getString("profile_image");

            from_verify_petition = login_bundle.getBoolean("from_verify_petition");
        }

        //editTextOTP = (EditText) findViewById(R.id.editText_otp);

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

                if(editTextOtp1.getText().length() >= 1)
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

                if(editTextOtp2.getText().length() >= 1)
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

                if(editTextOtp3.getText().length() >= 1)
                    editTextOtp4.requestFocus();
            }

            @Override
            public void afterTextChanged(Editable s) {
                utility.hideKeyBoard(editTextOtp4);
            }
        });


        mTextViewResendOTP = (TextView) findViewById(R.id.textView_resend_otp);
        mTextViewResendOTP.setText(Html.fromHtml("<u>Resend OTP?</u>"));
        mTextViewResendOTP.setEnabled(false);
        mTextViewResendOTP.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mTextViewResendOTP.setClickable(false);
                sendOTPRequest();
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
                    Toast.makeText(OTPActivity.this, "Please enter a valid OTP", Toast.LENGTH_SHORT).show();
                else
                    validateOTP(otp);
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

        sendOTPRequest();
        SMSPermission();
    }

    public void sendOTPRequest() {

        showProgressDialog("Sending OTP...");

        String url = Const.FINAL_URL + Const.URLs.SEND_OTP;
        url = url + "memberid=" + member_id;
        url = url + "&memberid_type=" + memberid_type;
        url = url + "&mobileno=" + mobile;
        url = url + "&emailto=" + email;

        if (prefs.getString("current_country", "IN").equalsIgnoreCase("IN"))
            url = url + "&try_with_email=false";
        else
            url = url + "&try_with_email=true";

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Send OTP Url = " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {
                            dismissProgressDialog();
                            try {

                                if (response.getString("responce").equalsIgnoreCase("success")) {
                                    Toast.makeText(OTPActivity.this, response.getString("status"), Toast.LENGTH_LONG).show();

                                    if (mProgressBar.getVisibility() == View.GONE)
                                        mProgressBar.setVisibility(View.VISIBLE);

                                    timer_count = 0;
                                    mCountDownTimer.start();
                                } else
                                    Toast.makeText(OTPActivity.this, response.getString("status"), Toast.LENGTH_LONG).show();

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(OTPActivity.this, "Cannot send OTP at this time. Please try again later", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dismissProgressDialog();
            }
        });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);
    }


    public void validateOTP(String otp) {

        showProgressDialog("Validating OTP...");

        String url = Const.FINAL_URL + Const.URLs.READ_OTP;
        url = url + "memberid=" + member_id;
        url = url + "&memberid_type=" + memberid_type;
        url = url + "&mobileno=" + mobile;
        url = url + "&otp=" + otp;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Read OTP Url = " + url);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (response != null) {
                            dismissProgressDialog();
                            try {

                                if (response.getString("responce").equalsIgnoreCase("success")) {
                                    prefs.edit().putBoolean(Const.Prefs.OTP_VERIFIED, true).apply();

                                    mProfile.setPreferences(member_id, name, user_id, mobile, email, memberid_type, profile_image);

                                    if (from_verify_petition) {
                                        startActivity(new Intent(OTPActivity.this, VerifyPetitionActivity.class).putExtra("from_login", true));
                                    } else {
                                        startActivity(new Intent(OTPActivity.this, IntroductionActivity.class));
                                    }

                                    Toast.makeText(OTPActivity.this, "Logged In", Toast.LENGTH_LONG).show();
                                    OTPActivity.this.finish();
                                } else {
                                    Toast.makeText(OTPActivity.this, "Authentication Failed", Toast.LENGTH_LONG).show();
                                    prefs.edit().putBoolean(Const.Prefs.OTP_VERIFIED, false).apply();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(OTPActivity.this, "Cannot verify OTP at this time. Please try again later", Toast.LENGTH_SHORT).show();
                            }

                        }
                    }
                }, new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                dismissProgressDialog();
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

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SMSPermission()
    {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permission = new String[]{Manifest.permission.READ_SMS};
            requestPermissions(permission,SMSREADPERMISSION);
        }
        else
        {
            autoReadOTP();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMSREADPERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

            }
        }
    }

    public void autoReadOTP()
    {
        SmsReceiver.bindListener(new SmsListner() {
            @Override
            public void messageReceived(String message) {
                if (message!=null)
                {

                }

//                editTextOtp1.setText(message.charAt(0));
//                editTextOtp2.setText(message.charAt(1));
//                editTextOtp3.setText(message.charAt(2));
//                editTextOtp4.setText(message.charAt(3));
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();


        member_id = "";
        name = "";
        user_id = "";
        mobile = "";
        email = "";
        memberid_type = "";
        profile_image = "";

        if (!prefs.getBoolean(Const.Prefs.OTP_VERIFIED, false))
            mProfile.removePreferences();
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
        //editTextOTP.setText(message);

        message = message.replace(".", "").replace(" ", "");

        editTextOtp1.setText(String.valueOf(message.charAt(0)));
        editTextOtp2.setText(String.valueOf(message.charAt(1)));
        editTextOtp3.setText(String.valueOf(message.charAt(2)));
        editTextOtp4.setText(String.valueOf(message.charAt(3)));

        mCountDownTimer.cancel();
        mProgressBar.setVisibility(View.GONE);
        mTextViewResendOTP.setVisibility(View.GONE);

        validateOTP(message);
    }
}
