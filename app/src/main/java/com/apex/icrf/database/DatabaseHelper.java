package com.apex.icrf.database;

import android.content.Context;
import android.content.SharedPreferences;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import com.apex.icrf.Const;
import com.apex.icrf.utils.Profile;

public class DatabaseHelper extends SQLiteOpenHelper {
    public static final String DATABASE_HELPER_SERVICE = "database_helper_service";

    private static final String DATABASE_NAME = "apexdb";
    private static final int DATABASE_VERSION = 5;
    public static String SMSMATTER;
    private Context context;
    private Profile mProfile;
    private SQLiteDatabase mDb;

    public static final String DELIVERY_REPORTS_TABLE = "delivery_reports_table";
    public static final String USER_PROFILE_TABLE = "user_profile_table";
    public static final String PETITIONS_TABLE = "petitions_table";
    public static final String GROUPS_TABLE = "groups_table";
    public static final String PUSH_NOTIFICATIONS_TABLE = "push_notifications_table";
    public static final String FAVOURITES_TABLE = "favourites_table";
    public static final String LOCATIONS_TABLE = "locations_table";

    public static final String PETITION_TYPE_POPULAR = "popular";
    public static final String PETITION_TYPE_NEW = "new";
    public static final String PETITION_TYPE_VICTORY = "success";
    public static final String PETITION_TYPE_ALL = "all";
    public static final String PETITION_TYPE_SUCCESS = "success";
    public static final String PETITION_TYPE_UNVERIFIED = "unverified";
    public static final String PETITION_TYPE_VERIFIED_BY_ME = "verified_by_me";
    public static final String PETITION_TYPE_SUPPORTED_BY_ME = "supported_by_me";

    // COMMON TABLE COLUMNS
    public static final String SNO = "_sno";
    public static final String MEMBER_ID_KEY = "member_id";
    public static final String E_PETITION_NUMBER_KEY = "e_petition_number";
    public static final String PETITION_NUMBER_KEY = "petition_number";
    public static final String MEMBER_ID_TYPE_KEY = "member_id_type";

    // DELIVERY REPORTS TABLE
    public static final String SENT_FROM = "sent_from";
    public static final String SENT_TO = "sent_to";
    public static final String SMS_CONTENT = "sms_content";
    public static final String CONFIRMATION_MESSAGE_CONTENT = "confirmation_content";
    public static final String SENT_SMS_SUCCESS = "sent_sms_success";
    public static final String DELIVER_SMS_SUCCESS = "deliver_sms_success";
    public static final String SYNCED = "synced";

    // USER PROFILE TABLE
    public static final String NAME = "name";
    public static final String PHONE_NUMBER = "phone_number";
    public static final String EMAIL = "email";
    public static final String USER_ID = "user_id";
    public static final String PHOTO_URL = "photo_url";
    public static final String ADDRESS = "address";

    // PETITION TABLE
    public static final String PETITION_TYPE = "petition_type";
    public static final String PETITION_ADDRESS = "petition_address";
    public static final String OFFICIAL_NAME = "official_name";
    public static final String OFFICIAL_DESIGNATION = "official_designation";
    public static final String OFFICE_DEPARTMENT_NAME = "office_department_name";
    public static final String OFFICE_ADDRESS = "office_address";
    public static final String CITY = "city";
    public static final String STATE = "state";
    public static final String DISTRICT = "district";
    public static final String PINCODE = "pincode";
    public static final String OFFICIAL_MOBILE = "official_mobile";
    public static final String OFFICIAL_EMAIL = "official_email";
    public static final String PETITION_TITLE = "petition_title";
    public static final String PETITION_DESCRIPTION = "petition_description";
    public static final String SMS_MATTER = "sms_matter";
    public static final String OTP = "otp";
    public static final String SMS_SEND_OR_NOT = "sms_send_or_not";
    public static final String STATUS = "status";
    public static final String DATE = "date";
    public static final String PETITIONER_NAME = "petitioner_name";
    public static final String PETITIONER_GENDER = "petitioner_gender";
    public static final String PETITIONER_CITY = "petitioner_city";
    public static final String PETITIONER_DISTRICT = "petitioner_district";
    public static final String PETITIONER_STATE = "petitioner_state";
    public static final String PETITIONER_PINCODE = "petitioner_pincode";
    public static final String PETITIONER_MOBILE = "petitioner_mobile";
    public static final String PETITIONER_EMAIL = "petitioner_email";

