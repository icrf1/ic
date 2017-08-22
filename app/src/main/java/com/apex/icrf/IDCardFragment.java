package com.apex.icrf;

/**
 * Created by WASPVamsi on 03/09/15.
 */

import android.app.Activity;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.apex.icrf.classes.IIDCardListerner;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class IDCardFragment extends Fragment {


    Activity activity;

    LinearLayout llIDCardRootView;
    RelativeLayout llIDCardContainer;
    TextView mTextViewProfileName, mTextViewMemberID, mTextViewMobile, mTextViewEmail;
    ImageView mImageViewEditProfile;
    CircleImageView mImageViewProfilePic;

    private static final String DIRECTORY = "Icrf";
    private static final String FILE_NAME = "icrf_profile.png";

    private Uri outputFileUri;
    private static final int SELECT_PICTURE = 1;
    private static final String SELECT_PHOTO = "Choose Profile Picture";
    private static final String SHOULD_LOAD = "should_load";

    boolean hasPictureChanged = false;
    boolean isEditable = false;
    boolean isFromImageView = false;

    IIDCardListerner mIIidCardListerner;

    SharedPreferences prefs;
    Profile mProfile;
    Bitmap bitmap, originalBitmap, resizedBitmap;

    //boolean toCameraUpload = false;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IIDCardListerner) {
            mIIidCardListerner = (IIDCardListerner) activity;
        } else {
            Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_idcard_2, container, false);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mProfile = new Profile(activity);

        llIDCardRootView = (LinearLayout) view.findViewById(R.id.ll_id_card_root_view);
        llIDCardContainer = (RelativeLayout) llIDCardRootView.findViewById(R.id.ll_id_card_view);
        mTextViewProfileName = (TextView) view.findViewById(R.id.textView_user_profile_name);
        mTextViewProfileName.setText(Html.fromHtml("<font color=#147A6A>Name:</font> " + mProfile.getUserName()));

        mTextViewMobile = (TextView) view.findViewById(R.id.textView_user_profile_phone_number);
        mTextViewMobile.setText(Html.fromHtml("<font color=#147A6A>Mobile:</font> " + mProfile.getUserMobile()));

        mTextViewEmail = (TextView) view.findViewById(R.id.textView_user_profile_email);
        mTextViewEmail.setText(Html.fromHtml("<font color=#147A6A>Email:</font> " + mProfile.getUserEmail()));

        mImageViewProfilePic = (CircleImageView) view.findViewById(R.id.imageView_user_profile_pic);

        mImageViewEditProfile = (ImageView) view.findViewById(R.id.profile_imageview_edit);
        mImageViewEditProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //setProfileImage();

                mImageViewEditProfile.setEnabled(true);
                mImageViewEditProfile.setClickable(true);

                //toCameraUpload = true;

                mIIidCardListerner.onEditButtonClicked();

            }
        });

        setEnabled(false);

        //downloadImage();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        getImage();

//        if (toCameraUpload) {
//            getImage();
//        } else {
//            Picasso.with(activity).load(mProfile.getProfileImage()).into(mImageViewProfilePic);
//        }


    }


    public void getImage() {

        String url = Const.FINAL_URL + Const.URLs.ID_CARD_PROFILE_PIC;
        url = url + "memberid=" + mProfile.getMemberId();
        url = url + "&memberid_type=" + mProfile.getMemberIdType();

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        StringRequest request = new StringRequest(Request.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                if (response != null) {

                    if (Const.DEBUGGING) {
                        Log.d(Const.DEBUG,
                                "Response => " + response);
                        Log.d(Const.DEBUG, "Length = " + response.length());
                    }


                    prefs.edit().putString(Const.Login.USER_PROFILE_IMAGE, response.replace("\"", "").trim()).apply();
                    //Log.d(Const.DEBUG, "Profile Image Url: " + prefs.getString(Const.Login.USER_PROFILE_IMAGE, ""));

                    Picasso.with(activity).invalidate(mProfile.getProfileImage());
                    Picasso.with(activity).load(mProfile.getProfileImage()).memoryPolicy(MemoryPolicy.NO_STORE).networkPolicy(NetworkPolicy.NO_CACHE).into(mImageViewProfilePic);
                }

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, error.getMessage());
            }
        });

        request.setTag(Const.VOLLEY_TAG);
        request.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(request);
    }


    private void downloadImage() {

        DownloadImage image = new DownloadImage();
        image.execute(mProfile.getProfileImage());
    }


    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;

            }
            //return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (result != null) {
                RoundedBitmapDrawable dr =
                        RoundedBitmapDrawableFactory.create(activity.getResources(), result);
                dr.setCircular(true);

                mImageViewProfilePic.setImageDrawable(dr);
            }

        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_id_card, menu);

        MenuItem menuItem = menu.findItem(R.id.edit);
        //menuItem.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_share) {
            shareIDCard();
        } else if (id == R.id.action_save_as_image) {
            saveAsImage();
        } else if (id == R.id.action_save_as_wallpaper) {
            SaveAsWallPaper();
        } else if (id == R.id.edit) {

            setEnabled(true);
            //isEditable = true;
            //ActivityCompat.invalidateOptionsMenu(activity);
        }

