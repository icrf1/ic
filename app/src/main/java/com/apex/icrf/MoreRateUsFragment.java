package com.apex.icrf;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * Created by WASPVamsi on 03/01/16.
 */
public class MoreRateUsFragment extends Fragment {

    Activity activity;
    Button btnSubmit;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment_rate_us, container, false);

        btnSubmit = (Button) view.findViewById(R.id.button);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    Uri uri = Uri.parse("market://details?id="
                            + activity.getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                    Uri uri = Uri
                            .parse("http://play.google.com/store/apps/details?id="
                                    + activity.getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                }

            }
        });

        return view;
    }
}
