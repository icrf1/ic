package com.apex.icrf;

/**
 * Created by WASPVamsi on 04/09/15.
 */

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.apex.icrf.classes.IMyPostsListener;
import com.apex.icrf.classes.IOnLoadMoreListener;
import com.apex.icrf.classes.ItemPetitionsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.PetitionsTableDbAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.TypeFaceHelper;
import com.apex.icrf.utils.Utils;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;


public class MainMyPostsViewPagerFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, SearchView.OnQueryTextListener {

    private Activity activity;
    private SharedPreferences prefs;
    private Profile mProfile;

    private SearchView searchView;
    private MenuItem searchMenuItem;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private IMyPostsListener mIMyPostsListener;
    private PetitionsTableDbAdapter mPetitionsTableDbAdapter;

    private List<ItemPetitionsTable> mAlMyPosts = new ArrayList<ItemPetitionsTable>();
    private List<String> mAlPetitionTitles = new ArrayList<String>();

    private static int pageIndex = 0;
    private static boolean isRefreshing = false;
    private static boolean isProgressVisible = false;

    private ViewPagerPetitionsRecyclerViewAdapter mViewPagerPetitionsRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;

    Typeface font_robotoslab_bold, font_robotoslab_regular;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IMyPostsListener) {
            mIMyPostsListener = (IMyPostsListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //setRetainInstance(true);

        setHasOptionsMenu(true);

//        font_robotoslab_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Bold.ttf");
//
//        font_robotoslab_regular = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Regular.ttf");

        font_robotoslab_bold = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Bold");
        font_robotoslab_regular = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Regular");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_viewpager_my_posts, container, false);
        mAlMyPosts.clear();
        mAlPetitionTitles.clear();

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        //prefs.edit().putString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).apply();

        mProfile = new Profile(activity);
        mPetitionsTableDbAdapter = DatabaseHelper.get(activity).getPetitionsTableDbAdapter();

        mSwipeRefreshLayout = (SwipeRefreshLayout) view
                .findViewById(R.id.swipeRefreshLayout);

        WindowManager wm = ((WindowManager) activity
                .getSystemService(Context.WINDOW_SERVICE));
        Display display = wm.getDefaultDisplay();
        Point size = Utils.getDisplaySize(display);
        int height = size.y;
        mSwipeRefreshLayout.setProgressViewOffset(false, -100, height / 12);

        mSwipeRefreshLayout.setOnRefreshListener(this);

        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_my_posts);
        mRecyclerView.setHasFixedSize(true);
        mStaggeredLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mViewPagerPetitionsRecyclerViewAdapter = new ViewPagerPetitionsRecyclerViewAdapter(activity);
        mRecyclerView.setAdapter(mViewPagerPetitionsRecyclerViewAdapter);

        mViewPagerPetitionsRecyclerViewAdapter.setOnLoadMoreListener(new IOnLoadMoreListener() {
            @Override
            public void onLoadMore() {

                // Loading More
                Log.d(Const.DEBUG, "Loading More");

                mAlMyPosts.add(null);
                mViewPagerPetitionsRecyclerViewAdapter.notifyItemChanged(mAlMyPosts.size() - 1);
                mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();

                isProgressVisible = true;

                getDataFromServer(false);

            }
        });

        return view;
    }

    public void onResume() {
        super.onResume();

        if(searchView != null) {
            searchView.clearFocus();

            closeKeyboard(activity, searchView.getWindowToken());
        }

        if (System.currentTimeMillis() - prefs.getLong(Const.Prefs.UNVERIFIED_PETITIONS_REFRESH_TIME, 0) > Const.REFRESH_TIME)
            getDataFromServer(true);
        else {

            if (mPetitionsTableDbAdapter.isTableEmpty(DatabaseHelper.PETITION_TYPE_UNVERIFIED))
                getDataFromServer(true);
            else
                getDataFromDatabase();
        }
    }


    private void getDataFromServer(final boolean isPullToRefresh) {

        isRefreshing = true;

        if (isPullToRefresh) {
            prefs.edit().putInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, 1).apply();

            mAlMyPosts.clear();
            mAlPetitionTitles.clear();
            mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
        } else {
            prefs.edit().putInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, prefs.getInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, 1) + 1).apply();
        }
        pageIndex = prefs.getInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, 1);

        String url = Const.FINAL_URL + Const.URLs.ALL_PETITIONS;
        url = url + "type_of_petitions=myunverified";
        url = url + "&memberid=" + mProfile.getMemberId();
        url = url + "&memberid_type=" + mProfile.getMemberIdType();
        url = url + "&PageIndex=" + pageIndex;
        url = url + "&Search_PetitionNo=";

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Url = " + url);


