package com.apex.icrf;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.apex.icrf.classes.ItemDeliveryReportsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;
import com.apex.icrf.gcm.QuickstartPreferences;
import com.apex.icrf.gcm.RegistrationIntentService;
import com.apex.icrf.utils.InternetConnectivity;
import com.apex.icrf.utils.Profile;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.security.MessageDigest;
import java.util.List;

public class SplashActivity extends AppCompatActivity {

    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;
    //private static final int SPLASH_TIME = 2000;
    private static final int SPLASH_TIME = 500;
    private static final int END_OF_INTERVAL = 0;
    private boolean mInterrupted = false;

    private boolean mIsUserLoggedIn = false;
    private boolean mTriedSync = false;
    private boolean mSplashTimeOut = false;

    private boolean hasInternetConnection = false;

    private boolean DEBUG = false;


    private Profile mProfile;
    //private ProgressDialog progressDialog;
    private SharedPreferences prefs;

    private DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;

    private Handler handler = new Handler(new Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case END_OF_INTERVAL:

                    if (mSplashTimeOut && mTriedSync) {

                        if (mIsUserLoggedIn) {
//                            startActivity(new Intent(SplashActivity.this,
//                                    MainActivity.class));

                            prefs.edit().putInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN).apply();
                            startActivity(new Intent(SplashActivity.this,
                                    MainTabbedActivity.class));

                        } else {
                            Bundle bundle = getIntent().getExtras();
                            if (bundle != null && bundle.containsKey("from_verify_petition"))
                                startActivity(new Intent(SplashActivity.this,
                                        LoginActivity.class).putExtra("from_verify_petition",
                                        true));
                            else
                                startActivity(new Intent(SplashActivity.this,
                                        LoginActivity.class).putExtra("from_verify_petition",
                                        false));
                        }

                        finish();
                    }

