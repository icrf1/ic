package com.apex.icrf.utils;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.SmsManager;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;

/**
 * Created by WASPVamsi on 19/09/15.
 */
public class SMSSentReceiver extends BroadcastReceiver {

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

        mDeliveryReportsTableDbAdapter = DatabaseHelper.get(context.getApplicationContext()).getDeliveryReportsTableDbAdapter();

        switch (getResultCode()) {
            case Activity.RESULT_OK:

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Sent Successfully to " + to_mobile);

                mDeliveryReportsTableDbAdapter.beginTransaction();
                try {

                    mDeliveryReportsTableDbAdapter.updateSentSMSSuccess(member_id, Integer.parseInt(e_petition_no), petition_no, from_mobile, to_mobile, 1);
                    mDeliveryReportsTableDbAdapter.setTransactionSuccessful();

                    if (Const.DEBUGGING)
                        Log.d(Const.DEBUG, "SMS Sent Updated in Database");

                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mDeliveryReportsTableDbAdapter.endTransaction();
                }

                break;
            case SmsManager.RESULT_ERROR_GENERIC_FAILURE:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Sending Failed to " + to_mobile + ". Reason: Generic Failure");
                break;
            case SmsManager.RESULT_ERROR_NO_SERVICE:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Sending Failed to " + to_mobile + ". Reason: No Service");
                break;
            case SmsManager.RESULT_ERROR_NULL_PDU:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Sending Failed to " + to_mobile + ". Reason: Null PDU");
                break;
            case SmsManager.RESULT_ERROR_RADIO_OFF:
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Message Sending Failed to " + to_mobile + ". Reason: Radio OFF");
                break;
        }
    }
}
