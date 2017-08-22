package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.apex.icrf.classes.ItemFavouritesTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 11/02/16.
 */
public class FavouritesTableDbAdapter extends BaseDbAdapter {

    Cursor c;

    public void insertRow(ItemFavouritesTable item) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.E_PETITION_NUMBER_KEY, item.getE_pno());

        super.insertRow(DatabaseHelper.FAVOURITES_TABLE, values);
    }

    public boolean isTableEmpty() {

        String query = "SELECT * FROM " + DatabaseHelper.FAVOURITES_TABLE;

        c = super.query(query);

        return (c.equals(null) || c.getCount() == 0 || !c.moveToFirst());
    }

    public List<String> getFavourites() {

        List<String> items = new ArrayList<String>();

        String query = "SELECT * FROM " + DatabaseHelper.FAVOURITES_TABLE;

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            items.clear();
        else {

            do {

                String item = c.getString(c.getColumnIndex(DatabaseHelper.E_PETITION_NUMBER_KEY));

                items.add(item);

            } while (c.moveToNext());
        }

        return items;
    }

    public void deleteRow(ItemFavouritesTable item) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.E_PETITION_NUMBER_KEY, item.getE_pno());

        String whereClause = DatabaseHelper.E_PETITION_NUMBER_KEY + "=?";
        String[] whereArgs = new String[]{item.getE_pno()};

        super.deleteRow(DatabaseHelper.FAVOURITES_TABLE, whereClause, whereArgs);
    }
}
