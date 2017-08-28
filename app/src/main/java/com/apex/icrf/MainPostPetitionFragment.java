package com.apex.icrf;

import android.*;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.JsResult;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.apex.icrf.fragments.Guidelines_Fragment;
import com.apex.icrf.utils.InternetConnectivity;
import com.apex.icrf.utils.Profile;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationRequest;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by WASPVamsi on 26/10/15.
 */
public class MainPostPetitionFragment extends Fragment/* implements LocationListener*/ {


    private static final String DIRECTORY = "Icrf";

    public static final String URL = "http://icrf.org.in/index.aspx";
    public static final String STRING_1 = "90ac16783fbc54c3689c63b3ab89b39cea28f8743a88ff34e6a1c61ed746d0e2=";
    public static final String STRING_2 = "&39fea455630cbe2061bb70aa8eb1c6af5012c330694af0f096782ea95493e7e7=";
    public static final String STRING_3 = "&f27fede2220bcd326aee3e86ddfd4ebd0fe58cb9=app";
    public static final String STRING_4 = "&latitude=";
    public static final String STRING_5 = "&longitude=";

//    public static final String URL = "http://www.icrf.org.in/member/app_e_petition_new.aspx?";
//    public static final String STRING_1 = "&latitude=";
//    public static final String STRING_2 = "&longitude=";
    private Activity activity;
    private WebView webView;
    private ProgressDialog mProgressDialog;
    private MyWebViewClient mMyWebViewClient;
    private MyWebChromeClient mMyWebChromeClient;

    private ValueCallback<Uri> mUploadMessage;
    private final static int FILECHOOSER_RESULTCODE = 1;
    public ValueCallback<Uri[]> uploadMessage;
    public static final int REQUEST_SELECT_FILE = 100;
    public static final int INPUT_FILE_REQUEST_CODE = 2;
    private Uri mCapturedImageURI = null;

    private ValueCallback<Uri[]> mFilePathCallback;
    private String mCameraPhotoPath;

    String final_url;

    Profile profile;

    private GoogleApiClient mGoogleApiClient;
    private Location mCurrentLocation, mLastLocation;
    private String mLastUpdateTime;
    private LocationRequest mLocationRequest;

    private double mLatitude = 0.0, mLongitude = 0.0;

    private Uri imageUri;
    private Bundle bundle;

    public final int SMSREADPERMISSION = 0002;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_post_petition, container, false);

        Log.d(Const.DEBUG,"MainPostPetitionFragment called");
        bundle = getArguments();
        if (bundle != null) {
            mLatitude = Double.parseDouble(bundle.getString("latitude"));
            mLongitude = Double.parseDouble(bundle.getString("longitude"));
        }

        webView = (WebView) view.findViewById(R.id.webView);

        profile = new Profile(activity);

        /*
        if (mGoogleApiClient == null) {
            mGoogleApiClient = new GoogleApiClient.Builder(activity)
                    .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                        @Override
                        public void onConnected(Bundle bundle) {

                            mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
                                    mGoogleApiClient);
                            if (mLastLocation != null) {
                                mLatitude = mLastLocation.getLatitude();
                                mLongitude = mLastLocation.getLongitude();
                            }

                            startLocationUpdates();
                        }

                        @Override
                        public void onConnectionSuspended(int i) {

                        }
                    })
                    .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                        @Override
                        public void onConnectionFailed(ConnectionResult connectionResult) {

                        }
                    })
                    .addApi(LocationServices.API)
                    .build();

            createLocationRequest();
        }
        */

        mProgressDialog = new ProgressDialog(activity);
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setMessage("Loading...");
        mProgressDialog.setCancelable(true);

        mMyWebViewClient = new MyWebViewClient();
        webView.setWebViewClient(mMyWebViewClient);
        webView.setWebChromeClient(new MyWebChromeClient());

        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAppCacheEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setPluginState(WebSettings.PluginState.ON);
        webView.setVerticalScrollBarEnabled(false);
        webView.setHorizontalScrollBarEnabled(false);