//        else if (item.getItemId() == R.id.save) {
//
//            if (hasPictureChanged) {
//                saveProfilePicture();
//            } else {
//                //saveDetailsToParse(null);
//                setEnabled(false);
//            }
//            isEditable = false;
//            ActivityCompat.invalidateOptionsMenu(activity);
//        } else if (item.getItemId() == R.id.cancel) {
//            isEditable = false;
//            //resetDetails();
//            setEnabled(false);
//            ActivityCompat.invalidateOptionsMenu(activity);
//        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem menuItemEdit = menu.findItem(R.id.edit);
        MenuItem menuItemSave = menu.findItem(R.id.save);
        MenuItem menuItemCancel = menu.findItem(R.id.cancel);
        if (isEditable) {
            menuItemEdit.setVisible(false);
            menuItemSave.setVisible(true);
            menuItemCancel.setVisible(true);
        } else {
            menuItemEdit.setVisible(true);
            menuItemSave.setVisible(false);
            menuItemCancel.setVisible(false);
        }

//          menuItemEdit.setVisible(false);
        menuItemSave.setVisible(false);
        menuItemCancel.setVisible(false);


        if (!mProfile.getMemberIdType().equalsIgnoreCase("I"))
            menuItemEdit.setVisible(false);


    }

    /*
    private void saveProfilePicture() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "saveProfilePicture()");

        if (bitmap != null) {

            getResizedBitmap(bitmap, 300, 300);

            String member_id = mProfile.getMemberId();
            String member_id_type = mProfile.getMemberIdType();

            UploadTask task = new UploadTask(member_id, member_id_type, bitmap);
            task.execute();
        }

    }


//    private String bitmapToBase64() {
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
//        byte[] byteArray = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(byteArray, Base64.DEFAULT);
//    }


    public void getResizedBitmap(Bitmap bm, int newHeight, int newWidth) {
        int width = bm.getWidth();
        int height = bm.getHeight();

        float scaleWidth = width;
        float scaleHeight = height;

        if (width > newWidth)
            scaleWidth = ((float) newWidth) / width;
        else
            scaleWidth = (float) 1.0;

        if (height > newHeight)
            scaleHeight = ((float) newHeight) / height;
        else
            scaleHeight = (float) 1.0;

        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);

        try {
            bitmap = Bitmap
                    .createBitmap(bm, 0, 0, width, height, matrix, false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void resetDetails() {
        mImageViewProfilePic.setImageBitmap(originalBitmap);

        hideKeyboard();
        //removeFocus();
    }
    */

    private void setEnabled(boolean editable) {
        //mImageViewProfilePic.setEnabled(editable);

        if (editable)
            mImageViewEditProfile.setVisibility(View.VISIBLE);
        else
            mImageViewEditProfile.setVisibility(View.GONE);

    }

    private void hideKeyboard() {
        // Check if no view has focus:
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) activity
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(),
                    InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void SaveAsWallPaper() {

        saveAsImage();
        setWallPaper();
    }

    private void setWallPaper() {

        File directory = new File(Environment.getExternalStorageDirectory()
                + File.separator + DIRECTORY);
        File file = new File(directory, FILE_NAME);
        String path = file.getAbsolutePath();

        Bitmap bmp = BitmapFactory.decodeFile(path);
        WallpaperManager wallpaper = WallpaperManager.getInstance(activity);

        try {
            wallpaper.setBitmap(bmp);
            Toast.makeText(activity, "ID Card set as Wallpaper", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private void saveAsImage() {

        if (hasFile()) {

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "File Exists");

            File directory = new File(Environment.getExternalStorageDirectory()
                    + File.separator + DIRECTORY);
            File file = new File(directory, FILE_NAME);
            file.delete();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "File Deleted");
        }

        createBitmap();

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Created Bitmap");
        Toast.makeText(activity, "Image Saved", Toast.LENGTH_LONG).show();
    }

    private boolean hasFile() {

        boolean filePresent = false;

        File directory = new File(Environment.getExternalStorageDirectory()
                + File.separator + DIRECTORY);
        File file = new File(directory, FILE_NAME);

        if (file.exists())
            filePresent = true;
        else
            filePresent = false;

        return filePresent;
    }

    private void createBitmap() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Creating Bitmap");

        View v = llIDCardRootView;
        Bitmap bmp = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bmp);
        Drawable bgDrawable = v.getBackground();

        if (bgDrawable != null)
            bgDrawable.draw(canvas);
        else
            canvas.drawColor(Color.WHITE);

        v.draw(canvas);


        File directory = new File(Environment.getExternalStorageDirectory()
                + File.separator + DIRECTORY);

        if (!directory.exists())
            directory.mkdir();

        File file = new File(directory, FILE_NAME);

        try {
            FileOutputStream out = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        v.destroyDrawingCache();
        v.setDrawingCacheEnabled(false);

        MediaScannerConnection.scanFile(activity,
                new String[]{file.toString()}, null,
                new MediaScannerConnection.OnScanCompletedListener() {
                    public void onScanCompleted(String path, Uri uri) {
                    }
                });
    }

    private void shareIDCard() {

        if (hasFile()) {
            shareFile();
        } else {
            saveAsImage();
            shareFile();
        }
    }

    private void shareFile() {

        Intent share = new Intent(Intent.ACTION_SEND);

        // If you want to share a png image only, you can do:
        // setType("image/png"); OR for jpeg: setType("image/jpeg");
        share.setType("image/*");

        // Make sure you put example png image named myImage.png in your
        // directory
        String imagePath = Environment.getExternalStorageDirectory()
                + File.separator + DIRECTORY
                + File.separator + FILE_NAME;

        File imageFileToShare = new File(imagePath);

        Uri uri = Uri.fromFile(imageFileToShare);
        share.putExtra(Intent.EXTRA_STREAM, uri);

        startActivity(Intent.createChooser(share, "Share ID Card"));
    }

    protected void setProfileImage() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "setProfileImage()");

        final File root = new File(Environment.getExternalStorageDirectory()
                + File.separator + DIRECTORY);
        final String fname = FILE_NAME;
        final File sdImageMainDirectory = new File(root, fname);
        outputFileUri = Uri.fromFile(sdImageMainDirectory);

        // Camera.
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(
                android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = activity.getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(
                captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent intent = new Intent(captureIntent);
            intent.setComponent(new ComponentName(res.activityInfo.packageName,
                    res.activityInfo.name));
            intent.setPackage(packageName);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, outputFileUri);
            cameraIntents.add(intent);
        }

        // Filesystem.
        final Intent galleryIntent = new Intent();
        galleryIntent.setType("image/*");
        galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        galleryIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);

        // Chooser of filesystem options.
        final Intent chooserIntent = Intent.createChooser(galleryIntent,
                SELECT_PHOTO);

        // Add the camera options.
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS,
                cameraIntents.toArray(new Parcelable[]{}));

        isFromImageView = true;
        startActivityForResult(chooserIntent, SELECT_PICTURE);

    }

    /*
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "onActivityResult()");

        if (resultCode == activity.RESULT_OK) {
            if (requestCode == SELECT_PICTURE) {
                final boolean isCamera;
                if (data == null)
                    isCamera = true;
                else {
                    final String action = data.getAction();
                    if (action == null)
                        isCamera = false;
                    else
                        isCamera = action
                                .equals(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                }

                Uri selectedImageUri;
                if (isCamera) {
                    selectedImageUri = outputFileUri;
                } else {
                    selectedImageUri = data == null ? null : data.getData();
                }

                if (selectedImageUri != null) {

                    try {
                        bitmap = MediaStore.Images.Media.getBitmap(
                                activity.getContentResolver(), selectedImageUri);

                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);

                        Bitmap b1 = decodeSampledBitmapFromByte(
                                stream.toByteArray(), 300, 300);

                        RoundedBitmapDrawable dr =
                                RoundedBitmapDrawableFactory.create(activity.getResources(), b1);
                        //dr.setCornerRadius(Math.max(b1.getWidth(), b1.getHeight()));
                        dr.setCircular(true);

                        mImageViewProfilePic.setImageDrawable(dr);

//                        mImageViewProfilePic.setImageBitmap(decodeSampledBitmapFromByte(
//                                stream.toByteArray(), 300, 300));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    hasPictureChanged = true;
                } else {
                    hasPictureChanged = false;
                }

            }

        }

    }



    public Bitmap decodeSampledBitmapFromByte(byte[] res, int reqWidth,
                                              int reqHeight) {

        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(res, 0, res.length, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth,
                reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeByteArray(res, 0, res.length, options);
    }

    public int calculateInSampleSize(BitmapFactory.Options options,
                                     int reqWidth, int reqHeight) {

        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG,
                    "Height, Width, RequiredHeight, RequiredWidth:  " + height
                            + ", " + width + ", " + reqHeight + ", " + reqWidth);

        if (height > reqHeight || width > reqWidth) {
            if (width > height) {
                inSampleSize = Math.round((float) height / (float) reqHeight);
            } else {
                inSampleSize = Math.round((float) width / (float) reqWidth);
            }
        }

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "InsampleSize: " + inSampleSize);
        return inSampleSize;
    }


    public class UploadTask extends AsyncTask<Void, Void, Boolean> {

        String member_id, member_id_type;
        Bitmap bitmap;

        public UploadTask(String member_id, String member_id_type, Bitmap bitmap) {
            this.member_id = member_id;
            this.member_id_type = member_id_type;
            this.bitmap = bitmap;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            String response = uploadFile(member_id, member_id_type);
            if (response != null) {
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Response: " + response.toString());
            } else {
                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Response is Null");
            }


            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);
            mImageViewEditProfile.setVisibility(View.GONE);
            isEditable = false;
            ActivityCompat.invalidateOptionsMenu(activity);
        }


        private String uploadFile(String member_id, String member_id_type) {

            String url = Const.FINAL_URL + Const.URLs.SAVE_IMAGE;

            Log.d(Const.DEBUG, "Save Image URL: " + url);

            String responseString = null;

            HttpClient httpClient = new DefaultHttpClient();
            HttpContext localContext = new BasicHttpContext();
            HttpPost httpPost = new HttpPost(url);

            try {
                MultipartEntity entity = new MultipartEntity(
                        HttpMultipartMode.BROWSER_COMPATIBLE);

                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                byte[] data = bos.toByteArray();
                String file = Base64.encodeToString(data, Base64.DEFAULT);
                entity.addPart("ImageByteString", new StringBody(file));

                entity.addPart("memberid", new StringBody(member_id));
                entity.addPart("memberid_type", new StringBody(member_id_type));

                httpPost.setEntity(entity);
                HttpResponse response = httpClient.execute(httpPost,
                        localContext);
                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(
                                response.getEntity().getContent(), "UTF-8"));

                responseString = reader.readLine();

                Log.d(Const.DEBUG, "Response: " + responseString);

            } catch (Exception e) {
                e.printStackTrace();
            }


//            HttpClient httpclient = new DefaultHttpClient();
//            HttpPost httppost = new HttpPost(url);
//
//            try {
//
//                MultipartEntityBuilder builder = MultipartEntityBuilder.create();
//                builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
//
//                builder.addPart("memberid", new StringBody(member_id, ContentType.TEXT_PLAIN));
//                builder.addPart("memberid_type", new StringBody(member_id_type, ContentType.TEXT_PLAIN));
//                builder.addPart("ImageByteString", new StringBody(bitmapToBase64(), ContentType.TEXT_PLAIN));
//
//                // Adding Image
////                File directory = new File(Environment.getExternalStorageDirectory()
////                        + File.separator + DIRECTORY);
////                File file = new File(directory, FILE_NAME);
////
////                FileBody fileBody = new FileBody(new File(file.getPath()));
////                builder.addPart("ImageByteString", fileBody);
//
//                HttpEntity entity = builder.build();
//                httppost.setEntity(entity);
//
//                // Making server call
//                HttpResponse response = httpclient.execute(httppost);
//                HttpEntity r_entity = response.getEntity();
//
//                int statusCode = response.getStatusLine().getStatusCode();
//                if (statusCode == 200) {
//                    // Server response
//                    responseString = EntityUtils.toString(r_entity);
//                } else {
//                    responseString = "Error occurred! Http Status Code: "
//                            + statusCode;
//                }
//
//            } catch (ClientProtocolException e) {
//                e.printStackTrace();
//                responseString = e.toString();
//            } catch (IOException e) {
//                e.printStackTrace();
//                responseString = e.toString();
//            } catch (Exception e) {
//                e.printStackTrace();
//            }

            return responseString;

        }


    }

    */


}
