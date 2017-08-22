package com.apex.icrf.classes;

/**
 * Created by WASPVamsi on 29/09/15.
 */
public interface IDetailVerifyMyPetitionsListener {

    void onContactsButtonClicked(String sms_text);
    void onGroupsButtonClicked(String sms_text);

    void onScrolled(boolean hide);

}
