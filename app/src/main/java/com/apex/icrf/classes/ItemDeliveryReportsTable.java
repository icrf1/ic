package com.apex.icrf.classes;

/**
 * Created by WASPVamsi on 20/09/15.
 */
public class ItemDeliveryReportsTable {

    public String e_petition_number;
    public String petition_number;
    public String sent_from;
    public String sent_to;
    public String sms_content;
    public int sent_sms_success;
    public int deliver_sms_success;
    public int synced;
    public String member_id;
    public String member_id_type;

    public String confirmation_message;

    public String getConfirmation_message() {
        return confirmation_message;
    }

    public void setConfirmation_message(String confirmation_message) {
        this.confirmation_message = confirmation_message;
    }


    public String getMember_id_type() {
        return member_id_type;
    }

    public void setMember_id_type(String member_id_type) {
        this.member_id_type = member_id_type;
    }

    public String getMember_id() {
        return member_id;
    }

    public void setMember_id(String member_id) {
        this.member_id = member_id;
    }


    public String getE_petition_number() {
        return e_petition_number;
    }

    public void setE_petition_number(String e_petition_number) {
        this.e_petition_number = e_petition_number;
    }

    public String getPetition_number() {
        return petition_number;
    }

    public void setPetition_number(String petition_number) {
        this.petition_number = petition_number;
    }

    public void setSms_content(String sms_content) {
        this.sms_content = sms_content;
    }

    public String getSms_content() {
        return sms_content;
    }


    public String getSent_from() {
        return sent_from;
    }

    public void setSent_from(String sent_from) {
        this.sent_from = sent_from;
    }

    public String getSent_to() {
        return sent_to;
    }

    public void setSent_to(String sent_to) {
        this.sent_to = sent_to;
    }

    public int getSent_sms_success() {
        return sent_sms_success;
    }

    public void setSent_sms_success(int sent_sms_success) {
        this.sent_sms_success = sent_sms_success;
    }

    public int getDeliver_sms_success() {
        return deliver_sms_success;
    }

    public void setDeliver_sms_success(int deliver_sms_success) {
        this.deliver_sms_success = deliver_sms_success;
    }

    public int getSynced() {
        return synced;
    }

    public void setSynced(int synced) {
        this.synced = synced;
    }

}
