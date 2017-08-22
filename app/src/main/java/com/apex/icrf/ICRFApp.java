package com.apex.icrf;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.support.multidex.MultiDex;

import com.android.volley.VolleyLog;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.diskcache.ImageCacheManager;
import com.apex.icrf.diskcache.RequestManager;
import com.crashlytics.android.Crashlytics;
import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import java.util.HashMap;

import io.fabric.sdk.android.Fabric;

/**
 * Created by WASPVamsi on 10/09/15.
 */
public class ICRFApp extends Application {

    private static int DISK_IMAGECACHE_SIZE = 1024 * 1024 * 1000; // (1000MB)
    private static Bitmap.CompressFormat DISK_IMAGECACHE_COMPRESS_FORMAT = Bitmap.CompressFormat.PNG;
    private static int DISK_IMAGECACHE_QUALITY = 100;

    DatabaseHelper mDatabaseHelper;
    private Tracker mTracker;

    private static final String PROPERTY_ID = "UA-78518064-1";

    public enum TrackerName {
        APP_TRACKER, // Tracker used only in this app.
        GLOBAL_TRACKER, // Tracker used by all the apps from a company
        ECOMMERCE_TRACKER, // Tracker used by all ecommerce transactions from a
        // company.
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());

        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(getApplicationContext());

        VolleyLog.DEBUG = Const.DEBUGGING;

        RequestManager.init(this);
        createImageCache();

        mDatabaseHelper = new DatabaseHelper(getApplicationContext());
        mDatabaseHelper.initialize();
    }

    @Override
    public Object getSystemService(String name) {
        if (DatabaseHelper.DATABASE_HELPER_SERVICE.equals(name)) {
            return mDatabaseHelper;
        } else {
            return super.getSystemService(name);
        }
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void createImageCache() {
        ImageCacheManager.getInstance().init(this, this.getPackageCodePath(),
                DISK_IMAGECACHE_SIZE, DISK_IMAGECACHE_COMPRESS_FORMAT,
                DISK_IMAGECACHE_QUALITY);
    }

    HashMap<TrackerName, Tracker> mTrackers = new HashMap<TrackerName, Tracker>();

    public synchronized Tracker getTracker(TrackerName trackerId) {
        if (!mTrackers.containsKey(trackerId)) {

            GoogleAnalytics analytics = GoogleAnalytics.getInstance(this);
            analytics.getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            Tracker t = null;

            if (trackerId == TrackerName.APP_TRACKER) {
                t = analytics.newTracker(PROPERTY_ID);
                t.enableAdvertisingIdCollection(true);
                t.enableAutoActivityTracking(true);
            }

            mTrackers.put(trackerId, t);

        }
        return mTrackers.get(trackerId);
    }



}
