package com.apex.icrf;

import android.*;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.RecyclerView;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.adapters.DetailPetitionRecyclerViewAdapter;
import com.apex.icrf.classes.IDetailVerifiedPetitionsListener;
import com.apex.icrf.classes.ItemDeliveryReportsTable;
import com.apex.icrf.classes.ItemFavouritesTable;
import com.apex.icrf.classes.ItemPetitionsTable;
import com.apex.icrf.classes.WorkaroundSupportMapFragment;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;
import com.apex.icrf.database.FavouritesTableDbAdapter;
import com.apex.icrf.database.PetitionsTableDbAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.LinearLayoutManager;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.SMSDeliveredReceiver;
import com.apex.icrf.utils.SMSSentReceiver;
import com.apex.icrf.utils.TypeFaceHelper;
import com.apex.icrf.utils.Utils;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.share.Sharer;
import com.facebook.share.model.ShareLinkContent;
import com.facebook.share.widget.ShareDialog;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
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
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


/**
 * Created by WASPVamsi on 14/09/15.
 */
public class DetailVerifiedPetitionsFragment2 extends Fragment implements OnMapReadyCallback {

    Activity activity;
    private GoogleMap mMap;
    Profile mProfile;
    ProgressDialog progressDialog;
    SharedPreferences prefs;

    BroadcastReceiver mSMSSentBroadcastReceiver = new SMSSentReceiver();
    BroadcastReceiver mSMSDeliveredBroadcastReciever = new SMSDeliveredReceiver();

    String SENT_INTENT = "SMS_SENT";
    String DELIVERED_INTENT = "SMS_DELIVERED";

    //LinearLayout rootview;

    TextView mTextViewPetitionByName, mTextViewPetitionByDate, mTextViewPetitionByAddress,
            mTextViewPetitionOnName, mTextViewPetitionOnAddress, mTextViewPetitionOnPhone,
            mTextViewPetitionOnEmail, mTextViewSMSMatter;

    TextView mTextViewPetitionTitle, mTextViewPetitionDescription, mTextViewPetitionNumber;

    ImageView mImageViewYouTubeImage, mImageViewDocumentImage;
    TextView mTextViewYouTubeUrl, mTextViewDocumentUrl, mTextViewAttachmentsTitle;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    DetailPetitionRecyclerViewAdapter mDetailPetitionRecyclerViewAdapter;
    ImageView mImageViewAttachments;
    //ImageView mImageViewPetitionBy;
    CircleImageView mImageViewPetitionBy;

    CheckBox mCheckBoxTerms;

    Button mButtonSupport;

    EditText mEditTextConfirmationMessage;

    DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;
    PetitionsTableDbAdapter mPetitionsTableDbAdapter;
    FavouritesTableDbAdapter mFavouritesTableDbAdapter;

    boolean mLiked = false, mFavourite = false;
    int mLikeCount = 0;
    String e_petition_number, petition_number, petition_title;
    int fragment_id = 0;

    ArrayList<String> mUrls = new ArrayList<String>();
    List<String> mFavourites = new ArrayList<>();

    RelativeLayout mRLRatings;
    TextView mTextViewFetchRatings, mTextViewSupports, mTextViewLikes, mTextViewComments, mTextViewRatings, mTextViewDate, mTextViewSuccessMessage;
    TextView mTextViewSupportsLabel, mTextViewLikesLabel, mTextViewCommentsLabel;
    RatingBar mRatingBarRating;

    TextView mTextViewPetitionerEmailID, mTextViewRespondentLabel;

    Typeface font_robotoslab_bold, font_robotoslab_regular;
    //FloatingActionButton fab;

    LinearLayout mLinearLayoutBottomBar, mLinearLayoutBottomBarLike, mLinearLayoutBottomBarComment, mLinearLayoutBottomBarSupport;
    ImageView mImageViewBottomBarLike, mImageViewBottomBarComment, mImageViewBottomBarSupport;
    TextView mTextViewBottomBarLikes, mTextViewBottomBarComments, mTextViewBottomBarSupports;

    ScrollView mScrollView;
    int previous_scollY = 0;
    int toolbar_height = 0;

    boolean editHasFocus = false;

    public final int SMSREADPERMISSION = 0002;

    CallbackManager fb_callback_manager;
    IDetailVerifiedPetitionsListener mIDetailVerifiedPetitionsListener;

