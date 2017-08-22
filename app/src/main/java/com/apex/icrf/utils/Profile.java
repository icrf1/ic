package com.apex.icrf.utils;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.apex.icrf.Const;

/**
 * Created by WASPVamsi on 10/09/15.
 */
public class Profile {

    SharedPreferences prefs;

    public Profile(Activity activity) {
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
    }

    public void setPreferences(String member_id, String name, String user_id, String mobile, String email, String member_id_type, String profile_image) {
        prefs.edit().putString(Const.Login.USER_MEMBER_ID, member_id).apply();
        prefs.edit().putString(Const.Login.USER_NAME, name)
                .apply();
        prefs.edit().putString(Const.Login.USER_ID, user_id)
                .apply();
        prefs.edit().putString(Const.Login.USER_MOBILE, mobile)
                .apply();
        prefs.edit().putString(Const.Login.USER_EMAIL, email)
                .apply();
        prefs.edit().putString(Const.Login.USER_MEMBER_ID_TYPE, member_id_type).apply();
        prefs.edit().putString(Const.Login.USER_PROFILE_IMAGE, profile_image).apply();
    }


    public void removePreferences() {

        prefs.edit().remove(Const.Login.USER_MEMBER_ID).apply();
        prefs.edit().remove(Const.Login.USER_NAME).apply();
        prefs.edit().remove(Const.Login.USER_ID).apply();
        prefs.edit().remove(Const.Login.USER_MOBILE).apply();
        prefs.edit().remove(Const.Login.USER_EMAIL).apply();
        prefs.edit().remove(Const.Login.USER_MEMBER_ID_TYPE).apply();
        prefs.edit().remove(Const.Login.USER_PROFILE_IMAGE).apply();
    }

    public boolean isUserLoggedIn() {
        return (prefs.contains(Const.Login.USER_ID) || prefs.contains(Const.Login.USER_NAME));
    }


    public String getUserId() {
        return prefs.getString(Const.Login.USER_ID, "");
    }

    public String getMemberId() {
        return prefs.getString(Const.Login.USER_MEMBER_ID, "");
    }

    public String getUserName() {
        return prefs.getString(Const.Login.USER_NAME, "");
    }

    public String getUserMobile() {
        return prefs.getString(Const.Login.USER_MOBILE, "");
    }

    public String getUserEmail() {
        return prefs.getString(Const.Login.USER_EMAIL, "");
    }

    public String getMemberIdType() {
        return prefs.getString(Const.Login.USER_MEMBER_ID_TYPE, "");
    }

    public String getProfileImage() {
        return prefs.getString(Const.Login.USER_PROFILE_IMAGE, "");
    }


}