    public static final String LIKED_OR_NOT = "liked_or_not";
    public static final String LIKE_COUNT = "like_count";
    public static final String COMMENT_POSTED_OR_NOT = "comment_posted_or_not";
    public static final String COMMENTS_COUNT = "comments_count";
    public static final String SUPPORTS_COUNT = "supports_count";

    public static final String LATITUDE = "latitude";
    public static final String LONGITUDE = "langitude";
    public static final String PETITIONER_PROFILE_IMAGE_URL = "profile_img_url";

    // JSON Array is stored as String
    public static final String COMMENTS = "comments";

    // JSON Array is stored as String
    public static final String ATTACHMENTS = "attachments";

    public static final String SENT_SUPPORT = "sent_support";
    //public static final String IS_FAVOURITE = "is_favourite";

    // GROUPS TABLE
    public static final String GROUP_NAME = "group_name";

    // PUSH NOTIFICATIONS TABLE
    public static final String PUSH_IMAGE = "push_image";
    public static final String PUSH_MESSAGE = "push_message";

    // LOCATIONS TABLE
    public static final String STATE_NAME = "state";
    public static final String DISTRICT_NAME = "district";
    public static final String CITY_NAME = "city";
    public static final String PIN_CODE = "pin_code";
    public static final String STATE_ID = "state_id";
    public static final String DISTRICT_ID = "district_id";


    public static final String DATABASE_TABLE_CREATE_DELIVERY_REPORTS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + DELIVERY_REPORTS_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MEMBER_ID_KEY
            + " TEXT, "
            + MEMBER_ID_TYPE_KEY
            + " TEXT, "
            + E_PETITION_NUMBER_KEY
            + " TEXT, "
            + PETITION_NUMBER_KEY
            + " TEXT, "
            + SENT_FROM
            + " TEXT, "
            + SENT_TO
            + " TEXT, "
            + SMS_CONTENT
            + " TEXT, "
            + CONFIRMATION_MESSAGE_CONTENT
            + " TEXT, "
            + SENT_SMS_SUCCESS
            + " INTEGER, "
            + DELIVER_SMS_SUCCESS
            + " INTEGER, "
            + SYNCED
            + " INTEGER" + ")";

    public static final String DATABASE_TABLE_CREATE_USER_PROFILE_TABLE = "CREATE TABLE IF NOT EXISTS "
            + USER_PROFILE_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + USER_ID
            + " TEXT, "
            + MEMBER_ID_KEY
            + " TEXT, "
            + MEMBER_ID_TYPE_KEY
            + " TEXT, "
            + NAME
            + " TEXT, "
            + PHONE_NUMBER
            + " TEXT, "
            + EMAIL
            + " TEXT, "
            + ADDRESS
            + " TEXT, "
            + PHOTO_URL
            + " TEXT" + ")";