    Typeface font_robot_regular,
            font_roboto_light, font_roboto_medium, font_roboto_bold, font_roboto_thin,
            font_roboto_condensed_bold;

    private double mLatitude = 0.0, mLongitude = 0.0;

    WorkaroundSupportMapFragment mapFragment;

    TextView mTextViewLocationLabel;
    Tracker t;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IDetailVerifiedPetitionsListener) {
            mIDetailVerifiedPetitionsListener = (IDetailVerifiedPetitionsListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }

    private void setGoogleAnalytics() {
        t = ((ICRFApp) activity.getApplication())
                .getTracker(ICRFApp.TrackerName.APP_TRACKER);
        t.setScreenName("Support Petition");
        t.send(new HitBuilders.AppViewBuilder().build());
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

        View view = inflater.inflate(R.layout.detail_fragment_verified_petitions_new_4, container, false);
        mUrls.clear();
        mFavourites.clear();

        setGoogleAnalytics();

//        mapFragment = (SupportMapFragment) this.getChildFragmentManager()
//                .findFragmentById(R.id.map);

        mapFragment = (WorkaroundSupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        ((WorkaroundSupportMapFragment) mapFragment).setListener(new WorkaroundSupportMapFragment.OnTouchListener() {
            @Override
            public void onTouch() {

                mScrollView.requestDisallowInterceptTouchEvent(true);
            }
        });


        toolbar_height = Utils.getToolbarHeight(activity);

        Bundle bundle = getArguments();
        e_petition_number = bundle.getString(Const.Bundle.E_PETITION_NUMBER);
        petition_number = bundle.getString(Const.Bundle.PETITION_NUMBER);
        fragment_id = bundle.getInt(Const.Bundle.FROM_FRAGMENT);

        mPetitionsTableDbAdapter = DatabaseHelper.get(activity).getPetitionsTableDbAdapter();
        mFavouritesTableDbAdapter = DatabaseHelper.get(activity).getFavouritesTableDbAdapter();
        mFavourites = mFavouritesTableDbAdapter.getFavourites();

        //rootview = (LinearLayout) view.findViewById(R.id.rootview);
        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(activity, android.support.v7.widget.LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTextViewPetitionTitle = (TextView) view.findViewById(R.id.textView_petition_title);
        //mTextViewPetitionTitle.setTypeface(font_robotoslab_bold);
        //mTextViewPetitionTitle.setTypeface(font_roboto_bold);
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


        mTextViewSMSMatter = (TextView) view.findViewById(R.id.textView_sms_matter);
        mTextViewSMSMatter.setTypeface(font_robotoslab_bold);

        mEditTextConfirmationMessage = (EditText) view.findViewById(R.id.editText_sms_matter);
        //mEditTextConfirmationMessage.setTypeface(font_robotoslab_regular);
        mEditTextConfirmationMessage.setTypeface(font_roboto_thin);
        mEditTextConfirmationMessage.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                if (hasFocus) {
                    editHasFocus = true;
                    hideTranslation();
                } else {
                    if (editHasFocus)
                        showTranslation();
                }
            }
        });

        mCheckBoxTerms = (CheckBox) view.findViewById(R.id.checkBox_terms);
        mCheckBoxTerms.setVisibility(View.VISIBLE);

        mButtonSupport = (Button) view.findViewById(R.id.button_support);
        mButtonSupport.setEnabled(false);

        mProfile = new Profile(activity);

        mRLRatings = (RelativeLayout) view.findViewById(R.id.ratings);
        mRLRatings.setVisibility(View.GONE);

        mTextViewFetchRatings = (TextView) mRLRatings.findViewById(R.id.textView_fetching_ratings);

        mTextViewSupports = (TextView) mRLRatings.findViewById(R.id.textView_supports_count);
        mTextViewSupports.setTypeface(font_roboto_thin);
        mTextViewSupportsLabel = (TextView) mRLRatings.findViewById(R.id.textView_supports_label);
        mTextViewSupportsLabel.setTypeface(font_roboto_thin);


        mTextViewLikes = (TextView) mRLRatings.findViewById(R.id.textView_likes_count);
        mTextViewLikes.setTypeface(font_roboto_thin);
        mTextViewLikesLabel = (TextView) mRLRatings.findViewById(R.id.textView_likes_label);
        mTextViewLikesLabel.setTypeface(font_roboto_thin);

        mTextViewComments = (TextView) mRLRatings.findViewById(R.id.textView_comments_count);
        mTextViewComments.setTypeface(font_roboto_thin);
        mTextViewCommentsLabel = (TextView) mRLRatings.findViewById(R.id.textView_comments_label);
        mTextViewCommentsLabel.setTypeface(font_roboto_thin);

        mTextViewRatings = (TextView) mRLRatings.findViewById(R.id.textView_rating);
        mTextViewRatings.setTypeface(font_roboto_thin);

        mTextViewDate = (TextView) mRLRatings.findViewById(R.id.textView_date);
        mRatingBarRating = (RatingBar) mRLRatings.findViewById(R.id.ratingBar_rating);
        mTextViewSuccessMessage = (TextView) mRLRatings.findViewById(R.id.textView_success_message);

        mLinearLayoutBottomBar = (LinearLayout) view.findViewById(R.id.ll_bottom_bar);
        mLinearLayoutBottomBarLike = (LinearLayout) view.findViewById(R.id.ll_bottom_bar_like);
        mLinearLayoutBottomBarComment = (LinearLayout) view.findViewById(R.id.ll_bottom_bar_comment);
        mLinearLayoutBottomBarSupport = (LinearLayout) view.findViewById(R.id.ll_bottom_bar_support);

        mImageViewBottomBarLike = (ImageView) mLinearLayoutBottomBarLike.findViewById(R.id.imageview_bottom_bar_like);
        mImageViewBottomBarComment = (ImageView) mLinearLayoutBottomBarComment.findViewById(R.id.imageview_bottom_bar_comment);
        mImageViewBottomBarSupport = (ImageView) mLinearLayoutBottomBarSupport.findViewById(R.id.imageview_bottom_bar_support);

        mTextViewBottomBarLikes = (TextView) mLinearLayoutBottomBarLike.findViewById(R.id.textview_bottom_bar_likes);
        mTextViewBottomBarComments = (TextView) mLinearLayoutBottomBarComment.findViewById(R.id.textview_bottom_bar_comments);
        mTextViewBottomBarSupports = (TextView) mLinearLayoutBottomBarSupport.findViewById(R.id.textview_bottom_bar_supports);

        //fab = (FloatingActionButton) view.findViewById(R.id.fab);

        mScrollView = (ScrollView) view.findViewById(R.id.scrollView);
        mScrollView.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                int scrollY = mScrollView.getScrollY();

                if (scrollY > previous_scollY + 20/* && previous_scollY > toolbar_height*/) {
                    hideTranslation();
                } else if (/*toolbar_height < scrollY && */scrollY < previous_scollY - 20) {
                    showTranslation();
                }

                previous_scollY = scrollY;

            }
        });

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        getDataFromDatabase(e_petition_number);

        // Getting success petition ratings
        getRatingsFromServer(e_petition_number);

        mLinearLayoutBottomBarLike.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mLiked)
                    sendLike();
                else {
                    Toast.makeText(activity, "Already Liked", Toast.LENGTH_LONG).show();
                    //Toast.makeText(activity, "Likes are disabled", Toast.LENGTH_LONG).show();
                }
            }
        });

        mLinearLayoutBottomBarComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(activity, CommentsActivity.class)
                        .putExtra("petition_no", petition_number)
                        .putExtra("e_petition_no", e_petition_number)
                        .putExtra("isSuccessPetition", true));
            }
        });


        return view;
    }


    private void hideTranslation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            //fab.animate().setDuration(500).alpha(0.0f);
            mIDetailVerifiedPetitionsListener.onVerifiedPetitionsScrolled(true);
            mLinearLayoutBottomBar.animate().setDuration(1000).alpha(0.0f);
            //mLinearLayoutBottomBar.setVisibility(View.GONE);
        }

    }

    private void showTranslation() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1) {
            //fab.animate().setDuration(500).alpha(1.0f);
            mIDetailVerifiedPetitionsListener.onVerifiedPetitionsScrolled(false);
            mLinearLayoutBottomBar.animate().setDuration(1000).alpha(1.0f);
            //mLinearLayoutBottomBar.setVisibility(View.VISIBLE);
        }
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


            mTextViewPetitionByDate.setText("Date: " + item.getDate().split(" ")[0].toString());
            mTextViewPetitionNumber.setText("Petition Number: " + item.getPetition_number());


            String state = item.getState().substring(0, 1).toUpperCase()
                    + item.getState().substring(1).toLowerCase();

            mTextViewPetitionOnName.setText(Html.fromHtml(item.getOfficial_name()
                    + COMMA
                    + item.getOfficial_designation()
                    + COMMA
                    + item.getOffice_department_name()
            ));
