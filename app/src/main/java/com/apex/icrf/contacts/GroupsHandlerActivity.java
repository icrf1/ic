package com.apex.icrf.contacts;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.classes.IGroupsGroupMembersListener;

/**
 * Created by WASPVamsi on 08/10/15.
 */
public class GroupsHandlerActivity extends AppCompatActivity implements IGroupsGroupMembersListener {

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    private GroupsCreateGroupFragment mGroupsCreateGroupFragment;
    private GroupsGroupMembersFragment mGroupsGroupMembersFragment;

    SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_handler);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTextViewTitle.setText("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        Bundle bundle = getIntent().getExtras();

        if (bundle != null) {

            int fragment_id = bundle.getInt(Const.Bundle.GROUP_CATEGORY);
            setFragmentForId(fragment_id, bundle);
        }
    }


    private void setFragmentForId(int id, Bundle bundle) {

        if (id == Const.Bundle.CREATE_GROUP_FRAGMENT) {

            if (mToolbar != null && mTextViewTitle != null)
                mTextViewTitle.setText("Create Group");

            mGroupsCreateGroupFragment = new GroupsCreateGroupFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_groups_handler_container, mGroupsCreateGroupFragment)
                    .commit();
        } else if (id == Const.Bundle.GROUP_MEMBERS) {

            String group_name = bundle.getString(Const.Bundle.GROUP_NAME);

            if (mToolbar != null && mTextViewTitle != null && group_name != null)
                mTextViewTitle.setText("Group - " + group_name);

            mGroupsGroupMembersFragment = new GroupsGroupMembersFragment();
            mGroupsGroupMembersFragment.setArguments(bundle);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.activity_groups_handler_container, mGroupsGroupMembersFragment)
                    .commit();
        }
    }

    @Override
    public void onAddGroupMembersClicked(String group_name) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "GroupsHandlerActivity -> onAddGroupMembersClicked -> Group Name: " + group_name);

        startActivity(new Intent(this, GroupsAddGroupMembersActivity.class)
                .putExtra(Const.Bundle.CURRENT_GROUP_NAME, prefs.getString(Const.Prefs.CURRENT_GROUP_NAME, "")));
    }

//    @Override
//    public void onGroupMembersButtonClicked(ArrayList<String> arrayList) {
//
//        Intent i = new Intent();
//        i.putExtra("selected_contacts", arrayList);
//        setResult(RESULT_OK, i);
//
//        finish();
//    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
