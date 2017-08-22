package com.apex.icrf;

/**
 * Created by WASPVamsi on 02/09/15.
 */
public class Const {

    public static final boolean DEBUGGING = false;
    public static final boolean DEBUGGING_DB = false;
    public static final String DEBUG = "ICRF";

    public static String VOLLEY_TAG = "tag";
    public static int VOLLEY_TIME_OUT = 30000;
    public static long REFRESH_TIME = 1000L * 60L * 60L;

    public static String APP_INSTALL_LINK = "Install app @ goo.gl/a8dH3K";


    public static final String STATUSBAR_BACKGROUND_COLOR = "#0076BD";
    public static final String TOOLBAR_BACKGROUND_COLOR = "#0076BD";

    public static final String BASE_URL = "http://www.icrf.org.in/Icrf_Android_Service.asmx/";
    public static final String BASE_URL_2 = "http://www.icrf.org.in/test3.asmx/";

    public static final String CURRENT_URL = "http://www.icrf.org.in/apex_icrf_android_api.asmx/";
    public static final String TEST_URL = "http://icrf.org.in/apex_icrf_android_api_2.asmx/";

    public static final String FINAL_URL = CURRENT_URL;

    public static final String DEFAULT_IMAGE_URL = "http://icrf.org.in/Attachment/e-petition_img.jpg";

    public static final String APPEND_URL = "?";
    public static final String TYPE_NEW = "type=new";
    public static final String TYPE_POPULAR = "type=popular";
    public static final String TYPE_SUCCESS = "type=success";
    public static final String TYPE_ALL = "type=all";
    //public static final String TYPE_VERIFIED_PETITIONS_BY_ME = "";


    public static final String ID_CARD_BACKGROUND_COLOR = "#0C97E4";

    public static class Prefs {

        public static final String CURRENT_LEFT_NAV_LIST_POSITION = "current_left_nav_list_position";
        public static final String MY_TOTAL_POINTS = "my_total_points";

        public static final String POPULAR_PETITIONS_REFRESH_TIME = "popular_petitions_refresh_time";
        public static final String NEW_PETITIONS_REFRESH_TIME = "new_petitions_refresh_time";
        public static final String VICTORY_PETITIONS_REFRESH_TIME = "victory_petitions_refresh_time";
        public static final String ALL_PETITIONS_REFRESH_TIME = "all_petitions_refresh_time";
        public static final String UNVERIFIED_PETITIONS_REFRESH_TIME = "unverified_petitions_refresh_time";
        public static final String MY_TOTAL_POINTS_REFRESH_TIME = "my_total_points_refresh_time";
        public static final String VERIFIED_PETITIONS_BY_ME_REFRESH_TIME = "verified_petitions_by_me_refresh_time";
        public static final String SUPPORTED_PETITIONS_BY_ME_REFRESH_TIME = "verified_petitions_by_me_refresh_time";

        public static final String CURRENT_GROUP_NAME = "current_group_name";

        public static final String NEW_PETITIONS_PAGE_INDEX = "new_petitions_page_index";
        public static final String POPULAR_PETITIONS_PAGE_INDEX = "popular_petitions_page_index";
        public static final String VICTORY_PETITIONS_PAGE_INDEX = "victory_petitions_page_index";
        public static final String ALL_PETITIONS_PAGE_INDEX = "all_petitions_page_index";
        public static final String UNVERIFIED_PETITIONS_PAGE_INDEX = "unverified_page_index";
        public static final String VERIFIED_PETITIONS_BY_ME_PAGE_INDEX = "verified_petitions_by_me_page_index";
        public static final String SUPPORTED_PETITIONS_BY_ME_PAGE_INDEX = "supported_petitions_by_me_page_index";

        public static final String IS_INTRO_SHOWN = "is_intro_shown";
        public static final String OTP_VERIFIED = "otp_verified";
        //public static final String IS_HOME_SCREEN = "is_home_screen";

