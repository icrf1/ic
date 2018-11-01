package com.apex.icrf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.apex.icrf.adapters.NotificationsRecyclerViewAdapter;
import com.apex.icrf.classes.ItemPushNotificationsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.PushNotificationsTableDbAdapter;
import com.apex.icrf.utils.LinearLayoutManager;

import java.util.List;

/**
 * Created by WASPVamsi on 12/02/16.
 */
public class MainPushNotificationsFragment extends android.support.v4.app.Fragment {

    Activity activity;
    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;
    NotificationsRecyclerViewAdapter mNotificationsRecyclerViewAdapter;
    PushNotificationsTableDbAdapter mPushNotificationsTableDbAdapter;

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
        View view = inflater.inflate(R.layout.main_fragment_notifications, container, false);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview);
        mLayoutManager = new LinearLayoutManager(activity, android.support.v7.widget.LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mPushNotificationsTableDbAdapter = DatabaseHelper.get(activity).getPushNotificationsTableDbAdapter();

        //insertDummyDataIntoDatabase();

        getDataFromDatabase();

        return view;
    }


    private void getDataFromDatabase() {

        List<ItemPushNotificationsTable> items = mPushNotificationsTableDbAdapter.getPushMessages();

        mNotificationsRecyclerViewAdapter = new NotificationsRecyclerViewAdapter(activity, items);
        mRecyclerView.setAdapter(mNotificationsRecyclerViewAdapter);
        mNotificationsRecyclerViewAdapter.notifyDataSetChanged();

    }


    private void insertDummyDataIntoDatabase() {

        mPushNotificationsTableDbAdapter.beginTransaction();
        ItemPushNotificationsTable item;


        try {

            for (int i = 0; i < 10; i++) {
                item = new ItemPushNotificationsTable();
                item.setPush_message("This is Push Message " + (i + 1));
                item.setPush_image("");

                mPushNotificationsTableDbAdapter.insertRow(item);

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Push inserted into Database successfully");

            }

            mPushNotificationsTableDbAdapter.setTransactionSuccessful();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mPushNotificationsTableDbAdapter.endTransaction();
        }
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_push_notifications, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_delete) {

            AlertDialog.Builder alert = new AlertDialog.Builder(activity,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
            AlertDialog dialog;

            alert.setTitle("Alert");
            alert.setMessage("This will clear all ICRF Notifications. Do you want to proceed?");
            alert.setPositiveButton("YES",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();

                            mPushNotificationsTableDbAdapter.clearTable();
                            getDataFromDatabase();
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

        return super.onOptionsItemSelected(item);
    }




}
