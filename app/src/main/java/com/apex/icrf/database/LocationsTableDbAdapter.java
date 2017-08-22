package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.classes.Address;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 29/04/16.
 */
public class LocationsTableDbAdapter extends BaseDbAdapter {

    public static final String SNO = "_sno";
    public static final String STATE_NAME = "state";
    public static final String DISTRICT_NAME = "district";
    public static final String CITY_NAME = "city";
    public static final String PIN_CODE = "pin_code";
    public static final String STATE_ID = "state_id";
    public static final String DISTRICT_ID = "district_id";

    Cursor c;

    public void insertRow(Address item) {

        ContentValues values = new ContentValues();
        values.put(STATE_NAME, item.getState_name());
        values.put(DISTRICT_NAME, item.getDistrict_name());
        values.put(CITY_NAME, "");
        values.put(PIN_CODE, "");
        values.put(STATE_ID, item.getState_id());
        values.put(DISTRICT_ID, item.getDistrict_id());

        super.insertRow(DatabaseHelper.LOCATIONS_TABLE, values);
    }

    public List<String> getPincodes() {

        List<String> pincodes = new ArrayList<>();

        String query = "SELECT DISTINCT " + PIN_CODE + " FROM " + DatabaseHelper.LOCATIONS_TABLE;

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            pincodes.clear();
        } else {
            c.moveToFirst();

            do {

                String pin_code = c.getString(c.getColumnIndex(PIN_CODE));

                if (!pincodes.contains(pin_code))
                    pincodes.add(pin_code);

            } while (c.moveToNext());
        }

        Log.d(Const.DEBUG, "Pincodes Length: " + pincodes.size());

        return pincodes;
    }

    public boolean isTableEmpty() {

        boolean isEmpty = false;

        String query = "SELECT * FROM " + DatabaseHelper.LOCATIONS_TABLE;

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            isEmpty = true;
        } else {
            isEmpty = false;
        }

        return isEmpty;
    }

    public List<String> getStates() {

        List<String> states = new ArrayList<>();

        String query = "SELECT DISTINCT " + STATE_NAME + " FROM " + DatabaseHelper.LOCATIONS_TABLE + " ORDER BY " + STATE_ID + " ASC";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            states.clear();
        } else {
            c.moveToFirst();

            do {

                String state_name = c.getString(c.getColumnIndex(STATE_NAME));

                if (!states.contains(state_name))
                    states.add(state_name);

            } while (c.moveToNext());
        }

        Log.d(Const.DEBUG, "States Length: " + states.size());

        return states;
    }

    public List<String> getDistrictsForState(String state) {

        List<String> districts = new ArrayList<>();

        String query = "SELECT DISTINCT " + DISTRICT_NAME + " FROM " + DatabaseHelper.LOCATIONS_TABLE
                + " WHERE " + STATE_NAME + " = '" + state +"' ORDER BY "  + DISTRICT_ID + " ASC ";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            districts.clear();
        } else {
            c.moveToFirst();

            do {

                String district_name = c.getString(c.getColumnIndex(DISTRICT_NAME));

                if (!districts.contains(district_name))
                    districts.add(district_name);

            } while (c.moveToNext());
        }

        Log.d(Const.DEBUG, "Districts Length: " + districts.size());

        return districts;
    }
}
