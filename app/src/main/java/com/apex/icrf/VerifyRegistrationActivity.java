package com.apex.icrf;

import android.*;
import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.diskcache.RequestManager;

import org.json.JSONObject;

import java.util.List;

/**
 * Created by WASPVamsi on 26/04/16.
 */
public class VerifyRegistrationActivity extends AppCompatActivity {

    Toolbar toolbar;
    TextView title, txtView1_2;
    String country, phone, email;
    String team;
    LinearLayout llVerify;

    private boolean calling = false;
    ProgressDialog progressDialog;
    String uri = "tel: 8977176253";
    final int MAKECALLPERMISSION = 143;
    Intent intent;
    private List<PhoneAccountHandle> phoneAccountHandleList;


    final CharSequence[] items = {"Sim1", "Sim2"};
    private final static String simSlotName[] = {
            "extra_asus_dial_use_dualsim",
            "com.android.phone.extra.slot",
            "slot",
            "simslot",
            "sim_slot",
            "subscription",
            "Subscription",
            "phone",
            "com.android.phone.DialingMode",
            "simSlot",
            "slot_id",
            "simId",
            "simnum",
            "phone_type",
            "slotId",
            "slotIdx"
    };

    @SuppressLint("MissingPermission")
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_verify);
        TelecomManager telecomManager = null;

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        if (ActivityCompat.checkSelfPermission(VerifyRegistrationActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            Log.d("phone_call1", "phone_call_permission");
            getCallPermissions();
        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {

            telecomManager = (TelecomManager) this.getSystemService(Context.TELECOM_SERVICE);
            phoneAccountHandleList = telecomManager.getCallCapablePhoneAccounts();
        }

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        txtView1_2 = (TextView) findViewById(R.id.textView_1_2);
        llVerify = (LinearLayout) findViewById(R.id.ll_verify);

        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        Bundle bundle = getIntent().getExtras();
        if (bundle.containsKey("phone")) {

            country = bundle.getString("country");
            phone = bundle.getString("phone");
            txtView1_2.setText(phone);

            email = bundle.getString("email");
        }

        llVerify.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View v) {
                Log.d("phone_call", "phone_call_entered");
                calling = true;

                if (ActivityCompat.checkSelfPermission(VerifyRegistrationActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    Log.d("phone_call1", "phone_call_permission");
                    getCallPermissions();

                } else if (ActivityCompat.checkSelfPermission(VerifyRegistrationActivity.this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    getCallPermissions();
                } else {

                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        CreateAlertDialogWithRadioButtonGroup();
                    }

//                    Log.d("phone_call2","to call entered");
//                    if (country.equalsIgnoreCase("IN"))
//                    { Log.d("phone_call3","to In");
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
//                            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//
//                            Log.d("Test", "Current list = " + subsInfoList);
//
//                            for (SubscriptionInfo subscriptionInfo : subsInfoList) {
//
//                                String number = subscriptionInfo.getNumber();
//
//                                Log.d("Test", " Number is  " + number);
//                            }
//                        }
//
//
//
//
//
//                        Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                        intent.setData(Uri.parse("tel: +918977176253" ));
//                        intent.putExtra("com.android.phone.force.slot", true);
//                        intent.putExtra("Cdma_Supp", true);
//                        if (item == 0) {//for sim1
//                            for (String s : simSlotName){
//                                intent.putExtra(s, 0); //0 or 1 according to sim.......
//                            }
//                            if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0)
//                            {
//                                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE",
//                                        phoneAccountHandleList.get(0));
//                            }
//                        } else {//for sim2
//                            for (String s : simSlotName) {
//                                intent.putExtra(s, 1); //0 or 1 according to sim.......
//                            }
//                            if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 1){
//                                intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE",
//                                        phoneAccountHandleList.get(1));
//                            }
//                        }
//                        startActivity(intent);
//
//
//
////                        uri = "tel: +918977176253";
////                        intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
////                        intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
////                        intent.putExtra("simSlotIndex", 1);
////                        startActivity(intent);
//                    }
//                    else
//                    {
////                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
////                            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
////                            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
////
////                            Log.d("Test", "Current list = " + subsInfoList);
////
////                            for (SubscriptionInfo subscriptionInfo : subsInfoList) {
////
////                                String number = subscriptionInfo.getNumber();
////
////                                Log.d("Test", " Number is  " + number);
////                            }
////                        }
//                        Log.d("phone_call4","to no");
//                        uri = "tel: +918977176253";
//
//                        intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
//                        intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
//                        intent.putExtra("simSlotIndex", 1);
//                        startActivity(intent);
//                    }
                }
            }

        });
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void getCallPermissions() {
        String[] permissions = new String[]{Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE};
        requestPermissions(permissions, MAKECALLPERMISSION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MAKECALLPERMISSION) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED)) {

                if (country.equalsIgnoreCase("IN")) {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        CreateAlertDialogWithRadioButtonGroup();
                    }
//                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                        SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
//                        List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//
//                        Log.d("Test", "Current list = " + subsInfoList);
//
//                        for (SubscriptionInfo subscriptionInfo : subsInfoList) {
//
//                            String number = subscriptionInfo.getNumber();
//
//                            Log.d("Test", " Number is  " + number);
//                        }
//                    }
//                    intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
//                    intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
//                    intent.putExtra("simSlotIndex", 1);
                } else {
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        CreateAlertDialogWithRadioButtonGroup();
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
                        SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling
                            //    ActivityCompat#requestPermissions
                            // here to request the missing permissions, and then overriding
                            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                            //                                          int[] grantResults)
                            // to handle the case where the user grants the permission. See the documentation
                            // for ActivityCompat#requestPermissions for more details.
                            return;
                        }
                        List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();

                        Log.d("Test", "Current list = " + subsInfoList);

                        for (SubscriptionInfo subscriptionInfo : subsInfoList) {

                            String number = subscriptionInfo.getNumber();

                            Log.d("Test", " Number is  " + number);
                        }
                    }

                    uri = "tel: +918977176253";
                    intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
                    intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
                    intent.putExtra("simSlotIndex", 1);
                }

                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }


