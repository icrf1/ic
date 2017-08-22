package com.apex.icrf.contacts;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.classes.ItemGroupsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.GroupsTableDbAdapter;
import com.apex.icrf.utils.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 08/10/15.
 */
public class GroupsAddGroupMembersActivity extends AppCompatActivity {

    private ArrayList<String> alNames = new ArrayList<String>();
    private ArrayList<String> alPhoneNumbers = new ArrayList<String>();
    ArrayList<Boolean> mAlIsItemChecked = new ArrayList<Boolean>();
    List<String> mAlGroupMembers = new ArrayList<String>();

    private AddGroupMembersAdapter mAddGroupMembersAdapter;
    private GroupsTableDbAdapter mGroupsTableDbAdapter;
    Profile mProfile;

    //private AutoCompleteTextView autoCompleteTextView;
    private ListView listview;
    private Button mButtonSelectedGroupMembers;

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    private String group_name = "";
    SharedPreferences prefs;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_picker);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTextViewTitle.setText("");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        mProfile = new Profile(this);
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mGroupsTableDbAdapter = DatabaseHelper.get(this).getGroupsTableDbAdapter();

        //getContacts();

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            getGroupMembers(bundle.getString(Const.Bundle.CURRENT_GROUP_NAME));
            mTextViewTitle.setText(bundle.getString(Const.Bundle.CURRENT_GROUP_NAME) + " - Add Members");
            group_name = bundle.getString(Const.Bundle.CURRENT_GROUP_NAME);
        }


        //autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_contacts);
        listview = (ListView) findViewById(R.id.listView_contacts);
        //listview.setAdapter(new AddGroupMembersAdapter());

        mButtonSelectedGroupMembers = (Button) findViewById(R.id.button_selected_contacts);
        mButtonSelectedGroupMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                insertSelectedContacts();
            }
        });

        ContactsTask task = new ContactsTask(this);
        task.execute();
    }


    private void insertSelectedContacts() {

        boolean isSuccessful = false;

        mGroupsTableDbAdapter.beginTransaction();

        try {

            String member_id = mProfile.getMemberId();
            if (group_name.equalsIgnoreCase(""))
                group_name = prefs.getString(Const.Prefs.CURRENT_GROUP_NAME, "");

            for (int j = 0; j < mAlIsItemChecked.size(); j++) {

                if (mAlIsItemChecked.get(j)) {

                    ItemGroupsTable item = new ItemGroupsTable();
                    item.setMember_id(member_id);
                    item.setGroup_name(group_name);
                    item.setContact_name(alNames.get(j));
                    item.setContact_number(alPhoneNumbers.get(j));

                    mGroupsTableDbAdapter.insertRow(item);
                }
            }

            mGroupsTableDbAdapter.setTransactionSuccessful();
            isSuccessful = true;
            Toast.makeText(this, "Members added to Group - " + group_name, Toast.LENGTH_LONG).show();

        } catch (Exception e) {
            e.printStackTrace();
            isSuccessful = false;
        } finally {
            mGroupsTableDbAdapter.endTransaction();
        }

        if (isSuccessful)
            this.finish();

    }


    private class AddGroupMembersAdapter extends BaseAdapter {

        public AddGroupMembersAdapter() {

            for (int i = 0; i < alNames.size(); i++) {
                mAlIsItemChecked.add(false);
            }
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
            return alNames.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;

            if (convertView == null) {
                convertView = getLayoutInflater().inflate(R.layout.item_contacts, null);

                holder = new ViewHolder();
                holder.mTextViewName = (TextView) convertView.findViewById(R.id.textView_name);
                holder.mTextViewPhoneNumber = (TextView) convertView.findViewById(R.id.textView_phone_number);
                holder.mCheckBoxContacts = (CheckBox) convertView.findViewById(R.id.checkBox_contacts);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextViewName.setText(alNames.get(position));
            holder.mTextViewPhoneNumber.setText(alPhoneNumbers.get(position));
            holder.mCheckBoxContacts.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked)
                        mAlIsItemChecked.set(position, true);
                    else
                        mAlIsItemChecked.set(position, false);

                    mButtonSelectedGroupMembers.setText("Add Selected (" + checkedContacts() + ") contacts to Group");
                }
            });

            holder.mCheckBoxContacts.setChecked(mAlIsItemChecked.get(position));

            if (mAlGroupMembers.contains(alPhoneNumbers.get(position)))
                holder.mCheckBoxContacts.setVisibility(View.GONE);
            else
                holder.mCheckBoxContacts.setVisibility(View.VISIBLE);

            return convertView;
        }

        private int checkedContacts() {

            int count = 0;
            for (int i = 0; i < mAlIsItemChecked.size(); i++) {
                if (mAlIsItemChecked.get(i))
                    count++;
            }

            return count;
        }
    }


    public static class ViewHolder {

        TextView mTextViewName, mTextViewPhoneNumber;
        CheckBox mCheckBoxContacts;
    }

    private void getContacts() {
        ContentResolver cr = getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        phoneNumber = phoneNumber.replace("+91", "").replace(" ", "");

                        if (!alPhoneNumbers.contains(phoneNumber)) {
                            alPhoneNumbers.add(phoneNumber);
                            alNames.add(name);
                        }
                    }
                    pCur.close();
                }
            }
        }
        cur.close();
    }

    private void getGroupMembers(String group_name) {

        mAlGroupMembers.clear();
        mAlGroupMembers = mGroupsTableDbAdapter.getPhoneNumbersForGroupName(mProfile.getMemberId(), group_name);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            this.finish();
        }

//        else if (id == R.id.action_done) {
//            sendResult();
//            this.finish();
//        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //getMenuInflater().inflate(R.menu.menu_contacts_picker, menu);
        return super.onCreateOptionsMenu(menu);
    }


    private void sendResult() {

        ArrayList<String> alPhNos = new ArrayList<String>();
        alPhNos.clear();

        for (int j = 0; j < mAlIsItemChecked.size(); j++) {

            if (mAlIsItemChecked.get(j))
                alPhNos.add(alPhoneNumbers.get(j));
        }

        Intent i = new Intent();
        if (alPhoneNumbers.size() > 0) {
            i.putExtra("picked_contacts", alPhNos);
            setResult(RESULT_OK, i);
        } else {
            i.putExtra("picked_contacts", alPhNos);
            setResult(RESULT_CANCELED, i);
        }
    }



    public class ContactsTask extends AsyncTask<Void, Void, Boolean> {

        Context context;
        ProgressDialog progressDialog;

        public ContactsTask(Context context) {
            this.context = context;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = new ProgressDialog(context);
            progressDialog.setMessage("Getting your contacts list...");
            progressDialog.setIndeterminate(true);
            progressDialog.setCancelable(true);
            progressDialog.show();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{id},
                                null);
                        while (pCur.moveToNext()) {
                            String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            phoneNumber = phoneNumber.replace("+91", "").replace(" ", "");

                            if (!alPhoneNumbers.contains(phoneNumber)) {
                                alPhoneNumbers.add(phoneNumber);
                                alNames.add(name);
                            }
                        }
                        pCur.close();
                    }
                }
            }
            cur.close();


            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            mAddGroupMembersAdapter = new AddGroupMembersAdapter();
            listview.setAdapter(mAddGroupMembersAdapter);
        }
    }
}
