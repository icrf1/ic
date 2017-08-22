package com.apex.icrf.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;

/**
 * Created by WASPVamsi on 19/09/15.
 */
public class SMSDeliveredReceiver extends BroadcastReceiver {

    DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;

    @Override
    public void onReceive(Context context, Intent intent) {

        String from_mobile = intent.getStringExtra("from_mobile");
        String to_mobile = intent.getStringExtra("to_mobile");
        String member_id = intent.getStringExtra("member_id");
        String petition_no = intent.getStringExtra("petition_no");
        String sms_message = intent.getStringExtra("sms_message");
        String confirmation_message = intent.getStringExtra("confirmation_message");
        String e_petition_no = intent.getStringExtra("e_petition_no");
        String member_id_type = intent.getStringExtra("member_id_type");

        if (Const.DEBUGGING) {
            Log.d(Const.DEBUG, "Details in Receiver: ");
            Log.d(Const.DEBUG, from_mobile + "  " + to_mobile + "  " + member_id + "  " + petition_no + "  " + sms_message + "  " +
                    confirmation_message + "  " + "" + e_petition_no + "  " + member_id_type);
        }


        mDeliveryReportsTableDbAdapter = DatabaseHelper.get(context.getApplicationContext()).getDeliveryReportsTableDbAdapter();

        switch (getResultCode()) {
            case Activity.RESULT_OK:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Delivered Successfully to " + to_mobile);

                mDeliveryReportsTableDbAdapter.beginTransaction();
                try {

                    mDeliveryReportsTableDbAdapter.updateDeliverSMSSuccess(member_id, Integer.parseInt(e_petition_no), petition_no, from_mobile, to_mobile, 1);
                    mDeliveryReportsTableDbAdapter.setTransactionSuccessful();

                    if (Const.DEBUGGING)
                        Log.d(Const.DEBUG, "SMS Delivery Updated in Database");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mDeliveryReportsTableDbAdapter.endTransaction();
                }


                Intent serviceIntent = new Intent(context, SMSDeliveredService.class);
                serviceIntent.putExtra("from_mobile", from_mobile);
                serviceIntent.putExtra("to_mobile", to_mobile);
                serviceIntent.putExtra("member_id", member_id);
                serviceIntent.putExtra("petition_no", petition_no);
                serviceIntent.putExtra("sms_message", sms_message);
                serviceIntent.putExtra("confirmation_message", confirmation_message);
                serviceIntent.putExtra("e_petition_no", e_petition_no);
                serviceIntent.putExtra("memberid_type", member_id_type);

                context.startService(serviceIntent);
                break;
            case Activity.RESULT_CANCELED:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Delivery Failed to " + to_mobile);
                break;
        }
    }
}
