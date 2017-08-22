package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.classes.ItemDeliveryReportsTable;

import java.util.ArrayList;
import java.util.List;

public class DeliveryReportsTableDbAdapter extends BaseDbAdapter {

    public static final String SNO = "_sno";
    public static final String MEMBER_ID_KEY = "member_id";
    public static final String E_PETITION_NUMBER_KEY = "e_petition_number";
    public static final String PETITION_NUMBER_KEY = "petition_number";
    public static final String SENT_FROM = "sent_from";
    public static final String SENT_TO = "sent_to";
    public static final String SMS_CONTENT = "sms_content";
    public static final String CONFIRMATION_MESSAGE_CONTENT = "confirmation_content";
    public static final String SENT_SMS_SUCCESS = "sent_sms_success";
    public static final String DELIVER_SMS_SUCCESS = "deliver_sms_success";
    public static final String SYNCED = "synced";
    public static final String MEMBER_ID_TYPE_KEY = "member_id_type";

    Cursor c;

    public void insertRow(ItemDeliveryReportsTable item) {

        ContentValues values = new ContentValues();
        values.put(MEMBER_ID_KEY, item.getMember_id());
        values.put(E_PETITION_NUMBER_KEY, item.getE_petition_number());
        values.put(PETITION_NUMBER_KEY, item.getPetition_number());
        values.put(SENT_FROM, item.getSent_from());
        values.put(SENT_TO, item.getSent_to());
        values.put(SMS_CONTENT, item.getSms_content());
        values.put(CONFIRMATION_MESSAGE_CONTENT, item.getConfirmation_message());
        values.put(SENT_SMS_SUCCESS, item.getSent_sms_success());
        values.put(DELIVER_SMS_SUCCESS, item.getDeliver_sms_success());
        values.put(SYNCED, item.getSynced());
        values.put(MEMBER_ID_TYPE_KEY, item.getMember_id_type());

        super.insertRow(DatabaseHelper.DELIVERY_REPORTS_TABLE, values);
    }

    public void updateSentSMSSuccess(String member_id, int e_pno, String pno, String from_mobile, String to_mobile, int success) {

        if (Const.DEBUGGING) {
            Log.d(Const.DEBUG, "E-Petition No: " + e_pno + " Petition No: " + pno + " Success: " + success);
        }

        ContentValues values = new ContentValues();
        values.put(SENT_SMS_SUCCESS, success);

        String whereClause = E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
                + PETITION_NUMBER_KEY + " = '" + pno + "' AND " + ""
                + SENT_FROM + " = '" + from_mobile + "' AND " + ""
                + SENT_TO + " = '" + to_mobile + "'";

        super.updateRow(DatabaseHelper.DELIVERY_REPORTS_TABLE, values, whereClause);
    }

    public void updateDeliverSMSSuccess(String member_id, int e_pno, String pno, String from_mobile, String to_mobile, int success) {

        if (Const.DEBUGGING) {
            Log.d(Const.DEBUG, "E-Petition No: " + e_pno + " Petition No: " + pno + " Success: " + success);
        }

        ContentValues values = new ContentValues();
        values.put(DELIVER_SMS_SUCCESS, success);

        String whereClause = E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
                + PETITION_NUMBER_KEY + " = '" + pno + "' AND " + ""
                + SENT_FROM + " = '" + from_mobile + "' AND " + ""
                + SENT_TO + " = '" + to_mobile + "'";

        super.updateRow(DatabaseHelper.DELIVERY_REPORTS_TABLE, values, whereClause);
    }

    public void updateSynced(String member_id, String e_pno, String pno, String from_mobile, String to_mobile, int success) {

        if (Const.DEBUGGING) {
            Log.d(Const.DEBUG, "E-Petition No: " + e_pno + " Petition No: " + pno + " Success: " + success);
        }

        ContentValues values = new ContentValues();
        values.put(SYNCED, success);

        String whereClause = E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
                + PETITION_NUMBER_KEY + " = '" + pno + "' AND " + ""
                + SENT_FROM + " = '" + from_mobile + "' AND " + ""
                + SENT_TO + " = '" + to_mobile + "'";

        super.updateRow(DatabaseHelper.DELIVERY_REPORTS_TABLE, values, whereClause);
    }


