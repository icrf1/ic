package com.apex.icrf;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import com.apex.icrf.Interfaces.SmsListner;

/**
 * Created by Apex on 8/21/2017.
 */

public class SmsReceiver extends BroadcastReceiver {
    public static SmsListner smsListner;
    Boolean b = false;
    String OTP = "";
    String TAG = "SmsReceiver";
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d(TAG,"receiver started ");
        Bundle bundle= intent.getExtras();
        Log.d(TAG,"bundle "+ bundle);
        Object[] pdus = (Object[]) bundle.get("pdus");
        Log.d(TAG,"pdus "+ pdus);
        for (int i = 0;i<pdus.length;i++)
        {
            SmsMessage smsMessage=SmsMessage.createFromPdu((byte[]) pdus[i]);
            String sender = smsMessage.getDisplayOriginatingAddress();
            Log.d(TAG,"sender "+ sender);
            b = sender.endsWith("ICRFEP");

            Log.d(TAG,"b "+ b);

            OTP=sender.replaceAll("[^0-9]","");

            Log.d(TAG,"OTP "+ OTP);

            if (b)
            {
                smsListner.messageReceived(OTP);
            }
            else
            {

            }
        }
    }
    public static void bindListener(SmsListner listener) {
        smsListner = listener;
    }


}
