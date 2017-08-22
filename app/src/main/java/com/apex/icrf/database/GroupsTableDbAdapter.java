package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.apex.icrf.classes.ItemGroupsTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 07/10/15.
 */
public class GroupsTableDbAdapter extends BaseDbAdapter {

    Cursor c;

    public void insertRow(ItemGroupsTable item) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MEMBER_ID_KEY, item.getMember_id());
        values.put(DatabaseHelper.GROUP_NAME, item.getGroup_name());
        values.put(DatabaseHelper.NAME, item.getContact_name());
        values.put(DatabaseHelper.PHONE_NUMBER, item.getContact_number());

        super.insertRow(DatabaseHelper.GROUPS_TABLE, values);
    }

    public List<String> getMainGroups(String member_id) {

        List<String> groupsList = new ArrayList<String>();

        String query = "SELECT DISTINCT " + DatabaseHelper.GROUP_NAME + " FROM " + DatabaseHelper.GROUPS_TABLE;

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            groupsList.clear();
        else {

            do {

                String group_name = c.getString(c.getColumnIndex(DatabaseHelper.GROUP_NAME));
                groupsList.add(group_name);

            } while (c.moveToNext());
        }

        return groupsList;
    }

    public int getGroupMembersCount(String group_name) {

        String query = "SELECT * FROM " + DatabaseHelper.GROUPS_TABLE + " WHERE "
                + DatabaseHelper.GROUP_NAME + " = '" + group_name + "'";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            return 0;
        else {
            return c.getCount();
        }
    }

    public List<ItemGroupsTable> getDataForGroupName(String member_id, String group_name) {

        List<ItemGroupsTable> items = new ArrayList<ItemGroupsTable>();
        ItemGroupsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.GROUPS_TABLE + " WHERE "
                + DatabaseHelper.GROUP_NAME + " = '" + group_name + "'";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            items.clear();
        else {

            do {

                item = new ItemGroupsTable();
                item.setContact_name(c.getString(c.getColumnIndex(DatabaseHelper.NAME)));
                item.setContact_number(c.getString(c.getColumnIndex(DatabaseHelper.PHONE_NUMBER)));

                items.add(item);

            } while (c.moveToNext());
        }


        return items;
    }

    public List<String> getPhoneNumbersForGroupName(String member_id, String group_name) {

        List<String> items = new ArrayList<String>();

        String query = "SELECT * FROM " + DatabaseHelper.GROUPS_TABLE + " WHERE "
                + DatabaseHelper.GROUP_NAME + " = '" + group_name + "'";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            items.clear();
        else {
            do {

                String number = c.getString(c.getColumnIndex(DatabaseHelper.PHONE_NUMBER));

                items.add(number);

            } while (c.moveToNext());
        }

        return items;
    }

    public void deleteContactFromGroup(String number, String group_name) {

        String where = DatabaseHelper.GROUP_NAME + " = '" + group_name +
                "' AND " + DatabaseHelper.PHONE_NUMBER + " = '" + number + "'";

        super.deleteRow(DatabaseHelper.GROUPS_TABLE, where, null);
    }

    public void clearTable() {
        super.clearTable(DatabaseHelper.GROUPS_TABLE);
    }
}
