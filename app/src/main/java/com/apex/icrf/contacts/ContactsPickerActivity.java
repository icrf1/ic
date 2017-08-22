package com.apex.icrf.contacts;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.telephony.SmsManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.apex.icrf.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 28/09/15.
 */
public class ContactsPickerActivity extends AppCompatActivity {

    private ArrayList<String> alNames = new ArrayList<String>();
    private ArrayList<String> alPhoneNumbers = new ArrayList<String>();
    ArrayList<Boolean> mAlIsItemChecked = new ArrayList<Boolean>();
    private ContactsAdapter mContactsAdapter;

    //private AutoCompleteTextView autoCompleteTextView;
    private ListView listview;
    private Button mButtonSelectedContacts;

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    String sms_text = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contacts_picker);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTextViewTitle.setText("Pick Contacts");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            sms_text = bundle.getString("sms_text");
        }

        alPhoneNumbers.clear();
        alNames.clear();
        mAlIsItemChecked.clear();

        //autoCompleteTextView = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_contacts);
        listview = (ListView) findViewById(R.id.listView_contacts);
        //mContactsAdapter = new ContactsAdapter();
        //listview.setAdapter(mContactsAdapter);

        mButtonSelectedContacts = (Button) findViewById(R.id.button_selected_contacts);
        mButtonSelectedContacts.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendSMSes();
            }
        });


        //getContacts();
        //mContactsAdapter.notifyDataSetChanged();

        ContactsTask task = new ContactsTask(this);
        task.execute();
    }


    public void sendSMSes() {

        List<String> alSelectedGroups = new ArrayList<String>();
        alSelectedGroups.clear();

        for (int i = 0; i < mAlIsItemChecked.size(); i++) {

            if (mAlIsItemChecked.get(i)) {
                alSelectedGroups.add(alPhoneNumbers.get(i));
            }
        }

        displaySMSAlert(alSelectedGroups.size(), alSelectedGroups);

    }

    public void displaySMSAlert(int number, final List<String> alNumbers) {

        AlertDialog.Builder alert = new AlertDialog.Builder(this);
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

                    mButtonSelectedContacts.setText("Contacts Selected (" + checkedContacts() + " / 100)");
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

            mContactsAdapter = new ContactsAdapter();
            listview.setAdapter(mContactsAdapter);
        }
    }

}
