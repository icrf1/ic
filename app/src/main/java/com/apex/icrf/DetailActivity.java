package com.apex.icrf;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.apex.icrf.classes.IDetailVerifiedPetitionsListener;
import com.apex.icrf.classes.IDetailVerifyMyPetitionsListener;
import com.apex.icrf.contacts.ContactsPickerActivity;
import com.apex.icrf.contacts.GroupsPickerActivity;
import com.github.clans.fab.FloatingActionButton;
import com.github.clans.fab.FloatingActionMenu;

/**
 * Created by WASPVamsi on 04/09/15.
 */
public class DetailActivity extends AppCompatActivity implements IDetailVerifyMyPetitionsListener, IDetailVerifiedPetitionsListener {

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    //private DetailPetitionsFragment mDetailPetitionsFragment;
    private DetailContactsFragment mDetailContactsFragment;

    private DetailMyTotalPointsFragment mDetailMyTotalPointsFragment;
    //private DetailVerifyMyPetitionsFragment mDetailVerifyMyPetitionsFragment;
    //private DetailVerifiedPetitionsFragment mDetailVerifiedPetitionsFragment;
    private DetailVerifyMyPetitionsFragment2 mDetailVerifyMyPetitionsFragment2;
    private DetailVerifiedPetitionsFragment2 mDetailVerifiedPetitionsFragment2;

    //FloatingActionButton fab;
    FloatingActionMenu fabMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");


        fabMenu = (FloatingActionMenu) findViewById(R.id.fab_menu);
        //fabMenu.setAnimated(false);
        fabMenu.setIconAnimated(false);

        Bundle bundle = getIntent().getExtras();
        final int fragment_id = bundle.getInt(Const.Bundle.FROM_FRAGMENT);

        fabMenu.setOnMenuToggleListener(new FloatingActionMenu.OnMenuToggleListener() {
            @Override
            public void onMenuToggle(boolean opened) {

                if(opened) {
                    final FloatingActionButton fab_facebook = (FloatingActionButton) fabMenu.findViewById(R.id.fab_facebook);
                    fab_facebook.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            fabMenu.close(false);

                            shareWithFacebook(fragment_id);
                        }
                    });

                    FloatingActionButton fab_others = (FloatingActionButton) fabMenu.findViewById(R.id.fab_others);
                    fab_others.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            fabMenu.close(false);

                            sharePetition(fragment_id);
                        }
                    });


                }
            }
        });

