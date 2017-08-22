package com.apex.icrf;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.apex.icrf.adapters.MenuExpandableListAdapter;
import com.apex.icrf.classes.IIDCardListerner;
import com.apex.icrf.classes.IMainAllPetitionsListener;
import com.apex.icrf.classes.IMainFavouritePetitionsListener;
import com.apex.icrf.classes.IMainNewPetitionsListener;
import com.apex.icrf.classes.IMainPostPetitionMapsListener;
import com.apex.icrf.classes.IMainSuccessPetitionsListener;
import com.apex.icrf.classes.IMainSupportedPetitionsByMeListener;
import com.apex.icrf.classes.IMainVerifiedPetitionsByMeListener;
import com.apex.icrf.classes.IMyPostsListener;
import com.apex.icrf.utils.Profile;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 24/02/16.
 */
public class MainTabbedActivity extends AppCompatActivity implements IMyPostsListener, IMainAllPetitionsListener, /*IHomeListener,*/
        /*IMainPopularPetitionsListener,*/ IMainNewPetitionsListener, IMainSuccessPetitionsListener, IMainVerifiedPetitionsByMeListener,
        IMainSupportedPetitionsByMeListener, IMainFavouritePetitionsListener, IMainPostPetitionMapsListener, IIDCardListerner {

    private AppBarLayout mAppBarLayout;
    private Toolbar mToolbar;
    private TextView mTextViewTitle;
    private DrawerLayout mDrawerLayout;
    private ExpandableListView mDrawerLeft;
    private ActionBarDrawerToggle mDrawerToggle;
    private MenuExpandableListAdapter mMenuExpandableListAdapter;
    private View mNavDrawerHeaderView, mNavDrawerPointsHeaderView;
    private View mNavDrawerFooterView;

    //TabLayout tabs;
    //ViewPager viewPager;
    FrameLayout frameLayout;
    FloatingActionButton fab;
    CircleImageView header_image;

    private SharedPreferences prefs;

    private int mNavListCurrentPosition = 0;
    private int mNavListDefaultPosition = 0;
    Profile mProfile;


    private MainHomeViewPagerFragment mMainHomeViewPagerFragment;
    private MainPostPetitionFragment mMainPostPetitionFragment;
    private MainDonateFragment mMainDonateFragment;
    private MainMyEarningsFragment mMainMyEarningsFragment;
    private MainMyTotalPointsFragment mMainMyTotalPointsFragment;
    private MainProfileFragment mMainProfileFragment;
    private IDCardFragment mIDCardFragment;
    private MainPushNotificationsFragment mMainPushNotificationsFragment;
    private MainPostPetitionMapsFragment mMainPostPetitionMapsFragment;
    private IDCardWebViewFragment mIdCardWebViewFragment;


    private MoreRateUsFragment mMoreRateUsFragment;
    private MoreAboutICRFFragment mMoreAboutICRFFragment;
    private MoreFeedbackFragment mMoreFeedbackFragment;
    private MoreHowItWorksFragment mMoreHowItWorksFragment;
    private MoreShareFragment mMoreShareFragment;

    private NotificationsNoticeBoardFragment mNotificationsNoticeBoardFragment;

    private boolean isRateUsVisible = false;
    private boolean isShareVisible = false;

    private boolean dontshowpopup = false;

    Typeface font_roboto_thin, font_robot_regular;

    Tracker t;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tabbed_main);

        t = ((ICRFApp) this.getApplication())
                .getTracker(ICRFApp.TrackerName.APP_TRACKER);

        mAppBarLayout = (AppBarLayout) findViewById(R.id.appBarLayout);
        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mTextViewTitle.setText("ICRF");

        font_roboto_thin = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Thin.ttf");

        font_robot_regular = Typeface.createFromAsset(getAssets(),
                "fonts/Roboto-Regular.ttf");


        //frameLayout = (FrameLayout) findViewById(R.id.activity_main_fragment_container);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        mDrawerLeft = (ExpandableListView) findViewById(R.id.expandableListView);

        mNavListCurrentPosition = prefs.getInt(Const.Prefs.CURRENT_LEFT_NAV_LIST_POSITION, mNavListDefaultPosition);
        mProfile = new Profile(this);


        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setFragmentForPosition(Const.MENULIST.POST_NEW_PETITION);
            }
        });


        setUpNavDrawerHeader();
        setUpNavigationDrawer();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            if (bundle.containsKey("from_service")) {
                setFragmentForPosition(Const.MENULIST.NOTIFICATIONS, Const.MENULIST.NOTIFICATIONS_NEWS);
            }
        } else {
            setFragmentForPosition(Const.MENULIST.HOME);
        }
    }

    private void setUpNavigationDrawer() {

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);

        mMenuExpandableListAdapter = new MenuExpandableListAdapter(this);
        mDrawerLeft.setAdapter(mMenuExpandableListAdapter);

        mDrawerLeft.setOnGroupClickListener(new DrawerGroupClickListener());
        mDrawerLeft.setOnChildClickListener(new DrawerChildClickListener());

        mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close) {

            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);

                if (drawerView == mDrawerLeft)
                    refreshHeaderImage();
            }

            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {

                if (drawerView == mDrawerLeft)
                    super.onDrawerSlide(drawerView, slideOffset);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
    }

    private void setUpNavDrawerHeader() {

        LayoutInflater inflater = getLayoutInflater();
        mNavDrawerHeaderView = inflater.inflate(R.layout.navdrawer_header_view,
                mDrawerLeft, false);
        View header_area = mNavDrawerHeaderView.findViewById(R.id.header_display);
        header_image = (CircleImageView) header_area.findViewById(R.id.header_profile_pic);
        //Picasso.with(this).load(mProfile.getProfileImage()).into(header_image);

        //Picasso.with(this).invalidate(mProfile.getProfileImage());
        Picasso.with(this).load(mProfile.getProfileImage()).into(header_image);

        //downloadImage();

        TextView header_title = (TextView) header_area
                .findViewById(R.id.textview_navdrawer_header_title);
        TextView header_website = (TextView) header_area
                .findViewById(R.id.textview_navdrawer_header_website);

        header_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawer(GravityCompat.START);

                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setFragmentForPosition(Const.MENULIST.ID_CARD);
                    }
                }, 200);
            }
        });


        View points_area = mNavDrawerHeaderView.findViewById(R.id.header_points);
        points_area.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mDrawerLayout.closeDrawer(GravityCompat.START);

                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        setFragmentForPosition(Const.MENULIST.MY_POINTS);
                    }
                }, 200);
            }
        });


        if (mProfile.isUserLoggedIn()) {

            String name = mProfile.getUserName().toLowerCase().substring(0, 1).toUpperCase()
                    + mProfile.getUserName().toLowerCase().substring(1).toLowerCase();
            header_title.setText(name);
            header_website.setText(mProfile.getUserMobile());
        } else {
            header_title.setText("Profile Name");
            header_title.setText("Mobile Number");
        }


        mDrawerLeft.addHeaderView(mNavDrawerHeaderView, null, false);


        mNavDrawerFooterView = inflater.inflate(R.layout.navdrawer_footer_view, mDrawerLeft, false);
        mDrawerLeft.addFooterView(mNavDrawerFooterView, null, false);


    }

    public void refreshHeaderImage() {

        if (header_image != null) {
            Picasso.with(this).load(mProfile.getProfileImage()).into(header_image);
        }
    }

    private class DrawerGroupClickListener implements ExpandableListView.OnGroupClickListener {

        @Override
        public boolean onGroupClick(ExpandableListView parent, final View v, final int groupPosition, long id) {

            mMenuExpandableListAdapter.setGroup_position(groupPosition);

            if (groupPosition == Const.MENULIST.HOME
                    || groupPosition == Const.MENULIST.MY_ACTIVITY
                    || groupPosition == Const.MENULIST.POST_NEW_PETITION
                    || groupPosition == Const.MENULIST.SUCCESS_PETITIONS
                    || groupPosition == Const.MENULIST.DONATE
                    || groupPosition == Const.MENULIST.MY_EARNINGS) {

                mMenuExpandableListAdapter.notifyDataSetChanged();

                mDrawerLayout.closeDrawer(GravityCompat.START);

                mDrawerLayout.postDelayed(new Runnable() {
                    @Override
                    public void run() {
//                        RequestManager.getRequestQueue()
//                                .cancelAll(Const.VOLLEY_TAG);

                        setFragmentForPosition(groupPosition);
                    }
                }, 300);

                return true;
            } else if (groupPosition == Const.MENULIST.NOTIFICATIONS) {
                mMenuExpandableListAdapter.setChild_position(-1);
                mMenuExpandableListAdapter.notifyDataSetChanged();
                return false;
            } else if (groupPosition == Const.MENULIST.MORE) {
                mMenuExpandableListAdapter.setChild_position(-1);
                mMenuExpandableListAdapter.notifyDataSetChanged();
                return false;
            }

            return false;
        }
    }

    private class DrawerChildClickListener implements ExpandableListView.OnChildClickListener {

        @Override
        public boolean onChildClick(ExpandableListView parent, View v, final int groupPosition, final int childPosition, long id) {

            mMenuExpandableListAdapter.setGroup_position(groupPosition);
            mMenuExpandableListAdapter.setChild_position(childPosition);
            mMenuExpandableListAdapter.notifyDataSetChanged();

            mDrawerLayout.closeDrawer(GravityCompat.START);

            mDrawerLayout.postDelayed(new Runnable() {
                @Override
                public void run() {
                    setFragmentForPosition(groupPosition, childPosition);
                }
            }, 200);

            return true;
        }
    }


    // Navigation drawer method
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    // Navigation drawer method
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(mDrawerLeft)) {
                mDrawerLayout.closeDrawer(mDrawerLeft);
            } else {
                mDrawerLayout.openDrawer(mDrawerLeft);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onShareButtonClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

//    @Override
//    public void onMyTotalPointsClicked(Bundle bundle) {
//
//        Intent intent = new Intent(this, DetailActivity.class);
//        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
//        startActivity(intent);
//    }

//    @Override
//    public void onProfileClicked() {
//
//        if (mToolbar != null && mTextViewTitle != null)
//            mTextViewTitle.setText("Profile");
//
//        mMainProfileFragment = new MainProfileFragment();
//
//        getSupportFragmentManager().beginTransaction()
//                .replace(R.id.activity_main_fragment_container, mMainProfileFragment)
//                .commit();
//    }


    @Override
    public void onSuccessItemClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    @Override
    public void onVerifiedPetitionsByMeItemClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    @Override
    public void onViewButtonClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    @Override
    public void onNewItemClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    @Override
    public void onFavouriteItemClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    @Override
    public void onSupportedPetitionsByMeItemClicked(Bundle bundle) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(Const.Bundle.E_PETITION_NUMBER, bundle.getString(Const.Bundle.E_PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_NUMBER, bundle.getString(Const.Bundle.PETITION_NUMBER));
        intent.putExtra(Const.Bundle.PETITION_TITLE, bundle.getString(Const.Bundle.PETITION_TITLE));
        intent.putExtra(Const.Bundle.FROM_FRAGMENT, bundle.getInt(Const.Bundle.FROM_FRAGMENT));
        startActivity(intent);
    }

    private void setFragmentForPosition(int group_position) {

        fab.setVisibility(View.VISIBLE);

//        if (group_position == Const.MENULIST.HOME
//                || group_position == Const.MENULIST.MY_ACTIVITY
//                || group_position == Const.MENULIST.SUCCESS_PETITIONS) {
//
//            if (group_position != Const.MENULIST.SUCCESS_PETITIONS)
//                tabs.setVisibility(View.VISIBLE);
//            else
//                tabs.setVisibility(View.GONE);
//
//            viewPager.setVisibility(View.VISIBLE);
//            fab.setVisibility(View.VISIBLE);
//            frameLayout.setVisibility(View.GONE);
//        } else {
//
//            tabs.setVisibility(View.GONE);
//            viewPager.setVisibility(View.GONE);
//            fab.setVisibility(View.GONE);
//            frameLayout.setVisibility(View.VISIBLE);
//        }


        if (group_position == Const.MENULIST.HOME) {

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("ICRF");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(0);
                }
            }


            prefs.edit().putInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN).apply();

            mMainHomeViewPagerFragment = new MainHomeViewPagerFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainHomeViewPagerFragment)
                    .commit();

