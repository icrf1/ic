package com.apex.icrf;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.apex.icrf.adapters.MainTabbedViewPagerAdapter;

/**
 * Created by WASPVamsi on 23/03/16.
 */
public class MainHomeViewPagerFragment extends Fragment {

    Activity activity;
    TabLayout tabs;
    ViewPager viewPager;

    MainTabbedViewPagerAdapter mMainTabbedViewPagerAdapter;
    SharedPreferences prefs;

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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_viewpager_home, container, false);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);

        tabs = (TabLayout) view.findViewById(R.id.tabs);
        viewPager = (ViewPager) view.findViewById(R.id.pager);

        mMainTabbedViewPagerAdapter = new MainTabbedViewPagerAdapter(getChildFragmentManager(), activity);
        viewPager.setAdapter(mMainTabbedViewPagerAdapter);
        viewPager.setOffscreenPageLimit(2);
        tabs.setupWithViewPager(viewPager);

        if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_SUCCESS_PETITIONS_SCREEN)
            tabs.setVisibility(View.GONE);
        else
            tabs.setVisibility(View.VISIBLE);

        return view;
    }


    public void onLayoutChangedListener() {
        mMainTabbedViewPagerAdapter.setLayoutViewForFragments();
    }

}
