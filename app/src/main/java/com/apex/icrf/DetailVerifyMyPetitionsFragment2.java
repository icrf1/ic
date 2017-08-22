package com.apex.icrf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.apex.icrf.adapters.DetailPetitionRecyclerViewAdapter;
import com.apex.icrf.classes.IDetailVerifyMyPetitionsListener;
import com.apex.icrf.classes.ItemPetitionsTable;
import com.apex.icrf.classes.WorkaroundSupportMapFragment;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.PetitionsTableDbAdapter;
import com.apex.icrf.utils.LinearLayoutManager;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.TypeFaceHelper;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 14/09/15.
 */
public class DetailVerifyMyPetitionsFragment2 extends Fragment implements OnMapReadyCallback {

    Activity activity;
    private GoogleMap mMap;
    Profile mProfile;
    ProgressDialog progressDialog;
    SharedPreferences prefs;

    //LinearLayout rootview;

    private TextView mTextViewPetitionTitle, mTextViewPetitionDescription;

    TextView mTextViewPetitionByName, mTextViewPetitionByDate, mTextViewPetitionByAddress, mTextViewPetitionOnName, mTextViewPetitionOnAddress,
            mTextViewPetitionOnPhone, mTextViewPetitionOnEmail, mTextViewSMSMatter, mTextViewSMSMatterLabel;

    private Button mButtonContacts, mButtonGroups;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    DetailPetitionRecyclerViewAdapter mDetailPetitionRecyclerViewAdapter;
    TextView mTextViewYouTubeUrl, mTextViewDocumentUrl, mTextViewAttachmentsTitle;
    EditText mEditTextConfirmationMessage;
    CircleImageView mImageViewPetitionBy;

    TextView mTextViewPetitionerEmailID, mTextViewRespondentLabel, mTextViewPetitionNumber;

    private IDetailVerifyMyPetitionsListener mIDetailVerifyMyPetitionsListener;

    private PetitionsTableDbAdapter mPetitionsTableDbAdapter;

    String e_petition_number, petition_number, petition_title;

    ArrayList<String> mUrls = new ArrayList<String>();

    Typeface font_robotoslab_bold, font_robotoslab_regular;
    //FloatingActionButton fab;

    ImageView mImageViewAttachments;

    CallbackManager fb_callback_manager;

    Typeface font_robot_regular,
            font_roboto_light, font_roboto_medium, font_roboto_bold, font_roboto_thin,
            font_roboto_condensed_bold;

    private double mLatitude = 0.0, mLongitude = 0.0;

    WorkaroundSupportMapFragment mapFragment;

