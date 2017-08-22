package com.apex.icrf.utils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Telephony;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.SmsMessage;

/**
 * Created by WASPVamsi on 14/04/16.
 */
public class IncomingSmsReceiver extends BroadcastReceiver {

    public String ICRF_OTP_ORIGIN = "ICRFEP";
    public String OTP_DELIMITER = "-";

    @Override
    public void onReceive(Context context, Intent intent) {

        SmsMessage currentMessage = null;

        final Bundle bundle = intent.getExtras();
        try {

            if (bundle != null) {
                Object[] pdusObjArray = (Object[]) bundle.get("pdus");

                for (Object pdusObj : pdusObjArray) {

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        SmsMessage[] msgs = Telephony.Sms.Intents.getMessagesFromIntent(intent);
                        currentMessage = msgs[0];
                    } else {
                        currentMessage = SmsMessage.createFromPdu((byte[]) pdusObj);
                    }

                    if (currentMessage != null) {

                        String senderAddress = currentMessage.getDisplayOriginatingAddress();
                        String message = currentMessage.getDisplayMessageBody();

                        if (!senderAddress.toLowerCase().contains(ICRF_OTP_ORIGIN.toLowerCase())) {
                            return;
                        }

                        String verification_code = getVerificationCode(message);
                        if(verification_code != null) {

                            Intent i = new Intent("received_otp_sms");
                            i.putExtra("otp_code", verification_code);
                            LocalBroadcastManager.getInstance(context).sendBroadcast(i);
                        }
                    }


                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public String getVerificationCode(String message) {

        String code = null;

        int index = message.indexOf(OTP_DELIMITER);

        if (index != -1) {
            int start = index + 2;
            int length = 5;
            code = message.substring(start, start + length).trim();
        }

        return code;
    }
}
