package com.apex.icrf;

/**
 * Created by WASPVamsi on 03/09/15.
 */

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apex.icrf.classes.IHomeListener;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.InternetConnectivity;
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

public class HomeFragment extends Fragment {

    Activity activity;
    ProgressDialog progressDialog;

    private TextView mTextViewQuotation, mTextViewPostPetition;
    private Button mButtonPopularPetitions, mButtonNewPetitions, mButtonVictoryPetitions, mButtonMyPoints;

    private IHomeListener mIHomeListener;

    SharedPreferences prefs;
    Profile mProfile;
    InternetConnectivity check;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;


        if (activity instanceof IHomeListener) {
            mIHomeListener = (IHomeListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_home, container, false);

        mTextViewQuotation = (TextView) view.findViewById(R.id.textView_quotation);
        mTextViewQuotation.setText(getResources().getString(R.string.quotation_1));

        mTextViewPostPetition = (TextView) view.findViewById(R.id.textView_post_petition);
        mTextViewPostPetition.setText(Html.fromHtml("<b><u>Post a Petition</u></b> and get maximum points. Help the Nation."));
        mTextViewPostPetition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIHomeListener.onPostAPetitionClicked();
            }
        });

        mButtonMyPoints = (Button) view.findViewById(R.id.button_points);
        mButtonMyPoints.setText(getResources().getString(R.string.points_1) + "\n" +
                getResources().getString(R.string.points_2));
        mButtonMyPoints.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.MY_TOTAL_POINTS_FRAGMENT);
                mIHomeListener.onMyTotalPointsClicked(bundle);
            }
        });

        mButtonPopularPetitions = (Button) view.findViewById(R.id.button_popular_petitions);
        mButtonPopularPetitions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.POPULAR_PETITIONS_FRAGMENT);
                mIHomeListener.onPopularButtonClicked(bundle);
            }
        });

        mButtonNewPetitions = (Button) view.findViewById(R.id.button_new_petitions);
        mButtonNewPetitions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.NEW_PETITIONS_FRAGMENT);
                mIHomeListener.onNewButtonClicked(bundle);
            }
        });

        mButtonVictoryPetitions = (Button) view.findViewById(R.id.button_victory_petitions);
        mButtonVictoryPetitions.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Bundle bundle = new Bundle();
                bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.VICTORY_PETITIONS_FRAGMENT);
                mIHomeListener.onVictoryButtonClicked(bundle);
            }
        });

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mProfile = new Profile(activity);

        //getDataFromServer();

        if (System.currentTimeMillis() - prefs.getLong(Const.Prefs.MY_TOTAL_POINTS_REFRESH_TIME, 0) > Const.REFRESH_TIME)
            getDataFromServer();

        return view;
    }

    private void getDataFromServer() {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        String url = Const.FINAL_URL + Const.URLs.MY_TOTAL_POINTS;
        url = url + "memberid=" + member_id;
        url = url + "&memberid_type=" + member_id_type;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        showProgressDialog("Getting My Points...");

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

                        if (mButtonMyPoints != null)
                            mButtonMyPoints.setText(String.valueOf((prefs.getInt(Const.Prefs.MY_TOTAL_POINTS, 0))) + "\n" + "My Points");
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    private void parseResponse(JSONArray response) {

        dismissProgressDialog();

        try {

            JSONObject jsonObject = response.getJSONObject(0);

            String points = jsonObject.getString("Total_Points");

            if (prefs != null) {
                prefs.edit().putInt(Const.Prefs.MY_TOTAL_POINTS, Integer.parseInt(points)).commit();
                prefs.edit().putLong(Const.Prefs.MY_TOTAL_POINTS_REFRESH_TIME, System.currentTimeMillis()).commit();
            }

            if (mButtonMyPoints != null)
                mButtonMyPoints.setText(points + "\n" + "My points");

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


    @Override
    public void onPause() {
        super.onPause();

        dismissProgressDialog();
    }

    @Override
    public void onResume() {
        super.onResume();

        if (mButtonMyPoints != null)
            mButtonMyPoints.setText(String.valueOf((prefs.getInt(Const.Prefs.MY_TOTAL_POINTS, 0))) + "\n" + "My Points");

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_notifications) {
            mIHomeListener.onNotificationsClicked();
        } else if (item.getItemId() == R.id.action_favourites) {
            mIHomeListener.onFavouritesClicked();
        } else if (item.getItemId() == R.id.action_profile) {
            mIHomeListener.onProfileClicked();
        }

        return super.onOptionsItemSelected(item);
    }
}


