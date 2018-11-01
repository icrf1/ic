package com.apex.icrf;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by WASPVamsi on 26/04/16.
 */
public class RegistrationFinalStep1Activity extends AppCompatActivity {

    Toolbar toolbar;
    TextView title;
    TextView textViewMobile, textViewEmail;
    ImageView imageViewMale, imageViewFemale, imageViewDOB;

    SharedPreferences prefs;
    String country, phone, email;

    EditText editTextName, editTextDOB;

    boolean isMale = false;
    boolean isFemale = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registration_final_step_1);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        title = (TextView) toolbar.findViewById(R.id.toolbar_title);

        setSupportActionBar(toolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        textViewMobile = (TextView) findViewById(R.id.textview_registration_final_step_1_mobile_no);
        textViewEmail = (TextView) findViewById(R.id.textview_registration_final_step_1_email);

        editTextName = (EditText) findViewById(R.id.edittext_registration_final_step_1_name);
        editTextDOB = (EditText) findViewById(R.id.edittext_registration_final_step_1_dob);

        imageViewMale = (ImageView) findViewById(R.id.imageview_gender_male);
        imageViewFemale = (ImageView) findViewById(R.id.imageview_gender_female);
        imageViewDOB = (ImageView) findViewById(R.id.imageview_registration_final_step_1_dob);
        imageViewDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogFragment dialogFragment = new StartDatePicker();
                dialogFragment.show(getSupportFragmentManager(), "dob");
            }
        });


        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {

            country = bundle.getString("country");
            phone = bundle.getString("phone");
            email = bundle.getString("email");

            textViewMobile.setText(phone);
            textViewEmail.setText(email);
        }


        imageViewMale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = true;
                isFemale = false;
                setImage(0);
            }
        });

        imageViewFemale.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isMale = false;
                isFemale = true;
                setImage(1);
            }
        });
    }

    public void setImage(int position) {
        if (position == 0) {
            imageViewMale.setImageResource(R.drawable.drawable_male_green_512);
            imageViewFemale.setImageResource(R.drawable.drawable_female_grey_512);
        } else {
            imageViewMale.setImageResource(R.drawable.drawable_male_grey_512);
            imageViewFemale.setImageResource(R.drawable.drawable_female_green_512);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (toolbar != null && title != null) {
            title.setText(getResources().getString(R.string.title_activity_registration_final_step_1));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_registration_final_step_1, menu);

        MenuItem item = menu.findItem(R.id.action_next);
        if (item != null) {
            MenuItemCompat.getActionView(item).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    int result = canRedirect();

                    if (result == -1) {

                        String name = editTextName.getText().toString();
                        String dob = editTextDOB.getText().toString();
                        String gender = isMale ? "male" : "female";


                        startActivity(new Intent(RegistrationFinalStep1Activity.this, RegistrationFinalStep2Activity.class)
                                .putExtra("country", country)
                                //.putExtra("country", "IN")
                                .putExtra("phone", phone)
                                .putExtra("email", email)
                                .putExtra("name", name)
                                .putExtra("dob", dob)
                                .putExtra("gender", gender));
                    } else {

                        // Show Toast depending on result

                        if (result == 1) {
                            Toast.makeText(RegistrationFinalStep1Activity.this, "Name cannot be empty", Toast.LENGTH_LONG).show();
                        } else if (result == 3) {
                            Toast.makeText(RegistrationFinalStep1Activity.this, "Please select your gender", Toast.LENGTH_LONG).show();
                        } else if (result == 2) {
                            Toast.makeText(RegistrationFinalStep1Activity.this, "Please select your date of birth", Toast.LENGTH_LONG).show();
                        }
                    }


                }
            });
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }


    public int canRedirect() {

        int result = -1;

        String name = editTextName.getText().toString();
        String dob = editTextDOB.getText().toString();

        if (name.length() == 0) {
            result = 1;
        } else if (isMale == false && isFemale == false) {
            result = 3;
        } else if (dob.length() == 0) {
            result = 2;
        }


        return result;
    }

    public static class StartDatePicker extends DialogFragment implements DatePickerDialog.OnDateSetListener {

        Context context;

        Calendar c = Calendar.getInstance();
        int startYear = c.get(Calendar.YEAR);
        int startMonth = c.get(Calendar.MONTH);
        int startDay = c.get(Calendar.DAY_OF_MONTH);

        public StartDatePicker() {

        }

        @Override
        public void onAttach(Context context) {
            super.onAttach(context);
            this.context = context;
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            DatePickerDialog dialog = new DatePickerDialog(context, AlertDialog.THEME_DEVICE_DEFAULT_DARK, this, startYear, startMonth, startDay);
            dialog.getDatePicker().setMaxDate(Calendar.getInstance().getTimeInMillis());
            return dialog;
        }

        public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
            startYear = year;
            startMonth = monthOfYear + 1;
            startDay = dayOfMonth;

            Log.d("dob",startDay + "-" + startMonth + "-" + startYear);
            String tag = getTag();
            String display_text = startMonth + "-" + startDay + "-" + startYear;
            ((RegistrationFinalStep1Activity) context).editTextDOB.setText(display_text);
            //updateStartDateDisplay(tag);
        }

        private void updateStartDateDisplay(String tag) {

            SimpleDateFormat format1 = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault());
            Date date;
            String display_text = "";

            try {
                date = format1.parse(startDay + "-" + startMonth + "-" + startYear);
                display_text = format1.format(date);
                Log.d("dob after",display_text);

                if (tag.equalsIgnoreCase("dob")) {
                    if (((RegistrationFinalStep1Activity) context).editTextDOB != null) {

                        ((RegistrationFinalStep1Activity) context).editTextDOB.setText(display_text);
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
