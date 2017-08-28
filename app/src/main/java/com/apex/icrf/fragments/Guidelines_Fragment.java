package com.apex.icrf.fragments;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;

import com.apex.icrf.Const;
import com.apex.icrf.MainPostPetitionFragment;
import com.apex.icrf.R;

/**
 * A simple {@link Fragment} subclass.
 */
public class Guidelines_Fragment extends Fragment implements View.OnClickListener {
    View rootView;
    WebView webView;
    Button continue_next;
    private double mLatitude = 0.0, mLongitude = 0.0;
    private double mFinalLatitude = 0.0, mFinalLongitude = 0.0;
    private MainPostPetitionFragment mainPostPetitionFragment;
    protected Bundle bundle;

    //String htmlText = "<html><body style=\"text-align:justify\" text=\"black\" > %s </body></Html>";
    String htmlText = "<html><body text=\"black\" > %s </body></Html>";
    String guideLinesText = "&#9755; <strong>Guidelines to Post E-Petitions</strong><br><br>" +
            "<ht>"+
            "<ul> <li>A petition should not be used to promote a business or service, or for solicitation.</li><br>" +
            "<li> While filling for a petition it requires correct spelling, punctuation, grammar and capitalization to be used and should be a complete sentence.</li><br>" +
            "<li> A petition should contain enough information in simple language for anyone to understand.</li><br>" +
            "<li> A petition should not target an individual,Cast,Creed,Gender (male/female/others),political parties or elected people representatives.</li><br>" +
            "<li> All content on should be written in simple.</li><br>" +
            "<li> Always verify the validity of the mobile number and e-mail Id of the official before you post a petition.</li><br>" +
            "<li> Find out that the official on whom you submit the petition is the concerned person for completion of the work.</li><br>" +
            "<li> Always address to the exact concerned official and if the work is still pending, then,you can escalate the petition to higher officials.</li><br>" +
            "<li> Try to attach images, documents, videos, web links, etc in support of your petition.</li><br>" +
            "<li> In case of any doubts in posting petitions, clarify with your district E-Petition co-ordinator or send a mail to (info@icrf.org.in).</li><br>" +
            "<li> Invalid petitions or petitions posted against the above guidelines will be deleted and even negative points will be awarded.</ul></ol><br>";
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_guidelines, container, false);
        Initialize();
        bundle = getArguments();
        if (bundle != null) {
            mLatitude = Double.parseDouble(bundle.getString("latitude"));
            mLongitude = Double.parseDouble(bundle.getString("longitude"));
        }
        getActivity().setTitle("Petition Guidelines");
        continue_next.setOnClickListener(this);
        webView.loadData(String.format(htmlText,guideLinesText),"text/html","UTF-8");
        return rootView;
    }

    void Initialize()
    {
        webView = (WebView) rootView.findViewById(R.id.webView);
        continue_next  = (Button) rootView.findViewById(R.id.guidelines_continue);
    }

    @Override
    public void onClick(View v) {
        mainPostPetitionFragment=new MainPostPetitionFragment();
        mainPostPetitionFragment.setArguments(bundle);
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.activity_main_fragment_container,mainPostPetitionFragment).commit();
        Log.d(Const.DEBUG,"in Guidelines_Fragment continue clicked");
    }
}