//        RequestManager.getRequestQueue()
//                .cancelAll(Const.VOLLEY_TAG);

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        parseResponse(response, isPullToRefresh);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    activity,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }

                        getDataFromDatabase();
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }

    private void parseResponse(JSONArray response, boolean isPullToRefresh) {

        if (response.length() == 0) {

//            if (mAlMyPosts.size() == 0)
//                Toast.makeText(activity, "No petitions to display in this category.", Toast.LENGTH_LONG).show();
//            else
//                Toast.makeText(activity, "No more petitions to display in this category.", Toast.LENGTH_LONG).show();

            if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);

            if (isProgressVisible) {
                if (mAlMyPosts.size() > 0)
                    mAlMyPosts.remove(mAlMyPosts.size() - 1);
                mViewPagerPetitionsRecyclerViewAdapter.notifyItemRemoved(mAlMyPosts.size());
                mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
                isProgressVisible = false;
            }

            isRefreshing = false;

            if (prefs.getInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, 1) > 1)
                prefs.edit().putInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, prefs.getInt(Const.Prefs.UNVERIFIED_PETITIONS_PAGE_INDEX, 1) - 1).apply();
        } else {

            mPetitionsTableDbAdapter = new PetitionsTableDbAdapter();

            if (isPullToRefresh)
                mPetitionsTableDbAdapter.clearFeed(DatabaseHelper.PETITION_TYPE_UNVERIFIED);

            mPetitionsTableDbAdapter.beginTransaction();

            try {

                if (!isPullToRefresh) {
                    mAlPetitionTitles = mPetitionsTableDbAdapter.getPetitionNumbers(DatabaseHelper.PETITION_TYPE_UNVERIFIED);
                }

                for (int i = 0; i < response.length(); i++) {

                    JSONObject object = response.getJSONObject(i);

                    ItemPetitionsTable item = new ItemPetitionsTable();
                    item.setPetition_type(DatabaseHelper.PETITION_TYPE_UNVERIFIED);

                    // Petition Details Array
                    JSONArray petition_details = object.getJSONArray("Petition_Details");
                    JSONObject petition_details_object = petition_details.getJSONObject(0);

                    if (!mAlPetitionTitles.contains(petition_details_object.getString(Const.PETITION_DETAILS.PETITION_NUMBER))) {

                        item.setE_petition_number(String.valueOf(petition_details_object.getInt(Const.PETITION_DETAILS.EPETNO)));
                        item.setMember_id(petition_details_object.getString(Const.PETITION_DETAILS.MEMBER_ID));
                        item.setPetition_address(petition_details_object.getString(Const.PETITION_DETAILS.PETITION_ADDRESS));
                        item.setOfficial_name(petition_details_object.getString(Const.PETITION_DETAILS.OFFICIAL_NAME));
                        item.setOfficial_designation(petition_details_object.getString(Const.PETITION_DETAILS.OFFICIAL_DESIGNATION));
                        item.setOffice_department_name(petition_details_object.getString(Const.PETITION_DETAILS.OFFICE_DEPARTMENT_NAME));
                        item.setOffice_address(petition_details_object.getString(Const.PETITION_DETAILS.OFFICE_ADDRESS));
                        item.setCity(petition_details_object.getString(Const.PETITION_DETAILS.CITY));
                        item.setState(petition_details_object.getString(Const.PETITION_DETAILS.STATE));
                        item.setDistrict(petition_details_object.getString(Const.PETITION_DETAILS.DISTRICT));
                        item.setPincode(petition_details_object.getString(Const.PETITION_DETAILS.PIN_CODE));
                        item.setOfficial_mobile(petition_details_object.getString(Const.PETITION_DETAILS.OFFICIAL_MOBILE));
                        item.setOfficial_email(petition_details_object.getString(Const.PETITION_DETAILS.OFFICIAL_EMAIL));
                        item.setPetition_title(petition_details_object.getString(Const.PETITION_DETAILS.PETITION_TITLE));
                        item.setPetition_description(petition_details_object.getString(Const.PETITION_DETAILS.PETITION_DESCRIPTION));
                        item.setSms_matter(petition_details_object.getString(Const.PETITION_DETAILS.SMS_MATTER));
                        item.setPetition_number(petition_details_object.getString(Const.PETITION_DETAILS.PETITION_NUMBER));
                        item.setOtp(petition_details_object.getString(Const.PETITION_DETAILS.OTP));
                        item.setSms_send_or_not(petition_details_object.getString(Const.PETITION_DETAILS.SMS_SEND_OR_NOT));
                        item.setStatus(petition_details_object.getString(Const.PETITION_DETAILS.STATUS));
                        item.setDate(petition_details_object.getString(Const.PETITION_DETAILS.DATE));
                        item.setMember_id_type(petition_details_object.getString(Const.PETITION_DETAILS.MEMBER_ID_TYPE));

                        // Added latitude and longitude in v2.1
                        item.setLatitude(petition_details_object.getString(Const.PETITION_DETAILS.LATITUDE));
                        item.setLongitude(petition_details_object.getString(Const.PETITION_DETAILS.LONGITUDE));

                        // Petition Posted By Array
                        JSONArray petition_posted_by = object.getJSONArray("Petition_PostedBy");
                        JSONObject petition_posted_by_object = petition_posted_by.getJSONObject(0);

                        item.setPetitioner_name(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_NAME));
                        item.setPetitioner_gender(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_GENDER));
                        item.setPetitioner_city(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_CITY));
                        item.setPetitioner_district(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_DISTRICT));
                        item.setPetitioner_state(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_STATE));
                        item.setPetitioner_pincode(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_PINCODE));
                        item.setPetitioner_mobile(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_MOBILE));
                        item.setPetitioner_email(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_EMAIL));

                        //Added Petitioner Profile image url in v2.1
                        item.setPetitioner_profile_image_url(petition_posted_by_object.getString(Const.PETITION_DETAILS.PETITIONER_PROFILE_IMAGE_URL));

                        // Petition Attachments Array
                        JSONArray petition_attachments = object.getJSONArray("Petition_Attachments");
                        item.setAttachments(petition_attachments.toString());

                        // Petition_Wise_Checking Array
                        JSONArray petition_check = object.getJSONArray("Petition_Wise_Checking");
                        JSONObject petition_object = petition_check.getJSONObject(0);
                        JSONObject petition_supported = petition_object.getJSONObject("SupportedChecking");


                        if (petition_supported.getString("Am_I_Supported").equalsIgnoreCase("enable"))
                            item.setSent_support("0");
                        else
                            item.setSent_support("1");


                        // Comments Object
                        JSONObject comments = object.getJSONObject("Comments");

                        if (comments.getString("Am_I_Commented").equalsIgnoreCase("enable"))
                            item.setComment_posted_or_not("0");
                        else
                            item.setComment_posted_or_not("1");

                        item.setComments("");

                        // Likes Object
                        JSONObject likes = object.getJSONObject("Likes");
                        item.setLike_count(likes.getString("TotalLikesCount"));

                        if (likes.getString("Am_I_Liked").equalsIgnoreCase("enable"))
                            item.setLiked_or_not("0");
                        else
                            item.setLiked_or_not("1");

                        // Added in version 2.1
                        item.setComments_count(comments.getString("TotalCommentsCount"));

                        // Supports Object
                        JSONObject supports = object.getJSONObject("Supports");
                        item.setSupports_count(supports.getString("TotalSupportsCount"));

                        mPetitionsTableDbAdapter.insertRow(item);
                    }
                }

                mPetitionsTableDbAdapter.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                mPetitionsTableDbAdapter.endTransaction();
            }


            if (isPullToRefresh)
                prefs.edit().putLong(Const.Prefs.UNVERIFIED_PETITIONS_REFRESH_TIME, System.currentTimeMillis()).apply();
            getDataFromDatabase();

        }

    }

    @Override
    public void onRefresh() {
        if (!isRefreshing) {
            getDataFromServer(true);
        } else {
            if (mSwipeRefreshLayout.isRefreshing())
                mSwipeRefreshLayout.setRefreshing(false);

            Toast.makeText(activity, "Currently there is one other request under process. Please wait for a while and try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void getDataFromDatabase() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "getDataFromDatabase()");

        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);


        if (isProgressVisible) {
            if (mAlMyPosts.size() > 0)
                mAlMyPosts.remove(mAlMyPosts.size() - 1);
            mViewPagerPetitionsRecyclerViewAdapter.notifyItemRemoved(mAlMyPosts.size());
            mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
            isProgressVisible = false;
        }

        mAlMyPosts = mPetitionsTableDbAdapter.getPetitionsForType(DatabaseHelper.PETITION_TYPE_UNVERIFIED);
        mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();

        isRefreshing = false;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_all, menu);

        SearchManager searchManager = (SearchManager) activity.
                getSystemService(Context.SEARCH_SERVICE);
        searchMenuItem = menu.findItem(R.id.ic_action_search);
        searchView = (SearchView) searchMenuItem.getActionView();
        searchView.setQueryHint("Search by Title or ID");
        searchView.clearFocus();
        searchView.setSearchableInfo(searchManager.
                getSearchableInfo(activity.getComponentName()));
        searchView.setOnQueryTextListener(this);

        searchView.setOnQueryTextFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {

                mIMyPostsListener.onSearchFocusChanged(hasFocus);

                if (!hasFocus) {

                    closeKeyboard(activity, searchView.getWindowToken());
                }
            }
        });

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        mViewPagerPetitionsRecyclerViewAdapter.getFilter().filter(newText);
        return true;
    }

    public static void closeKeyboard(Context c, IBinder windowToken) {
        InputMethodManager mgr = (InputMethodManager) c.getSystemService(Context.INPUT_METHOD_SERVICE);
        mgr.hideSoftInputFromWindow(windowToken, 0);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.action_list) {
            prefs.edit().putString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).apply();
            mStaggeredLayoutManager.setSpanCount(1);
        } else if (item.getItemId() == R.id.action_grid) {
            prefs.edit().putString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.GRID_VIEW).apply();
            mStaggeredLayoutManager.setSpanCount(2);
        } else if (item.getItemId() == R.id.action_mini) {
            prefs.edit().putString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.MINI_VIEW).apply();
            mStaggeredLayoutManager.setSpanCount(1);
        }

        ActivityCompat.invalidateOptionsMenu(activity);
        mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
        mIMyPostsListener.onLayoutChangedListener();
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        MenuItem list_item = menu.findItem(R.id.action_list);
        MenuItem grid_item = menu.findItem(R.id.action_grid);
        MenuItem mini_item = menu.findItem(R.id.action_mini);

        String item_type = prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW);

        if (item_type.equalsIgnoreCase(Const.VIEWPAGER.LIST_VIEW)) {
            list_item.setChecked(true);
            grid_item.setChecked(false);
            mini_item.setChecked(false);
        } else if (item_type.equalsIgnoreCase(Const.VIEWPAGER.GRID_VIEW)) {
            list_item.setChecked(false);
            grid_item.setChecked(true);
            mini_item.setChecked(false);
        } else if (item_type.equalsIgnoreCase(Const.VIEWPAGER.MINI_VIEW)) {
            list_item.setChecked(false);
            grid_item.setChecked(false);
            mini_item.setChecked(true);
        }
    }


    //Adapter classes
    public class ViewPagerPetitionsRecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
            implements Filterable {

        private Context context;
        private SearchFilter searchFilter;

        private String url = Const.DEFAULT_IMAGE_URL;
        private final int VIEW_TYPE_ITEM = 0;
        private final int VIEW_TYPE_LOADING = 1;
        private final int VIEW_TYPE_MINI = 2;
        private final int VIEW_TYPE_GRID = 3;
        private IOnLoadMoreListener mIOnLoadMoreListener;

        private int visibleThreshold = 1;
        private int lastVisibleItem, totalItemCount;
        private int[] lastVisibleItems = new int[mStaggeredLayoutManager.getSpanCount()];

        public ViewPagerPetitionsRecyclerViewAdapter(Context context) {
            this.context = context;

            final StaggeredGridLayoutManager staggeredGridLayoutManager = (StaggeredGridLayoutManager) mRecyclerView.getLayoutManager();
            mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);

                    totalItemCount = staggeredGridLayoutManager.getItemCount();
                    lastVisibleItems = staggeredGridLayoutManager.findLastVisibleItemPositions(new int[mStaggeredLayoutManager.getSpanCount()]);

                    if (staggeredGridLayoutManager.getSpanCount() == 1) {
                        lastVisibleItem = lastVisibleItems[0];
                    } else if (staggeredGridLayoutManager.getSpanCount() == 2) {
                        lastVisibleItem = Math.max(lastVisibleItems[0], lastVisibleItems[1]);
                    }

                    if (!isRefreshing && totalItemCount <= lastVisibleItem + visibleThreshold) {

                        if (mIOnLoadMoreListener != null) {
                            mIOnLoadMoreListener.onLoadMore();
                        }

                        isRefreshing = true;
                    }
                }
            });
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == VIEW_TYPE_ITEM) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_viewpager_fragment_petitions_cardview, parent, false);
                return new CardViewHolder(v);
            } else if (viewType == VIEW_TYPE_MINI) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_viewpager_fragment_petitions_mini, parent, false);
                return new MiniCardsViewHolder(v);
            } else if (viewType == VIEW_TYPE_LOADING) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_viewpager_loading, parent, false);
                return new LoadingViewHolder(v);
            } else if (viewType == VIEW_TYPE_GRID) {

                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.item_viewpager_fragment_petitions_grid, parent, false);
                return new GridViewHolder(v);
            }

            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

            ItemPetitionsTable item = mAlMyPosts.get(position);

            if (holder.getItemViewType() == VIEW_TYPE_ITEM) {

                CardViewHolder cardViewHolder = (CardViewHolder) holder;

                if (item.getAttachments() == null
                        || item.getAttachments().equalsIgnoreCase("")) {
                    //Picasso.with(context).load(url).into(cardViewHolder.mImageView);
                    OkHttpClientHelper.getPicassoBuilder(context).load(url).into(cardViewHolder.mImageView);
                } else {

                    try {
                        JSONArray array = new JSONArray(item.getAttachments());
                        if (array.length() == 0)
                            url = Const.DEFAULT_IMAGE_URL;
                        else {
                            for (int i = 0; i < array.length(); i++) {

                                JSONObject attachment_object = array.getJSONObject(i);

                                String type = attachment_object.getString("typ");
                                if (type.equalsIgnoreCase("i")) {
                                    url = attachment_object.getString("Doc_path");
                                }
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    //Picasso.with(context).load(url).into(cardViewHolder.mImageView);
                    OkHttpClientHelper.getPicassoBuilder(context).load(url).into(cardViewHolder.mImageView);
                }

                //Picasso.with(context).load(item.getPetitioner_profile_image_url()).into(cardViewHolder.mImageViewPetitionerProfilePic);
                OkHttpClientHelper.getPicassoBuilder(context).load(item.getPetitioner_profile_image_url()).resize(100, 100).into(cardViewHolder.mImageViewPetitionerProfilePic);

                cardViewHolder.mTextViewTitle.setText(item.getPetition_title());
                cardViewHolder.mTextViewPetitionNumber.setText(item.getPetition_number());
                cardViewHolder.mTextViewPetitionerName.setText(item.getPetitioner_name());
                cardViewHolder.mTextViewPetitionerState.setText(item.getPetitioner_state());
            } else if (holder.getItemViewType() == VIEW_TYPE_MINI) {

                if (holder instanceof MiniCardsViewHolder) {
                    MiniCardsViewHolder miniCardsViewHolder = (MiniCardsViewHolder) holder;

                    if (item.getAttachments() == null
                            || item.getAttachments().equalsIgnoreCase("")) {
                        Picasso.with(context).load(url).into(miniCardsViewHolder.mImageView);
                    } else {

                        try {
                            JSONArray array = new JSONArray(item.getAttachments());
                            if (array.length() == 0)
                                url = Const.DEFAULT_IMAGE_URL;
                            else {
                                for (int i = 0; i < array.length(); i++) {

                                    JSONObject attachment_object = array.getJSONObject(i);

                                    String type = attachment_object.getString("typ");
                                    if (type.equalsIgnoreCase("i")) {
                                        url = attachment_object.getString("Doc_path");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Picasso.with(context).load(url).into(miniCardsViewHolder.mImageView);
                    }

                    miniCardsViewHolder.mTextViewTitle.setText(item.getPetition_title());
                    miniCardsViewHolder.mTextViewPetitionNumber.setText(item.getPetition_number());
                    miniCardsViewHolder.mTextViewPetitionerName.setText(item.getPetitioner_name());
                }
            } else if (holder.getItemViewType() == VIEW_TYPE_LOADING) {

                if (holder instanceof LoadingViewHolder) {
                    LoadingViewHolder loadingViewHolder = (LoadingViewHolder) holder;
                    loadingViewHolder.progressBar.setIndeterminate(true);
                }
            } else if (holder.getItemViewType() == VIEW_TYPE_GRID) {

                if (holder instanceof GridViewHolder) {
                    GridViewHolder gridViewHolder = (GridViewHolder) holder;

                    if (item.getAttachments() == null
                            || item.getAttachments().equalsIgnoreCase("")) {
                        Picasso.with(context).load(url).into(gridViewHolder.mImageView);
                    } else {

                        try {
                            JSONArray array = new JSONArray(item.getAttachments());
                            if (array.length() == 0)
                                url = Const.DEFAULT_IMAGE_URL;
                            else {
                                for (int i = 0; i < array.length(); i++) {

                                    JSONObject attachment_object = array.getJSONObject(i);

                                    String type = attachment_object.getString("typ");
                                    if (type.equalsIgnoreCase("i")) {
                                        url = attachment_object.getString("Doc_path");
                                    }
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        Picasso.with(context).load(url).into(gridViewHolder.mImageView);
                    }

                    gridViewHolder.mTextViewTitle.setText(item.getPetition_title());
                    gridViewHolder.mTextViewPetitionerName.setText(item.getPetitioner_name());
                    gridViewHolder.mTextViewPetitionNumber.setText(item.getPetition_number());
                }
            }
        }

        @Override
        public int getItemCount() {
            return mAlMyPosts == null ? 0 : mAlMyPosts.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (mAlMyPosts.get(position) == null) {
                return VIEW_TYPE_LOADING;
            } else {

                if (prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.LIST_VIEW)) {
                    return VIEW_TYPE_ITEM;
                } else if (prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.GRID_VIEW)) {
                    return VIEW_TYPE_GRID;
                } else if (prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.MINI_VIEW)) {
                    return VIEW_TYPE_MINI;
                } else {
                    return VIEW_TYPE_ITEM;
                }
            }
        }

        public void setOnLoadMoreListener(IOnLoadMoreListener mIOnLoadMoreListener) {
            this.mIOnLoadMoreListener = mIOnLoadMoreListener;
        }

        @Override
        public Filter getFilter() {

            if (searchFilter == null)
                searchFilter = new SearchFilter();

            return searchFilter;
        }

        private class SearchFilter extends Filter {

            private PetitionsTableDbAdapter mPetitionsTableDbAdapter = DatabaseHelper.get(context).getPetitionsTableDbAdapter();

            @Override
            protected FilterResults performFiltering(CharSequence constraint) {

                FilterResults results = new FilterResults();
                mAlMyPosts = mPetitionsTableDbAdapter.getPetitionsForType(DatabaseHelper.PETITION_TYPE_UNVERIFIED);

                if (constraint != null && constraint.length() > 0) {

                    ArrayList<ItemPetitionsTable> searchResults = new ArrayList<ItemPetitionsTable>();

                    for (ItemPetitionsTable item : mAlMyPosts) {

                        if (item.getPetition_title().toLowerCase().contains(constraint.toString().toLowerCase())
                                || item.getPetition_number().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            searchResults.add(item);
                        }
                    }

                    results.count = searchResults.size();
                    results.values = searchResults;
                } else {
                    //mAlMyPosts = mPetitionsTableDbAdapter.getPetitionsForType(DatabaseHelper.PETITION_TYPE_UNVERIFIED);
                    results.count = mAlMyPosts.size();
                    results.values = mAlMyPosts;
                }


                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mAlMyPosts = (List<ItemPetitionsTable>) results.values;
                notifyDataSetChanged();
            }
        }

    }


    public class LoadingViewHolder extends RecyclerView.ViewHolder {
        public ProgressBar progressBar;

        public LoadingViewHolder(View progressView) {
            super(progressView);
            progressBar = (ProgressBar) progressView.findViewById(R.id.progressBar1);
        }
    }

    public class CardViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextViewTitle, mTextViewPetitionerName, mTextViewPetitionerState, mTextViewPetitionNumber;
        public ImageView mImageView, mImageViewSupported;
        public CircleImageView mImageViewPetitionerProfilePic;
        public LinearLayout mLLCardView;

        public CardViewHolder(View itemView) {
            super(itemView);
            mLLCardView = (LinearLayout) itemView.findViewById(R.id.ll_cardView);
            mLLCardView.setOnClickListener(this);
            mTextViewTitle = (TextView) mLLCardView.findViewById(R.id.textView_description);
            mTextViewTitle.setTypeface(font_robotoslab_regular);

            mImageViewPetitionerProfilePic = (CircleImageView) mLLCardView.findViewById(R.id.imageView_petitioner_image);

            mTextViewPetitionerName = (TextView) mLLCardView.findViewById(R.id.textView_petitioner_name);
            mTextViewPetitionerName.setTypeface(font_robotoslab_regular);

            mTextViewPetitionerState = (TextView) mLLCardView.findViewById(R.id.textView_petitioner_state);
            mTextViewPetitionerState.setTypeface(font_robotoslab_regular);

            mTextViewPetitionNumber = (TextView) mLLCardView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_regular);

            mImageView = (ImageView) mLLCardView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) itemView.findViewById(R.id.imageView_supported_or_not);
            mImageViewSupported.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlMyPosts.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT);

            mIMyPostsListener.onShareButtonClicked(bundle);
        }
    }

    public class MiniCardsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextViewTitle, mTextViewPetitionerName, mTextViewPetitionNumber;
        public ImageView mImageView, mImageViewSupported;
        public LinearLayout mLLMiniView;

        public MiniCardsViewHolder(View itemView) {
            super(itemView);
            mLLMiniView = (LinearLayout) itemView.findViewById(R.id.ll_miniView);
            mLLMiniView.setOnClickListener(this);
            mTextViewTitle = (TextView) mLLMiniView.findViewById(R.id.textView_description);
            mTextViewTitle.setTypeface(font_robotoslab_regular);

            mTextViewPetitionerName = (TextView) mLLMiniView.findViewById(R.id.textView_petitioner_name);
            mTextViewPetitionerName.setTypeface(font_robotoslab_regular);

            mTextViewPetitionNumber = (TextView) mLLMiniView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_regular);

            mImageView = (ImageView) mLLMiniView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) itemView.findViewById(R.id.imageView_supported_or_not);
            mImageViewSupported.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlMyPosts.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT);

            mIMyPostsListener.onShareButtonClicked(bundle);
        }
    }

    public class GridViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        public TextView mTextViewTitle, mTextViewPetitionerName, mTextViewPetitionNumber;
        public ImageView mImageView, mImageViewSupported;
        public LinearLayout mLLGridView;

        public GridViewHolder(View itemView) {
            super(itemView);
            mLLGridView = (LinearLayout) itemView.findViewById(R.id.ll_gridView);
            mLLGridView.setOnClickListener(this);
            mTextViewTitle = (TextView) mLLGridView.findViewById(R.id.textView_description);
            mTextViewTitle.setTypeface(font_robotoslab_regular);

            mTextViewPetitionerName = (TextView) mLLGridView.findViewById(R.id.textView_petitioner_name);
            mTextViewPetitionerName.setTypeface(font_robotoslab_regular);

            mTextViewPetitionNumber = (TextView) mLLGridView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_regular);

            mImageView = (ImageView) mLLGridView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) mLLGridView.findViewById(R.id.imageView_supported_or_not);
            mImageViewSupported.setVisibility(View.GONE);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlMyPosts.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.DETAIL_VERIFY_MY_PETITION_FRAGMENT);

            mIMyPostsListener.onShareButtonClicked(bundle);
        }
    }

    public void setLayoutManagerForFragment(int column_count) {
        ActivityCompat.invalidateOptionsMenu(activity);
        mStaggeredLayoutManager.setSpanCount(column_count);
        mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
    }

}