//            mTextViewPetitionOnAddress.setText(Html.fromHtml("<b>Address:</b> <br />" + item.getPetition_address() + COMMA
//                    + item.getOffice_address() + COMMA
//                    + item.getDistrict() + COMMA + state + " - "
//                    + item.getPincode() + "."));

            mTextViewPetitionOnAddress.setText(Html.fromHtml(item.getOffice_address() + COMMA
                    + item.getCity() + COMMA
                    + item.getDistrict() + COMMA + state + " - "
                    + item.getPincode() + "."));


            mTextViewPetitionOnPhone.setText(item.getOfficial_mobile());
            mTextViewPetitionOnEmail.setText(item.getOfficial_email());


            mLatitude = Double.parseDouble(item.getLatitude());
            mLongitude = Double.parseDouble(item.getLongitude());

            //mLatitude = 17.3971226;
            //mLongitude = 78.431973;

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

                            if (Const.DEBUGGING)
                                Log.d(Const.DEBUG, "3. Type = I and Urls size: " + mUrls.size());
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


            // 0 -> can send support
            // 1 -> already supported
            if (item.getSent_support().equalsIgnoreCase("0")) {
                mButtonSupport.setEnabled(true);
                mButtonSupport.setText("SEND SMS SUPPORT");
                mLinearLayoutBottomBar.setVisibility(View.VISIBLE);
                mImageViewBottomBarSupport.setImageResource(R.drawable.drawable_bottom_bar_support_white_512);
            } else {
                mButtonSupport.setEnabled(false);
                mButtonSupport.setText("Supported");
                mLinearLayoutBottomBar.setVisibility(View.VISIBLE);
                mImageViewBottomBarSupport.setImageResource(R.drawable.drawable_bottom_bar_support_grey_512);
            }

            if (fragment_id == Const.Bundle.MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT) {
                mButtonSupport.setEnabled(false);
                mButtonSupport.setText("Verified");

                mCheckBoxTerms.setVisibility(View.GONE);
                mLinearLayoutBottomBar.setVisibility(View.GONE);
            } else if (fragment_id == Const.Bundle.MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT) {
                mButtonSupport.setEnabled(false);
                mButtonSupport.setText("Supported");

                mCheckBoxTerms.setVisibility(View.GONE);
                mLinearLayoutBottomBar.setVisibility(View.VISIBLE);
            } else if (fragment_id == Const.Bundle.VICTORY_PETITIONS_FRAGMENT) {
                mTextViewSMSMatter.setVisibility(View.GONE);
                mEditTextConfirmationMessage.setVisibility(View.GONE);
                mCheckBoxTerms.setVisibility(View.GONE);
                mButtonSupport.setVisibility(View.GONE);

                mRLRatings.setVisibility(View.VISIBLE);

                mLinearLayoutBottomBar.setVisibility(View.GONE);

                // Getting success petition ratings
                //getRatingsFromServer(e_petition_number);
            }


            mButtonSupport.setOnClickListener(new View.OnClickListener() {
                @RequiresApi(api = Build.VERSION_CODES.M)
                @Override
                public void onClick(View v) {

                    sendHit(petition_title);

                    SMSPermission();

                    checkCanSupport(petition_number, item.getOfficial_mobile(), e_petition_number, item.getSms_matter(), "");


//                    if (mEditTextConfirmationMessage.length() == 0 || mEditTextConfirmationMessage.length() < 15) {
//                        Toast.makeText(activity, "Please enter at-least 15 characters to proceed.", Toast.LENGTH_LONG).show();
//                    } else if (mCheckBoxTerms.isChecked()) {
//                        checkCanSupport(petition_number, item.getOfficial_mobile(), e_petition_number, item.getSms_matter(), mEditTextConfirmationMessage.getText().toString());
//                    } else {
//                        Toast.makeText(activity, "Please accept terms and conditions to proceed.", Toast.LENGTH_LONG).show();
//                    }

                }
            });

            if (item.getLiked_or_not().equalsIgnoreCase("1")) {
                mLiked = true;
                mImageViewBottomBarLike.setImageResource(R.drawable.drawable_bottom_bar_like_grey_512);
            } else {
                mLiked = false;
            }

            if (mFavourites.contains(e_petition_number)) {
                mFavourite = true;
            } else {
                mFavourite = false;
            }

            if (item.getComment_posted_or_not().equalsIgnoreCase("1")) {
                mImageViewBottomBarComment.setImageResource(R.drawable.drawable_bottom_bar_comment_grey_512);
            } else {
                mImageViewBottomBarComment.setImageResource(R.drawable.drawable_bottom_bar_comment_white_512);
            }

            mLikeCount = Integer.parseInt(item.getLike_count().trim());


            mTextViewBottomBarLikes.setText("(" + item.getLike_count().trim() + ")");
            mTextViewBottomBarComments.setText("(" + item.getComments_count().trim() + ")");
            mTextViewBottomBarSupports.setText("(" + item.getSupports_count().trim() + ")");

            ActivityCompat.invalidateOptionsMenu(activity);
        }
    }


    private void displaySMSAlert(final String pno, final String official_mobile, final String e_pno, final String sms_message, final String confirmation_message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        AlertDialog dialog;

        alert.setTitle("Alert");
        alert.setMessage("An SMS will be sent from your mobile. " +
                "This will inccur charges depending on your mobile operator. " +
                "Do you want to proceed with sending SMS?");
        alert.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    @RequiresApi(api = Build.VERSION_CODES.M)
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendSMS(pno, official_mobile, e_pno, sms_message, confirmation_message);
                    }
                });

        alert.setNegativeButton("NO",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = alert.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    private void displaySMSSentAlert() {


        AlertDialog.Builder alert = new AlertDialog.Builder(activity);
        AlertDialog dialog;

        alert.setTitle("Thank You");
        alert.setMessage("Thank you for showing your support.\n\nIf you receive a call from the respondent, please ask him to complete the work as per petition at the earliest.");
        alert.setNeutralButton("OK",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        dialog = alert.create();
        dialog.getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        dialog.show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void SMSPermission()
    {
        if (ActivityCompat.checkSelfPermission(getActivity(), android.Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED)
        {
            String[] permission = new String[]{android.Manifest.permission.READ_SMS};
            requestPermissions(permission,SMSREADPERMISSION);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMSREADPERMISSION)
        {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
            {

            }
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.M)
    private void sendSMS(String pno, String mobile, String e_petition_no, String sms_message, String confirmation_message) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "sendSMS -> MemberID Type: " + mProfile.getMemberIdType());

        mButtonSupport.setEnabled(false);
        mButtonSupport.setText("Thanks For Your Support");


        try {

            mDeliveryReportsTableDbAdapter = DatabaseHelper.get(activity.getApplicationContext()).getDeliveryReportsTableDbAdapter();
            mDeliveryReportsTableDbAdapter.beginTransaction();
            try {

                ItemDeliveryReportsTable item = new ItemDeliveryReportsTable();
                item.setMember_id(mProfile.getMemberId());
                item.setE_petition_number(e_petition_no);
                item.setPetition_number(pno);
                item.setSent_from(mProfile.getUserMobile());
                item.setSent_to(mobile);
                item.setSms_content(sms_message);
                item.setConfirmation_message(confirmation_message);
                item.setSent_sms_success(0);
                item.setDeliver_sms_success(0);
                item.setSynced(0);
                item.setMember_id_type(mProfile.getMemberIdType());

                mDeliveryReportsTableDbAdapter.insertRow(item);
                mDeliveryReportsTableDbAdapter.setTransactionSuccessful();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "SMS Content inserted in Database");

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mDeliveryReportsTableDbAdapter.endTransaction();
            }


            String SMS_SENT_ACTION = "SMS_SENT_" + mobile;
            String SMS_DELIVERED_ACTION = "SMS_DELIVERED_" + mobile;

            Intent sentSMSIntent = new Intent(activity, SMSSentReceiver.class);
            sentSMSIntent.setAction(SMS_SENT_ACTION);
            sentSMSIntent.putExtra("member_id", mProfile.getMemberId());
            sentSMSIntent.putExtra("e_petition_no", e_petition_no);
            sentSMSIntent.putExtra("petition_no", pno);
            sentSMSIntent.putExtra("from_mobile", mProfile.getUserMobile());
            sentSMSIntent.putExtra("to_mobile", mobile);
            sentSMSIntent.putExtra("sms_message", sms_message);
            sentSMSIntent.putExtra("confirmation_message", confirmation_message);
            sentSMSIntent.putExtra("member_id_type", mProfile.getMemberIdType());

            PendingIntent sentPI = PendingIntent.getBroadcast(activity, 0, sentSMSIntent, 0);

            Intent deliveredSMSIntent = new Intent(activity, SMSDeliveredReceiver.class);
            deliveredSMSIntent.setAction(SMS_DELIVERED_ACTION);
            deliveredSMSIntent.putExtra("member_id", mProfile.getMemberId());
            deliveredSMSIntent.putExtra("e_petition_no", e_petition_no);
            deliveredSMSIntent.putExtra("petition_no", pno);
            deliveredSMSIntent.putExtra("from_mobile", mProfile.getUserMobile());
            deliveredSMSIntent.putExtra("to_mobile", mobile);
            deliveredSMSIntent.putExtra("sms_message", sms_message);
            deliveredSMSIntent.putExtra("confirmation_message", confirmation_message);
            deliveredSMSIntent.putExtra("member_id_type", mProfile.getMemberIdType());

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Member Type ID: in deliveredSMSIntent: " + deliveredSMSIntent.getStringExtra("member_id_type"));

            PendingIntent deliveredPI = PendingIntent.getBroadcast(activity, 0,
                    deliveredSMSIntent, 0);

            SmsManager sms = SmsManager.getDefault();

            if (Const.DEBUGGING) {
                Log.d(Const.DEBUG, "Mobile:" + mobile);
                Log.d(Const.DEBUG, "SMS Message:" + sms_message);
                Log.d(Const.DEBUG, "SMS Length:" + sms_message.toString().length());
            }
            sms_message = sms_message.substring(0, Math.min(sms_message.length(), 160));


            if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.SEND_SMS)==PackageManager.PERMISSION_GRANTED)
            {
                try {
                    //SmsManager smsManager = SmsManager.getDefault();
                    sms.sendTextMessage(mobile, null, sms_message, sentPI, deliveredPI);
                    //Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_LONG).show();
                    displaySMSSentAlert();
                } catch (Exception ex) {
                    Toast.makeText(getActivity(),"SMS sent failed "+ex.getMessage().toString(),
                            Toast.LENGTH_LONG).show();
                    ex.printStackTrace();
                }
            }
            else
            {
                SMSPermission();
            }

//            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.SEND_SMS)) {
//
//                Log.d("smsper","sent");
//                sms.sendTextMessage(mobile, null, sms_message, sentPI, deliveredPI);
//                //Toast.makeText(getActivity(), "Message Sent", Toast.LENGTH_LONG).show();
//                displaySMSSentAlert();
//
//            }


        } catch (Exception e) {
            e.printStackTrace();
            Log.d("smsper","failed "+e.getMessage());
        }


    }

    private void dismissProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showProgressDialog(String message) {

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }


    @Override
    public void onPause() {
        super.onPause();
        dismissProgressDialog();
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        activity.getMenuInflater().inflate(R.menu.menu_support_petition, menu);

        MenuItem item = menu.findItem(R.id.action_like);
        MenuItemCompat.setActionView(item, R.layout.badge_like_layout);
        MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!mLiked)
                    sendLike();
                else {
                    //Toast.makeText(activity, "Already Liked", Toast.LENGTH_LONG).show();
                    Toast.makeText(activity, "Likes are disabled", Toast.LENGTH_LONG).show();
                }
            }
        });

        item.setVisible(false);

        MenuItem item1 = menu.findItem(R.id.action_comment);
        item1.setVisible(false);

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_share) {

            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Support this petition");
            intent.putExtra(
                    Intent.EXTRA_TEXT,
                    "I " + mProfile.getUserName() + ", " +
                            "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
                            petition_title + " " + Const.APP_INSTALL_LINK);
            startActivity(Intent.createChooser(intent, "Share Petition..."));
        } else if (id == R.id.action_like) {
            //sendLike();
        } else if (id == R.id.action_comment) {
            //displayComments();
            startActivity(new Intent(activity, CommentsActivity.class).putExtra("petition_no", petition_number).putExtra("e_petition_no", e_petition_number).putExtra("isSuccessPetition", true));
        } else if (id == R.id.action_favourites) {

            if (mFavourite) {

                // update database
                mFavouritesTableDbAdapter.beginTransaction();
                try {

                    ItemFavouritesTable itemFavouritesTable = new ItemFavouritesTable();
                    itemFavouritesTable.setE_pno(e_petition_number);

                    mFavouritesTableDbAdapter.deleteRow(itemFavouritesTable);
                    mFavouritesTableDbAdapter.setTransactionSuccessful();

                    mFavourite = false;
                    Toast.makeText(activity, "Removed from Favourites", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mFavouritesTableDbAdapter.endTransaction();
                }

                mFavourites = mFavouritesTableDbAdapter.getFavourites();
                ActivityCompat.invalidateOptionsMenu(activity);
            } else {

                // update database
                mFavouritesTableDbAdapter.beginTransaction();
                try {

                    ItemFavouritesTable itemFavouritesTable = new ItemFavouritesTable();
                    itemFavouritesTable.setE_pno(e_petition_number);

                    mFavouritesTableDbAdapter.insertRow(itemFavouritesTable);
                    mFavouritesTableDbAdapter.setTransactionSuccessful();

                    mFavourite = true;
                    Toast.makeText(activity, "Marked as Favourite", Toast.LENGTH_LONG).show();
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    mFavouritesTableDbAdapter.endTransaction();
                }

                mFavourites = mFavouritesTableDbAdapter.getFavourites();
                ActivityCompat.invalidateOptionsMenu(activity);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.action_like);

        if (item != null) {

            View view = MenuItemCompat.getActionView(item);

            ImageView imageview = (ImageView) view.findViewById(R.id.action_like_imageview);
            if (mLiked) {
                imageview.setImageResource(R.drawable.ic_thumb_up_grey600_24dp);
            } else {
                imageview.setImageResource(R.drawable.ic_thumb_up_white_24dp);
            }

            TextView count = (TextView) view.findViewById(R.id.actionbar_notifcation_textview);


            count.setText("(" + mLikeCount + ")");
        }

        MenuItem fav_item = menu.findItem(R.id.action_favourites);
        if (mFavourite) {
            fav_item.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_favourite_24_grey));
        } else {
            fav_item.setIcon(ContextCompat.getDrawable(activity, R.drawable.ic_favourite_24));
        }


    }


    public void sendLike() {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        String url = Const.FINAL_URL + Const.URLs.GIVE_LIKE;
        url = url + "epetno=" + e_petition_number;
        url = url + "&memberid=" + member_id;
        url = url + "&memberid_type=" + member_id_type;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Check Liked Url = " + url);

        showProgressDialog("Registering your like...");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        dismissProgressDialog();

                        try {

                            if (response.getString("responce").equalsIgnoreCase("success")) {

                                mLiked = true;
                                mLikeCount = Integer.parseInt(response.getString("status").split(":")[1].toString().trim());

                                mImageViewBottomBarLike.setImageResource(R.drawable.drawable_bottom_bar_like_grey_512);


                                // update database
                                mPetitionsTableDbAdapter.beginTransaction();
                                try {

                                    mPetitionsTableDbAdapter.updateLikeStatus(petition_number, "1");
                                    mPetitionsTableDbAdapter.updateLikeCount(petition_number, String.valueOf(mLikeCount));
                                    mPetitionsTableDbAdapter.setTransactionSuccessful();
                                } catch (Exception e) {
                                    e.printStackTrace();
                                } finally {
                                    mPetitionsTableDbAdapter.endTransaction();
                                }

                                ActivityCompat.invalidateOptionsMenu(activity);
                            }

                            Toast.makeText(activity, "Liked", Toast.LENGTH_SHORT).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        dismissProgressDialog();

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    activity,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);
    }


    private void checkCanSupport(final String pno, final String official_mobile, final String e_pno, final String sms_message, final String confirmation_message) {


        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        //String max_sms_reach_url = Const.BASE_URL + Const.URLs.CHECK_SUPPORT_ENABLE_MAX_SMS_REACH;
        String max_sms_reach_url = Const.FINAL_URL + Const.URLs.CHECK_SUPPORT_ENABLE_MAX_SMS_REACH;
        max_sms_reach_url = max_sms_reach_url + "memberid=" + member_id;
        //max_sms_reach_url = max_sms_reach_url + "&petitionno=" + pno;
        max_sms_reach_url = max_sms_reach_url + "&epetno=" + e_pno;
        max_sms_reach_url = max_sms_reach_url + "&towhom=" + official_mobile;
        max_sms_reach_url = max_sms_reach_url + "&memberid_type=" + member_id_type;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Max SMS Reach Url: " + max_sms_reach_url);

        showProgressDialog("Validating your support...");

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, max_sms_reach_url, null,
                new Response.Listener<JSONObject>() {

                    @Override
                    public void onResponse(JSONObject response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        try {

                            dismissProgressDialog();

                            if (response.getString("responce").equalsIgnoreCase("disable")) {

                                String message = response.getString("status");
                                Toast.makeText(activity, message, Toast.LENGTH_SHORT).show();
                            } else {
                                displaySMSAlert(pno, official_mobile, e_pno, sms_message, confirmation_message);
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            dismissProgressDialog();
                        }
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        dismissProgressDialog();

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    activity,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                    }
                });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);

    }


    private void getRatingsFromServer(String e_pno) {

        String url = Const.FINAL_URL + Const.URLs.GET_SUCCESS_PETITION_DETAILS;
        url = url + "epetno=" + e_pno;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        parseResponse(response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            mTextViewFetchRatings.setText("Cannot load Ratings at this point of time...");
                        }
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);

    }

    public void parseResponse(JSONArray response) {

        if (response != null && response.length() > 0) {

            try {

                JSONObject jsonObject = response.getJSONObject(0);

                mTextViewSupports.setText(jsonObject.getString("total_supports"));
                mTextViewLikes.setText(jsonObject.getString("total_likes"));
                mTextViewComments.setText(jsonObject.getString("total_comments"));
                mTextViewRatings.setText(jsonObject.getString("rating"));

                float rating = Float.parseFloat(jsonObject.getString("rating"));

                mRatingBarRating.setRating(rating);

//                LayerDrawable drawable = (LayerDrawable) mRatingBarRating.getProgressDrawable();
//
//                for (int i = 0; i < ((int) rating) - 1; i++) {
//                    Drawable progress = drawable.getDrawable(2);
//                    DrawableCompat.setTint(progress, ContextCompat.getColor(activity, R.color.ratings_star_color));
//                }
//
////                if ((int) rating < 5) {
////                    for (int j = 5 - (((int) rating) + 1); j < 5; j++) {
////                        Drawable progress = drawable.getDrawable(j);
////                        DrawableCompat.setTint(progress, ContextCompat.getColor(activity, R.color.ratings_star_color_light));
////                    }
////                }
//
////                Drawable progress = mRatingBarRating.getProgressDrawable();
////                DrawableCompat.setTint(progress, ContextCompat.getColor(activity, R.color.ratings_star_color));


                mTextViewDate.setText("Closed on: " + jsonObject.getString("closed_on"));
                mTextViewSuccessMessage.setText(Html.fromHtml("<b>\"</b>" + jsonObject.getString("success_message") + "<b>\"</b>"));

            } catch (Exception e) {
                e.printStackTrace();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Exception while parsing ratings response");
            }
        }
    }


    public void onFABClick() {

        if (mUrls != null) {

            if (mUrls.size() == 0) {

                Intent intent = new Intent(Intent.ACTION_SEND);
                intent.setType("text/plain");
                intent.putExtra(Intent.EXTRA_SUBJECT, "Support this petition");
                intent.putExtra(
                        Intent.EXTRA_TEXT,
                        "I " + mProfile.getUserName() + ", " +
                                "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
                                petition_title + " " + Const.APP_INSTALL_LINK);
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
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Support this petition");
                            intent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    "I " + mProfile.getUserName() + ", " +
                                            "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
                                            petition_title + " " + Const.APP_INSTALL_LINK);
                            startActivity(Intent.createChooser(intent, "Share Petition..."));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Support this petition");
                        intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "I " + mProfile.getUserName() + ", " +
                                        "Please support this petition in ICRF as www.icrf.org.in/s/" + petition_number + " - " +
                                        petition_title + "-" + Const.APP_INSTALL_LINK);
                        startActivity(Intent.createChooser(intent, "Share Petition..."));
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });
            }
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
                        .setContentTitle("Support this petition")
                        .setImageUrl(Uri.parse(mUrls.get(0)))
                        .setContentDescription("I " + mProfile.getUserName() + ", " +
                                "Please support this petition in ICRF as www.icrf.org.in/" + petition_number + " - " +
                                petition_title + " " + Const.APP_INSTALL_LINK)
                        .setContentUrl(Uri.parse("http://www.icrf.org.in/" + petition_number))
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

        return;
    }

    public void sendHit(String event) {
        t.setScreenName(Html.fromHtml(event).toString());
        t.send(new HitBuilders.AppViewBuilder().build());
    }
}
