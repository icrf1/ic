package com.apex.icrf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apex.icrf.classes.ItemMyTotalPoints;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;
import java.util.TreeSet;

/**
 * Created by WASPVamsi on 14/09/15.
 */
public class MainMyTotalPointsFragment extends Fragment {


    private Activity activity;
    ProgressDialog progressDialog;

    private ListView listview;

    SharedPreferences prefs;
    Profile mProfile;

    private ArrayList<ItemMyTotalPoints> mAlMyTotalPoints = new ArrayList<ItemMyTotalPoints>();

    MyTotalPointsAdapter mMyTotalPointsAdapter;

    TextView textViewPoints;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_my_total_points, container, false);
        mAlMyTotalPoints.clear();

        textViewPoints = (TextView) view.findViewById(R.id.textView_amount);

        listview = (ListView) view.findViewById(R.id.listView_my_total_points);
        mMyTotalPointsAdapter = new MyTotalPointsAdapter(activity);
        listview.setAdapter(mMyTotalPointsAdapter);

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        showProgressDialog("Loading...");

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mProfile = new Profile(activity);

        if (System.currentTimeMillis() - prefs.getLong(Const.Prefs.MY_TOTAL_POINTS_REFRESH_TIME, 0) > Const.REFRESH_TIME)
            getMyPointsFromServer();

        getDataFromServer();

        return view;

    }


    public class MyTotalPointsAdapter extends BaseAdapter {

        Context context;

        private TextView mTextViewPoints, mTextViewDescription, mTextViewDate;
        private TextView mTextViewTitle;

        String[] titles_text = {"Confirmed Reports", "Waiting Reports"};
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_SEPARATOR = 1;
        private TreeSet<Integer> sectionHeader = new TreeSet<Integer>();

        public MyTotalPointsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mAlMyTotalPoints.size();
        }


        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }


        @Override
        public int getItemViewType(int position) {

            boolean isSection = mAlMyTotalPoints.get(position).isSection;

            if (isSection)
                return TYPE_SEPARATOR;
            else
                return TYPE_ITEM;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ItemMyTotalPoints item = mAlMyTotalPoints.get(position);
            int switch_type = getItemViewType(position);

            if (convertView == null) {

                if (switch_type == TYPE_SEPARATOR) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_fragment_my_total_points_section_header, null);
                } else if (switch_type == TYPE_ITEM) {
                    convertView = LayoutInflater.from(context).inflate(R.layout.item_fragment_my_total_points, null);
                }
            }

            if (switch_type == TYPE_SEPARATOR) {

                mTextViewTitle = (TextView) convertView.findViewById(R.id.textView_section_header);
                mTextViewTitle.setText(item.getDescription().toUpperCase(Locale.ENGLISH));

            } else if (switch_type == TYPE_ITEM) {

                mTextViewPoints = (TextView) convertView.findViewById(R.id.textView_points_amount);
                mTextViewPoints.setText(item.getPoints());

                mTextViewDescription = (TextView) convertView.findViewById(R.id.textView_points_description);
                mTextViewDescription.setText(item.getDescription());

                mTextViewDate = (TextView) convertView.findViewById(R.id.textView_points_date);
                mTextViewDate.setText(item.getDate());
            }

            return convertView;
        }
    }


    private void getDataFromServer() {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        String url = Const.FINAL_URL + Const.URLs.TOTAL_POINTS_REPORTS;
        url = url + "memberid=" + member_id;
        url = url + "&points_type=" + "cr";
        url = url + "&memberid_type=" + member_id_type;

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

                        //dismissProgressDialog();
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
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }

    private void parseResponse(JSONArray response) {

        ItemMyTotalPoints item = new ItemMyTotalPoints();
        item.setPoints("");
        item.setDescription("Confirmed Reports");
        item.setDate("");
        item.setIsSection(true);
        mAlMyTotalPoints.add(item);

        if (response.length() == 0) {
            ItemMyTotalPoints item2 = new ItemMyTotalPoints();
            item2.setPoints("");
            item2.setDescription("No Reports in this section");
            item2.setDate("");
            item2.setIsSection(false);
            mAlMyTotalPoints.add(item2);

        } else {

            ItemMyTotalPoints item3;

            try {

                for (int i = 0; i < response.length(); i++) {

                    JSONObject object = response.getJSONObject(i);

                    String date = object.getString("date");

                    item3 = new ItemMyTotalPoints();
                    item3.setDate(date);
                    item3.setDescription(object.getString("Description"));
                    item3.setPoints("Points: " + object.getString("points"));
                    item3.setIsSection(false);

                    mAlMyTotalPoints.add(item3);
                }

            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(activity, "Cannot load your points summary at this time. Please try again later.", Toast.LENGTH_LONG).show();
            }
        }

        getDataFromServerWR();

    }

    private void getDataFromServerWR() {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        String url = Const.FINAL_URL + Const.URLs.TOTAL_POINTS_REPORTS;
        url = url + "memberid=" + member_id;
        url = url + "&points_type=" + "wr";
        url = url + "&memberid_type=" + member_id_type;

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

                        dismissProgressDialog();
                        parseResponseWR(response);
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

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }

    private void parseResponseWR(JSONArray response) {

        ItemMyTotalPoints item = new ItemMyTotalPoints();
        item.setPoints("");
        item.setDescription("Waiting Reports");
        item.setDate("");
        item.setIsSection(true);
        mAlMyTotalPoints.add(item);

        if (response.length() == 0) {
            ItemMyTotalPoints item2 = new ItemMyTotalPoints();
            item2.setPoints("");
            item2.setDescription("No Reports in this section");
            item2.setDate("");
            item2.setIsSection(false);
            mAlMyTotalPoints.add(item2);

        } else {

            ItemMyTotalPoints item3;

            try {

                for (int i = 0; i < response.length(); i++) {

                    JSONObject object = response.getJSONObject(i);

                    String date = object.getString("date");

                    item3 = new ItemMyTotalPoints();
                    item3.setDate(date);
                    item3.setDescription(object.getString("Description"));
                    item3.setPoints("Points: " + object.getString("points"));
                    item3.setIsSection(false);

                    mAlMyTotalPoints.add(item3);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        mMyTotalPointsAdapter.notifyDataSetChanged();
    }


    public void getMyPointsFromServer() {

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

                        parseResponsePoints(response);
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

                        if (textViewPoints != null)
                            textViewPoints.setText(String.valueOf((prefs.getInt(Const.Prefs.MY_TOTAL_POINTS, 0))));
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);

    }

    private void parseResponsePoints(JSONArray response) {

        //dismissProgressDialog();

        try {

            JSONObject jsonObject = response.getJSONObject(0);

            String points = jsonObject.getString("Total_Points");

            if (prefs != null) {
                prefs.edit().putInt(Const.Prefs.MY_TOTAL_POINTS, Integer.parseInt(points)).apply();
                prefs.edit().putLong(Const.Prefs.MY_TOTAL_POINTS_REFRESH_TIME, System.currentTimeMillis()).apply();
            }

            if (textViewPoints != null)
                textViewPoints.setText(points);

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
    public void onResume() {
        super.onResume();

        if (textViewPoints != null)
            textViewPoints.setText(String.valueOf((prefs.getInt(Const.Prefs.MY_TOTAL_POINTS, 0))));

    }
}