        public static final String REGISTRATION_OTP_VERIFIED = "registration_otp_verified";

        public static final String CURRENT_SCREEN = "current_screen";
        public static final int IS_HOME_SCREEN = 0;
        public static final int IS_MY_ACTIVITY_SCREEN = 1;
        public static final int IS_SUCCESS_PETITIONS_SCREEN = 2;

        public static final String RATE_US_SHOWN = "rate_us_shown";
        public static final String RATE_US_DONT_SHOW = "rate_us_dont_show";
        public static final String RATE_US_TIMER = "rate_us_timer";
        public static final String SHARE_US_SHOWN = "share_us_shown";
        public static final String SHARE_US_DONT_SHOW = "share_us_dont_show";
        public static final String SHARE_US_TIMER = "share_us_timer";

    }

    public static class NavLeftPositions {

        public static int HOME_POSITION = 1;
        //public static int IDCARD_POSITION = 2;
        public static int VERFITY_MY_PETITIONS_POSITION = 2;
        public static int VERIFIED_PETIIONS_BY_ME = 3;
        public static int VERIFIED_PETITIONS_POSITION = 4;
        public static int POST_PETITION_POSITION = 5;
        public static int DONATE_POSITION = 6;
        public static int IDCARD_POSITION = 7;
    }

    public static class Bundle {

        public static final String FROM_FRAGMENT = "from_fragment";
        public static final String POSITION = "position";


        public static final int VERIFY_MY_PETITION_FRAGMENT = 1;
        public static final int VERIFIED_PETITION_FRAGMENT = 2;
        public static final int POPULAR_PETITIONS_FRAGMENT = 3;
        public static final int NEW_PETITIONS_FRAGMENT = 4;
        public static final int VICTORY_PETITIONS_FRAGMENT = 5;
        public static final int MY_TOTAL_POINTS_FRAGMENT = 6;
        public static final int DETAIL_VERIFY_MY_PETITION_FRAGMENT = 7;
        public static final int DETAIL_VERIFIED_PETITION_FRAGMENT = 8;
        public static final int MAIN_VERIFIED_BY_ME_PETITON_FRAGMENT = 11;
        public static final int MAIN_SUPPORTED_BY_ME_PETITON_FRAGMENT = 12;
        public static final int MAIN_FAVOURITE_PETITION_FRAGMENT = 13;

        public static final String E_PETITION_NUMBER = "e_petition_number";
        public static final String PETITION_NUMBER = "petition_number";
        public static final String PETITION_TITLE = "petition_title";

        public static final String CURRENT_GROUP_NAME = "current_group_name";
        public static final String GROUP_CATEGORY = "group_category";
        public static final String GROUP_NAME = "group_name";
        public static final int CREATE_GROUP_FRAGMENT = 9;
        public static final int GROUP_MEMBERS = 10;
    }


    public static final class Login {

        public static final String USER_DISPLAY_NAME = "display_name";
        public static final String USER_FIRST_NAME = "first_name";
        public static final String USER_LAST_NAME = "last_name";

        public static final String LOGGEDOUT = "Logged out";
        public static final String LOGGEDIN = "Logged in";
        public static final String NOT_LOGGED_IN = "Tap to Login";
        public static final String LOGIN_FAILED = "Login Failed. ";

        public static final String HEADER_EDITABLE = "header_editable";

        public static final String USER_MEMBER_ID = "member_id";
        public static final String USER_NAME = "name";
        public static final String USER_ID = "user_id";
        public static final String USER_MOBILE = "mobile";
        public static final String USER_EMAIL = "email";
        public static final String USER_MEMBER_ID_TYPE = "memberid_type";
        public static final String USER_PROFILE_IMAGE = "profileImage";
    }

    public static final class URLs {