//                startActivity(intent);
            } else {
                Toast.makeText(VerifyRegistrationActivity.this, "You should give permission to make this call", Toast.LENGTH_LONG).show();
            }
        }
    }


    @Override
    protected void onPause() {
        super.onPause();


    }

    @Override
    protected void onResume() {
        super.onResume();

        if (toolbar != null && title != null) {
            title.setText(getResources().getString(R.string.title_activity_registration_verify));
        }
        checkVerification();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_verify_registration, menu);

        MenuItem item = menu.findItem(R.id.action_check);
        if (item != null) {
            MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    checkVerification();
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

    public void checkVerification() {

        checkCanRegister(country, phone, email);
    }

    void call(String iteam) {

        String iteam1 = iteam;
        Log.d("phone_call2", "to call entered");
        if (country.equalsIgnoreCase("IN")) {
            Log.d("phone_call3", "to In");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();

                Log.d("Test", "Current list = " + subsInfoList);

                for (SubscriptionInfo subscriptionInfo : subsInfoList) {

                    String number = subscriptionInfo.getNumber();

                    Log.d("Test", " Number is  " + number);
                }
            }


            Intent intent = new Intent(Intent.ACTION_CALL).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setData(Uri.parse("tel: +918977176253"));
            intent.putExtra("com.android.phone.force.slot", true);
            intent.putExtra("Cdma_Supp", true);
            if (iteam1.equals("Sim1")) {//for sim1
                for (String s : simSlotName) {
                    intent.putExtra(s, 0); //0 or 1 according to sim.......
                }
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 0) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                                phoneAccountHandleList.get(0));
                    }
                }
            } else {//for sim2
                for (String s : simSlotName) {
                    intent.putExtra(s, 1); //0 or 1 according to sim.......
                }
                if (phoneAccountHandleList != null && phoneAccountHandleList.size() > 1) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        intent.putExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE",
                                phoneAccountHandleList.get(1));
                    }
                }
            }
            startActivity(intent);


//                        uri = "tel: +918977176253";
//                        intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
//                        intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
//                        intent.putExtra("simSlotIndex", 1);
//                        startActivity(intent);
        } else {
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP_MR1) {
//                            SubscriptionManager subscriptionManager = SubscriptionManager.from(getApplicationContext());
//                            List<SubscriptionInfo> subsInfoList = subscriptionManager.getActiveSubscriptionInfoList();
//
//                            Log.d("Test", "Current list = " + subsInfoList);
//
//                            for (SubscriptionInfo subscriptionInfo : subsInfoList) {
//
//                                String number = subscriptionInfo.getNumber();
//
//                                Log.d("Test", " Number is  " + number);
//                            }
//                        }
            Log.d("phone_call4", "to no");
            uri = "tel: +918977176253";

            intent = new Intent(Intent.ACTION_CALL, Uri.parse(uri));
            intent.putExtra("com.android.phone.extra.simSlotIndex", 1);
            intent.putExtra("simSlotIndex", 1);
            startActivity(intent);
        }
    }




    public void CreateAlertDialogWithRadioButtonGroup(){


        AlertDialog.Builder builder = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        builder.setTitle("Select Sim card")
                .setSingleChoiceItems(items, -1, new DialogInterface.OnClickListener()

                {
                    public void onClick(DialogInterface dialogInterface, int item) {

                        team= (String) items[item];
                    }
                });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
//                Toast.makeText(getApplicationContext(), team, Toast.LENGTH_SHORT).show();
                call(team);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {


                dialog.dismiss();
            }
        });

        builder.create().show();

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
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
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
                                    VerifyRegistrationActivity.this,
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

                    if (response.getBoolean("is_done_mobile_verified")) {

                        startActivity(new Intent(VerifyRegistrationActivity.this, RegistrationFinalStep1Activity.class)
                                .putExtra("country", country)
                                .putExtra("phone", phone_number)
                                .putExtra("email", email));
                    } else {

                        Toast.makeText(VerifyRegistrationActivity.this, "Your mobile number is not yet verified.", Toast.LENGTH_LONG).show();
                    }

                } else if (response.getString("responce").equalsIgnoreCase("failure")) {
                    String message = response.getString("status");
                    Toast.makeText(VerifyRegistrationActivity.this, message, Toast.LENGTH_LONG).show();
                }

            } catch (Exception e) {
                e.printStackTrace();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing response");
            }


        }
    }
}