                    break;
            }
            return true;
        }
    });

    @Override
    public void onCreate(Bundle savedInstanceState) {
        //for ActionbarSherlock and AppCompat we have to use requestWindowFeature()
        //before super.onCreate() else use it after super.onCreate() and before
        //setContentView()
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if(DEBUG) {
            try {
                PackageInfo info = getPackageManager().getPackageInfo(
                        getPackageName(),
                        PackageManager.GET_SIGNATURES);
                for (Signature signature : info.signatures) {
                    MessageDigest md = MessageDigest.getInstance("SHA");
                    md.update(signature.toByteArray());
                    Log.d(Const.DEBUG, Base64.encodeToString(md.digest(), Base64.DEFAULT));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mProfile = new Profile(this);
//        progressDialog = new ProgressDialog(this);
//        progressDialog.setIndeterminate(true);
//        progressDialog.setCancelable(true);
//        progressDialog.getWindow().setGravity(Gravity.BOTTOM);

        if (mProfile.isUserLoggedIn())
            mIsUserLoggedIn = true;
        else
            mIsUserLoggedIn = false;


        new Thread() {
            @Override
            public void run() {
                try {
                    int waited = 0;
                    while (waited < SPLASH_TIME) {
                        sleep(100);
                        waited += 100;
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (!mInterrupted) {
                        mSplashTimeOut = true;
                        handler.sendEmptyMessage(END_OF_INTERVAL);
                    }
                }
            }
        }.start();


        if (Const.DEBUGGING)
            displayDeliveryReportsTable();


        SharedPreferences sharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(this);
        boolean sentToken = sharedPreferences
                .getBoolean(QuickstartPreferences.SENT_TOKEN_TO_SERVER, false);
        if (sentToken) {
            if (Const.DEBUGGING) {
                Toast.makeText(SplashActivity.this, "Already registered for push.", Toast.LENGTH_SHORT).show();
            }
        } else {
            if (Const.DEBUGGING) {
                Toast.makeText(SplashActivity.this, "Not registered for push.", Toast.LENGTH_SHORT).show();
            }

            if (checkPlayServices()) {
                // Start IntentService to register this application with GCM.

                Intent intent = new Intent(this, RegistrationIntentService.class);
                startService(intent);
            } else {
                if (Const.DEBUGGING) {
                    Toast.makeText(SplashActivity.this, "Doesn't have Google Play Services.", Toast.LENGTH_SHORT).show();
                    Log.d(Const.DEBUG, "Doesn't have Google Play Services");
                }

            }

        }


        if (mIsUserLoggedIn) {
            checkIsUprgraded();
        } else {
            checkShouldUpdate();
        }

    }

    private void checkIsUprgraded() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "checkIsUpgraded()");

        CheckIsUpgradedTask task = new CheckIsUpgradedTask();
        task.execute();
    }


    private class CheckIsUpgradedTask extends AsyncTask<Void, Void, Boolean> {

        String check_is_upgraded_url = Const.FINAL_URL + Const.URLs.CHECK_ID_UPGRADE_OR_NOT
                + "memberid=" + mProfile.getMemberId()
                + "&memberid_type=" + mProfile.getMemberIdType();
        String response = "";

        @Override
        protected Boolean doInBackground(Void... params) {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Check is Upgraded Url = " + check_is_upgraded_url);

            response = getResponse(check_is_upgraded_url);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (response != null) {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Response: " + response);

                /*
                [
                {
                    responce: "failure",
                            status: "Not Upgraded",
                        is_upgraded: false
                }
                ]
                */

                try {

                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    String response = jsonObject.getString("responce");

                    if (response.equalsIgnoreCase("success")) {

                        // User upgraded, re-login
                        Toast.makeText(SplashActivity.this, "Your session expired, Please login again.", Toast.LENGTH_LONG).show();
                        mProfile.removePreferences();
                        prefs.edit().clear().apply();

                        startActivity(new Intent(SplashActivity.this, LoginActivity.class));
                        SplashActivity.this.finish();
                    } else {

                        // Check for App Update
                        checkShouldUpdate();
                    }


                } catch (Exception e) {
                    e.printStackTrace();

                    if (Const.DEBUGGING)
                        Log.d(Const.DEBUG, "Exception while parsing Check App Update Url");
                }
            } else {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Response for Check ID Upgrade or Not is Null");

                checkShouldUpdate();
            }
        }
    }


    private void checkShouldUpdate() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "checkShouldUpdate()");

        CheckAppUpdateTask task = new CheckAppUpdateTask();
        task.execute();
    }

    private void displayDeliveryReportsTable() {

        mDeliveryReportsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getDeliveryReportsTableDbAdapter();
        List<ItemDeliveryReportsTable> items = mDeliveryReportsTableDbAdapter.displayDeliveryReportsTable();

        if (items.size() > 0) {

            for (int i = 0; i < items.size(); i++) {

                ItemDeliveryReportsTable item = items.get(i);

                if (Const.DEBUGGING) {

                    Log.d(Const.DEBUG, "Row " + (i + 1));
                    Log.d(Const.DEBUG, "E-Petition Number: " + item.getE_petition_number());
                    Log.d(Const.DEBUG, "Petition Number: " + item.getPetition_number());
                    Log.d(Const.DEBUG, "Sent From: " + item.getSent_from());
                    Log.d(Const.DEBUG, "Sent To: " + item.getSent_to());
                    Log.d(Const.DEBUG, "Sent SMS Success: " + item.getSent_sms_success());
                    Log.d(Const.DEBUG, "Deliver SMS Success: " + item.getDeliver_sms_success());
                    Log.d(Const.DEBUG, "E-Petition Number: " + item.getE_petition_number());
                    Log.d(Const.DEBUG, "Synced: " + item.getSynced());
                    Log.d(Const.DEBUG, "Member ID: " + item.getMember_id());
                    Log.d(Const.DEBUG, "Member ID Type: " + item.getMember_id_type());
                }

            }

        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Table is Empty");
        }
    }

    private void displayVersionUpdateAlert(boolean canSkip, String msg) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        AlertDialog dialog;

        alert.setTitle("New Version Available");