    TextView mTextViewLocationLabel;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IDetailVerifyMyPetitionsListener) {
            mIDetailVerifyMyPetitionsListener = (IDetailVerifyMyPetitionsListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        font_robotoslab_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Bold.ttf");
//
//        font_robotoslab_regular = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Regular.ttf");
//
//        font_robot_regular = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Regular.ttf");
//
//        font_roboto_light = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Light.ttf");
//
//        font_roboto_medium = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Medium.ttf");
//
//        font_roboto_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Bold.ttf");
//
//        font_roboto_thin = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Thin.ttf");
//
//        font_roboto_condensed_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoCondensed-Bold.ttf");

        font_robotoslab_bold = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Bold");
        font_robotoslab_regular = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Regular");
        font_robot_regular = TypeFaceHelper.getTypeFace(activity, "Roboto-Regular");
        font_roboto_light = TypeFaceHelper.getTypeFace(activity, "Roboto-Light");
        font_roboto_medium = TypeFaceHelper.getTypeFace(activity, "Roboto-Medium");
        font_roboto_bold = TypeFaceHelper.getTypeFace(activity, "Roboto-Bold");
        font_roboto_thin = TypeFaceHelper.getTypeFace(activity, "Roboto-Thin");
        font_roboto_condensed_bold = TypeFaceHelper.getTypeFace(activity, "RobotoCondensed-Bold");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        final View view = inflater.inflate(R.layout.detail_fragment_verify_my_petitions_new_4, container, false);
        mUrls.clear();

        //        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
//                .findFragmentById(R.id.map);

        mapFragment = (WorkaroundSupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.setListener(new WorkaroundSupportMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {

                ((ScrollView) view.findViewById(R.id.scrollView)).requestDisallowInterceptTouchEvent(true);
            }
        });

        Bundle bundle = getArguments();
        petition_number = bundle.getString(Const.Bundle.PETITION_NUMBER);
        e_petition_number = bundle.getString(Const.Bundle.E_PETITION_NUMBER);

        mPetitionsTableDbAdapter = DatabaseHelper.get(activity).getPetitionsTableDbAdapter();

        //rootview = (LinearLayout) view.findViewById(R.id.rootview);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(activity, android.support.v7.widget.LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTextViewPetitionTitle = (TextView) view.findViewById(R.id.textView_petition_title);
        //mTextViewPetitionTitle.setTypeface(font_robotoslab_bold);
        mTextViewPetitionTitle.setTypeface(font_roboto_condensed_bold);

        mImageViewPetitionBy = (CircleImageView) view.findViewById(R.id.imageView_petition_by_image);

        mTextViewPetitionByName = (TextView) view.findViewById(R.id.textView_petition_by);
        //mTextViewPetitionByName.setTypeface(font_robotoslab_bold);
        mTextViewPetitionByName.setTypeface(font_robot_regular);

        mTextViewPetitionByAddress = (TextView) view.findViewById(R.id.textView_petition_by_address);
        //mTextViewPetitionByAddress.setTypeface(font_robotoslab_regular);
        mTextViewPetitionByAddress.setTypeface(font_roboto_light);

        mTextViewPetitionerEmailID = (TextView) view.findViewById(R.id.textView_petitioner_email);
        //mTextViewPetitionerEmailID.setTypeface(font_robotoslab_regular);
        mTextViewPetitionerEmailID.setTypeface(font_roboto_light);

        mImageViewAttachments = (ImageView) view.findViewById(R.id.imageView_attachments);
        mTextViewAttachmentsTitle = (TextView) view.findViewById(R.id.textView_attachments_title);
        mTextViewAttachmentsTitle.setTypeface(font_robotoslab_bold);

        mTextViewYouTubeUrl = (TextView) view.findViewById(R.id.textView_petition_youtube_url);
        mTextViewYouTubeUrl.setTypeface(font_robotoslab_regular);

        mTextViewDocumentUrl = (TextView) view.findViewById(R.id.textView_petition_document_url);
        mTextViewDocumentUrl.setTypeface(font_robotoslab_regular);


        mTextViewPetitionByDate = (TextView) view.findViewById(R.id.textView_petition_by_date);
        mTextViewPetitionNumber = (TextView) view.findViewById(R.id.textView_petition_number);


        mTextViewRespondentLabel = (TextView) view.findViewById(R.id.textView_petition_on_respondent);
        //mTextViewRespondentLabel.setTypeface(font_robotoslab_bold);
        mTextViewRespondentLabel.setTypeface(font_roboto_bold);

        mTextViewPetitionOnName = (TextView) view.findViewById(R.id.textView_petition_on);
        //mTextViewPetitionOnName.setTypeface(font_robotoslab_bold);
        mTextViewPetitionOnName.setTypeface(font_roboto_medium);

        mTextViewPetitionOnAddress = (TextView) view.findViewById(R.id.textView_petition_on_address);
        //mTextViewPetitionOnAddress.setTypeface(font_robotoslab_regular);
        mTextViewPetitionOnAddress.setTypeface(font_roboto_light);

        mTextViewPetitionOnPhone = (TextView) view.findViewById(R.id.textView_petition_on_phone);
        //mTextViewPetitionOnPhone.setTypeface(font_robotoslab_regular);
        mTextViewPetitionOnPhone.setTypeface(font_roboto_light);

        mTextViewPetitionOnEmail = (TextView) view.findViewById(R.id.textView_petition_on_email);
        //mTextViewPetitionOnEmail.setTypeface(font_robotoslab_regular);
        mTextViewPetitionOnEmail.setTypeface(font_roboto_light);

        mTextViewPetitionDescription = (TextView) view.findViewById(R.id.textView_petition_desc);
        //mTextViewPetitionDescription.setTypeface(font_robotoslab_regular);
        mTextViewPetitionDescription.setTypeface(font_robot_regular);

        mTextViewLocationLabel = (TextView) view.findViewById(R.id.textView_location_label);
        //mTextViewPetitionDescription.setTypeface(font_robotoslab_regular);
        mTextViewLocationLabel.setTypeface(font_robotoslab_bold);

        mTextViewSMSMatterLabel = (TextView) view.findViewById(R.id.textView_sms_matter_label);
        mTextViewSMSMatterLabel.setTypeface(font_robotoslab_bold);

        mTextViewSMSMatter = (TextView) view.findViewById(R.id.textView_sms_matter);
        mTextViewSMSMatter.setTypeface(font_roboto_light);

        mProfile = new Profile(activity);

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        mButtonContacts = (Button) view.findViewById(R.id.button_contacts);
        mButtonContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIDetailVerifyMyPetitionsListener.onContactsButtonClicked(mTextViewSMSMatter.getText().toString());
            }
        });


        mButtonGroups = (Button) view.findViewById(R.id.button_groups);
        mButtonGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIDetailVerifyMyPetitionsListener.onGroupsButtonClicked(mTextViewSMSMatter.getText().toString());
            }
        });

        getDataFromDatabase(e_petition_number);