        public static final String LOGIN_CHECK = "LoginCheck" + APPEND_URL;
        public static final String MY_TOTAL_POINTS = "MyTotalPoints" + APPEND_URL;
        public static final String SHOW_PETITION_TITLES = "ShowPetitionTitles" + APPEND_URL;
        public static final String UNVERIFIED_PETITION_TITLES = "UnverifiedPetitionTitles" + APPEND_URL;
        public static final String VIEW_DETAIL_PETITIONS = "ViewDetailPetitions" + APPEND_URL;
        public static final String VERIFY_PETITION = "VerifyPetition" + APPEND_URL;
        public static final String CHECK_SUPPORT_ENABLE_MAX_SMS_REACH = "checkSupportenable_MaxSMSReach" + APPEND_URL;
        public static final String GIVE_SUPPORT = "GiveSupport" + APPEND_URL;
        public static final String STORE_PTS_DRS = "Store_Pts_Drs" + APPEND_URL;
        public static final String CHECK_LIKED_OR_NOT = "CheckLikedOrNot" + APPEND_URL;
        public static final String GIVE_LIKE = "GiveLike" + APPEND_URL;
        public static final String TOTAL_POINTS_REPORTS = "TotalPointsReports" + APPEND_URL;
        public static final String SAVE_IMAGE = "SaveImage" /*+ APPEND_URL*/;
        public static final String GET_PETITION_ATTACHMENTS = "Get_Petition_Attachments" + APPEND_URL;
        public static final String CHECK_COMMENTED_OR_NOT_THEN_DISABLE = "Check_Commented_Or_not_Then_Disable" + APPEND_URL;
        public static final String GET_COMMENTS = "Get_Commnets" + APPEND_URL;
        public static final String VERIFIED_PETITIONS_BY_ME = "Get_Reports_petitions_verified_by_me" + APPEND_URL;
        public static final String POST_COMMENT_TO_PETITION = "PostComment_to_Petition" + APPEND_URL;
        public static final String SAVE_IMAGE_2 = "SaveImage2" + APPEND_URL;

        public static final String SHOW_PETITION_TITLES_PAGE_WISE = "ShowPetitionsTitlesPageWise" + APPEND_URL;

        public static final String ALL_PETITIONS = "All_Petitions" + APPEND_URL;
        public static final String LOGIN_DETAILS = "LoginDetails" + APPEND_URL;
        public static final String MY_EARNINGS_REPORT = "MyEarnings_Report" + APPEND_URL;
        public static final String GET_SUCCESS_PETITION_DETAILS = "get_success_petition_details" + APPEND_URL;

        public static final String FORGOT_PASSWORD = "ForgotPassword" + APPEND_URL;
        public static final String TAKE_FEEDBACK = "Take_Feedback" + APPEND_URL;
        public static final String SEND_OTP = "Send_OTP" + APPEND_URL;
        public static final String READ_OTP = "Read_OTP" + APPEND_URL;

        public static final String CHECK_APP_VERSION_UPDATES = "chk_app_version_updates";
        public static final String STORE_APP_USERS_TOKENS = "Store_App_users_Tokens" + APPEND_URL;
        public static final String UPDATE_BANK_DETAILS = "Update_Bank_details" + APPEND_URL;

        public static final String NEW_REGISTRATION_CHECKING = "new_register_checking" + APPEND_URL;
        public static final String NEW_REGISTRATION = "new_register" + APPEND_URL;
        public static final String CHECK_REFERRAL_ID = "check_refferal_id" + APPEND_URL;

        public static final String GET_DATA_BY_PINCODE = "get_data_by_pincode" + APPEND_URL;

        public static final String SEND_REGISTRATION_OTP = "Send_Regi_OTP" + APPEND_URL;
        public static final String READ_REGISTRATION_OTP = "Read_Regi_OTP" + APPEND_URL;

        public static final String CHECK_ID_UPGRADE_OR_NOT = "chk_ID_upgrade_or_not" + APPEND_URL;
//        public static final String E_PETITION_GUIDELINES_APP = "http://www.icrf.org.in/member/" +
//                "E_Petition_guidelines_app.aspx?";
        public static final String E_PETITION_GUIDELINES_APP = "http://www.icrf.org.in/member/app_e_petition_guidelines.aspx?";