    public static final String DATABASE_TABLE_CREATE_PETITION_TABLE = "CREATE TABLE IF NOT EXISTS "
            + PETITIONS_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PETITION_TYPE
            + " TEXT, "
            + E_PETITION_NUMBER_KEY
            + " TEXT, "
            + MEMBER_ID_KEY
            + " TEXT, "
            + PETITION_ADDRESS
            + " TEXT, "
            + OFFICIAL_NAME
            + " TEXT, "
            + OFFICIAL_DESIGNATION
            + " TEXT, "
            + OFFICE_DEPARTMENT_NAME
            + " TEXT, "
            + OFFICE_ADDRESS
            + " TEXT, "
            + CITY
            + " TEXT, "
            + STATE
            + " TEXT, "
            + DISTRICT
            + " TEXT, "
            + PINCODE
            + " TEXT, "
            + OFFICIAL_MOBILE
            + " TEXT, "
            + OFFICIAL_EMAIL
            + " TEXT, "
            + PETITION_TITLE
            + " TEXT, "
            + PETITION_DESCRIPTION
            + " TEXT, "
            + SMS_MATTER
            + " TEXT, "
            + PETITION_NUMBER_KEY
            + " TEXT, "
            + OTP
            + " TEXT, "
            + SMS_SEND_OR_NOT
            + " TEXT, "
            + STATUS
            + " TEXT, "
            + DATE
            + " TEXT, "
            + MEMBER_ID_TYPE_KEY
            + " TEXT, "
            + PETITIONER_NAME
            + " TEXT, "
            + PETITIONER_GENDER
            + " TEXT, "
            + PETITIONER_CITY
            + " TEXT, "
            + PETITIONER_DISTRICT
            + " TEXT, "
            + PETITIONER_STATE
            + " TEXT, "
            + PETITIONER_PINCODE
            + " TEXT, "
            + PETITIONER_MOBILE
            + " TEXT, "
            + PETITIONER_EMAIL
            + " TEXT, "
            + LIKED_OR_NOT
            + " TEXT, "
            + LIKE_COUNT
            + " TEXT, "
            + COMMENT_POSTED_OR_NOT
            + " TEXT, "
            + COMMENTS
            + " TEXT, "
            + COMMENTS_COUNT
            + " TEXT, "
            + SUPPORTS_COUNT
            + " TEXT, "
            + ATTACHMENTS
            + " TEXT, "
            + SENT_SUPPORT
            + " TEXT, "
            + LATITUDE
            + " TEXT, "
            + LONGITUDE
            + " TEXT, "
            + PETITIONER_PROFILE_IMAGE_URL
            + " TEXT"

            + ")";


    public static final String DATABASE_TABLE_CREATE_GROUPS = "CREATE TABLE IF NOT EXISTS "
            + GROUPS_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + MEMBER_ID_KEY
            + " TEXT, "
            + NAME
            + " TEXT, "
            + PHONE_NUMBER
            + " TEXT, "
            + GROUP_NAME
            + " TEXT" + ")";


    public static final String DATABASE_TABLE_CREATE_PUSH_NOTIFICATIONS = "CREATE TABLE IF NOT EXISTS "
            + PUSH_NOTIFICATIONS_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + PUSH_IMAGE
            + " TEXT, "
            + PUSH_MESSAGE
            + " TEXT" + ")";


    public static final String DATABASE_TABLE_CREATE_FAVOURITES = "CREATE TABLE IF NOT EXISTS "
            + FAVOURITES_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + E_PETITION_NUMBER_KEY
            + " TEXT" + ")";

    public static final String DATABASE_TABLE_CREATE_LOCATIONS_TABLE = "CREATE TABLE IF NOT EXISTS "
            + LOCATIONS_TABLE
            + "("
            + SNO
            + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + STATE_NAME
            + " TEXT, "
            + DISTRICT_NAME
            + " TEXT, "
            + CITY_NAME
            + " TEXT, "
            + STATE_ID
            + " INTEGER, "
            + DISTRICT_ID
            + " INTEGER, "
            + PIN_CODE
            + " TEXT" + ")";