/*
 * this is guide line url
 * real url = http://www.icrf.org.in/member/app_e_petition_guidelines.aspx?90ac16783fbc54c3689c63b3ab89b39cea28f8743a88ff34e6a1c61ed746d0e2=10905&39fea455630cbe2061bb70aa8eb1c6af5012c330694af0f096782ea95493e7e7=I&f27fede2220bcd326aee3e86ddfd4ebd0fe58cb9=app&latitude=17.385044&longitude=78.486671
 */
        final_url = Const.URLs.E_PETITION_GUIDELINES_APP
                + STRING_1 + profile.getMemberId()
                + STRING_2 + profile.getMemberIdType()
                + STRING_3
                + STRING_4 + String.valueOf(mLatitude)
                + STRING_5 + String.valueOf(mLongitude);



//// direct to petition page skipping guidelines
//        final_url = Const.URLs.E_PETITION_GUIDELINES_APP
//                + STRING_1 + String.valueOf(mLatitude)
//                + STRING_2 + String.valueOf(mLongitude);

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Final URL = " + final_url);


        webView.setVisibility(View.VISIBLE);

        CheckNetworkTask task = new CheckNetworkTask();
        task.execute();

        return view;
    }




    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_post_petition, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if(item.getItemId() == R.id.action_browse) {
            String url = "http://www.icrf.org.in";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        }
//        if(item.getItemId() == R.id.action_guideLines) {
//        Guidelines_Fragment guidelines_fragment =new Guidelines_Fragment();
//        guidelines_fragment.setArguments(bundle);
//        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_fragment_container, guidelines_fragment).commit();
//        }

        return super.onOptionsItemSelected(item);
    }

    private class CheckNetworkTask extends AsyncTask<Void, Void, Boolean> {


        @Override
        protected Boolean doInBackground(Void... params) {

            InternetConnectivity obj = new InternetConnectivity(activity);

            if (obj.isNetworkAvailable() && obj.hasActiveInternetConnection()) {

                Log.d(Const.DEBUG, "Network is available");

                return true;
            } else {
                Log.d(Const.DEBUG, "Network is not available");

                return false;
            }


        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (true) {
                webView.setVisibility(View.VISIBLE);
                webView.loadUrl(final_url);
            } else {
                webView.setVisibility(View.GONE);
                Toast.makeText(activity, "No Internet Connection", Toast.LENGTH_LONG).show();
            }

        }
    }


    private class MyWebViewClient extends WebViewClient {

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);

            // Fix for #13 in Crashlytics - in Version ICRF v1.1
            if (!activity.isFinishing())
                mProgressDialog.show();
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            mProgressDialog.dismiss();
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);

            view.setVisibility(View.GONE);

            Toast.makeText(activity,
                    "Please check your internet " + "connection and try again",
                    Toast.LENGTH_SHORT).show();
        }


    }

        private class MyWebChromeClient extends WebChromeClient {

        @Override
        public boolean onJsAlert(WebView view, String url, String message, JsResult result) {
            return super.onJsAlert(view, url, message, result);
        }

        /*
        // For Android 3.0+
        public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;

            try {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                File externalDataDir = Environment.getExternalStorageDirectory();
//                File cameraDataDir = new File(externalDataDir.getAbsolutePath() +
//                        File.separator + "browser-photos");
//                cameraDataDir.mkdirs();

                File directory = new File(Environment.getExternalStorageDirectory()
                        + File.separator + DIRECTORY);

                if (!directory.exists())
                    directory.mkdir();

                String mCameraFilePath = directory.getAbsolutePath() + File.separator +
                        System.currentTimeMillis() + ".jpg";
                mCapturedImageURI = Uri.fromFile(new File(mCameraFilePath));

                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);

                Intent i = new Intent(Intent.ACTION_GET_CONTENT);
                i.addCategory(Intent.CATEGORY_OPENABLE);
                i.setType("image/*");

                Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, new Parcelable[]{cameraIntent});

                startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
            } catch (Exception e) {
                Toast.makeText(activity, "Camera Exception:" + e, Toast.LENGTH_LONG).show();
            }


//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("*//*");
//            startActivityForResult(
//                    Intent.createChooser(i, "File Browser"),
//                    FILECHOOSER_RESULTCODE);
        }



        // For 3.0+ Devices
        protected void openFileChooser(ValueCallback uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");
            startActivityForResult(Intent.createChooser(i, "File Browser"), FILECHOOSER_RESULTCODE);
        }
        */

        // openFileChooser for Android 3.0+
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
            mUploadMessage = uploadMsg;

//            File imageStorageDir = new File(
//                    Environment.getExternalStoragePublicDirectory(
//                            Environment.DIRECTORY_PICTURES)
//                    , "ICRF");
//            if (!imageStorageDir.exists()) {
//                imageStorageDir.mkdirs();
//            }

            File imageStorageDir =  Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[] { captureIntent });
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
        }


        //For Android 4.1
        public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
            mUploadMessage = uploadMsg;


            File imageStorageDir =  Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            Intent chooserIntent = Intent.createChooser(i, "File Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[] { captureIntent });
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);




