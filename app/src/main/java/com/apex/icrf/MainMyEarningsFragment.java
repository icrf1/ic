package com.apex.icrf;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
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
import com.apex.icrf.classes.ItemMyEarnings;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.Profile;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * Created by WASPVamsi on 01/01/16.
 */
public class MainMyEarningsFragment extends Fragment {

    Activity activity;
    Profile mProfile;
    ProgressDialog progressDialog;
    MyEarningsAdapter mMyEarningsAdapter;

    TextView mTextViewGrandTotal;
    //Button mButtonGrandTotal;
    ListView listview;

    private ArrayList<ItemMyEarnings> mAlMyEarnings = new ArrayList<ItemMyEarnings>();

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_my_earnings, container, false);
        mAlMyEarnings.clear();

        mProfile = new Profile(activity);

        mTextViewGrandTotal = (TextView) view.findViewById(R.id.textView_amount);
        //mButtonGrandTotal = (Button) view.findViewById(R.id.button_total_points);

        listview = (ListView) view.findViewById(R.id.listView_my_earnings);
        mMyEarningsAdapter = new MyEarningsAdapter(activity);
        listview.setAdapter(mMyEarningsAdapter);

        progressDialog = new ProgressDialog(activity);
        progressDialog.setIndeterminate(true);
        showProgressDialog("Loading...");

        getDataFromServer();

        return view;
    }


    public class MyEarningsAdapter extends BaseAdapter {

        Context context;

        public MyEarningsAdapter(Context context) {
            this.context = context;
        }

        @Override
        public int getCount() {
            return mAlMyEarnings.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            final ItemMyEarnings item = mAlMyEarnings.get(position);
            final ViewHolder holder;

            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.item_fragment_my_earnings, null);

                holder = new ViewHolder();
                holder.mTextViewMonth = (TextView) convertView.findViewById(R.id.textview_column_month);
                holder.mTextViewAmount = (TextView) convertView.findViewById(R.id.textview_column_amount);
                holder.mTextViewIssued = (TextView) convertView.findViewById(R.id.textview_column_issued);
                holder.mTextViewIssuedDate = (TextView) convertView.findViewById(R.id.textView_column_issued_date);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }


            holder.mTextViewMonth.setText(item.getMonth());
            holder.mTextViewAmount.setText(item.getAmount());
            holder.mTextViewIssued.setText(item.getIssued());
            holder.mTextViewIssuedDate.setText(item.getIssued_date());

            return convertView;
        }
    }

    public static class ViewHolder {

        TextView mTextViewMonth, mTextViewAmount, mTextViewIssued, mTextViewIssuedDate;
    }


    private void getDataFromServer() {

        String member_id = mProfile.getMemberId();
        String member_id_type = mProfile.getMemberIdType();

        String url = Const.FINAL_URL + Const.URLs.MY_EARNINGS_REPORT;
        url = url + "memberid=" + member_id;
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

        ItemMyEarnings item;

        try {

            JSONObject jsonObject = response.getJSONObject(0);

            String grand_total = jsonObject.getString("Grand Total Amount");
            mTextViewGrandTotal.setText(grand_total);
            //mButtonGrandTotal.setText("TOTAL EARNINGS\n\n" + grand_total);

            JSONArray jsonArray = jsonObject.getJSONArray("Earnings_Reports");

            for (int i = 0; i < jsonArray.length(); i++) {

                JSONObject object = jsonArray.getJSONObject(i);

                item = new ItemMyEarnings();

                item.setMonth(object.getString("Month"));
                item.setAmount(object.getString("Amount"));
                item.setIssued(object.getString("Issued"));
                item.setIssued_date(object.getString("Issued Date"));

                mAlMyEarnings.add(item);
            }

            mMyEarningsAdapter.notifyDataSetChanged();
            dismissProgressDialog();
        } catch (Exception e) {
            dismissProgressDialog();
            e.printStackTrace();
            Toast.makeText(activity, "Cannot load your earnings summary at this time. Please try again later.", Toast.LENGTH_LONG).show();
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
}
