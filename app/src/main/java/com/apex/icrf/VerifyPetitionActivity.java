package com.apex.icrf;

import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.text.Html;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.adapters.DetailPetitionRecyclerViewAdapter;
import com.apex.icrf.classes.ItemDeliveryReportsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.LinearLayoutManager;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.SMSDeliveredReceiver;
import com.apex.icrf.utils.SMSSentReceiver;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

/**
 * Created by WASPVamsi on 30/09/15.
 */
public class VerifyPetitionActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    SharedPreferences prefs;
    Profile mProfile;
    ProgressDialog progressDialog;

    LinearLayout rootview;

    private TextView mTextViewPetitionTitle, mTextViewPetitionDescription;

    TextView mTextViewPetitionByName, mTextViewPetitionByDate, mTextViewPetitionByAddress, mTextViewPetitionOnName, mTextViewPetitionOnAddress,
            mTextViewPetitionOnPhone, mTextViewPetitionOnEmail, mTextViewSMSMatter;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    DetailPetitionRecyclerViewAdapter mDetailPetitionRecyclerViewAdapter;
    TextView mTextViewYouTubeUrl, mTextViewDocumentUrl, mTextViewAttachmentsTitle;
    EditText mEditTextConfirmationMessage;

    CheckBox mCheckBoxTerms;

    Button mButtonVerify;

    String petition_number, e_petition_number, sms_matter, official_mobile;
    boolean isVerification = false;
    boolean isSupport = false;

    ArrayList<String> mUrls = new ArrayList<String>();

    DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_verify_petition);
        mUrls.clear();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        //mTextViewTitle.setText("Verify Petition");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mProfile = new Profile(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        rootview = (LinearLayout) findViewById(R.id.rootview);
        rootview.setVisibility(View.GONE);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(this, android.support.v7.widget.LinearLayoutManager.HORIZONTAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.HORIZONTAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mTextViewPetitionTitle = (TextView) findViewById(R.id.textView_petition_title);
        mTextViewAttachmentsTitle = (TextView) findViewById(R.id.textView_attachments_title);
        mTextViewYouTubeUrl = (TextView) findViewById(R.id.textView_petition_youtube_url);
        mTextViewDocumentUrl = (TextView) findViewById(R.id.textView_petition_document_url);

        mTextViewPetitionByName = (TextView) findViewById(R.id.textView_petition_by);
        mTextViewPetitionByDate = (TextView) findViewById(R.id.textView_petition_by_date);
        mTextViewPetitionByAddress = (TextView) findViewById(R.id.textView_petition_by_address);

        mTextViewPetitionOnName = (TextView) findViewById(R.id.textView_petition_on);
        mTextViewPetitionOnAddress = (TextView) findViewById(R.id.textView_petition_on_address);
        mTextViewPetitionOnPhone = (TextView) findViewById(R.id.textView_petition_on_phone);
        mTextViewPetitionOnEmail = (TextView) findViewById(R.id.textView_petition_on_email);

        mTextViewPetitionDescription = (TextView) findViewById(R.id.textView_petition_desc);

        mTextViewSMSMatter = (TextView) findViewById(R.id.textView_sms_matter);
        mEditTextConfirmationMessage = (EditText) findViewById(R.id.editText_sms_matter);

        mCheckBoxTerms = (CheckBox) findViewById(R.id.checkBox_terms);

        mButtonVerify = (Button) findViewById(R.id.button_verify);
        mButtonVerify.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isVerification) {
                    if (mCheckBoxTerms.isChecked() && !mEditTextConfirmationMessage.getText().toString().equalsIgnoreCase("")) {
                        mButtonVerify.setEnabled(false);
                        mButtonVerify.setText("VERIFICATION SENT");
                        verifyPetition(petition_number, mEditTextConfirmationMessage.getText().toString());
                    } else {
                        if (!mCheckBoxTerms.isChecked())
                            Toast.makeText(VerifyPetitionActivity.this, "Please accept terms and conditions to proceed.", Toast.LENGTH_LONG).show();
                        else if (mEditTextConfirmationMessage.getText().toString().equalsIgnoreCase(""))
                            Toast.makeText(VerifyPetitionActivity.this, "Please enter confirmation message.", Toast.LENGTH_LONG).show();
                    }
                } else if (isSupport) {
                    if (mEditTextConfirmationMessage.length() == 0) {
                        Toast.makeText(VerifyPetitionActivity.this, "Please enter confirmation message to proceed.", Toast.LENGTH_LONG).show();
                    } else if (mCheckBoxTerms.isChecked()) {
                        checkCanSupport(petition_number, official_mobile, e_petition_number, mEditTextConfirmationMessage.getText().toString());
                    } else {
                        Toast.makeText(VerifyPetitionActivity.this, "Please accept terms and conditions to proceed.", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });


        if (!mProfile.isUserLoggedIn()) {

            Intent intent = getIntent();
            String url = intent.getDataString();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Url = " + url);

            String[] url_split = url.split("/");
            petition_number = url_split[url_split.length - 1];

            if (url_split[url_split.length - 2].equalsIgnoreCase("s")) {
                isSupport = true;
                isVerification = false;
                mTextViewTitle.setText("Support Petition");
            } else if (url_split[url_split.length - 2].equalsIgnoreCase("v")) {
                isVerification = true;
                isSupport = false;
                mTextViewTitle.setText("Verify Petition");
            } else {
                isVerification = true;
                isSupport = false;
                mTextViewTitle.setText("Verify Petition");
            }

            prefs.edit().putString("current_petition_number", petition_number).commit();
            finish();
            startActivity(new Intent(this, SplashActivity.class).putExtra("from_verify_petition", true));
        } else {

            Intent intent = getIntent();
            if (intent.hasExtra("from_login")) {

                petition_number = prefs.getString("current_petition_number", "");
                getDataFromServer(petition_number);
            } else {
                String url = intent.getDataString();

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Url = " + url);

                String[] url_split = url.split("/");
                petition_number = url_split[url_split.length - 1];

                if (url_split[url_split.length - 2].equalsIgnoreCase("s")) {
                    isSupport = true;
                    isVerification = false;
                    mTextViewTitle.setText("Support Petition");
                } else if (url_split[url_split.length - 2].equalsIgnoreCase("v")) {
                    isVerification = true;
                    isSupport = false;
                    mTextViewTitle.setText("Verify Petition");
                } else {
                    isVerification = false;
                    isSupport = true;
                    mTextViewTitle.setText("Support Petition");
                }
                getDataFromServer(petition_number);
            }
        }

    }


    public void verifyPetition(final String petition_number, final String sms_matter) {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();
        String verify_msg_body = "";
        try {
            verify_msg_body = URLEncoder.encode(sms_matter, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }

        String url = Const.FINAL_URL + Const.URLs.VERIFY_PETITION;
        url = url + "memberid=" + member_id;
        url = url + "&petitionno=" + petition_number;
        url = url + "&verifimsgbody=" + verify_msg_body;
        url = url + "&memberid_type=" + member_id_type;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        showProgressDialog("Verifying the Petition...");

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
                            String status = response.getString("status");
                            Toast.makeText(VerifyPetitionActivity.this, status, Toast.LENGTH_LONG).show();
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
                                    VerifyPetitionActivity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                        Toast.makeText(VerifyPetitionActivity.this, "Cannot reach our servers now. Please try again later.", Toast.LENGTH_LONG).show();
                    }
                });

        jsonObjectRequest.setTag(Const.VOLLEY_TAG);
        jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonObjectRequest);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();

            if (mProfile.isUserLoggedIn())
                startActivity(new Intent(this, MainTabbedActivity.class));
            else
                startActivity(new Intent(this, SplashActivity.class));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        if (mProfile.isUserLoggedIn())
            startActivity(new Intent(this, MainTabbedActivity.class));
        else
            startActivity(new Intent(this, SplashActivity.class));
    }


    private void getDataFromServer(final String pno) {

        String url = Const.FINAL_URL + Const.URLs.ALL_PETITIONS;
        url = url + "type_of_petitions=search";
        url = url + "&memberid=" + mProfile.getMemberId();
        url = url + "&memberid_type=" + mProfile.getMemberIdType();
        url = url + "&pageIndex=1";
        url = url + "&Search_PetitionNo=" + pno;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        showProgressDialog("Getting Petition Details...");

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        parseResponse(pno, response);
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
                                    VerifyPetitionActivity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    private void parseResponse(final String pno, JSONArray response) {


        String COMMA = ", ";

        try {

            final JSONObject object = response.getJSONObject(0);

            // Petition Details Array
            JSONArray petitionDetailsArray = object.getJSONArray("Petition_Details");
            JSONObject petitionDetailsObject = petitionDetailsArray.getJSONObject(0);

            e_petition_number = petitionDetailsObject.getString("epetno");
            official_mobile = petitionDetailsObject.getString("official_mobile");
            sms_matter = petitionDetailsObject.getString("smsmatter");

            JSONArray petitionPostedByArray = object.getJSONArray("Petition_PostedBy");
            JSONObject petitionPostedByObject = petitionPostedByArray.getJSONObject(0);


            mTextViewPetitionTitle.setText(petitionDetailsObject.getString("petition_sublect"));

            String state1 = petitionPostedByObject.getString("petitioner_state").substring(0, 1).toUpperCase()
                    + petitionPostedByObject.getString("petitioner_state").substring(1).toLowerCase();

            mTextViewPetitionByName.setText(Html.fromHtml("<b>Petition By:</b> <br />" + petitionPostedByObject.getString("petitioner_name")));
            mTextViewPetitionByDate.setText("Date: " + petitionDetailsObject.getString("dt"));
            mTextViewPetitionByAddress.setText(Html.fromHtml("<b>Address:</b> <br />" + petitionPostedByObject.getString("petitioner_city") + COMMA
                    + petitionPostedByObject.getString("petitioner_dist") + COMMA + petitionPostedByObject.getString("petitioner_state") + COMMA
                    + " - " + petitionPostedByObject.getString("petitioner_pincode") + "."));

            String state = petitionDetailsObject.getString("state").substring(0, 1).toUpperCase()
                    + petitionDetailsObject.getString("state").substring(1).toLowerCase();

            mTextViewPetitionOnName.setText(Html.fromHtml("<b>Respondent:</b> <br />" + petitionDetailsObject.getString("official_name") + COMMA
                    + petitionDetailsObject.getString("official_designation") + COMMA + petitionDetailsObject.getString("office_dep_name")));
//            mTextViewPetitionOnAddress.setText(Html.fromHtml("<b>Address:</b> <br />" + petitionDetailsObject.getString("petitionAddress") + COMMA
//                    + petitionDetailsObject.getString("officeAdress") + COMMA
//                    + petitionDetailsObject.getString("dist") + COMMA + state + " - "
//                    + petitionDetailsObject.getString("pincode") + "."));

            mTextViewPetitionOnAddress.setText(Html.fromHtml("<b>Address:</b> <br />"
                    + petitionDetailsObject.getString("officeAdress") + COMMA
                    + petitionDetailsObject.getString("dist") + COMMA + state + " - "
                    + petitionDetailsObject.getString("pincode") + "."));


            mTextViewPetitionOnPhone.setText(Html.fromHtml("<b>Mobile:</b> " + petitionDetailsObject.getString("official_mobile")));
            mTextViewPetitionOnEmail.setText(Html.fromHtml("<b>Email:</b> " + petitionDetailsObject.getString("official_email")));

            mTextViewPetitionDescription.setText(Html.fromHtml("<b>Petition Description:</b> <br />" + Html.fromHtml(petitionDetailsObject.getString("petition"))));


            // Attachments
            String attachments = object.getJSONArray("Petition_Attachments").toString();

            if (attachments != null && !attachments.equalsIgnoreCase("")) {

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

                            mTextViewAttachmentsTitle.setVisibility(View.VISIBLE);
                            mTextViewYouTubeUrl.setVisibility(View.VISIBLE);
                            mTextViewYouTubeUrl.setText("Youtube Link: " + path);
                        }

                        if (!hasImage) {
                            String path = "http://icrf.org.in/Attachment/e-petition_img.jpg";
                            mUrls.add(path);
                        }
                    }

                    mDetailPetitionRecyclerViewAdapter = new DetailPetitionRecyclerViewAdapter(this, mUrls);
                    mRecyclerView.setAdapter(mDetailPetitionRecyclerViewAdapter);

                    rootview.setVisibility(View.VISIBLE);
                } catch (Exception e) {
                    e.printStackTrace();

                    dismissProgressDialog();

                    mTextViewAttachmentsTitle.setVisibility(View.GONE);
                    mTextViewDocumentUrl.setVisibility(View.GONE);
                    mTextViewYouTubeUrl.setVisibility(View.GONE);
                }
            }

            // Petition-Wise Checking Array
            JSONArray petitionWiseCheckingArray = object.getJSONArray("Petition_Wise_Checking");
            JSONObject petitionWiseCheckingObject = petitionWiseCheckingArray.getJSONObject(0);
            JSONObject verifiedCheckingObject = petitionWiseCheckingObject.getJSONObject("VerifiedChecking");

            if (isVerification) {

                mButtonVerify.setText("I VERIFY");
                if (verifiedCheckingObject.getString("Am_I_Verified").equalsIgnoreCase("enable")) {
                    mButtonVerify.setEnabled(true);
                } else {
                    mButtonVerify.setEnabled(false);
                    mButtonVerify.setText("ALREADY VERIFIED");
                }
            }


            JSONObject supportCheckingObject = petitionWiseCheckingObject.getJSONObject("SupportedChecking");

            if (isSupport) {
                mButtonVerify.setText("I SUPPORT");

                if (supportCheckingObject.getString("Am_I_Supported").equalsIgnoreCase("enable"))
                    mButtonVerify.setEnabled(true);
                else {
                    mButtonVerify.setEnabled(false);
                    mButtonVerify.setText("ALREADY SUPPORTED");
                }

                //mTextViewSMSMatter.setVisibility(View.GONE);
                //mEditTextSMSContent.setVisibility(View.GONE);
            }

            dismissProgressDialog();
        } catch (Exception e) {
            e.printStackTrace();
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


    private void checkCanSupport(final String pno, final String official_mobile, final String e_pno, final String sms_message) {


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
                                Toast.makeText(VerifyPetitionActivity.this, message, Toast.LENGTH_SHORT).show();
                            } else {
                                displaySMSAlert(pno, official_mobile, e_pno, sms_message);
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
                                    VerifyPetitionActivity.this,
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

    private void displaySMSAlert(final String pno, final String official_mobile, final String e_pno, final String sms_message) {

        AlertDialog.Builder alert = new AlertDialog.Builder(VerifyPetitionActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        AlertDialog dialog;

        alert.setTitle("Alert");
        alert.setMessage("An SMS will be sent from your mobile. " +
                "This will inccur charges depending on your mobile operator. " +
                "Do you want to proceed with sending SMS?");
        alert.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendSMS(pno, official_mobile, e_pno, sms_message);
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

        AlertDialog.Builder alert = new AlertDialog.Builder(VerifyPetitionActivity.this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
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

    private void sendSMS(String pno, String mobile, String e_petition_no, String sms_message) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "sendSMS -> MemberID Type: " + mProfile.getMemberIdType());

        mButtonVerify.setEnabled(false);
        mButtonVerify.setText("Thanks For Your Support");

        try {

            mDeliveryReportsTableDbAdapter = DatabaseHelper.get(getApplicationContext()).getDeliveryReportsTableDbAdapter();
            mDeliveryReportsTableDbAdapter.beginTransaction();
            try {

                ItemDeliveryReportsTable item = new ItemDeliveryReportsTable();
                item.setMember_id(mProfile.getMemberId());
                item.setE_petition_number(e_petition_no);
                item.setPetition_number(pno);
                item.setSent_from(mProfile.getUserMobile());
                item.setSent_to(mobile);
                item.setSms_content(sms_message);
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

            Intent sentSMSIntent = new Intent(VerifyPetitionActivity.this, SMSSentReceiver.class);
            sentSMSIntent.setAction(SMS_SENT_ACTION);
            sentSMSIntent.putExtra("member_id", mProfile.getMemberId());
            sentSMSIntent.putExtra("e_petition_no", e_petition_no);
            sentSMSIntent.putExtra("petition_no", pno);
            sentSMSIntent.putExtra("from_mobile", mProfile.getUserMobile());
            sentSMSIntent.putExtra("to_mobile", mobile);
            sentSMSIntent.putExtra("sms_message", sms_message);
            sentSMSIntent.putExtra("member_id_type", mProfile.getMemberIdType());

            PendingIntent sentPI = PendingIntent.getBroadcast(VerifyPetitionActivity.this, 0, sentSMSIntent, 0);

            Intent deliveredSMSIntent = new Intent(VerifyPetitionActivity.this, SMSDeliveredReceiver.class);
            deliveredSMSIntent.setAction(SMS_DELIVERED_ACTION);
            deliveredSMSIntent.putExtra("member_id", mProfile.getMemberId());
            deliveredSMSIntent.putExtra("e_petition_no", e_petition_no);
            deliveredSMSIntent.putExtra("petition_no", pno);
            deliveredSMSIntent.putExtra("from_mobile", mProfile.getUserMobile());
            deliveredSMSIntent.putExtra("to_mobile", mobile);
            deliveredSMSIntent.putExtra("sms_message", sms_message);
            deliveredSMSIntent.putExtra("member_id_type", mProfile.getMemberIdType());

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Member Type ID: in deliveredSMSIntent: " + deliveredSMSIntent.getStringExtra("member_id_type"));

            PendingIntent deliveredPI = PendingIntent.getBroadcast(VerifyPetitionActivity.this, 0,
                    deliveredSMSIntent, 0);

            SmsManager sms = SmsManager.getDefault();

            if (Const.DEBUGGING) {
                Log.d(Const.DEBUG, "Mobile:" + mobile);
                Log.d(Const.DEBUG, "SMS Message:" + sms_message);
                Log.d(Const.DEBUG, "SMS Length:" + sms_message.toString().length());
            }
            sms_message = sms_message.substring(0, Math.min(sms_message.length(), 160));

            sms.sendTextMessage(mobile, null, sms_message, sentPI, deliveredPI);

            displaySMSSentAlert();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
