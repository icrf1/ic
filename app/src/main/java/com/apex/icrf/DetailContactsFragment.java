package com.apex.icrf;

/**
 * Created by WASPVamsi on 08/09/15.
 */

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

public class DetailContactsFragment extends Fragment {

    Activity activity;

    private AutoCompleteTextView autoCompleteTextView;
    private ListView listview;

    private ArrayList<String> alNames = new ArrayList<String>();
    private ArrayList<String> alPhoneNumbers = new ArrayList<String>();
    private ContactsAdapter mContactsAdapter;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_contacts, container, false);

        // Get Phone contacts and store in arraylist
        getContacts();

        autoCompleteTextView = (AutoCompleteTextView) view.findViewById(R.id.autoCompleteTextView_contacts);
        listview = (ListView) view.findViewById(R.id.listView_contacts);
        listview.setAdapter(new ContactsAdapter());

        return view;
    }


    private class ContactsAdapter extends BaseAdapter {

        private TextView mTextViewName, mTextViewPhoneNumber;

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
        public View getView(int position, View convertView, ViewGroup parent) {


            if (convertView == null)
                convertView = activity.getLayoutInflater().inflate(R.layout.item_contacts, null, false);

            mTextViewName = (TextView) convertView.findViewById(R.id.textView_name);
            mTextViewName.setText(alNames.get(position));


            mTextViewPhoneNumber = (TextView) convertView.findViewById(R.id.textView_phone_number);
            mTextViewPhoneNumber.setText(alPhoneNumbers.get(position));


            return convertView;
        }
    }


    private void getContacts() {
        ContentResolver cr = activity.getContentResolver();
        Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null);
        if (cur.getCount() > 0) {
            while (cur.moveToNext()) {
                String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                alNames.add(name);
                if (Integer.parseInt(cur.getString(cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                    Cursor pCur = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while (pCur.moveToNext()) {
                        String phoneNumber = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                        alPhoneNumbers.add(phoneNumber);
                    }
                    pCur.close();
                }
            }
        }
    }

}
