package com.apex.icrf.contacts;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.GroupsTableDbAdapter;
import com.apex.icrf.utils.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 29/09/15.
 */
public class GroupsPickerActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    ListView listViewGroupsList;

    Button mButtonSelectedGroups;

    Profile mProfile;

    GroupsTableDbAdapter mGroupsTableDbAdapter;
    GroupsAdapter mGroupsAdapter;

    List<String> mAlGroupsList = new ArrayList<String>();
    List<Integer> mAlGroupCount = new ArrayList<Integer>();
    ArrayList<Boolean> mAlIsItemChecked = new ArrayList<Boolean>();

    SharedPreferences prefs;

    String sms_text = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_groups_picker);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTextViewTitle.setText("Select Groups");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sms_text = bundle.getString("sms_text");
        }

        mProfile = new Profile(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mGroupsTableDbAdapter = DatabaseHelper.get(this).getGroupsTableDbAdapter();

        prefs.edit().remove(Const.Prefs.CURRENT_GROUP_NAME);

        listViewGroupsList = (ListView) findViewById(R.id.listView_groups_list);
        mGroupsAdapter = new GroupsAdapter();
        listViewGroupsList.setAdapter(mGroupsAdapter);

        listViewGroupsList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                if (Const.DEBUGGING)
                    Log.d(Const.DEBUG, "Position: " + position);

                prefs.edit().putString(Const.Prefs.CURRENT_GROUP_NAME, mAlGroupsList.get(position)).commit();

                startActivity(new Intent(GroupsPickerActivity.this, GroupsHandlerActivity.class)
                        .putExtra(Const.Bundle.GROUP_CATEGORY, Const.Bundle.GROUP_MEMBERS)
                        .putExtra(Const.Bundle.GROUP_NAME, mAlGroupsList.get(position)));
            }
        });


        mButtonSelectedGroups = (Button) findViewById(R.id.button_selected_groups);
        mButtonSelectedGroups.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                sendSMSes();
            }
        });
    }


    public void sendSMSes() {

        List<String> alSelectedGroups = new ArrayList<String>();
        alSelectedGroups.clear();

        for (int i = 0; i < mAlIsItemChecked.size(); i++) {

            if (mAlIsItemChecked.get(i)) {
                alSelectedGroups.add(mAlGroupsList.get(i));
            }
        }

        List<String> alCombinedContacts = getContactsForGroup(alSelectedGroups);

        if (alCombinedContacts.size() > 0)
            displaySMSAlert(alCombinedContacts.size(), alCombinedContacts);
        else
            Toast.makeText(this, "Select atleast one Group to share", Toast.LENGTH_LONG).show();

    }

    public void displaySMSAlert(int number, final List<String> alNumbers) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this,AlertDialog.THEME_DEVICE_DEFAULT_LIGHT);
        AlertDialog dialog;

        alert.setTitle("Alert");
        alert.setMessage("You have selected a total of " + number + " contacts. " +
                "An SMS will be sent from your mobile " +
                "to each of them. " +
                "This will inccur charges depending on your mobile operator. " +
                "Do you want to proceed with sending SMS?");
        alert.setPositiveButton("YES",
                new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        sendMessages(alNumbers);
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

    public void sendMessages(List<String> arrayList) {


        Toast.makeText(this, "Started sending messages to selected contacts...", Toast.LENGTH_LONG).show();
        SmsManager sms = SmsManager.getDefault();

        for (int i = 0; i < arrayList.size(); i++) {

            String number = arrayList.get(i);
            sms.sendTextMessage(number, null, sms_text, null, null);
        }
    }


    public List<String> getContactsForGroup(List<String> arrayList) {

        List<String> alCombined = new ArrayList<String>();
        alCombined.clear();

        for (int i = 0; i < arrayList.size(); i++) {

            List<String> alIndividual = mGroupsTableDbAdapter.getPhoneNumbersForGroupName(mProfile.getMemberId(), arrayList.get(i));

            for (int j = 0; j < alIndividual.size(); j++) {

                if (!alCombined.contains(alIndividual.get(j)))
                    alCombined.add(alIndividual.get(j));
            }
        }


        return alCombined;
    }


    public void getGroups() {

        String member_id = mProfile.getMemberId();
        mAlGroupsList = mGroupsTableDbAdapter.getMainGroups(member_id);
        mAlGroupCount.clear();

        for (int j = 0; j < mAlGroupsList.size(); j++) {

            int count = mGroupsTableDbAdapter.getGroupMembersCount(mAlGroupsList.get(j));
            mAlGroupCount.add(count);
        }

        for (int i = 0; i < mAlGroupsList.size(); i++) {
            mAlIsItemChecked.add(false);
        }

        if (mAlGroupsList.size() > 0) {

        } else {
            Toast.makeText(this, "No Groups Added", Toast.LENGTH_LONG).show();
        }

        mGroupsAdapter.notifyDataSetChanged();
    }

    public class GroupsAdapter extends BaseAdapter {

        public GroupsAdapter() {

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mAlGroupsList.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {


            final ViewHolder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_groups_list, null);

                holder = new ViewHolder();
                holder.mTextViewName = (TextView) convertView.findViewById(R.id.textView_name);
                holder.mTextViewGroupCount = (TextView) convertView.findViewById(R.id.textView_group_count);
                holder.mCheckBoxGroupsList = (CheckBox) convertView.findViewById(R.id.checkBox_groups_list);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextViewName.setText(mAlGroupsList.get(position));
            holder.mTextViewGroupCount.setText("Contacts in group: " + mAlGroupCount.get(position));
            holder.mCheckBoxGroupsList.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {


                    if (isChecked)
                        mAlIsItemChecked.set(position, true);
                    else
                        mAlIsItemChecked.set(position, false);

                    mButtonSelectedGroups.setText("SHARE WITH SELECTED (" + checkedGroups() + ") GROUPS");
                }
            });

            return convertView;
        }

        private int checkedGroups() {

            int count = 0;
            for (int i = 0; i < mAlIsItemChecked.size(); i++) {
                if (mAlIsItemChecked.get(i))
                    count++;
            }

            return count;
        }
    }

    public static class ViewHolder {

        TextView mTextViewName, mTextViewGroupCount;
        CheckBox mCheckBoxGroupsList;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_groups_picker, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();
        else if (id == R.id.action_add) {
            startActivity(new Intent(GroupsPickerActivity.this,
                    GroupsHandlerActivity.class).putExtra(Const.Bundle.GROUP_CATEGORY, Const.Bundle.CREATE_GROUP_FRAGMENT));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getGroups();
    }


}
