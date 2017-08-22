package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.apex.icrf.classes.ItemPushNotificationsTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 11/02/16.
 */
public class PushNotificationsTableDbAdapter extends BaseDbAdapter {

    Cursor c;

    public void insertRow(ItemPushNotificationsTable item) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PUSH_MESSAGE, item.getPush_message());
        values.put(DatabaseHelper.PUSH_IMAGE, item.getPush_image());

        super.insertRow(DatabaseHelper.PUSH_NOTIFICATIONS_TABLE, values);
    }

    public boolean isTableEmpty() {

        String query = "SELECT * FROM " + DatabaseHelper.PUSH_NOTIFICATIONS_TABLE;

        c = super.query(query);

        return (c.equals(null) || c.getCount() == 0 || !c.moveToFirst());
    }

    public List<ItemPushNotificationsTable> getPushMessages() {

        List<ItemPushNotificationsTable> items = new ArrayList<ItemPushNotificationsTable>();

        ItemPushNotificationsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.PUSH_NOTIFICATIONS_TABLE + " ORDER BY " + DatabaseHelper.SNO + " DESC";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            items.clear();
        else {

            do {

                item = new ItemPushNotificationsTable();

                item.setPush_message(c.getString(c.getColumnIndex(DatabaseHelper.PUSH_MESSAGE)));
                item.setPush_image(c.getString(c.getColumnIndex(DatabaseHelper.PUSH_IMAGE)));

                items.add(item);

            } while (c.moveToNext());
        }

        return items;
    }

    public void clearTable() {
        super.clearTable(DatabaseHelper.PUSH_NOTIFICATIONS_TABLE);
    }
}
