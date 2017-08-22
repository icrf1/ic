package com.apex.icrf.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.apex.icrf.Const;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by WASPVamsi on 21/09/15.
 */
public class InternetConnectivity {

    private Context context;


    public InternetConnectivity(Context context) {
        this.context = context;
    }


    public boolean isNetworkAvailable() {

        ConnectivityManager connectivityManager
                = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null;
    }


    public boolean hasActiveInternetConnection() {
        if (isNetworkAvailable()) {
            try {
                HttpURLConnection urlc = (HttpURLConnection) (new URL("http://www.google.com").openConnection());
                urlc.setRequestProperty("User-Agent", "Test");
                urlc.setRequestProperty("Connection", "close");
                urlc.setConnectTimeout(1500);
                urlc.connect();
                return (urlc.getResponseCode() == 200);
            } catch (IOException e) {
                if (Const.DEBUGGING)
                    Log.e(Const.DEBUG, "Error checking internet connection", e);
            }
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "No network available!");
        }
        return false;
    }
}