        public static final String ID_CARD_URL = "http://www.icrf.org.in/member/app_profile_pic.aspx?";

        public static final String ID_CARD_PROFILE_PIC = "get_profile_pic_image" + APPEND_URL;

        public static final String NOTICE_BOARD_NEWS = "NoticeBoardNews" + APPEND_URL;
    }


    public static final class PETITION_DETAILS {

        // Petition Details
        public static final String EPETNO = "epetno";
        public static final String MEMBER_ID = "memberid";
        public static final String PETITION_ADDRESS = "petitionAddress";
        public static final String OFFICIAL_NAME = "official_name";
        public static final String OFFICIAL_DESIGNATION = "official_designation";
        public static final String OFFICE_DEPARTMENT_NAME = "office_dep_name";
        public static final String OFFICE_ADDRESS = "officeAdress";
        public static final String CITY = "city";
        public static final String STATE = "state";
        public static final String DISTRICT = "dist";
        public static final String PIN_CODE = "pincode";
        public static final String OFFICIAL_MOBILE = "official_mobile";
        public static final String OFFICIAL_EMAIL = "official_email";
        public static final String PETITION_TITLE = "petition_sublect";
        public static final String PETITION_DESCRIPTION = "petition";
        public static final String SMS_MATTER = "smsmatter";
        public static final String PETITION_NUMBER = "petition_num";
        public static final String OTP = "otp";
        public static final String SMS_SEND_OR_NOT = "sms_send_or_not";
        public static final String STATUS = "stat";
        public static final String DATE = "dt";
        public static final String MEMBER_ID_TYPE = "memberid_type";

        // Petition Posted By
        public static final String PETITIONER_NAME = "petitioner_name";
        public static final String PETITIONER_GENDER = "petitioner_gender";
        public static final String PETITIONER_CITY = "petitioner_city";
        public static final String PETITIONER_DISTRICT = "petitioner_dist";
        public static final String PETITIONER_STATE = "petitioner_state";
        public static final String PETITIONER_PINCODE = "petitioner_pincode";
        public static final String PETITIONER_MOBILE = "petitioner_mobile";
        public static final String PETITIONER_EMAIL = "petitioner_email";

        public static final String LATITUDE = "latitude";
        public static final String LONGITUDE = "langitude";
        public static final String PETITIONER_PROFILE_IMAGE_URL = "profile_img_url";


    }

    public static final class MENULIST {

        public static final int HOME = 0;
        public static final int MY_ACTIVITY = 1;
        public static final int POST_NEW_PETITION = 2;
        public static final int SUCCESS_PETITIONS = 3;
        public static final int NOTIFICATIONS = 4;
        public static final int DONATE = 5;
        public static final int MY_EARNINGS = 6;
        public static final int MORE = 7;
        public static final int ID_CARD = 8;
        public static final int MY_POINTS = 10;


        public static final int MY_ACTIVITY_PETITIONS_VERIFIED_BY_ME = 0;
        public static final int MY_ACTIVITY_PETITIONS_SUPPORTED_BY_ME = 1;

        public static final int BANK_DETAILS = 0;
        public static final int MORE_RATE_US = 1;
        public static final int MORE_FEEDBACK = 2;
        public static final int MORE_INVITE = 3;
        public static final int MORE_ABOUT_ICRF = 4;
        public static final int MORE_HOW_IT_WORKS = 5;
        public static final int MORE_CHECK_UPDATE = 6;

        public static final int NOTIFICATIONS_NOTICE_BOARD = 0;
        public static final int NOTIFICATIONS_NEWS = 1;



    }


    public static final class VIEWPAGER {

        public static final String VIEW_TYPE = "view_type";
        public static final String LIST_VIEW = "list";
        public static final String GRID_VIEW = "grid";
        public static final String MINI_VIEW = "mini";
    }

}
