package com.apex.icrf.utils;

import android.content.Context;

import com.jakewharton.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import okhttp3.OkHttpClient;

/**
 * Created by WASPVamsi on 06/06/16.
 */
public class OkHttpClientHelper {

    public static OkHttpClient client = null;
    public static Picasso picasso = null;

    public static OkHttpClient getClient() {

        if (client == null) {
            client = new OkHttpClient();
        }

        return client;
    }

    public static Picasso getPicassoBuilder(Context context) {

        if (picasso == null)
            picasso = new Picasso.Builder(context).downloader(new OkHttp3Downloader(getClient())).build();

        return picasso;
    }
}