//        fab = (FloatingActionButton) view.findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//
//                if (mUrls.size() == 0) {
//
//                    Intent intent = new Intent(Intent.ACTION_SEND);
//                    intent.setType("text/plain");
//                    intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
//                    intent.putExtra(
//                            Intent.EXTRA_TEXT,
//                            "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
//                                    "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
//                    startActivity(Intent.createChooser(intent, "Share Petition..."));
//                } else {
//
//                    Picasso.with(activity).load(mUrls.get(0)).resize(300, 300).into(new Target() {
//                        @Override
//                        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                            String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                                    "/images";
//                            File dir = new File(file_path);
//                            if (!dir.exists())
//                                dir.mkdir();
//                            File file = new File(dir, "image");
//                            FileOutputStream fOut;
//                            try {
//                                fOut = new FileOutputStream(file);
//                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                                fOut.flush();
//                                fOut.close();
//
//
//                                Uri uri = Uri.fromFile(file);
//                                Intent intent = new Intent();
//                                intent.setAction(Intent.ACTION_SEND);
//                                intent.setType("image/*");
//
//                                intent.putExtra(Intent.EXTRA_SUBJECT, "");
//                                intent.putExtra(Intent.EXTRA_TEXT, "");
//                                intent.putExtra(Intent.EXTRA_STREAM, uri);
//                                intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
//                                intent.putExtra(
//                                        Intent.EXTRA_TEXT,
//                                        "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
//                                                "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
//                                startActivity(Intent.createChooser(intent, "Share Petition..."));
//
//                            } catch (Exception e) {
//                                e.printStackTrace();
//                            }
//                        }
//
//                        @Override
//                        public void onBitmapFailed(Drawable errorDrawable) {
//
//                            Intent intent = new Intent(Intent.ACTION_SEND);
//                            intent.setType("text/plain");
//                            intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
//                            intent.putExtra(
//                                    Intent.EXTRA_TEXT,
//                                    "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
//                                            "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
//                            startActivity(Intent.createChooser(intent, "Share Petition..."));
//                        }
//
//                        @Override
//                        public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                        }
//                    });
//                }
//
//
//            }
//        });

        return view;
    }


    private void getDataFromDatabase(final String e_petition_number) {

        final ItemPetitionsTable item = mPetitionsTableDbAdapter.getPetitionDetailsForID(e_petition_number);
        String COMMA = ", ";

        if (item != null) {

            mTextViewPetitionTitle.setText(item.getPetition_title());
            petition_title = item.getPetition_title();

            //Picasso.with(activity).load(item.getPetitioner_profile_image_url()).into(mImageViewPetitionBy);
            OkHttpClientHelper.getPicassoBuilder(activity).load(item.getPetitioner_profile_image_url()).resize(100, 100).into(mImageViewPetitionBy);

            mTextViewPetitionByName.setText(item.getPetitioner_name());
            mTextViewPetitionerEmailID.setText(item.getPetitioner_email());

            String state1 = item.getPetitioner_state().substring(0, 1).toUpperCase()
                    + item.getPetitioner_state().substring(1).toLowerCase();
            mTextViewPetitionByAddress.setText(Html.fromHtml(item.getPetitioner_city() + COMMA
                    + item.getPetitioner_district() + COMMA + state1 + COMMA
                    + " - " + item.getPetitioner_pincode() + "."));

            mTextViewPetitionByDate.setText("Date: " + item.getDate());
            mTextViewPetitionNumber.setText("Petition Number: " + item.getPetition_number());


            String state = item.getState().substring(0, 1).toUpperCase()
                    + item.getState().substring(1).toLowerCase();
            mTextViewPetitionOnName.setText(Html.fromHtml(item.getOfficial_name()
                    + COMMA
                    + item.getOfficial_designation()
                    + COMMA
                    + item.getOffice_department_name()
            ));

            mTextViewPetitionOnAddress.setText(Html.fromHtml(item.getOffice_address() + COMMA
                    + item.getCity() + COMMA
                    + item.getDistrict() + COMMA + state + " - "
                    + item.getPincode() + "."));

            mTextViewPetitionOnPhone.setText(item.getOfficial_mobile());
            mTextViewPetitionOnEmail.setText(item.getOfficial_email());


            mLatitude = Double.parseDouble(item.getLatitude());
            mLongitude = Double.parseDouble(item.getLongitude());

            if (mLatitude != 0.0 && mLongitude != 0.0) {
                mTextViewLocationLabel.setVisibility(View.VISIBLE);
                mapFragment.getMapAsync(this);
            } else {
                mTextViewLocationLabel.setVisibility(View.GONE);
                mapFragment.getView().setVisibility(View.GONE);
            }

            mTextViewPetitionDescription.setText(Html.fromHtml(item.getPetition_description()));

            String attachments = item.getAttachments();

            if (attachments != null && !attachments.equalsIgnoreCase("")) {

                mImageViewAttachments.setVisibility(View.GONE);
                mTextViewAttachmentsTitle.setVisibility(View.GONE);
                mTextViewDocumentUrl.setVisibility(View.GONE);
                mTextViewYouTubeUrl.setVisibility(View.GONE);

                try {
                    mUrls.clear();
                    JSONArray array = new JSONArray(attachments);

                    if (array.length() == 0) {

                        String path = "http://icrf.org.in/Attachment/e-petition_img.jpg";
                        mUrls.add(path);
                    }

                    boolean hasImage = false;
                    for (int i = 0; i < array.length(); i++) {

                        JSONObject attachment_object = array.getJSONObject(i);

                        String type = attachment_object.getString("typ");

                        if (type.equalsIgnoreCase("d")) {
                            String path = attachment_object.getString("Doc_path");

                            mImageViewAttachments.setVisibility(View.VISIBLE);
                            mTextViewAttachmentsTitle.setVisibility(View.VISIBLE);
                            mTextViewDocumentUrl.setVisibility(View.VISIBLE);
                            mTextViewDocumentUrl.setText("Document Link: " + path);
                        } else if (type.equalsIgnoreCase("i")) {
                            String path = attachment_object.getString("Doc_path");

                            hasImage = true;

                            mUrls.add(path);
                        } else if (type.equalsIgnoreCase("y")) {
                            String path = attachment_object.getString("Doc_path");

                            mImageViewAttachments.setVisibility(View.VISIBLE);
                            mTextViewAttachmentsTitle.setVisibility(View.VISIBLE);
                            mTextViewYouTubeUrl.setVisibility(View.VISIBLE);
                            mTextViewYouTubeUrl.setText("Youtube Link: " + path);
                        }

                        if (!hasImage) {
                            String path = "http://icrf.org.in/Attachment/e-petition_img.jpg";
                            mUrls.add(path);
                        }
                    }

                    mDetailPetitionRecyclerViewAdapter = new DetailPetitionRecyclerViewAdapter(activity, mUrls);
                    mRecyclerView.setAdapter(mDetailPetitionRecyclerViewAdapter);


                } catch (Exception e) {
                    e.printStackTrace();

                    mImageViewAttachments.setVisibility(View.GONE);
                    mTextViewAttachmentsTitle.setVisibility(View.GONE);
                    mTextViewDocumentUrl.setVisibility(View.GONE);
                    mTextViewYouTubeUrl.setVisibility(View.GONE);
                }
            }

            String sms_matter = "I " + mProfile.getUserName() + ", " +
                    "Please verify my petition in ICRF as www.icrf.org.in/" + petition_number + " - " +
                    petition_title;
            mTextViewSMSMatter.setText(sms_matter);


            //ActivityCompat.invalidateOptionsMenu(activity);
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        //activity.getMenuInflater().inflate(R.menu.menu_verify_petition, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        // Old message
        //"I " + mProfile.getUserName() + ", " +
        //        "Please verify my petition in ICRF as www.icrf.org.in/" + petition_number + " - " +
        //        petition_title

        int id = item.getItemId();

        if (id == R.id.action_share) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
                            "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
            startActivity(Intent.createChooser(intent, "Share Petition..."));
        }

        return super.onOptionsItemSelected(item);
    }


    public void onFABClick() {

        if (mUrls.size() == 0) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
                            "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
            startActivity(Intent.createChooser(intent, "Share Petition..."));
        } else {

            Picasso.with(activity).load(mUrls.get(0)).resize(300, 300).into(new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                    String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                            "/images";
                    File dir = new File(file_path);
                    if (!dir.exists())
                        dir.mkdir();
                    File file = new File(dir, "image");
                    FileOutputStream fOut;
                    try {
                        fOut = new FileOutputStream(file);
                        bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                        fOut.flush();
                        fOut.close();


                        Uri uri = Uri.fromFile(file);
                        Intent intent = new Intent();
                        intent.setAction(Intent.ACTION_SEND);
                        intent.setType("image/*");

                        intent.putExtra(Intent.EXTRA_SUBJECT, "");
                        intent.putExtra(Intent.EXTRA_TEXT, "");
                        intent.putExtra(Intent.EXTRA_STREAM, uri);
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
                        intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
                                        "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
                        startActivity(Intent.createChooser(intent, "Share Petition..."));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {

                    Intent intent = new Intent(Intent.ACTION_SEND);
                    intent.setType("text/plain");
                    intent.putExtra(Intent.EXTRA_SUBJECT, "Verify my petition");
                    intent.putExtra(
                            Intent.EXTRA_TEXT,
                            "I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
                                    "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK);
                    startActivity(Intent.createChooser(intent, "Share Petition..."));
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {

                }
            });
        }


    }


    public void onFacebookShare() {

        Log.d(Const.DEBUG, "onFacebookShare()");

        if (mUrls != null) {

            final ShareDialog fb_share_dialog = new ShareDialog(activity);
            fb_callback_manager = CallbackManager.Factory.create();
            fb_share_dialog.registerCallback(fb_callback_manager, new FacebookCallback<Sharer.Result>() {
                @Override
                public void onSuccess(Sharer.Result result) {

                    Toast.makeText(activity, "Successfully posted on facebook", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onCancel() {
                    Toast.makeText(activity, "Operation Cancelled", Toast.LENGTH_LONG).show();
                }

                @Override
                public void onError(FacebookException error) {
                    Toast.makeText(activity, "There was an error posting on facebook. Please try again later.", Toast.LENGTH_LONG).show();
                }
            });

            if (ShareDialog.canShow(ShareLinkContent.class)) {
                ShareLinkContent linkContent = new ShareLinkContent.Builder()
                        .setContentTitle("Verify this petition")
                        .setImageUrl(Uri.parse(mUrls.get(0)))
                        .setContentDescription("I have posted a petition on www.icrf.org.in/v/" + petition_number + ". " +
                                "Please verify the petition and get 5 points. Sub: " + petition_title + " " + Const.APP_INSTALL_LINK)
                        .setContentUrl(Uri.parse("http://www.icrf.org.in"))
                        .build();

                fb_share_dialog.show(linkContent);
            }


//            if (mUrls.size() == 0) {
//
//                Log.d(Const.DEBUG, "2");
//
//                Picasso.with(activity).load(R.drawable.icrf_splash_icrf_logo_square_1).into(new Target() {
//                    @Override
//                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
//
//                        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
//                                "/images";
//                        File dir = new File(file_path);
//                        if (!dir.exists())
//                            dir.mkdir();
//                        File file = new File(dir, "image");
//                        FileOutputStream fOut;
//                        try {
//                            fOut = new FileOutputStream(file);
//                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
//                            fOut.flush();
//                            fOut.close();
//
//
//                            Uri uri = Uri.fromFile(file);
//
//                            Log.d(Const.DEBUG, "3");
//
//                            if (ShareDialog.canShow(ShareLinkContent.class)) {
//
//                                Log.d(Const.DEBUG, "5");
//
//                                ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                                        .setContentTitle("Support this petition")
//                                        .setContentDescription("I " + mProfile.getUserName() + ", " +
//                                                "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
//                                                petition_title + " " + Const.APP_INSTALL_LINK)
//                                        .setContentUrl(Uri.parse("http://www.icrf.org.in"))
//                                        .setImageUrl(uri)
//                                        .build();
//
//                                fb_share_dialog.show(linkContent);
//                            }
//
//                        } catch (Exception e) {
//                            e.printStackTrace();
//
//                            Log.d(Const.DEBUG, "4");
//                        }
//                    }
//
//                    @Override
//                    public void onBitmapFailed(Drawable errorDrawable) {
//
//                        Log.d(Const.DEBUG, "6");
//
//                        if (ShareDialog.canShow(ShareLinkContent.class)) {
//                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                                    .setContentTitle("Support this petition")
//                                    .setContentDescription("I " + mProfile.getUserName() + ", " +
//                                            "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
//                                            petition_title + " " + Const.APP_INSTALL_LINK)
//                                    .setContentUrl(Uri.parse("http://www.icrf.org.in"))
//                                    .build();
//
//                            fb_share_dialog.show(linkContent);
//                        }
//                    }
//
//                    @Override
//                    public void onPrepareLoad(Drawable placeHolderDrawable) {
//
//                    }
//                });
//            } else {
//
//                Log.d(Const.DEBUG, "7");
//
//                if (ShareDialog.canShow(ShareLinkContent.class)) {
//                    ShareLinkContent linkContent = new ShareLinkContent.Builder()
//                            .setContentTitle("Support this petition")
//                            .setContentDescription("I " + mProfile.getUserName() + ", " +
//                                    "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
//                                    petition_title + " " + Const.APP_INSTALL_LINK)
//                            .setContentUrl(Uri.parse("http://www.icrf.org.in"))
//                            .build();
//
//                    fb_share_dialog.show(linkContent);
//                }
//
//
//
////                Picasso.with(activity).load(mUrls.get(0)).resize(300, 300).into(new Target() {
////                    @Override
////                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
////
////                        Log.d(Const.DEBUG, "8");
////
////                        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
////                                "/images";
////                        File dir = new File(file_path);
////                        if (!dir.exists())
////                            dir.mkdir();
////                        File file = new File(dir, "image");
////                        FileOutputStream fOut;
////                        try {
////                            fOut = new FileOutputStream(file);
////                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
////                            fOut.flush();
////                            fOut.close();
////
////
////                            Uri uri = Uri.fromFile(file);
////
////                            Log.d(Const.DEBUG, "9");
////
////                            if (ShareDialog.canShow(ShareLinkContent.class)) {
////                                ShareLinkContent linkContent = new ShareLinkContent.Builder()
////                                        .setContentTitle("Support this petition")
////                                        .setContentDescription("I " + mProfile.getUserName() + ", " +
////                                                "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
////                                                petition_title + " " + Const.APP_INSTALL_LINK)
////                                        .setContentUrl(Uri.parse("http://www.icrf.org.in"))
////                                        .setImageUrl(uri)
////                                        .build();
////
////                                fb_share_dialog.show(linkContent);
////                            }
////
////                        } catch (Exception e) {
////                            e.printStackTrace();
////                        }
////                    }
////
////                    @Override
////                    public void onBitmapFailed(Drawable errorDrawable) {
////
////                        if (ShareDialog.canShow(ShareLinkContent.class)) {
////                            ShareLinkContent linkContent = new ShareLinkContent.Builder()
////                                    .setContentTitle("Support this petition")
////                                    .setContentDescription("I " + mProfile.getUserName() + ", " +
////                                            "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
////                                            petition_title + " " + Const.APP_INSTALL_LINK)
////                                    .setContentUrl(Uri.parse("http://www.icrf.org.in"))
////                                    .build();
////
////                            fb_share_dialog.show(linkContent);
////                        }
////                    }
////
////                    @Override
////                    public void onPrepareLoad(Drawable placeHolderDrawable) {
////
////                    }
////                });
//            }
        }

    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        fb_callback_manager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setAllGesturesEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setIndoorLevelPickerEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);
        mMap.getUiSettings().setMyLocationButtonEnabled(true);
        mMap.getUiSettings().setRotateGesturesEnabled(true);

        mMap.getUiSettings().setScrollGesturesEnabled(true);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setZoomGesturesEnabled(true);

        if (mLatitude != 0.0 && mLongitude != 0.0) {
            mapFragment.getView().setVisibility(View.VISIBLE);

            LatLng marker = new LatLng(mLatitude, mLongitude);

            mMap.clear();
            mMap.addMarker(new MarkerOptions().position(marker));
            mMap.moveCamera(CameraUpdateFactory.newLatLng(marker));
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker, 15));
        } else {
            mapFragment.getView().setVisibility(View.GONE);
        }
    }

}