//            mMainTabbedViewPagerAdapter = new MainTabbedViewPagerAdapter(getSupportFragmentManager(), this);
//            viewPager.setAdapter(mMainTabbedViewPagerAdapter);
//            tabs.setupWithViewPager(viewPager);

        } else if (group_position == Const.MENULIST.MY_ACTIVITY) {

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("My Activity");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(0);
                }
            }

            prefs.edit().putInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_MY_ACTIVITY_SCREEN).apply();

            mMainHomeViewPagerFragment = new MainHomeViewPagerFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainHomeViewPagerFragment)
                    .commit();

        } else if (group_position == Const.MENULIST.SUCCESS_PETITIONS) {

            t.send(new HitBuilders.EventBuilder().setCategory("Success Petition")
                    .setAction("View").build());


            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("Success Petitions");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

            prefs.edit().putInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_SUCCESS_PETITIONS_SCREEN).apply();

            mMainHomeViewPagerFragment = new MainHomeViewPagerFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainHomeViewPagerFragment)
                    .commit();

        }


//        else if (group_position == Const.MENULIST.NOTIFICATIONS) {
//
//            if (mToolbar != null && mTextViewTitle != null) {
//                mTextViewTitle.setText("Notifications");
//
//                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
//                }
//            }
//
//            if (mMainPushNotificationsFragment == null)
//                mMainPushNotificationsFragment = new MainPushNotificationsFragment();
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.activity_main_fragment_container, mMainPushNotificationsFragment)
//                    .commit();
//        }


        else if (group_position == Const.MENULIST.POST_NEW_PETITION) {


            //mMainPostPetitionFragment = new MainPostPetitionFragment();
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.activity_main_fragment_container, mMainPostPetitionFragment)
//                    .commit();

            if (mMainPostPetitionMapsFragment == null)
                mMainPostPetitionMapsFragment = new MainPostPetitionMapsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainPostPetitionMapsFragment)
                    .commit();

            fab.setVisibility(View.GONE);

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("Post New Petition");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

        } else if (group_position == Const.MENULIST.DONATE) {

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("Donate");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

            if (mMainDonateFragment == null)
                mMainDonateFragment = new MainDonateFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainDonateFragment)
                    .commit();

        } else if (group_position == Const.MENULIST.MY_EARNINGS) {

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("My Earnings");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

            if (mMainMyEarningsFragment == null)
                mMainMyEarningsFragment = new MainMyEarningsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainMyEarningsFragment)
                    .commit();

        }

        // ID Card
        else if (group_position == Const.MENULIST.ID_CARD) {

            fab.setVisibility(View.GONE);

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("ID Card");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

            if (mIDCardFragment == null)
                mIDCardFragment = new IDCardFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mIDCardFragment)
                    .commit();

        }

        // My Points
        else if (group_position == Const.MENULIST.MY_POINTS) {

            if (mToolbar != null && mTextViewTitle != null) {
                mTextViewTitle.setText("My Points");

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                }
            }

            if (mMainMyTotalPointsFragment == null)
                mMainMyTotalPointsFragment = new MainMyTotalPointsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_main_fragment_container, mMainMyTotalPointsFragment)
                    .commit();

        }

    }

    @Override
    public void onBackPressed() {

        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {


            Fragment f = getSupportFragmentManager().findFragmentById(R.id.activity_main_fragment_container);
            if (f instanceof MainHomeViewPagerFragment
                    && prefs.getInt(Const.Prefs.CURRENT_SCREEN, Const.Prefs.IS_HOME_SCREEN) == Const.Prefs.IS_HOME_SCREEN) {
                //displayExitAlert();

                if (dontshowpopup) {

                    if (isRateUsVisible) {
                        hideRateUsPopup();
                    } else if (isShareVisible) {
                        hideSharePopup();
                    } else {
                        displayExitAlert();
                    }

                } else {

                    if (prefs.getLong(Const.Prefs.RATE_US_TIMER, 0) == 0
                            || (prefs.getLong(Const.Prefs.RATE_US_TIMER, 0) - System.currentTimeMillis() > (24 * 60 * 60 * 1000))) {
                        prefs.edit().putBoolean(Const.Prefs.RATE_US_SHOWN, false).apply();
                    }

                    if (prefs.getLong(Const.Prefs.SHARE_US_TIMER, 0) == 0
                            || prefs.getLong(Const.Prefs.SHARE_US_TIMER, 0) - System.currentTimeMillis() > (24 * 60 * 60 * 1000)) {
                        prefs.edit().putBoolean(Const.Prefs.SHARE_US_SHOWN, false).apply();
                    }

                    if (!prefs.getBoolean(Const.Prefs.RATE_US_SHOWN, false)
                            && !prefs.getBoolean(Const.Prefs.RATE_US_DONT_SHOW, false)
                            && !isRateUsVisible) {

                        if (isShareVisible) {
                            hideSharePopup();
                        } else {
                            displayRateUsPopup();
                        }

                    } else if (!prefs.getBoolean(Const.Prefs.SHARE_US_SHOWN, false)
                            && !prefs.getBoolean(Const.Prefs.SHARE_US_DONT_SHOW, false)
                            && !isShareVisible) {

                        if (isRateUsVisible) {
                            hideRateUsPopup();
                        } else {
                            displaySharePopup();
                        }

                    } else {

                        if (isRateUsVisible) {
                            hideRateUsPopup();
                        } else if (isShareVisible) {
                            hideSharePopup();
                        } else {
                            displayExitAlert();
                        }
                    }

                    dontshowpopup = true;
                }


//                if (isRateUsShown) {
//                    if (isRateUsVisible)
//                        hidePopup();
//                    else
//                        displayExitAlert();
//                } else {
//
//                    if (isRateUsVisible) {
//                        hidePopup();
//                    } else {
//                        displayPopup();
//                        isRateUsShown = true;
//                    }
//                }

            } else if (f instanceof MainPostPetitionFragment) {

                if (mMainPostPetitionFragment != null) {
                    if (mMainPostPetitionFragment.canGoBack()) {
                        mMainPostPetitionFragment.goBack();
                    } else {
                        setFragmentForPosition(Const.MENULIST.HOME);
                    }
                }

            } else if (f instanceof IDCardWebViewFragment) {

                setFragmentForPosition(Const.MENULIST.ID_CARD);
            } else {
                setFragmentForPosition(Const.MENULIST.HOME);
            }

//            if (viewPager.getVisibility() == View.GONE) {
//                viewPager.setVisibility(View.VISIBLE);
//                setFragmentForPosition(Const.MENULIST.HOME);
//            } else {
//
//                TabLayout.Tab first_tab = tabs.getTabAt(0);
//
//                if (first_tab != null) {
//                    if (first_tab.getText().toString().equalsIgnoreCase("New")) {
//                        displayExitAlert();
//                    } else {
//                        setFragmentForPosition(Const.MENULIST.HOME);
//                    }
//                }
//            }
        }
    }

    public void displayExitAlert() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        AlertDialog dialog;

        alert.setTitle("Alert");
        alert.setMessage("Do you really want to exit the application?");
        alert.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        MainTabbedActivity.this.finish();
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


    public void displayRateUsPopup() {

        prefs.edit().putBoolean(Const.Prefs.RATE_US_SHOWN, true).apply();
        prefs.edit().putLong(Const.Prefs.RATE_US_TIMER, System.currentTimeMillis()).apply();

        isRateUsVisible = true;

        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        LinearLayout hiddenPanel = (LinearLayout) findViewById(R.id.ll_rate_us);

        TextView textView = (TextView) hiddenPanel.findViewById(R.id.textView_thank_you);
        textView.setTypeface(font_robot_regular);
        TextView textViewDesc = (TextView) hiddenPanel.findViewById(R.id.textView_desc);
        textViewDesc.setTypeface(font_robot_regular);

        Button button = (Button) hiddenPanel.findViewById(R.id.button_rate);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {
                    Uri uri = Uri.parse("market://details?id="
                            + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                    Uri uri = Uri
                            .parse("http://play.google.com/store/apps/details?id="
                                    + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                }
            }
        });