//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("image/*");
//            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);

        }

        protected void openFileChooser(ValueCallback<Uri> uploadMsg) {
            mUploadMessage = uploadMsg;

            File imageStorageDir =  Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES);

            File file = new File(
                    imageStorageDir + File.separator + "IMG_"
                            + String.valueOf(System.currentTimeMillis())
                            + ".jpg");
            mCapturedImageURI = Uri.fromFile(file);

            final Intent captureIntent = new Intent(
                    android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
            captureIntent.putExtra(MediaStore.EXTRA_OUTPUT, mCapturedImageURI);
            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("image/*");

            Intent chooserIntent = Intent.createChooser(i, "File Chooser");
            // Set camera intent to file chooser
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS
                    , new Parcelable[] { captureIntent });
            // On select image call onActivityResult method of activity
            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);


//            Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//            i.addCategory(Intent.CATEGORY_OPENABLE);
//            i.setType("image/*");
//            startActivityForResult(Intent.createChooser(i, "File Chooser"), FILECHOOSER_RESULTCODE);
        }

        /*

        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
//            if (uploadMessage != null) {
//                uploadMessage.onReceiveValue(null);
//                uploadMessage = null;
//            }
//
//            uploadMessage = filePathCallback;
//
//            Intent intent = fileChooserParams.createIntent();
//            try {
//                startActivityForResult(intent, REQUEST_SELECT_FILE);
//            } catch (ActivityNotFoundException e) {
//                uploadMessage = null;
//                Toast.makeText(getActivity().getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
//                return false;
//            }
//            return true;


            // New Code
            // Double check that we don't have any existing callbacks
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }
            uploadMessage = filePathCallback;

            // Set up the take picture intent
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {

                // Create the File where the photo should go
                File directory = new File(Environment.getExternalStorageDirectory()
                        + File.separator + DIRECTORY);

                if (!directory.exists())
                    directory.mkdir();

                String mCameraFilePath = directory.getAbsolutePath() + File.separator +
                        System.currentTimeMillis() + ".jpg";

                File photoFile = null;
                try {
                    photoFile = new File(mCameraFilePath);
                    takePictureIntent.putExtra("PhotoPath", mCameraFilePath);
                } catch (Exception ex) {
                    // Error occurred while creating the File
                    Log.d(Const.DEBUG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraFilePath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            // Set up the intent to get an existing image
            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            // Set up the intents for the Intent chooser
            Intent[] intentArray;
            if (takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);


            return true;


        }



        // For Lollipop 5.0+ Devices
        public boolean onShowFileChooser(WebView mWebView, ValueCallback<Uri[]> filePathCallback, WebChromeClient.FileChooserParams fileChooserParams) {
            if (uploadMessage != null) {
                uploadMessage.onReceiveValue(null);
                uploadMessage = null;
            }

            uploadMessage = filePathCallback;

            Intent intent = fileChooserParams.createIntent();
            try {
                startActivityForResult(intent, REQUEST_SELECT_FILE);
            } catch (ActivityNotFoundException e) {
                uploadMessage = null;
                Toast.makeText(getActivity().getApplicationContext(), "Cannot Open File Chooser", Toast.LENGTH_LONG).show();
                return false;
            }
            return true;
        }

        */


        public boolean onShowFileChooser(
                WebView webView, ValueCallback<Uri[]> filePathCallback,
                WebChromeClient.FileChooserParams fileChooserParams) {
            if(mFilePathCallback != null) {
                mFilePathCallback.onReceiveValue(null);
            }
            mFilePathCallback = filePathCallback;

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (takePictureIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                File photoFile = null;
                try {
                    photoFile = createImageFile();
                    takePictureIntent.putExtra("PhotoPath", mCameraPhotoPath);
                } catch (IOException ex) {
                    Log.d(Const.DEBUG, "Unable to create Image File", ex);
                }

                // Continue only if the File was successfully created
                if (photoFile != null) {
                    mCameraPhotoPath = "file:" + photoFile.getAbsolutePath();
                    takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                            Uri.fromFile(photoFile));
                } else {
                    takePictureIntent = null;
                }
            }

            Intent contentSelectionIntent = new Intent(Intent.ACTION_GET_CONTENT);
            contentSelectionIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contentSelectionIntent.setType("image/*");

            Intent[] intentArray;
            if(takePictureIntent != null) {
                intentArray = new Intent[]{takePictureIntent};
            } else {
                intentArray = new Intent[0];
            }

            Intent chooserIntent = new Intent(Intent.ACTION_CHOOSER);
            chooserIntent.putExtra(Intent.EXTRA_INTENT, contentSelectionIntent);
            chooserIntent.putExtra(Intent.EXTRA_TITLE, "Image Chooser");
            chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intentArray);

            startActivityForResult(chooserIntent, INPUT_FILE_REQUEST_CODE);

            return true;
        }

    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES);
        File imageFile = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        Log.d(Const.DEBUG, "ImageFile Path: "+imageFile.getAbsolutePath());

        imageUri = Uri.fromFile(imageFile);

        return imageFile;
    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            if (requestCode == REQUEST_SELECT_FILE) {
//                if (uploadMessage == null)
//                    return;
//                uploadMessage.onReceiveValue(WebChromeClient.FileChooserParams.parseResult(resultCode, intent));
//                uploadMessage = null;
//            }


            if(requestCode != INPUT_FILE_REQUEST_CODE || mFilePathCallback == null) {
                super.onActivityResult(requestCode, resultCode, intent);
                return;
            }

            Uri[] results = null;

            // Check that the response is a good one
            if(resultCode == Activity.RESULT_OK) {

                Log.d(Const.DEBUG, "Result is OK");

                if(intent == null) {

                    Log.d(Const.DEBUG, "Intent is null");

                    // If there is not data, then we may have taken a photo
                    if(mCameraPhotoPath != null) {

                        Log.d(Const.DEBUG, "Camera Photo Path is Not null");

                        results = new Uri[]{Uri.parse(mCameraPhotoPath)};
                    }
                } else {

                    Log.d(Const.DEBUG, "Intent is not null");

                    String dataString = intent.getDataString();
                    if (dataString != null) {

                        Log.d(Const.DEBUG, "DataString is not null");

                        results = new Uri[]{Uri.parse(dataString)};
                    } else {

                        Log.d(Const.DEBUG, "DataString is null");

                        results = new Uri[]{imageUri};
                    }
                }
            }

            mFilePathCallback.onReceiveValue(results);
            mFilePathCallback = null;
            return;

        } else if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage) return;

            Uri result = null;
            try {
                if (resultCode != activity.RESULT_OK) {
                    result = null;
                } else {
                    // retrieve from the private variable if the intent is null
                    result = intent == null ? mCapturedImageURI : intent.getData();
                }
            } catch (Exception e) {
                Toast.makeText(activity, "activity :" + e,
                        Toast.LENGTH_LONG).show();
            }



//            Uri result = intent == null || resultCode != activity.RESULT_OK ? null
//                    : intent.getData();


            mUploadMessage.onReceiveValue(result);
            mUploadMessage = null;
        }
    }


    public boolean canGoBack() {

        if (webView != null) {
            if (webView.canGoBack())
                return true;
            else return false;
        } else return false;
    }

    public void goBack() {

        if (canGoBack())
            webView.goBack();
    }

    /*
    @Override
    public void onResume() {
        super.onResume();

        if (mGoogleApiClient.isConnected()) {
            startLocationUpdates();
        }


    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setSmallestDisplacement(0);
    }

    protected void stopLocationUpdates() {
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient, this);
    }


    protected void startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, this);
    }

    @Override
    public void onPause() {
        super.onPause();
        stopLocationUpdates();
    }


    @Override
    public void onStart() {
        mGoogleApiClient.connect();
        super.onStart();
    }

    @Override
    public void onStop() {
        mGoogleApiClient.disconnect();
        super.onStop();
    }

    @Override
    public void onLocationChanged(Location location) {
        mCurrentLocation = location;
        mLatitude = mCurrentLocation.getLatitude();
        mLongitude = mCurrentLocation.getLongitude();

        stopLocationUpdates();
    }
    */
}
