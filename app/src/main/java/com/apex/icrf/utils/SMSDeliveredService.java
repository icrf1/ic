package com.apex.icrf.utils;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;
import com.apex.icrf.database.PetitionsTableDbAdapter;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * Created by WASPVamsi on 20/09/15.
 */
public class SMSDeliveredService extends Service {

    DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;
    PetitionsTableDbAdapter mPetitionsTableDbAdapter;

    @Override
    public void onCreate() {
        super.onCreate();

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "SMSDeliveredService - onCreate()");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, final int startId) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "SMSDeliveredService - onStartCommand()");

        new Thread(new Runnable() {
            @Override
            public void run() {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Thread is Running");

                String from_mobile = intent.getStringExtra("from_mobile");
                String to_mobile = intent.getStringExtra("to_mobile");
                String member_id = intent.getStringExtra("member_id");
                String petition_no = intent.getStringExtra("petition_no");
                String sms_message = intent.getStringExtra("sms_message");
                String confirmation_message = intent.getStringExtra("confirmation_message");
                String e_petition_no = intent.getStringExtra("e_petition_no");
                String member_id_type = intent.getStringExtra("memberid_type");

                if (Const.DEBUGGING) {
                    Log.d(Const.DEBUG, "Details in Service: ");
                    Log.d(Const.DEBUG, from_mobile + "  " + to_mobile + "  " + member_id + "  " + petition_no + "  " + sms_message + "  " +
                            confirmation_message + "  " + "" + e_petition_no + "  " + member_id_type);
                }

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

                        if (support_response.equalsIgnoreCase("success")) {

                            int points = Integer.parseInt(json.getString("status").split(":")[1]);

                            String store_points_drs_url = Const.FINAL_URL + Const.URLs.STORE_PTS_DRS;
                            store_points_drs_url = store_points_drs_url + "epetino=" + e_petition_no;
                            store_points_drs_url = store_points_drs_url + "&memberid=" + member_id;
                            store_points_drs_url = store_points_drs_url + "&frmmob=" + from_mobile;
                            store_points_drs_url = store_points_drs_url + "&towhom=" + to_mobile;
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

                                            mDeliveryReportsTableDbAdapter.updateSynced(member_id, e_petition_no, petition_no, from_mobile, to_mobile, 1);
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


                                        mPetitionsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getPetitionsTableDbAdapter();
                                        mPetitionsTableDbAdapter.beginTransaction();
                                        try {

                                            mPetitionsTableDbAdapter.updateStatus(e_petition_no, petition_no);
                                            mPetitionsTableDbAdapter.setTransactionSuccessful();


                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        } finally {
                                            mPetitionsTableDbAdapter.endTransaction();
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

                stopSelf(startId);
            }
        }).start();


        return Service.START_REDELIVER_INTENT;
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
}