    public int isDelivered(String e_pno) {

        String query = "SELECT * FROM " + DatabaseHelper.DELIVERY_REPORTS_TABLE + " WHERE " + E_PETITION_NUMBER_KEY + " = " + e_pno;

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            return 0;
        } else {
            c.moveToFirst();

            if ((c.getInt(c.getColumnIndex(SENT_SMS_SUCCESS)) == 1) &&
                    (c.getInt(c.getColumnIndex(DELIVER_SMS_SUCCESS)) == 1)) {
                return 1;
            } else if ((c.getInt(c.getColumnIndex(SENT_SMS_SUCCESS)) == 1) &&
                    (c.getInt(c.getColumnIndex(DELIVER_SMS_SUCCESS)) == 0)) {
                return 2;
            } else if ((c.getInt(c.getColumnIndex(SENT_SMS_SUCCESS)) == 0) &&
                    (c.getInt(c.getColumnIndex(DELIVER_SMS_SUCCESS)) == 0)) {
                return 3;
            }

            return 0;
        }
    }


    public List<ItemDeliveryReportsTable> getNonSyncedDeliveryReports() {

        List<ItemDeliveryReportsTable> items = new ArrayList<ItemDeliveryReportsTable>();

        ItemDeliveryReportsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.DELIVERY_REPORTS_TABLE
                + " WHERE " + SENT_SMS_SUCCESS + " = 1 AND " + DELIVER_SMS_SUCCESS + " = 1 AND " +
                SYNCED + "= 0";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            items.clear();
        } else {
            c.moveToFirst();

            do {

                item = new ItemDeliveryReportsTable();
                item.setMember_id(c.getString(c.getColumnIndex(MEMBER_ID_KEY)));
                item.setE_petition_number(c.getString(c.getColumnIndex(E_PETITION_NUMBER_KEY)));
                item.setPetition_number(c.getString(c.getColumnIndex(PETITION_NUMBER_KEY)));
                item.setSent_from(c.getString(c.getColumnIndex(SENT_FROM)));
                item.setSent_to(c.getString(c.getColumnIndex(SENT_TO)));
                item.setSms_content(c.getString(c.getColumnIndex(SMS_CONTENT)));
                item.setConfirmation_message(c.getString(c.getColumnIndex(CONFIRMATION_MESSAGE_CONTENT)));
                item.setSent_sms_success(c.getInt(c.getColumnIndex(SENT_SMS_SUCCESS)));
                item.setDeliver_sms_success(c.getInt(c.getColumnIndex(DELIVER_SMS_SUCCESS)));
                item.setSynced(c.getInt(c.getColumnIndex(SYNCED)));
                item.setMember_id_type(c.getString(c.getColumnIndex(MEMBER_ID_TYPE_KEY)));

                items.add(item);

            } while (c.moveToNext());
        }

        return items;
    }

    public List<ItemDeliveryReportsTable> displayDeliveryReportsTable() {

        List<ItemDeliveryReportsTable> items = new ArrayList<ItemDeliveryReportsTable>();

        ItemDeliveryReportsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.DELIVERY_REPORTS_TABLE;

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            items.clear();
        } else {
            c.moveToFirst();

            do {

                item = new ItemDeliveryReportsTable();
                item.setMember_id(c.getString(c.getColumnIndex(MEMBER_ID_KEY)));
                item.setE_petition_number(c.getString(c.getColumnIndex(E_PETITION_NUMBER_KEY)));
                item.setPetition_number(c.getString(c.getColumnIndex(PETITION_NUMBER_KEY)));
                item.setSent_from(c.getString(c.getColumnIndex(SENT_FROM)));
                item.setSent_to(c.getString(c.getColumnIndex(SENT_TO)));
                item.setSms_content(c.getString(c.getColumnIndex(SMS_CONTENT)));
                item.setConfirmation_message(c.getString(c.getColumnIndex(CONFIRMATION_MESSAGE_CONTENT)));
                item.setSent_sms_success(c.getInt(c.getColumnIndex(SENT_SMS_SUCCESS)));
                item.setDeliver_sms_success(c.getInt(c.getColumnIndex(DELIVER_SMS_SUCCESS)));
                item.setSynced(c.getInt(c.getColumnIndex(SYNCED)));
                item.setMember_id_type(c.getString(c.getColumnIndex(MEMBER_ID_TYPE_KEY)));

                items.add(item);

            } while (c.moveToNext());
        }

        return items;
    }

    public void clearTable() {
        super.clearTable(DatabaseHelper.DELIVERY_REPORTS_TABLE);
    }

}
