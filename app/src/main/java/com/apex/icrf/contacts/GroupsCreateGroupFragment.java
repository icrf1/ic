package com.apex.icrf.contacts;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
public class GroupsCreateGroupFragment extends Fragment {

    Activity activity;

    private ArrayList<String> alNames = new ArrayList<String>();
    private ArrayList<String> alPhoneNumbers = new ArrayList<String>();
    private ArrayList<String> alNamesCopy = new ArrayList<String>();
    private ArrayList<String> alPhoneNumbersCopy = new ArrayList<String>();
    private ContactsAdapter mContactsAdapter;


    ArrayList<Boolean> mAlIsItemChecked = new ArrayList<Boolean>();

    List<String> alGroupNames = new ArrayList<String>();

    GroupsTableDbAdapter mGroupsTableDbAdapter;

    private ListView listview;
    private Button mButtonCreateGroup;
    private EditText mEditTextGroupName;

    private Profile mProfile;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_groups_create_group, container, false);

        mProfile = new Profile(activity);
        mGroupsTableDbAdapter = DatabaseHelper.get(activity).getGroupsTableDbAdapter();

        //getContacts();
        //getGroups();

        mEditTextGroupName = (EditText) view.findViewById(R.id.editText_group_name);

        listview = (ListView) view.findViewById(R.id.listView_create_group_contacts);
        //listview.setAdapter(new ContactsAdapter());

        mButtonCreateGroup = (Button) view.findViewById(R.id.button_create_group);
        mButtonCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String group_name = mEditTextGroupName.getText().toString();

                if (group_name.equalsIgnoreCase(""))
                    Toast.makeText(activity, "Group Name cannot be empty.", Toast.LENGTH_LONG).show();
                else if (alGroupNames.size() > 0 && alGroupNames.contains(group_name))
                    Toast.makeText(activity, "Group Name already exists. Please select a different name for the group.", Toast.LENGTH_LONG).show();
                else
                    createGroup(group_name);
            }
        });

        ContactsTask task = new ContactsTask(activity);
        task.execute();

        return view;
    }

//    public void getGroups() {
//
//        String member_id = mProfile.getMemberId();
//        alGroupNames = mGroupsTableDbAdapter.getMainGroups(member_id);
//    }
//
//    private void getContacts() {
//        ContentResolver cr = activity.getContentResolver();
//        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, "UPPER(" + ContactsContract.Contacts.DISPLAY_NAME + ") ASC");
//        if (cur.getCount() > 0) {
//            while (cur.moveToNext()) {
//                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
//                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//
//                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
//                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
//                            null,
//                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
//                            new String[]{id},
//                            null);
//                    while (pCur.moveToNext()) {
//                        String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
//                        phoneNumber = phoneNumber.replace("+91", "").replace(" ", "");
//
//                        if (!alPhoneNumbers.contains(phoneNumber)) {
//                            alPhoneNumbers.add(phoneNumber);
//                            alNames.add(name);
//                        }
//                    }
//                    pCur.close();
//                }
//            }
//        }
//        cur.close();
//    }

    private class ContactsAdapter extends BaseAdapter {

        public ContactsAdapter() {

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
                convertView = activity.getLayoutInflater().inflate(R.layout.item_contacts, null);

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

                    // mButtonSelectedContacts.setText("Contacts Selected (" + checkedContacts() + " / 100)");
                }
            });

            holder.mCheckBoxContacts.setChecked(mAlIsItemChecked.get(position));

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

    public void createGroup(String group_name) {

        boolean successful = false;

        alNamesCopy.clear();
        alPhoneNumbersCopy.clear();

        for (int j = 0; j < mAlIsItemChecked.size(); j++) {

            if (mAlIsItemChecked.get(j)) {
                alPhoneNumbersCopy.add(alPhoneNumbers.get(j));
                alNamesCopy.add(alNames.get(j));
            }
        }

        if (alPhoneNumbersCopy.size() == 0 || alNamesCopy.size() == 0)
            Toast.makeText(activity, "Select atleast one contact to create a group", Toast.LENGTH_LONG).show();
        else {

            ItemGroupsTable item;
            String member_id = mProfile.getMemberId();

            mGroupsTableDbAdapter.beginTransaction();
            try {

                for (int k = 0; k < alPhoneNumbersCopy.size(); k++) {

                    item = new ItemGroupsTable();
                    item.setMember_id(member_id);
                    item.setGroup_name(group_name.trim());
                    item.setContact_name(alNamesCopy.get(k));
                    item.setContact_number(alPhoneNumbersCopy.get(k));

                    mGroupsTableDbAdapter.insertRow(item);
                }

                mGroupsTableDbAdapter.setTransactionSuccessful();
                successful = true;
            } catch (Exception e) {
                e.printStackTrace();
                successful = false;
            } finally {
                mGroupsTableDbAdapter.endTransaction();
            }

            if (successful) {
                Toast.makeText(activity, "Group Created Successfully", Toast.LENGTH_LONG).show();
                activity.finish();
            } else
                Toast.makeText(activity, "Group Created Successfully", Toast.LENGTH_LONG).show();


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

            getContacts();
            getGroups();

            return true;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            if (progressDialog != null && progressDialog.isShowing())
                progressDialog.dismiss();

            mContactsAdapter = new ContactsAdapter();
            listview.setAdapter(mContactsAdapter);
        }



        private void getContacts() {
            ContentResolver cr = context.getContentResolver();
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

        public void getGroups() {

            String member_id = mProfile.getMemberId();
            alGroupNames = mGroupsTableDbAdapter.getMainGroups(member_id);
        }

    }


}