//        CheckBox checkBox = (CheckBox) hiddenPanel.findViewById(R.id.checkbox_rate);
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                if (isChecked) {
//                    prefs.edit().putBoolean(Const.Prefs.RATE_US_DONT_SHOW, true).apply();
//                    hideRateUsPopup();
//                }
//            }
//        });

        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);
    }

    public void displaySharePopup() {

        prefs.edit().putBoolean(Const.Prefs.SHARE_US_SHOWN, true).apply();
        prefs.edit().putLong(Const.Prefs.SHARE_US_TIMER, System.currentTimeMillis()).apply();

        isShareVisible = true;

        Animation bottomUp = AnimationUtils.loadAnimation(this,
                R.anim.bottom_up);
        LinearLayout hiddenPanel = (LinearLayout) findViewById(R.id.ll_share);

        TextView textView = (TextView) hiddenPanel.findViewById(R.id.textview_share);
        textView.setTypeface(font_robot_regular);

        Button button = (Button) hiddenPanel.findViewById(R.id.button_share);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Picasso.with(MainTabbedActivity.this).load(R.drawable.icrf_splash_icrf_logo_square_1).into(new Target() {
                    @Override
                    public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {

                        String file_path = Environment.getExternalStorageDirectory().getAbsolutePath() +
                                "/images";
                        File dir = new File(file_path);
                        if (!dir.exists())
                            dir.mkdir();
                        File file = new File(dir, "logo");
                        FileOutputStream fOut;
                        try {
                            fOut = new FileOutputStream(file);
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
                            fOut.flush();
                            fOut.close();


                            Uri uri = Uri.fromFile(file);
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_SEND);
                            intent.setType("image/*");

                            intent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
                            intent.putExtra(android.content.Intent.EXTRA_TEXT, "");
                            intent.putExtra(Intent.EXTRA_STREAM, uri);
                            intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
                            intent.putExtra(
                                    Intent.EXTRA_TEXT,
                                    "https://play.google.com/store/apps/details?id=com.apex.icrf");
                            startActivity(Intent.createChooser(intent, "Share ICRF"));

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onBitmapFailed(Drawable errorDrawable) {

                        Intent intent = new Intent(Intent.ACTION_SEND);
                        intent.setType("text/plain");
                        intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
                        intent.putExtra(
                                Intent.EXTRA_TEXT,
                                "https://play.google.com/store/apps/details?id=com.apex.icrf");
                        startActivity(Intent.createChooser(intent, "Share ICRF"));
                    }

                    @Override
                    public void onPrepareLoad(Drawable placeHolderDrawable) {

                    }
                });

//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
//                intent.putExtra(
//                        Intent.EXTRA_TEXT,
//                        "https://play.google.com/store/apps/details?id=com.apex.icrf");
//                startActivity(Intent.createChooser(intent, "Share ICRF"));

            }
        });