//        String message = "We have a new version available on Play Store " +
//                "and its highly recommended that you update it immediately. ";

        alert.setMessage(msg);
        alert.setPositiveButton("UPDATE",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        mInterrupted = true;


                        try {
                            Uri uri = Uri.parse("market://details?id="
                                    + SplashActivity.this.getPackageName());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {

                            Uri uri = Uri
                                    .parse("http://play.google.com/store/apps/details?id="
                                            + SplashActivity.this.getPackageName());
                            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                                    | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                                    | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                            startActivity(intent);
                        }

                        SplashActivity.this.finish();
                    }
                });

        if (canSkip) {
            alert.setNegativeButton("SKIP", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                    dialog.dismiss();
                    syncDeliveryReports();
                }
            });
        }


        dialog = alert.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private class CheckAppUpdateTask extends AsyncTask<Void, Void, Boolean> {

        String check_app_update_url = Const.FINAL_URL + Const.URLs.CHECK_APP_VERSION_UPDATES;
        String response = "";

        @Override
        protected void onPreExecute() {
            super.onPreExecute();


//            if (Const.DEBUGGING) {
//                if (!isFinishing() && progressDialog != null && !progressDialog.isShowing()) {
//                    progressDialog.setMessage("Checking App Version...");
//                    progressDialog.show();
//                }
//            }

        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Check App Update Url = " + check_app_update_url);

            response = getResponse(check_app_update_url);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

//            if (!isFinishing() && progressDialog != null && progressDialog.isShowing())
//                progressDialog.dismiss();

            if (response != null) {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Response: " + response);

            /*
            [
                {
                    app_ver_id: "d2375b385ac44f22a31d72fabab029b5",
                    app_ver: "1.1",
                    app_nm: "ICRF - (For every Indian)",
                    display_msg: "Hey.., Right now you are you are using Older version, with huse changes Newer is waiting for you, Please be updated..!",
                    update_priority: "H"
                }
            ]
             */


                try {

                    JSONArray jsonArray = new JSONArray(response);
                    JSONObject jsonObject = jsonArray.getJSONObject(0);

                    if (BuildConfig.VERSION_CODE >= jsonObject.getInt("app_ver_code")) {

                        if (Const.DEBUGGING)
                            Log.d(Const.DEBUG, "Versions are equal");

                        syncDeliveryReports();
                    } else {

                        if (Const.DEBUGGING)
                            Log.d(Const.DEBUG, "Versions are not equal");


                        boolean mShouldShowSkip = false;

                        if (jsonObject.getString("update_priority").equalsIgnoreCase("H")) {//High
                            mShouldShowSkip = false;
                        } else {
                            mShouldShowSkip = true;
                        }

                        displayVersionUpdateAlert(mShouldShowSkip, jsonObject.getString("display_msg"));
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    if (Const.DEBUGGING)
                        Log.d(Const.DEBUG, "Exception while parsing Check App Update Url");
                }
            } else {
                // Response is Null
                // Proceed with SyncDeliveryReports
                syncDeliveryReports();
            }


        }
    }

    private void syncDeliveryReports() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "syncDeliveryReports()");


        mDeliveryReportsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getDeliveryReportsTableDbAdapter();
        List<ItemDeliveryReportsTable> items = mDeliveryReportsTableDbAdapter.getNonSyncedDeliveryReports();

        if (items.size() > 0) {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "No of Non-Synced Items: " + items.size());
            sync(items);
        } else {

//            if (!isFinishing() && progressDialog != null && progressDialog.isShowing())
//                progressDialog.dismiss();

            mTriedSync = true;
            handler.sendEmptyMessage(END_OF_INTERVAL);
        }
    }

    private void sync(List<ItemDeliveryReportsTable> arrayList) {

        SyncTask task = new SyncTask(arrayList, this);
        task.execute();
    }

    public class SyncTask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        List<ItemDeliveryReportsTable> items;
        private InternetConnectivity check;

        public SyncTask(List<ItemDeliveryReportsTable> items, Context context) {
            this.items = items;
            this.context = context;
            check = new InternetConnectivity(context);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

//            if (!isFinishing() && progressDialog != null && !progressDialog.isShowing()) {
//                progressDialog.setMessage("Initializing...");
//                progressDialog.show();
//            }
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            if (check.hasActiveInternetConnection())
                hasInternetConnection = true;
            else
                hasInternetConnection = false;

            if (hasInternetConnection) {

                for (int i = 0; i < items.size(); i++) {

                    ItemDeliveryReportsTable item = items.get(i);

                    String member_id = item.getMember_id();
                    String e_petition_no = item.getE_petition_number();
                    String petition_no = item.getPetition_number();
                    String sent_from = item.getSent_from();
                    String sent_to = item.getSent_to();
                    String sms_message = item.getSms_content();
                    String confirmation_message = item.getConfirmation_message();
                    int sms_sent_success = item.getSent_sms_success();
                    int sms_deliver_success = item.getDeliver_sms_success();
                    String member_id_type = item.getMember_id_type();

                    try {

                        String give_support_url = Const.FINAL_URL + Const.URLs.GIVE_SUPPORT;
                        give_support_url = give_support_url + "memberid=" + member_id;
                        give_support_url = give_support_url + "&petitionno=" + petition_no;
                        //give_support_url = give_support_url + "&message=" + URLEncoder.encode(sms_message, "UTF-8");
                        give_support_url = give_support_url + "&message=" + URLEncoder.encode(confirmation_message, "UTF-8");
                        give_support_url = give_support_url + "&memberid_type=" + member_id_type;

                        if (Const.DEBUGGING)
                            Log.d(Const.DEBUG, "Give Support Url = " + give_support_url);

                        String give_support_url_response = getResponse(give_support_url);

                        if (give_support_url_response != null) {

                            if (Const.DEBUGGING)
                                Log.d(Const.DEBUG, "Response: " + give_support_url_response);

                            JSONObject json = new JSONObject(give_support_url_response);

                            String support_response = json.getString("responce");
                            int points = Integer.parseInt(json.getString("status").split(":")[1]);

                            if (support_response.equalsIgnoreCase("success")) {

                                String store_points_drs_url = Const.FINAL_URL + Const.URLs.STORE_PTS_DRS;
                                store_points_drs_url = store_points_drs_url + "epetino=" + e_petition_no;
                                store_points_drs_url = store_points_drs_url + "&memberid=" + member_id;
                                store_points_drs_url = store_points_drs_url + "&frmmob=" + sent_from;
                                store_points_drs_url = store_points_drs_url + "&towhom=" + sent_to;
                                store_points_drs_url = store_points_drs_url + "&memberid_type=" + member_id_type;

                                if (Const.DEBUGGING)
                                    Log.d(Const.DEBUG, "Store Points Drs Url = " + store_points_drs_url);

                                String store_points_drs_url_response = getResponse(store_points_drs_url);

                                if (store_points_drs_url_response != null) {

                                    if (Const.DEBUGGING)
                                        Log.d(Const.DEBUG, "Response: " + store_points_drs_url_response);

                                    JSONObject jsonResponseDRs = new JSONObject(store_points_drs_url_response);
                                    if (jsonResponseDRs != null) {

                                        String response = jsonResponseDRs.getString("responce");
                                        if (response.equals("success")) {

                                            if (Const.DEBUGGING)
                                                Log.d(Const.DEBUG, "DR delivery to server successful");

                                            mDeliveryReportsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getDeliveryReportsTableDbAdapter();
                                            mDeliveryReportsTableDbAdapter.beginTransaction();
                                            try {

                                                mDeliveryReportsTableDbAdapter.updateSynced(member_id, e_petition_no, petition_no, sent_from, sent_to, 1);
                                                mDeliveryReportsTableDbAdapter.setTransactionSuccessful();

                                                if (Const.DEBUGGING)
                                                    Log.d(Const.DEBUG, "SMS Synced Updated in Database");

                                                Context ctx = getApplicationContext();
                                                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
                                                prefs.edit().putInt(Const.Prefs.MY_TOTAL_POINTS, prefs.getInt(Const.Prefs.MY_TOTAL_POINTS, 0) + points).commit();

                                            } catch (Exception e) {
                                                e.printStackTrace();
                                            } finally {
                                                mDeliveryReportsTableDbAdapter.endTransaction();
                                            }
                                        } else {
                                            if (Const.DEBUGGING)
                                                Log.d(Const.DEBUG, "DR delivery failed");
                                        }
                                    }
                                }
                            }


                        }


                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }


            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

//            if (!isFinishing() && progressDialog != null && progressDialog.isShowing())
//                progressDialog.dismiss();

            if (aBoolean) {
                mTriedSync = true;
                sendEmptyMessage();
            }
        }
    }


    public String getResponse(String url) {

        try {
            URL requestUrl = new URL(url);
            URLConnection con = requestUrl.openConnection();
            con.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder sb = new StringBuilder();
            int cp;
            try {
                while ((cp = in.read()) != -1) {
                    sb.append((char) cp);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return sb.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    private void sendEmptyMessage() {

        if (handler != null)
            handler.sendEmptyMessage(END_OF_INTERVAL);
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_BACK:
                mInterrupted = true;
                finish();
                return true;
            default:
                return false;
        }
    }


    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "SplashActivity -> checkPlayServices()");

        GoogleApiAvailability apiAvailability = GoogleApiAvailability.getInstance();
        int resultCode = apiAvailability.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS) {
            if (apiAvailability.isUserResolvableError(resultCode)) {
                apiAvailability.getErrorDialog(this, resultCode, PLAY_SERVICES_RESOLUTION_REQUEST)
                        .show();
            } else {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "This device is not supported.");
                //finish();
            }
            return false;
        }
        return true;
    }


}
