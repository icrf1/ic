package com.apex.icrf.database;

import android.content.ContentValues;
import android.database.Cursor;

import com.apex.icrf.classes.ItemPetitionsTable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 03/10/15.
 */
public class PetitionsTableDbAdapter extends BaseDbAdapter {

    Cursor c;

    public void insertRow(ItemPetitionsTable item) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.PETITION_TYPE, item.getPetition_type());
        values.put(DatabaseHelper.E_PETITION_NUMBER_KEY, item.getE_petition_number());
        values.put(DatabaseHelper.PETITION_NUMBER_KEY, item.getPetition_number());
        values.put(DatabaseHelper.MEMBER_ID_KEY, item.getMember_id());
        values.put(DatabaseHelper.MEMBER_ID_TYPE_KEY, item.getMember_id_type());
        values.put(DatabaseHelper.PETITION_ADDRESS, item.getPetition_address());
        values.put(DatabaseHelper.OFFICIAL_NAME, item.getOfficial_name());
        values.put(DatabaseHelper.OFFICIAL_DESIGNATION, item.getOfficial_designation());
        values.put(DatabaseHelper.OFFICE_DEPARTMENT_NAME, item.getOffice_department_name());
        values.put(DatabaseHelper.OFFICE_ADDRESS, item.getOffice_address());
        values.put(DatabaseHelper.CITY, item.getCity());
        values.put(DatabaseHelper.STATE, item.getState());
        values.put(DatabaseHelper.DISTRICT, item.getDistrict());
        values.put(DatabaseHelper.PINCODE, item.getPincode());
        values.put(DatabaseHelper.OFFICIAL_MOBILE, item.getOfficial_mobile());
        values.put(DatabaseHelper.OFFICIAL_EMAIL, item.getOfficial_email());
        values.put(DatabaseHelper.PETITION_TITLE, item.getPetition_title());
        values.put(DatabaseHelper.PETITION_DESCRIPTION, item.getPetition_description());
        values.put(DatabaseHelper.SMS_MATTER, item.getSms_matter());
       // values.put(DatabaseHelper.SMSMATTER, item.getSms_matter());

        values.put(DatabaseHelper.OTP, item.getOtp());
        values.put(DatabaseHelper.SMS_SEND_OR_NOT, item.getSms_send_or_not());
        values.put(DatabaseHelper.STATUS, item.getStatus());
        values.put(DatabaseHelper.DATE, item.getDate());
        values.put(DatabaseHelper.PETITIONER_NAME, item.getPetitioner_name());
        values.put(DatabaseHelper.PETITIONER_GENDER, item.getPetitioner_gender());
        values.put(DatabaseHelper.PETITIONER_CITY, item.getPetitioner_city());
        values.put(DatabaseHelper.PETITIONER_DISTRICT, item.getPetitioner_district());
        values.put(DatabaseHelper.PETITIONER_STATE, item.getPetitioner_state());
        values.put(DatabaseHelper.PETITIONER_PINCODE, item.getPetitioner_pincode());
        values.put(DatabaseHelper.PETITIONER_MOBILE, item.getPetitioner_mobile());
        values.put(DatabaseHelper.PETITIONER_EMAIL, item.getPetitioner_email());

        values.put(DatabaseHelper.LIKED_OR_NOT, item.getLiked_or_not());
        values.put(DatabaseHelper.LIKE_COUNT, item.getLike_count());
        values.put(DatabaseHelper.COMMENT_POSTED_OR_NOT, item.getComment_posted_or_not());
        values.put(DatabaseHelper.COMMENTS, item.getComments());
        values.put(DatabaseHelper.ATTACHMENTS, item.getAttachments());
        values.put(DatabaseHelper.SENT_SUPPORT, item.getSent_support());

        values.put(DatabaseHelper.COMMENTS_COUNT, item.getComments_count());
        values.put(DatabaseHelper.SUPPORTS_COUNT, item.getSupports_count());

        values.put(DatabaseHelper.LATITUDE, item.getLatitude());
        values.put(DatabaseHelper.LONGITUDE, item.getLongitude());
        values.put(DatabaseHelper.PETITIONER_PROFILE_IMAGE_URL, item.getPetitioner_profile_image_url());


        super.insertRow(DatabaseHelper.PETITIONS_TABLE, values);
    }

    public void updateRow(ItemPetitionsTable item, String pno) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.MEMBER_ID_TYPE_KEY, item.getMember_id_type());
        values.put(DatabaseHelper.PETITION_ADDRESS, item.getPetition_address());
        values.put(DatabaseHelper.OFFICIAL_NAME, item.getOfficial_name());
        values.put(DatabaseHelper.OFFICIAL_DESIGNATION, item.getOfficial_designation());
        values.put(DatabaseHelper.OFFICE_DEPARTMENT_NAME, item.getOffice_department_name());
        values.put(DatabaseHelper.OFFICE_ADDRESS, item.getMember_id_type());
        values.put(DatabaseHelper.CITY, item.getCity());
        values.put(DatabaseHelper.STATE, item.getState());
        values.put(DatabaseHelper.DISTRICT, item.getDistrict());
        values.put(DatabaseHelper.PINCODE, item.getPincode());
        values.put(DatabaseHelper.OFFICIAL_MOBILE, item.getOfficial_mobile());
        values.put(DatabaseHelper.OFFICIAL_EMAIL, item.getOfficial_email());
        values.put(DatabaseHelper.PETITION_TITLE, item.getPetition_title());
        values.put(DatabaseHelper.PETITION_DESCRIPTION, item.getPetition_description());
        values.put(DatabaseHelper.SMS_MATTER, item.getSms_matter());
        values.put(DatabaseHelper.OTP, item.getOtp());
        values.put(DatabaseHelper.SMS_SEND_OR_NOT, item.getSms_send_or_not());
        values.put(DatabaseHelper.STATUS, item.getStatus());
        values.put(DatabaseHelper.DATE, item.getDate());
        values.put(DatabaseHelper.PETITIONER_NAME, item.getPetitioner_name());
        values.put(DatabaseHelper.PETITIONER_GENDER, item.getPetitioner_gender());
        values.put(DatabaseHelper.PETITIONER_CITY, item.getPetitioner_city());
        values.put(DatabaseHelper.PETITIONER_DISTRICT, item.getPetitioner_district());
        values.put(DatabaseHelper.PETITIONER_STATE, item.getPetitioner_state());
        values.put(DatabaseHelper.PETITIONER_PINCODE, item.getPetitioner_pincode());
        values.put(DatabaseHelper.PETITIONER_MOBILE, item.getPetitioner_mobile());
        values.put(DatabaseHelper.PETITIONER_EMAIL, item.getPetitioner_email());

        values.put(DatabaseHelper.LIKED_OR_NOT, item.getLiked_or_not());
        values.put(DatabaseHelper.LIKE_COUNT, item.getLike_count());
        values.put(DatabaseHelper.COMMENT_POSTED_OR_NOT, item.getComment_posted_or_not());
        values.put(DatabaseHelper.COMMENTS, item.getComments());
        values.put(DatabaseHelper.ATTACHMENTS, item.getAttachments());
        values.put(DatabaseHelper.SENT_SUPPORT, item.getSent_support());

        String where = DatabaseHelper.PETITION_NUMBER_KEY + " = '" + pno + "'";

        super.updateRow(DatabaseHelper.PETITIONS_TABLE, values, where);
    }


    public boolean isTableEmpty(String petition_type) {

        String query = "SELECT * FROM " + DatabaseHelper.PETITIONS_TABLE + " WHERE "
                + DatabaseHelper.PETITION_TYPE + " = '" + petition_type + "'";

        c = super.query(query);

        return (c.equals(null) || c.getCount() == 0 || !c.moveToFirst());
    }

    public List<ItemPetitionsTable> getPetitionsForType(String petition_type) {

        List<ItemPetitionsTable> items = new ArrayList<ItemPetitionsTable>();

        ItemPetitionsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.PETITIONS_TABLE + " WHERE "
                + DatabaseHelper.PETITION_TYPE + " = '" + petition_type + "'";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            items.clear();
        else {

            do {

                item = new ItemPetitionsTable();

                item.setE_petition_number(c.getString(c.getColumnIndex(DatabaseHelper.E_PETITION_NUMBER_KEY)));
                item.setPetition_number(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_NUMBER_KEY)));
                item.setPetition_title(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TITLE)));
                item.setSent_support(c.getString(c.getColumnIndex(DatabaseHelper.SENT_SUPPORT)));
                item.setAttachments(c.getString(c.getColumnIndex(DatabaseHelper.ATTACHMENTS)));
                item.setPetitioner_city(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_CITY)));
                item.setPetitioner_name(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_NAME)));
                item.setPetitioner_state(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_STATE)));
                item.setPetitioner_profile_image_url(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_PROFILE_IMAGE_URL)));

                items.add(item);

            } while (c.moveToNext());
        }


        return items;
    }

    public void clearFeed(String petition_type) {
        String where = DatabaseHelper.PETITION_TYPE + " = '" + petition_type + "'";
        super.clearFeedFromTable(where, null);
    }

    public List<String> getPetitionNumbers(String petition_type) {

        List<String> petition_numbers = new ArrayList<String>();

        String query = "SELECT * FROM " + DatabaseHelper.PETITIONS_TABLE + " WHERE "
                + DatabaseHelper.PETITION_TYPE + " = '" + petition_type + "'";

        c = super.query(query);

        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            petition_numbers.clear();
        else {

            do {

                String petition_number = c.getString(c.getColumnIndex(DatabaseHelper.PETITION_NUMBER_KEY));
                petition_numbers.add(petition_number);

            } while (c.moveToNext());
        }

        return petition_numbers;
    }


    public ItemPetitionsTable getPetitionDetailsForID(String e_petition_number) {


        String query = "SELECT * FROM " + DatabaseHelper.PETITIONS_TABLE + " WHERE "
                + DatabaseHelper.E_PETITION_NUMBER_KEY + " = '" + e_petition_number + "'";

        c = super.query(query);


        if (c.equals(null) || c.getCount() == 0 || !c.moveToFirst())
            return null;
        else {

            c.moveToFirst();

            ItemPetitionsTable item = new ItemPetitionsTable();
            item.setPetition_type(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TYPE)));
            item.setE_petition_number(c.getString(c.getColumnIndex(DatabaseHelper.E_PETITION_NUMBER_KEY)));
            item.setPetition_number(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_NUMBER_KEY)));
            item.setMember_id(c.getString(c.getColumnIndex(DatabaseHelper.MEMBER_ID_KEY)));
            item.setPetition_title(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TITLE)));

            item.setMember_id_type(c.getString(c.getColumnIndex(DatabaseHelper.MEMBER_ID_TYPE_KEY)));
            item.setPetition_address(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_ADDRESS)));
            item.setOfficial_name(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_NAME)));
            item.setOfficial_designation(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_DESIGNATION)));
            item.setOffice_department_name(c.getString(c.getColumnIndex(DatabaseHelper.OFFICE_DEPARTMENT_NAME)));
            item.setOffice_address(c.getString(c.getColumnIndex(DatabaseHelper.OFFICE_ADDRESS)));
            item.setCity(c.getString(c.getColumnIndex(DatabaseHelper.CITY)));
            item.setState(c.getString(c.getColumnIndex(DatabaseHelper.STATE)));
            item.setDistrict(c.getString(c.getColumnIndex(DatabaseHelper.DISTRICT)));
            item.setPincode(c.getString(c.getColumnIndex(DatabaseHelper.PINCODE)));
            item.setOfficial_mobile(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_MOBILE)));
            item.setOfficial_email(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_EMAIL)));
            item.setPetition_title(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TITLE)));
            item.setPetition_description(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_DESCRIPTION)));
            item.setSms_matter(c.getString(c.getColumnIndex(DatabaseHelper.SMS_MATTER)));
           // item.setSms_matter(c.getString(c.getColumnIndex(DatabaseHelper.SMSMATTER)));
            item.setOtp(c.getString(c.getColumnIndex(DatabaseHelper.OTP)));
            item.setSms_send_or_not(c.getString(c.getColumnIndex(DatabaseHelper.SMS_SEND_OR_NOT)));
            item.setStatus(c.getString(c.getColumnIndex(DatabaseHelper.STATUS)));
            item.setDate(c.getString(c.getColumnIndex(DatabaseHelper.DATE)));
            item.setPetitioner_name(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_NAME)));
            item.setPetitioner_gender(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_GENDER)));
            item.setPetitioner_city(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_CITY)));
            item.setPetitioner_district(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_DISTRICT)));
            item.setPetitioner_state(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_STATE)));
            item.setPetitioner_pincode(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_PINCODE)));
            item.setPetitioner_mobile(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_MOBILE)));
            item.setPetitioner_email(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_EMAIL)));
            item.setLiked_or_not(c.getString(c.getColumnIndex(DatabaseHelper.LIKED_OR_NOT)));
            item.setLike_count(c.getString(c.getColumnIndex(DatabaseHelper.LIKE_COUNT)));
            item.setComment_posted_or_not(c.getString(c.getColumnIndex(DatabaseHelper.COMMENT_POSTED_OR_NOT)));
            item.setComments(c.getString(c.getColumnIndex(DatabaseHelper.COMMENTS)));
            item.setAttachments(c.getString(c.getColumnIndex(DatabaseHelper.ATTACHMENTS)));
            item.setSent_support(c.getString(c.getColumnIndex(DatabaseHelper.SENT_SUPPORT)));

            item.setComments_count(c.getString(c.getColumnIndex(DatabaseHelper.COMMENTS_COUNT)));
            item.setSupports_count(c.getString(c.getColumnIndex(DatabaseHelper.SUPPORTS_COUNT)));

            item.setLatitude(c.getString(c.getColumnIndex(DatabaseHelper.LATITUDE)));
            item.setLongitude(c.getString(c.getColumnIndex(DatabaseHelper.LONGITUDE)));
            item.setPetitioner_profile_image_url(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_PROFILE_IMAGE_URL)));

            return item;
        }

    }


    public void updateLikeStatus(String petition_number, String liked) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.LIKED_OR_NOT, liked);

        String where = DatabaseHelper.PETITION_NUMBER_KEY + " = '" + petition_number + "'";

        super.updateRow(DatabaseHelper.PETITIONS_TABLE, values, where);

    }

    public void updateLikeCount(String petition_number, String count) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.LIKE_COUNT, count);

        String where = DatabaseHelper.PETITION_NUMBER_KEY + " = '" + petition_number + "'";

        super.updateRow(DatabaseHelper.PETITIONS_TABLE, values, where);
    }

    public void updateStatus(String e_pno, String pno) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.SENT_SUPPORT, "1");

