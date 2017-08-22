/**
 * Copyright 2015 Google Inc. All Rights Reserved.
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.apex.icrf.gcm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.MainTabbedActivity;
import com.apex.icrf.R;
import com.apex.icrf.SplashActivity;
import com.apex.icrf.classes.ItemPushNotificationsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.PushNotificationsTableDbAdapter;
import com.apex.icrf.utils.Profile;
import com.google.android.gms.gcm.GcmListenerService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class MyGcmListenerService extends GcmListenerService {

    private static final String TAG = "MyGcmListenerService";
    private PushNotificationsTableDbAdapter mPushNotificationsTableDbAdapter;
    private String current_url;
    private Profile mProfile;

    /**
     * Called when message is received.
     *
     * @param from SenderID of the sender.
     * @param data Data bundle containing message data as key/value pairs.
     *             For Set of keys use data.keySet().
     */
    // [START receive_message]
    @Override
    public void onMessageReceived(String from, Bundle data) {
        String message = data.getString("message");
        String url = data.getString("image_url");
        current_url = url;

        if (Const.DEBUGGING) {
            Log.d(TAG, "From: " + from);
            Log.d(TAG, "Message: " + message);
            Log.d(TAG, "URL: " + url);
        }

        if (from.startsWith("/topics/")) {
            // message received from some topic.
        } else {
            // normal downstream message.
        }

        // [START_EXCLUDE]
        /**
         * Production applications would usually process the message here.
         * Eg: - Syncing with server.
         *     - Store message in local database.
         *     - Update UI.
         */

        mPushNotificationsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getPushNotificationsTableDbAdapter();
        mPushNotificationsTableDbAdapter.beginTransaction();

        ItemPushNotificationsTable item = new ItemPushNotificationsTable();
        item.setPush_message(message);
        item.setPush_image(url);

        try {
            mPushNotificationsTableDbAdapter.insertRow(item);

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Push inserted into Database successfully");

            mPushNotificationsTableDbAdapter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mPushNotificationsTableDbAdapter.endTransaction();
        }

        /**
         * In some cases it may be useful to show a notification indicating to the user
         * that a message was received.
         */
        sendNotification(message);
        // [END_EXCLUDE]
    }
    // [END receive_message]

    /**
     * Create and show a simple notification containing the received GCM message.
     *
     * @param message GCM message received.
     */
    private void sendNotification(String message) {

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(this);

        Intent notificationIntent;

        if(prefs.contains(Const.Login.USER_ID) || prefs
                .contains(Const.Login.USER_NAME)) {
            notificationIntent = new Intent(this, MainTabbedActivity.class).putExtra("from_service", "GCMService");
        } else {
            notificationIntent = new Intent(this, SplashActivity.class);
        }


        //Intent notificationIntent = new Intent(this, SplashActivity.class);
        notificationIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        int notificationId = prefs.getInt("notificationId", 0);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                notificationId, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Bitmap large_icon = BitmapFactory.decodeResource(getApplicationContext().getResources(), R.drawable.ic_splash_icrf_logo_v_2_1);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setLargeIcon(large_icon)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("ICRF")
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .setContentText(message)
                .setAutoCancel(true)
                .setSound(defaultSoundUri)
                .setDefaults(Notification.DEFAULT_ALL);

        if (current_url != null && !current_url.equalsIgnoreCase("")) {
            Bitmap url_icon = getBitmap(current_url);
            NotificationCompat.BigPictureStyle bigPicStyle = new NotificationCompat.BigPictureStyle();
            bigPicStyle.bigPicture(url_icon);
            bigPicStyle.setBigContentTitle(message);
            builder.setStyle(bigPicStyle);
        }


        NotificationManager mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(notificationId, builder.build());

        prefs.edit().putInt("notificationId", notificationId + 1).commit();


    }

    public Bitmap getBitmap(String image_url) {

        InputStream in;
        try {
            URL url = new URL(image_url);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoInput(true);
            connection.connect();
            in = connection.getInputStream();
            Bitmap myBitmap = BitmapFactory.decodeStream(in);
            return myBitmap;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;

    }
}