//        CheckBox checkBox = (CheckBox) hiddenPanel.findViewById(R.id.checkbox_share);
//        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//
//                if (isChecked) {
//                    prefs.edit().putBoolean(Const.Prefs.SHARE_US_DONT_SHOW, true).apply();
//                    hideRateUsPopup();
//                }
//            }
//        });

        hiddenPanel.startAnimation(bottomUp);
        hiddenPanel.setVisibility(View.VISIBLE);
    }

    public void hideRateUsPopup() {

        isRateUsVisible = false;

        Animation bottomDown = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        LinearLayout hiddenPanel = (LinearLayout) findViewById(R.id.ll_rate_us);
        hiddenPanel.startAnimation(bottomDown);
        hiddenPanel.setVisibility(View.GONE);
    }

    public void hideSharePopup() {

        isShareVisible = false;
        Animation bottomDown = AnimationUtils.loadAnimation(this,
                R.anim.bottom_down);
        LinearLayout hiddenPanel = (LinearLayout) findViewById(R.id.ll_share);
        hiddenPanel.startAnimation(bottomDown);
        hiddenPanel.setVisibility(View.GONE);

    }

    private void setFragmentForPosition(int groupPosition, int childPosition) {

        if (groupPosition == Const.MENULIST.MORE) {

            if (childPosition == Const.MENULIST.BANK_DETAILS) {

                if (mToolbar != null && mTextViewTitle != null) {
                    mTextViewTitle.setText("Bank Details");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                    }
                }

                if (mMainProfileFragment == null)
                    mMainProfileFragment = new MainProfileFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMainProfileFragment)
                        .commit();

            } else if (childPosition == Const.MENULIST.MORE_RATE_US) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("Rate Us");

                mMoreRateUsFragment = new MoreRateUsFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMoreRateUsFragment)
                        .commit();

            } else if (childPosition == Const.MENULIST.MORE_FEEDBACK) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("Feedback");

                mMoreFeedbackFragment = new MoreFeedbackFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMoreFeedbackFragment)
                        .commit();

            } else if (childPosition == Const.MENULIST.MORE_INVITE) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("Invite");

                mMoreShareFragment = new MoreShareFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMoreShareFragment)
                        .commit();