//        fabMenu.setOnFloatingActionsMenuUpdateListener(new FloatingActionsMenu.OnFloatingActionsMenuUpdateListener() {
//            @Override
//            public void onMenuExpanded() {
//
//                final FloatingActionButton fab_facebook = (FloatingActionButton) fabMenu.findViewById(R.id.fab_facebook);
//                fab_facebook.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        fabMenu.collapse();
//
//                        shareWithFacebook(fragment_id);
//                    }
//                });
//
//                FloatingActionButton fab_others = (FloatingActionButton) fabMenu.findViewById(R.id.fab_others);
//                fab_others.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//
//                        fabMenu.collapse();
//
//                        sharePetition(fragment_id);
//                    }
//                });
//
//            }
//
//            @Override
//            public void onMenuCollapsed() {
//
//            }
//        });
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                sharePetition(fragment_id);
//            }
//        });

        setFragmentForId(fragment_id);

    }


    private void setFragmentForId(int id) {

        if (id == Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT) {

            if (mToolbar != null && mTextViewTitle != null)
                mTextViewTitle.setText("Share My Petition");

            fabMenu.setVisibility(View.VISIBLE);

//            mDetailVerifyMyPetitionsFragment = new DetailVerifyMyPetitionsFragment();
//            mDetailVerifyMyPetitionsFragment.setArguments(getIntent().getExtras());
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.activity_detail_fragment_container, mDetailVerifyMyPetitionsFragment)
//                    .commit();

            mDetailVerifyMyPetitionsFragment2 = new DetailVerifyMyPetitionsFragment2();
            mDetailVerifyMyPetitionsFragment2.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_detail_fragment_container, mDetailVerifyMyPetitionsFragment2)
                    .commit();


            //Toast.makeText(this, "Position: " + id, Toast.LENGTH_LONG).show();
        } else if (id == Const.Bundle.DETAIL_VERIFIED_PETITION_FRAGMENT
                || id == Const.Bundle.POPULAR_PETITIONS_FRAGMENT
                || id == Const.Bundle.NEW_PETITIONS_FRAGMENT
                || id == Const.Bundle.VICTORY_PETITIONS_FRAGMENT
                || id == Const.Bundle.MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT
                || id == Const.Bundle.MAIN_SUCCESS_PETITION_FRAGMENT) {

            if (id == Const.Bundle.MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT
                    || id == Const.Bundle.MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT
                    || id == Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT) {

                if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("Petition Details");
            } else {
                if(id==Const.Bundle.MAIN_SUCCESS_PETITION_FRAGMENT){
                    if (mToolbar != null && mTextViewTitle != null)
                        mTextViewTitle.setText("Success Petition");
                }else if (mToolbar != null && mTextViewTitle != null)
                    mTextViewTitle.setText("Support Petition");
            }

            fabMenu.setVisibility(View.VISIBLE);

//            mDetailVerifiedPetitionsFragment = new DetailVerifiedPetitionsFragment();
//            mDetailVerifiedPetitionsFragment.setArguments(getIntent().getExtras());
//
//            getSupportFragmentManager().beginTransaction()
//                    .replace(R.id.activity_detail_fragment_container, mDetailVerifiedPetitionsFragment)
//                    .commit();

            mDetailVerifiedPetitionsFragment2 = new DetailVerifiedPetitionsFragment2();
            mDetailVerifiedPetitionsFragment2.setArguments(getIntent().getExtras());

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_detail_fragment_container, mDetailVerifiedPetitionsFragment2)
                    .commit();

            //Toast.makeText(this, "Position: " + id, Toast.LENGTH_LONG).show();
        } else if (id == Const.Bundle.MY_TOTAL_POINTS_FRAGMENT) {

            if (mToolbar != null && mTextViewTitle != null)
                mTextViewTitle.setText("Points Summary");

            fabMenu.setVisibility(View.GONE);

            mDetailMyTotalPointsFragment = new DetailMyTotalPointsFragment();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_detail_fragment_container, mDetailMyTotalPointsFragment)
                    .commit();

            //Toast.makeText(this, "Position: " + id, Toast.LENGTH_LONG).show();
        }
            // Toast.makeText(this, "Unknown Fragment", Toast.LENGTH_LONG).show();

    }


    private void sharePetition(int id) {

        if (id == Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT) {

            if(mDetailVerifyMyPetitionsFragment2 != null) {
                mDetailVerifyMyPetitionsFragment2.onFABClick();
            }

        } else if (id == Const.Bundle.DETAIL_VERIFIED_PETITION_FRAGMENT
                || id == Const.Bundle.POPULAR_PETITIONS_FRAGMENT
                || id == Const.Bundle.NEW_PETITIONS_FRAGMENT
                || id == Const.Bundle.VICTORY_PETITIONS_FRAGMENT
                || id == Const.Bundle.MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT
                || id == Const.Bundle.MAIN_SUCCESS_PETITION_FRAGMENT) {

            if(mDetailVerifiedPetitionsFragment2 != null) {
                mDetailVerifiedPetitionsFragment2.onFABClick();
            }

        }
    }

    private void shareWithFacebook(int id) {

        if (id == Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT) {

            if(mDetailVerifyMyPetitionsFragment2 != null) {
                mDetailVerifyMyPetitionsFragment2.onFacebookShare();
            }

        } else if (id == Const.Bundle.DETAIL_VERIFIED_PETITION_FRAGMENT
                || id == Const.Bundle.POPULAR_PETITIONS_FRAGMENT
                || id == Const.Bundle.NEW_PETITIONS_FRAGMENT
                || id == Const.Bundle.VICTORY_PETITIONS_FRAGMENT
                || id == Const.Bundle.MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT
                || id == Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT
                || id == Const.Bundle.MAIN_SUCCESS_PETITION_FRAGMENT) {

            if(mDetailVerifiedPetitionsFragment2 != null) {
                mDetailVerifiedPetitionsFragment2.onFacebookShare();
            }

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }


        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onContactsButtonClicked(String sms_text) {
        startActivity(new Intent(this, ContactsPickerActivity.class).putExtra("sms_text", sms_text));
    }

    @Override
    public void onGroupsButtonClicked(String sms_text) {
        startActivity(new Intent(this, GroupsPickerActivity.class).putExtra("sms_text", sms_text));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onScrolled(boolean hide) {

        if(hide) {
            fabMenu.animate().setDuration(500).alpha(0.0f);
        } else {
            fabMenu.animate().setDuration(500).alpha(1.0f);
        }
    }

    @Override
    public void onVerifiedPetitionsScrolled(boolean hide) {
        if(hide) {
            fabMenu.animate().setDuration(500).alpha(0.0f);
        } else {
            fabMenu.animate().setDuration(500).alpha(1.0f);
        }
    }



}
