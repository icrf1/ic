package com.apex.icrf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 15/02/16.
 */
public class MainProfileFragment extends Fragment {

    //private IHomeListener mIHomeListener;

    Activity activity;
    Profile mProfile;
    ProgressDialog progressDialog;

    CircleImageView imageView;
    TextView textViewName, textViewPhone, textViewEmail;
    EditText editTextAccount, editTextBankName, editTextBranch, editTextPan, editTextIFSC;
    Button buttonSave;

    TextView textViewBankDetailsTitle, textViewContactUs;
    ScrollView scrollView;
    byte[] imageAsBytes;
    boolean isEditable = false;
    SharedPreferences saved_values;
    String image_64;
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

//        if (activity instanceof IHomeListener) {
//            mIHomeListener = (IHomeListener) activity;
//        } else {
//            if (Const.DEBUGGING)
//                Log.d(Const.DEBUG, "Exception in onAttach");
//        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_profile, container, false);

        mProfile = new Profile(activity);

        imageView = (CircleImageView) view.findViewById(R.id.header_profile_pic);
        saved_values = PreferenceManager.getDefaultSharedPreferences(activity);
        if(!saved_values.getString("image_64","").equals(""))   {

            imageAsBytes = Base64.decode(saved_values.getString("image_64","").getBytes(), Base64.DEFAULT);
            BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageAsBytes, 0, imageAsBytes.length));
        }
        textViewName = (TextView) view.findViewById(R.id.textView_name);
        textViewName.setText("Name: "+mProfile.getUserName());

        textViewPhone = (TextView) view.findViewById(R.id.textView_phone_number);
        textViewPhone.setText("Mobile: "+mProfile.getUserMobile());

        textViewEmail = (TextView) view.findViewById(R.id.textView_email);
        textViewEmail.setText("Email: "+mProfile.getUserEmail());

        textViewBankDetailsTitle = (TextView) view.findViewById(R.id.textView_bank_details_title);
        textViewContactUs = (TextView) view.findViewById(R.id.textView_contact_us);

        if(mProfile.getMemberIdType().equalsIgnoreCase("I"))
        textViewContactUs.setText(Html.fromHtml("For any queries please drop a mail to : <u>info@icrf.org.in</u>"));
        else
            textViewContactUs.setText(Html.fromHtml("For any queries please drop a mail to : <u>info@apextelecom.net</u>"));
        scrollView = (ScrollView) view.findViewById(R.id.scrollView);

        editTextAccount = (EditText) view.findViewById(R.id.editText_account_number);
        editTextBankName = (EditText) view.findViewById(R.id.editText_bank_name);
        editTextBranch = (EditText) view.findViewById(R.id.editText_branch);
        editTextIFSC = (EditText) view.findViewById(R.id.editText_ifsc);
        editTextPan = (EditText) view.findViewById(R.id.editText_pan_number);

        buttonSave = (Button) view.findViewById(R.id.button_save);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                saveChanges();
            }
        });

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);


        if (mProfile.getMemberIdType().equalsIgnoreCase("I")) {
            showBankDetails(true);
            getDataFromServer();
        } else {
            showBankDetails(false);
        }


        return view;
    }


    public void showBankDetails(boolean show) {

        if (show) {

            //textViewBankDetailsTitle.setVisibility(View.VISIBLE);
            //textViewContactUs.setVisibility(View.VISIBLE);
            scrollView.setVisibility(View.VISIBLE);
        } else {

            //textViewBankDetailsTitle.setVisibility(View.GONE);
            //textViewContactUs.setVisibility(View.GONE);
            scrollView.setVisibility(View.GONE);
        }
    }


    public void getDataFromServer() {

        String member_id = mProfile.getMemberId();

        String url = Const.FINAL_URL + Const.URLs.UPDATE_BANK_DETAILS;
        url = url + "memberid=" + member_id;
        url = url + "&person_name=" + "";
        url = url + "&acount_no=" + "";
        url = url + "&bank_name=" + "";
        url = url + "&bank_branch=" + "";
        url = url + "&pan_no=" + "";
        url = url + "&ifsc_code=" + "";
        url = url + "&post_or_get=get";

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        showProgressDialog("Getting your details...");

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

                        makeEditable(false);
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);


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

    private void parseResponse(JSONArray response) {

        dismissProgressDialog();

        if (response.length() == 0) {
            // Editable
            isEditable = true;
            makeEditable(true);
        } else {
            // Non Editable
            isEditable = false;
            makeEditable(false);
        }

        if (!isEditable) {

            try {

                JSONObject jsonObject = response.getJSONObject(0);
                String person_name = jsonObject.getString("person_name");
                String account_no = jsonObject.getString("acount_no");
                String bank_name = jsonObject.getString("bank_name");
                String bank_branch = jsonObject.getString("bank_branch");
                String pan_no = jsonObject.getString("pan_no");
                String ifsc_code = jsonObject.getString("ifsc_code");

//                if(person_name != null || person_name.length() != 0) {
//
//                }


                if (account_no != null && account_no.length() > 0) {
                    editTextAccount.setText(account_no);
                }

                if (bank_name != null && bank_name.length() > 0) {
                    editTextBankName.setText(bank_name);
                }

                if (bank_branch != null && bank_branch.length() > 0) {
                    editTextBranch.setText(bank_branch);
                }

                if (pan_no != null && pan_no.length() > 0) {
                    editTextPan.setText(pan_no);
                }

                if (ifsc_code != null && ifsc_code.length() > 0) {
                    editTextIFSC.setText(ifsc_code);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Log.d(Const.DEBUG, "Exception while parsing bank details");

            }
        }


    }

    private void makeEditable(boolean canEdit) {

        editTextAccount.setEnabled(canEdit);
        editTextBankName.setEnabled(canEdit);
        editTextBranch.setEnabled(canEdit);
        editTextIFSC.setEnabled(canEdit);
        editTextPan.setEnabled(canEdit);

        buttonSave.setEnabled(canEdit);
    }


    private void saveChanges() {

        if (editTextAccount.length() == 0
                || editTextBankName.length() == 0
                || editTextBranch.length() == 0
                || editTextIFSC.length() == 0
                //|| editTextPan.length() == 0
                ) {

            Toast.makeText(activity, "All fields are mandatory", Toast.LENGTH_LONG).show();
        } else if (editTextPan.length() < 10) {
            Toast.makeText(activity, "Enter valid PAN Number", Toast.LENGTH_LONG).show();
        } else if (editTextIFSC.length() < 11) {
            Toast.makeText(activity, "Enter valid IFSC Code", Toast.LENGTH_LONG).show();
        } else {

            showProgressDialog("Saving...");

            sendDataToServer();
        }
    }

    public void sendDataToServer() {

        String member_id = mProfile.getMemberId();
        String account_no = editTextAccount.getText().toString();
        String bank_name = editTextBankName.getText().toString();
        String bank_branch = editTextBranch.getText().toString();
        String pan_no = editTextPan.getText().toString();
        String ifsc_code = editTextIFSC.getText().toString();

        try {

            String url = Const.FINAL_URL + Const.URLs.UPDATE_BANK_DETAILS;
            url = url + "memberid=" + member_id;
            url = url + "&person_name=" + "";
            url = url + "&acount_no=" + URLEncoder.encode(account_no, "UTF-8");
            url = url + "&bank_name=" + URLEncoder.encode(bank_name, "UTF-8");
            url = url + "&bank_branch=" + URLEncoder.encode(bank_branch, "UTF-8");
            url = url + "&pan_no=" + pan_no;
            url = url + "&ifsc_code=" + ifsc_code;
            url = url + "&post_or_get=post";

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Url = " + url);

            showProgressDialog("Saving your details...");

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            dismissProgressDialog();

                            if (Const.DEBUGGING) {
                                Log.d(Const.DEBUG,
                                        "Response => " + response.toString());
                                Log.d(Const.DEBUG, "Length = " + response.length());
                            }

                            try {
                                Toast.makeText(activity, response.getString("status"), Toast.LENGTH_LONG).show();
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

                            makeEditable(false);
                            Toast.makeText(activity, "Submission Failed. Please contact ICRF directly.", Toast.LENGTH_LONG).show();
                        }
                    });

            jsonObjectRequest.setTag(Const.VOLLEY_TAG);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestManager.getRequestQueue().add(jsonObjectRequest);


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

}
