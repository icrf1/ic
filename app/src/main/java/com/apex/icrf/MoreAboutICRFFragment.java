package com.apex.icrf;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by WASPVamsi on 03/01/16.
 */
public class MoreAboutICRFFragment extends Fragment {

    public static final String ABOUT_ICRF_2 = "For more Details:<br />Website : <u>www.icrf.org.in</u><br />Contact us : <u>info@icrf.org.in</u>";

    TextView textView2;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.more_fragment_about_icrf, container, false);

        textView2 = (TextView) view.findViewById(R.id.textView_2);
        textView2.setText(Html.fromHtml(ABOUT_ICRF_2));

        return view;
    }
}
