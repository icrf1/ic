package com.apex.icrf.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.apex.icrf.Const;
import com.apex.icrf.MainAllPetitionsViewPagerFragment;
import com.apex.icrf.MainFavouritePetitionsViewPagerFragment;
import com.apex.icrf.MainMyPostsViewPagerFragment;
import com.apex.icrf.MainNewPetitionsViewPagerFragment;
import com.apex.icrf.MainSuccessPetitionsViewPagerFragment;
import com.apex.icrf.MainSupportedPetitionsByMeViewPagerFragment;
import com.apex.icrf.MainVerifiedPetitionsByMeViewPagerFragment;

/**
 * Created by WASPVamsi on 24/02/16.
 */
public class MainTabbedViewPagerAdapter extends FragmentStatePagerAdapter {

    public String[] mTabTitles = {"New", "All", "My Posts"};
    public String[] mTabTitles2 = {"I Verified", "I Supported", "Favorites"};
    public String[] mTabTitles3 = {"Success Petitions"};

    Context context;
    SharedPreferences prefs;

    MainNewPetitionsViewPagerFragment mMainNewPetitionsViewPagerFragment/* = new MainNewPetitionsViewPagerFragment()*/;
    MainAllPetitionsViewPagerFragment mMainAllPetitionsViewPagerFragment/* = new MainAllPetitionsViewPagerFragment()*/;
    MainMyPostsViewPagerFragment mMainMyPostsFragment/* = new MainMyPostsViewPagerFragment()*/;
    MainVerifiedPetitionsByMeViewPagerFragment mMainVerifiedPetitionsByMeViewPagerFragment/* = new MainVerifiedPetitionsByMeViewPagerFragment()*/;
    MainSupportedPetitionsByMeViewPagerFragment mMainSupportedPetitionsByMeViewPagerFragment/* = new MainSupportedPetitionsByMeViewPagerFragment()*/;
    MainFavouritePetitionsViewPagerFragment mMainFavouritePetitionsViewPagerFragment/* = new MainFavouritePetitionsViewPagerFragment()*/;
    MainSuccessPetitionsViewPagerFragment mMainSuccessPetitionsViewPagerFragment/* = new MainSuccessPetitionsViewPagerFragment()*/;

    public MainTabbedViewPagerAdapter(FragmentManager fragmentManager, Context context) {
        super(fragmentManager);

        this.context = context;
        prefs = PreferenceManager.getDefaultSharedPreferences(context);
    }

    @Override
    public int getCount() {

        if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_HOME_SCREEN)
            return mTabTitles.length;
        else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_MY_ACTIVITY_SCREEN)
            return mTabTitles2.length;
        else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_SUCCESS_PETITIONS_SCREEN)
            return mTabTitles3.length;
        else
            return mTabTitles.length;
    }

    @Override
    public Fragment getItem(int position) {


        if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_HOME_SCREEN) {

            if (position == 0) {
                mMainNewPetitionsViewPagerFragment = new MainNewPetitionsViewPagerFragment();
                return mMainNewPetitionsViewPagerFragment;
            } else if (position == 1) {
                mMainAllPetitionsViewPagerFragment = new MainAllPetitionsViewPagerFragment();
                return mMainAllPetitionsViewPagerFragment;
            } else if (position == 2) {
                mMainMyPostsFragment = new MainMyPostsViewPagerFragment();
                return mMainMyPostsFragment;
            }
        } else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_MY_ACTIVITY_SCREEN) {

            if (position == 0) {
                mMainVerifiedPetitionsByMeViewPagerFragment = new MainVerifiedPetitionsByMeViewPagerFragment();
                return mMainVerifiedPetitionsByMeViewPagerFragment;
            } else if (position == 1) {
                mMainSupportedPetitionsByMeViewPagerFragment = new MainSupportedPetitionsByMeViewPagerFragment();
                return mMainSupportedPetitionsByMeViewPagerFragment;
            } else if (position == 2) {
                mMainFavouritePetitionsViewPagerFragment = new MainFavouritePetitionsViewPagerFragment();
                return mMainFavouritePetitionsViewPagerFragment;
            }
        } else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_SUCCESS_PETITIONS_SCREEN) {
            mMainSuccessPetitionsViewPagerFragment = new MainSuccessPetitionsViewPagerFragment();
            return mMainSuccessPetitionsViewPagerFragment;
        }


        return null;
    }

    @Override
    public CharSequence getPageTitle(int position) {

        if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_HOME_SCREEN)
            return mTabTitles[position];
        else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_MY_ACTIVITY_SCREEN)
            return mTabTitles2[position];
        else if (prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_SUCCESS_PETITIONS_SCREEN)
            return mTabTitles3[position];
        else
            return mTabTitles[position];
    }

    public void setLayoutViewForFragments() {

        String item_type = prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW);
        int column_count = 1;

        if (item_type.equalsIgnoreCase(Const.VIEWPAGER.LIST_VIEW) || item_type.equalsIgnoreCase(Const.VIEWPAGER.MINI_VIEW))
            column_count = 1;
        else if (item_type.equalsIgnoreCase(Const.VIEWPAGER.GRID_VIEW))
            column_count = 2;
        else
            column_count = 1;

        if (mMainNewPetitionsViewPagerFragment != null) {
            mMainNewPetitionsViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainAllPetitionsViewPagerFragment != null) {
            mMainAllPetitionsViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainMyPostsFragment != null) {
            mMainMyPostsFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainVerifiedPetitionsByMeViewPagerFragment != null) {
            mMainVerifiedPetitionsByMeViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainSupportedPetitionsByMeViewPagerFragment != null) {
            mMainSupportedPetitionsByMeViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainFavouritePetitionsViewPagerFragment != null) {
            mMainFavouritePetitionsViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

        if (mMainSuccessPetitionsViewPagerFragment != null) {
            mMainSuccessPetitionsViewPagerFragment.setLayoutManagerForFragment(column_count);
        }

    }

    @Override
    public int getItemPosition(Object object) {
        return POSITION_NONE;
    }
}
