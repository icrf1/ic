package com.apex.icrf;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apex.icrf.adapters.NoticeBoardRecyclerViewAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.LinearLayoutManager;
import com.apex.icrf.utils.Utils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 07/06/16.
 */
public class NotificationsNoticeBoardFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {

    Activity activity;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    NoticeBoardRecyclerViewAdapter mNoticeBoardRecyclerViewAdapter;
    List<String> items = new ArrayList<String>();
    private SwipeRefreshLayout mSwipeRefreshLayout;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.notifications_fragment_notice_board, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swipeRefreshLayout);

        WindowManager wm = ((WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        Point size = Utils.getDisplaySize(display);
        int height = size.y;
        mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 12);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_notice_board);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(activity, android.support.v7.widget.LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        items.clear();
        mNoticeBoardRecyclerViewAdapter = new NoticeBoardRecyclerViewAdapter(activity, items);
        mRecyclerView.setAdapter(mNoticeBoardRecyclerViewAdapter);

        getDataFromServer();

        return view;
    }


    @Override
    public void onRefresh() {

        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);

        items.clear();
        mNoticeBoardRecyclerViewAdapter.setItems(items);
        mNoticeBoardRecyclerViewAdapter.notifyDataSetChanged();

        getDataFromServer();
    }

    public void getDataFromServer() {

        String url = Const.FINAL_URL + Const.URLs.NOTICE_BOARD_NEWS;
        url = url + "state=";
        url = url + "&district=";


        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        try{
                            for (int i = 0; i < response.length(); i++) {

                                JSONObject jsonObject = response.getJSONObject(i);
                                items.add(jsonObject.getString("msg"));
                            }

                            mNoticeBoardRecyclerViewAdapter.setItems(items);
                            mNoticeBoardRecyclerViewAdapter.notifyDataSetChanged();
                        }catch (Exception e) {
                            e.printStackTrace();

                            items.clear();
                            mNoticeBoardRecyclerViewAdapter.setItems(items);
                            mNoticeBoardRecyclerViewAdapter.notifyDataSetChanged();
                        }


                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (mSwipeRefreshLayout.isRefreshing())
                            mSwipeRefreshLayout.setRefreshing(false);

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    activity,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                        items.clear();
                        mNoticeBoardRecyclerViewAdapter.setItems(items);
                        mNoticeBoardRecyclerViewAdapter.notifyDataSetChanged();
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);

    }

}