//        String whereClause = DatabaseHelper.MEMBER_ID_KEY + " = " + member_id + " AND " + ""
//                + DatabaseHelper.E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
//                + DatabaseHelper.PETITION_NUMBER_KEY + " = '" + pno + "'";

        String whereClause = DatabaseHelper.E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
                + DatabaseHelper.PETITION_NUMBER_KEY + " = '" + pno + "'";

        super.updateRow(DatabaseHelper.PETITIONS_TABLE, values, whereClause);

    }


    public String updateStatus(String e_pno, String pno, String status, String empty, String empty2) {

        String return_value = "";

        String query = "UPDATE " + DatabaseHelper.PETITIONS_TABLE + " SET " + DatabaseHelper.SENT_SUPPORT + " = 1"
                + " WHERE "
                + DatabaseHelper.E_PETITION_NUMBER_KEY + " = " + e_pno + " AND " + ""
                + DatabaseHelper.PETITION_NUMBER_KEY + " = '" + pno + "'";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            return_value = "No match";
        } else {
            return_value = String.valueOf(c.getCount());
        }

        return return_value;

    }


    public String getStatus(String petition_number) {

        String status = "";

        String query = "SELECT " + DatabaseHelper.SENT_SUPPORT + " FROM " + DatabaseHelper.PETITIONS_TABLE
                + " WHERE " + DatabaseHelper.PETITION_NUMBER_KEY + " = '" + petition_number + "'";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {

        } else {
            c.moveToFirst();

            status = c.getString(c.getColumnIndex(DatabaseHelper.SENT_SUPPORT));
        }

        return status;
    }


    public String getStatus(String member_id, String petition_number) {

        String status = "";

        String query = "SELECT " + DatabaseHelper.SENT_SUPPORT + " FROM " + DatabaseHelper.PETITIONS_TABLE
                + " WHERE " +
                DatabaseHelper.MEMBER_ID_KEY + " = " + member_id + " AND " + "" +
                DatabaseHelper.PETITION_NUMBER_KEY + " = '" + petition_number + "'";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            status = "No match";
        } else {
            c.moveToFirst();

            status = c.getString(c.getColumnIndex(DatabaseHelper.SENT_SUPPORT));
        }

        return status;
    }

    public List<ItemPetitionsTable> displayPetitionsTable() {

        List<ItemPetitionsTable> items = new ArrayList<ItemPetitionsTable>();

        ItemPetitionsTable item;

        String query = "SELECT * FROM " + DatabaseHelper.PETITIONS_TABLE;

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            items.clear();
        } else {
            c.moveToFirst();

            do {

                item = new ItemPetitionsTable();

                item.setPetition_type(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TYPE)));
                item.setE_petition_number(c.getString(c.getColumnIndex(DatabaseHelper.E_PETITION_NUMBER_KEY)));
                item.setPetition_number(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_NUMBER_KEY)));
                item.setMember_id(c.getString(c.getColumnIndex(DatabaseHelper.MEMBER_ID_KEY)));
                item.setPetition_title(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TITLE)));

                item.setMember_id_type(c.getString(c.getColumnIndex(DatabaseHelper.MEMBER_ID_TYPE_KEY)));
                item.setPetition_address(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_ADDRESS)));
                item.setOfficial_name(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_NAME)));
                item.setOfficial_designation(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_DESIGNATION)));
                item.setOffice_department_name(c.getString(c.getColumnIndex(DatabaseHelper.OFFICE_DEPARTMENT_NAME)));
                item.setOffice_address(c.getString(c.getColumnIndex(DatabaseHelper.OFFICE_ADDRESS)));
                item.setCity(c.getString(c.getColumnIndex(DatabaseHelper.CITY)));
                item.setState(c.getString(c.getColumnIndex(DatabaseHelper.STATE)));
                item.setDistrict(c.getString(c.getColumnIndex(DatabaseHelper.DISTRICT)));
                item.setPincode(c.getString(c.getColumnIndex(DatabaseHelper.PINCODE)));
                item.setOfficial_mobile(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_MOBILE)));
                item.setOfficial_email(c.getString(c.getColumnIndex(DatabaseHelper.OFFICIAL_EMAIL)));
                item.setPetition_title(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_TITLE)));
                item.setPetition_description(c.getString(c.getColumnIndex(DatabaseHelper.PETITION_DESCRIPTION)));
                item.setSms_matter(c.getString(c.getColumnIndex(DatabaseHelper.SMS_MATTER)));
               // item.setSms_matter(c.getString(c.getColumnIndex(DatabaseHelper.SMSMATTER)));
                item.setOtp(c.getString(c.getColumnIndex(DatabaseHelper.OTP)));
                item.setSms_send_or_not(c.getString(c.getColumnIndex(DatabaseHelper.SMS_SEND_OR_NOT)));
                item.setStatus(c.getString(c.getColumnIndex(DatabaseHelper.STATUS)));
                item.setDate(c.getString(c.getColumnIndex(DatabaseHelper.DATE)));
                item.setPetitioner_name(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_NAME)));
                item.setPetitioner_gender(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_GENDER)));
                item.setPetitioner_city(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_CITY)));
                item.setPetitioner_district(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_DISTRICT)));
                item.setPetitioner_state(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_STATE)));
                item.setPetitioner_pincode(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_PINCODE)));
                item.setPetitioner_mobile(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_MOBILE)));
                item.setPetitioner_email(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_EMAIL)));
                item.setLiked_or_not(c.getString(c.getColumnIndex(DatabaseHelper.LIKED_OR_NOT)));
                item.setLike_count(c.getString(c.getColumnIndex(DatabaseHelper.LIKE_COUNT)));
                item.setComment_posted_or_not(c.getString(c.getColumnIndex(DatabaseHelper.COMMENT_POSTED_OR_NOT)));
                item.setComments(c.getString(c.getColumnIndex(DatabaseHelper.COMMENTS)));
                item.setAttachments(c.getString(c.getColumnIndex(DatabaseHelper.ATTACHMENTS)));
                item.setSent_support(c.getString(c.getColumnIndex(DatabaseHelper.SENT_SUPPORT)));

                item.setLatitude(c.getString(c.getColumnIndex(DatabaseHelper.LATITUDE)));
                item.setLongitude(c.getString(c.getColumnIndex(DatabaseHelper.LONGITUDE)));
                item.setPetitioner_profile_image_url(c.getString(c.getColumnIndex(DatabaseHelper.PETITIONER_PROFILE_IMAGE_URL)));

                items.add(item);

            } while (c.moveToNext());

        }

        return items;

    }

    public void clearTable() {
        super.clearTable(DatabaseHelper.PETITIONS_TABLE);
    }


    public void updateCommentCheck(String e_pno) {

        ContentValues values = new ContentValues();
        values.put(DatabaseHelper.COMMENT_POSTED_OR_NOT, "1");

        String where = DatabaseHelper.E_PETITION_NUMBER_KEY + " = '" + e_pno + "'";

        super.updateRow(DatabaseHelper.PETITIONS_TABLE, values, where);
    }


    public boolean canComment(String e_pno) {

        boolean canComment = true;

        String query = "SELECT " + DatabaseHelper.COMMENT_POSTED_OR_NOT + " FROM " + DatabaseHelper.PETITIONS_TABLE
                + " WHERE " +
                DatabaseHelper.E_PETITION_NUMBER_KEY + " = '" + e_pno + "'";

        c = super.query(query);

        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
            canComment = false;
        } else {
            c.moveToFirst();

            if (c.getString(c.getColumnIndex(DatabaseHelper.COMMENT_POSTED_OR_NOT)).equalsIgnoreCase("0"))
                canComment = true;
            else
                canComment = false;
        }

        return canComment;
    }


//    public ArrayList<String> getFavouritePetitions() {
//
//        ArrayList<String> mAlFavourites = new ArrayList<>();
//
//        String query = "SELECT " + DatabaseHelper.E_PETITION_NUMBER_KEY + " FROM " + DatabaseHelper.PETITIONS_TABLE
//                + " WHERE " +
//                DatabaseHelper.IS_FAVOURITE + " = 1";
//
//        c = super.query(query);
//
//        if ((c.equals(null) || c.getCount() == 0 || !c.moveToFirst())) {
//            mAlFavourites.clear();
//        } else {
//            c.moveToFirst();
//
//            do {
//
//                String e_pno = c.getString(c.getColumnIndex(DatabaseHelper.E_PETITION_NUMBER_KEY));
//
//                if (!mAlFavourites.contains(e_pno))
//                    mAlFavourites.add(e_pno);
//
//            } while (c.moveToNext());
//
//        }
//
//        return mAlFavourites;
//    }

}