    public DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter;
    public PetitionsTableDbAdapter mPetitionsTableDbAdapter;
    public GroupsTableDbAdapter mGroupsTableDbAdapter;
    public PushNotificationsTableDbAdapter mPushNotificationsTableDbAdapter;
    public FavouritesTableDbAdapter mFavouritesTableDbAdapter;
    public LocationsTableDbAdapter mLocationsTableDbAdapter;


    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    public void initialize() {
        DatabaseHelper dbHelper = get(context);
        mDb = dbHelper.getWritableDatabase();

        BaseDbAdapter.setWritableDatabase(mDb);

        mDeliveryReportsTableDbAdapter = new DeliveryReportsTableDbAdapter();
        mPetitionsTableDbAdapter = new PetitionsTableDbAdapter();
        mGroupsTableDbAdapter = new GroupsTableDbAdapter();
        mPushNotificationsTableDbAdapter = new PushNotificationsTableDbAdapter();
        mFavouritesTableDbAdapter = new FavouritesTableDbAdapter();
        mLocationsTableDbAdapter = new LocationsTableDbAdapter();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public synchronized void close() {
        mDb.close();
        super.close();
    }

    public static DatabaseHelper get(Context context) {
        DatabaseHelper mDbHelper = (DatabaseHelper) context
                .getSystemService(DATABASE_HELPER_SERVICE);
        if (mDbHelper == null) {
            context = context.getApplicationContext();
            mDbHelper = (DatabaseHelper) context
                    .getSystemService(DATABASE_HELPER_SERVICE);
        }
        if (mDbHelper == null) {
            throw new IllegalStateException("Database not available");
        }
        return mDbHelper;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "onUpgrade()");

//        if (oldVersion == 1) {
//            if (newVersion == 2 || newVersion == 3 || newVersion == 4) {
//
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//                prefs.edit().clear().commit();
//
//                dropTables(db);
//                createTables(db);
//            }
//        } else if(oldVersion == 2) {
//            if(newVersion == 3 || newVersion == 4) {
//
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//                prefs.edit().clear().commit();
//
//                dropTables(db);
//                createTables(db);
//            }
//        } else if(oldVersion == 3) {
//            if(newVersion == 4) {
//
//                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//
//                prefs.edit().clear().commit();
//
//                dropTables(db);
//                createTables(db);
//            }
//        } else if(oldVersion == 4) {
//            if(newVersion == 5) {
//
//            }
//        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        prefs.edit().clear().apply();

        dropTables(db);
        createTables(db);
    }


    private void dropTables(SQLiteDatabase db) {

        db.execSQL("DROP TABLE IF EXISTS " + USER_PROFILE_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + DELIVERY_REPORTS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PETITIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + GROUPS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + PUSH_NOTIFICATIONS_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + FAVOURITES_TABLE);
        db.execSQL("DROP TABLE IF EXISTS " + LOCATIONS_TABLE);
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL(DATABASE_TABLE_CREATE_DELIVERY_REPORTS_TABLE);
        db.execSQL(DATABASE_TABLE_CREATE_USER_PROFILE_TABLE);
        db.execSQL(DATABASE_TABLE_CREATE_PETITION_TABLE);
        db.execSQL(DATABASE_TABLE_CREATE_GROUPS);
        db.execSQL(DATABASE_TABLE_CREATE_PUSH_NOTIFICATIONS);
        db.execSQL(DATABASE_TABLE_CREATE_FAVOURITES);
        db.execSQL(DATABASE_TABLE_CREATE_LOCATIONS_TABLE);
    }

    public DeliveryReportsTableDbAdapter getDeliveryReportsTableDbAdapter() {
        return mDeliveryReportsTableDbAdapter;
    }

    public PetitionsTableDbAdapter getPetitionsTableDbAdapter() {
        return mPetitionsTableDbAdapter;
    }

    public GroupsTableDbAdapter getGroupsTableDbAdapter() {
        return mGroupsTableDbAdapter;
    }

    public PushNotificationsTableDbAdapter getPushNotificationsTableDbAdapter() {
        return mPushNotificationsTableDbAdapter;
    }

    public FavouritesTableDbAdapter getFavouritesTableDbAdapter() {
        return mFavouritesTableDbAdapter;
    }

    public LocationsTableDbAdapter getLocationsTabletDbAdapter() {
        return mLocationsTableDbAdapter;
    }
}
