package com.apex.icrf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONObject;

import java.net.URLEncoder;

/**
 * Created by WASPVamsi on 03/01/16.
 */
public class MoreFeedbackFragment extends Fragment implements View.OnClickListener {

    Activity activity;

    ImageView mImageViewGoodTick, mImageViewOkTick, mImageViewSadTick;
    ImageView mImageViewGood, mImageViewOk, mImageViewSad;

    Button btnSubmit;
    EditText editTextFeedback;

    String feel = "";

    Profile mProfile;
    ProgressDialog progressDialog;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment_feedback, container, false);

        mImageViewGoodTick = (ImageView) view.findViewById(R.id.item_imageview_good_tick);
        mImageViewOkTick = (ImageView) view.findViewById(R.id.item_imageview_ok_tick);
        mImageViewSadTick = (ImageView) view.findViewById(R.id.item_imageview_sad_tick);

        mImageViewGoodTick.setVisibility(View.GONE);
        mImageViewOkTick.setVisibility(View.GONE);
        mImageViewSadTick.setVisibility(View.GONE);

        mImageViewGood = (ImageView) view.findViewById(R.id.item_imageview_good);
        mImageViewOk = (ImageView) view.findViewById(R.id.item_imageview_ok);
        mImageViewSad = (ImageView) view.findViewById(R.id.item_imageview_sad);

        mImageViewOk.setOnClickListener(this);
        mImageViewGood.setOnClickListener(this);
        mImageViewSad.setOnClickListener(this);

        btnSubmit = (Button) view.findViewById(R.id.button_submit_feedback);
        btnSubmit.setOnClickListener(this);

        editTextFeedback = (EditText) view.findViewById(R.id.editText_feeback);

        mProfile = new Profile(activity);
        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        return view;
    }

    @Override
    public void onClick(View v) {

        mImageViewGoodTick.setVisibility(View.GONE);
        mImageViewOkTick.setVisibility(View.GONE);
        mImageViewSadTick.setVisibility(View.GONE);

        if (v.getId() == R.id.item_imageview_good) {
            feel = "Good";
            mImageViewGoodTick.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.item_imageview_ok) {
            feel = "Ok";
            mImageViewOkTick.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.item_imageview_sad) {
            feel = "Sad";
            mImageViewSadTick.setVisibility(View.VISIBLE);
        } else if (v.getId() == R.id.button_submit_feedback) {

            String feedback = editTextFeedback.getText().toString();

            if (feel.equalsIgnoreCase("")) {
                Toast.makeText(activity, "Please select any one of the images above", Toast.LENGTH_LONG).show();
            } else if (feedback.equalsIgnoreCase("")) {
                Toast.makeText(activity, "Feedback cannot be empty.", Toast.LENGTH_LONG).show();
            } else if (feedback.length() < 10) {
                Toast.makeText(activity, "Feedback is too short. Enter a minimum of 10 characters to submit the feedback.", Toast.LENGTH_LONG).show();
            } else {
                submitFeedback(feel, feedback);
            }
        }
    }

    public void submitFeedback(String feel, String feedback) {

        showProgressDialog("Submitting your feedback...");

        try {

            String url = Const.FINAL_URL + Const.URLs.TAKE_FEEDBACK;
            url = url + "memberid=" + mProfile.getMemberId();
            url = url + "&memberid_type=" + mProfile.getMemberIdType();
            url = url + "&mobile=" + mProfile.getUserMobile();
            url = url + "&emailid=" + mProfile.getUserEmail();
            url = url + "&feel=" + feel;
            url = url + "&feedback_msg=" + URLEncoder.encode(feedback, "UTF-8");
            url = url + "&device_type=M";

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Post Feedback Url = " + url);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            if (response != null) {
                                dismissProgressDialog();
                                try {

                                    if (response.get("status") != null) {
                                        Toast.makeText(activity, response.getString("status"), Toast.LENGTH_LONG).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(activity, "Feedback submission failed. Please try again later", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                    dismissProgressDialog();
                }
            });

            jsonObjectRequest.setTag(Const.VOLLEY_TAG);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestManager.getRequestQueue().add(jsonObjectRequest);

        } catch (Exception e) {
            e.printStackTrace();
            dismissProgressDialog();

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Error while sending feedback.");
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
    public void onDestroy() {
        super.onDestroy();
        feel = "";
        dismissProgressDialog();
    }
}