//                Intent intent = new Intent(Intent.ACTION_SEND);
//                intent.setType("text/plain");
//                intent.putExtra(Intent.EXTRA_SUBJECT, "Share ICRF with your friends");
//                intent.putExtra(
//                        Intent.EXTRA_TEXT,
//                        "https://play.google.com/store/apps/details?id=com.apex.icrf");
//                startActivity(Intent.createChooser(intent, "Share ICRF"));
            } else if (childPosition == Const.MENULIST.MORE_ABOUT_ICRF) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("About ICRF");

                mMoreAboutICRFFragment = new MoreAboutICRFFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMoreAboutICRFFragment)
                        .commit();
            } else if (childPosition == Const.MENULIST.MORE_HOW_IT_WORKS) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("How it works");

                mMoreHowItWorksFragment = new MoreHowItWorksFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMoreHowItWorksFragment)
                        .commit();

            } else if (childPosition == Const.MENULIST.MORE_CHECK_UPDATE) {

                try {
                    Uri uri = Uri.parse("market://details?id="
                            + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                } catch (ActivityNotFoundException e) {

                    Uri uri = Uri
                            .parse("http://play.google.com/store/apps/details?id="
                                    + getPackageName());
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY
                            | Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET
                            | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

                    startActivity(intent);
                }
            }
        } else if(groupPosition == Const.MENULIST.NOTIFICATIONS) {

            if(childPosition == Const.MENULIST.NOTIFICATIONS_NOTICE_BOARD) {

                if (mToolbar != null && mTextViewTitle != null) {
                    mTextViewTitle.setText("Notice Board");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                    }
                }

                if (mNotificationsNoticeBoardFragment == null)
                    mNotificationsNoticeBoardFragment = new NotificationsNoticeBoardFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mNotificationsNoticeBoardFragment)
                        .commit();

            } else if(childPosition == Const.MENULIST.NOTIFICATIONS_NEWS) {

                if (mToolbar != null && mTextViewTitle != null) {
                    mTextViewTitle.setText("Latest News");

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
                    }
                }

                if (mMainPushNotificationsFragment == null)
                    mMainPushNotificationsFragment = new MainPushNotificationsFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.activity_main_fragment_container, mMainPushNotificationsFragment)
                        .commit();
            }
        }


    }


    private void downloadImage() {

        DownloadImage image = new DownloadImage();
        image.execute(mProfile.getProfileImage());
    }


    // DownloadImage AsyncTask
    private class DownloadImage extends AsyncTask<String, Void, Bitmap> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Bitmap doInBackground(String... URL) {

            String imageURL = URL[0];

            Bitmap bitmap = null;
            try {
                // Download Image from URL
                InputStream input = new java.net.URL(imageURL).openStream();
                // Decode Bitmap
                bitmap = BitmapFactory.decodeStream(input);
                return bitmap;
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
            //return bitmap;
        }

        @Override
        protected void onPostExecute(Bitmap result) {

            if (result != null) {

                RoundedBitmapDrawable dr =
                        RoundedBitmapDrawableFactory.create(getResources(), result);
                dr.setCircular(true);

                header_image.setImageDrawable(dr);
            }

        }

    }


    @Override
    public void onLayoutChangedListener() {
        mMainHomeViewPagerFragment.onLayoutChangedListener();
    }


    // To invoke onActivityResult of Fragments

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onContinueClicked(Bundle bundle) {

        fab.setVisibility(View.GONE);

        if (mToolbar != null && mTextViewTitle != null) {
            mTextViewTitle.setText("Post New Petition");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
            }
        }

        mMainPostPetitionFragment = new MainPostPetitionFragment();
        mMainPostPetitionFragment.setArguments(bundle);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, mMainPostPetitionFragment)
                .commit();

    }

    @Override
    public void onEditButtonClicked() {

        if (mToolbar != null && mTextViewTitle != null) {
            mTextViewTitle.setText("ID Card");

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                mAppBarLayout.setElevation(getResources().getDimension(R.dimen.toolbar_elevation));
            }
        }

        if (mIdCardWebViewFragment == null)
            mIdCardWebViewFragment = new IDCardWebViewFragment();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.activity_main_fragment_container, mIdCardWebViewFragment)
                .commit();


    }


    @Override
    public void onSearchFocusChanged(boolean hasFocus) {

        if (hasFocus) {

            if (fab != null) {
                fab.setVisibility(View.GONE);
            }
        } else {

            if (fab != null) {
                fab.setVisibility(View.VISIBLE);
            }

//            View view = this.getCurrentFocus();
//            if (view != null) {
//                InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
//                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
//            }
        }
    }

//    @Override
//    public void onIDCardLoaded() {
//
//        if(fab != null) {
//            fab.setVisibility(View.GONE);
//        }
//    }


}
