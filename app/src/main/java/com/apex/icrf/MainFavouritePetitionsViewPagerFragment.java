package com.apex.icrf;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.apex.icrf.classes.IMainFavouritePetitionsListener;
import com.apex.icrf.classes.ItemPetitionsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.DeliveryReportsTableDbAdapter;
import com.apex.icrf.database.FavouritesTableDbAdapter;
import com.apex.icrf.database.PetitionsTableDbAdapter;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.TypeFaceHelper;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 11/09/15.
 */
public class MainFavouritePetitionsViewPagerFragment extends Fragment implements SearchView.OnQueryTextListener {

    private Activity activity;
    private SharedPreferences prefs;
    private Profile mProfile;

    private SearchView searchView;
    private MenuItem searchMenuItem;
    private IMainFavouritePetitionsListener mIMainFavouritePetitionsListener;
    private PetitionsTableDbAdapter mPetitionsTableDbAdapter;
    private FavouritesTableDbAdapter mFavouritesTableDbAdapter;

    private List<String> mAlPetitionTitles = new ArrayList<String>();
    private List<String> mAlFavouritePetitionIDs = new ArrayList<>();
    private List<ItemPetitionsTable> mAlFavouritePetitions = new ArrayList<>();

    private ViewPagerPetitionsRecyclerViewAdapter mViewPagerPetitionsRecyclerViewAdapter;
    private RecyclerView mRecyclerView;
    private StaggeredGridLayoutManager mStaggeredLayoutManager;

    Typeface font_robotoslab_bold, font_robotoslab_regular, font_robot_regular,
            font_roboto_light, font_roboto_medium, font_roboto_bold, font_roboto_thin;


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IMainFavouritePetitionsListener) {
            mIMainFavouritePetitionsListener = (IMainFavouritePetitionsListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

//        font_robotoslab_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Bold.ttf");
//
//        font_robotoslab_regular = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/RobotoSlab-Regular.ttf");
//
//        font_robot_regular = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Regular.ttf");
//
//        font_roboto_light = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Light.ttf");
//
//        font_roboto_medium = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Medium.ttf");
//
//        font_roboto_bold = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Bold.ttf");
//
//        font_roboto_thin = Typeface.createFromAsset(activity.getAssets(),
//                "fonts/Roboto-Thin.ttf");

        font_robotoslab_bold = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Bold");
        font_robotoslab_regular = TypeFaceHelper.getTypeFace(activity, "RobotoSlab-Regular");
        font_robot_regular = TypeFaceHelper.getTypeFace(activity, "Roboto-Regular");
        font_roboto_light = TypeFaceHelper.getTypeFace(activity, "Roboto-Light");
        font_roboto_medium = TypeFaceHelper.getTypeFace(activity, "Roboto-Medium");
        font_roboto_bold = TypeFaceHelper.getTypeFace(activity, "Roboto-Bold");
        font_roboto_thin = TypeFaceHelper.getTypeFace(activity, "Roboto-Thin");
        //font_roboto_condensed_bold = TypeFaceHelper.getTypeFace(activity, "RobotoCondensed-Bold");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_viewpager_favourite_petitions, container, false);
        mAlPetitionTitles.clear();

        // Contains Favourite Petition IDs
        mAlFavouritePetitionIDs.clear();

        // Contains Favourite Petitions of entire DB
        mAlFavouritePetitions.clear();
        //mAlFavouritePetitionsCopy.clear();

        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mProfile = new Profile(activity);
        mPetitionsTableDbAdapter = DatabaseHelper.get(activity).getPetitionsTableDbAdapter();

        mFavouritesTableDbAdapter = DatabaseHelper.get(activity).getFavouritesTableDbAdapter();

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerview_favourite_petitions);
        mRecyclerView.setHasFixedSize(true);
        //mStaggeredLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);

        if (prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.LIST_VIEW)
                || prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.MINI_VIEW))
            mStaggeredLayoutManager = new StaggeredGridLayoutManager(1, StaggeredGridLayoutManager.VERTICAL);
        else if (prefs.getString(Const.VIEWPAGER.VIEW_TYPE, Const.VIEWPAGER.LIST_VIEW).equalsIgnoreCase(Const.VIEWPAGER.GRID_VIEW))
            mStaggeredLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);

        mRecyclerView.setLayoutManager(mStaggeredLayoutManager);
        mViewPagerPetitionsRecyclerViewAdapter = new ViewPagerPetitionsRecyclerViewAdapter(activity);
        mRecyclerView.setAdapter(mViewPagerPetitionsRecyclerViewAdapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if(searchView != null) {
            searchView.clearFocus();

            closeKeyboard(activity, searchView.getWindowToken());
        }

        getDataFromDatabase();
    }


    private void getDataFromDatabase() {

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "getDataFromDatabase()");

        mAlFavouritePetitionIDs.clear();
        mAlFavouritePetitions.clear();

        mAlFavouritePetitionIDs = mFavouritesTableDbAdapter.getFavourites();

        for (int i = 0; i < mAlFavouritePetitionIDs.size(); i++) {

            ItemPetitionsTable item = mPetitionsTableDbAdapter.getPetitionDetailsForID(mAlFavouritePetitionIDs.get(i));

            mAlFavouritePetitions.add(item);
        }

        mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();

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

                mIMainFavouritePetitionsListener.onSearchFocusChanged(hasFocus);

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
        mIMainFavouritePetitionsListener.onLayoutChangedListener();

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

        public ViewPagerPetitionsRecyclerViewAdapter(Context context) {
            this.context = context;
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

            ItemPetitionsTable item = mAlFavouritePetitions.get(position);

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

//                if(item.getSent_support().equalsIgnoreCase("0"))
//                    cardViewHolder.mImageViewSupported.setVisibility(View.GONE);

                if (item.getSent_support().equalsIgnoreCase("0")) {

                    DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter = DatabaseHelper.get(context.getApplicationContext()).getDeliveryReportsTableDbAdapter();

                    int result = mDeliveryReportsTableDbAdapter.isDelivered(item.getE_petition_number());

                    if (result == 3 || result == 0)
                        cardViewHolder.mImageViewSupported.setVisibility(View.GONE);
                    else if (result == 2) {
                        cardViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                        cardViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_pending_label_orange);
                    } else if (result == 1) {
                        cardViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                        cardViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_supported_label_green);
                    }

                } else
                    cardViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);

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

                    if (item.getSent_support().equalsIgnoreCase("0")) {
                        //miniCardsViewHolder.mImageViewSupported.setVisibility(View.GONE);

                        DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter = DatabaseHelper.get(context.getApplicationContext()).getDeliveryReportsTableDbAdapter();

                        int result = mDeliveryReportsTableDbAdapter.isDelivered(item.getE_petition_number());

                        if (result == 3 || result == 0)
                            miniCardsViewHolder.mImageViewSupported.setVisibility(View.GONE);
                        else if (result == 2) {
                            miniCardsViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                            miniCardsViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_pending_label_orange);
                        } else if (result == 1) {
                            miniCardsViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                            miniCardsViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_supported_label_green);
                        }

                    } else
                        miniCardsViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
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

                    if (item.getSent_support().equalsIgnoreCase("0")) {
                        //gridViewHolder.mImageViewSupported.setVisibility(View.GONE);

                        DeliveryReportsTableDbAdapter mDeliveryReportsTableDbAdapter = DatabaseHelper.get(context.getApplicationContext()).getDeliveryReportsTableDbAdapter();

                        int result = mDeliveryReportsTableDbAdapter.isDelivered(item.getE_petition_number());

                        if (result == 3 || result == 0)
                            gridViewHolder.mImageViewSupported.setVisibility(View.GONE);
                        else if (result == 2) {
                            gridViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                            gridViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_pending_label_orange);
                        } else if (result == 1) {
                            gridViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                            gridViewHolder.mImageViewSupported.setImageResource(R.drawable.ic_supported_label_green);
                        }
                    } else
                        gridViewHolder.mImageViewSupported.setVisibility(View.VISIBLE);
                }
            }
        }

        @Override
        public int getItemCount() {
            return mAlFavouritePetitions == null ? 0 : mAlFavouritePetitions.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (mAlFavouritePetitions.get(position) == null) {
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

                if (constraint != null && constraint.length() > 0) {

                    ArrayList<ItemPetitionsTable> searchResults = new ArrayList<ItemPetitionsTable>();

                    for (ItemPetitionsTable item : mAlFavouritePetitions) {

                        if (item.getPetition_title().toLowerCase().contains(constraint.toString().toLowerCase())) {
                            searchResults.add(item);
                        }
                    }

                    results.count = searchResults.size();
                    results.values = searchResults;
                } else {
                    resetValues();
                    results.count = mAlFavouritePetitions.size();
                    results.values = mAlFavouritePetitions;
                }


                return results;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {

                mAlFavouritePetitions = (List<ItemPetitionsTable>) results.values;
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
        CircleImageView mImageViewPetitionerProfilePic;
        public LinearLayout mLLCardView;

        public CardViewHolder(View itemView) {
            super(itemView);
            mLLCardView = (LinearLayout) itemView.findViewById(R.id.ll_cardView);
            mLLCardView.setOnClickListener(this);
            mTextViewTitle = (TextView) mLLCardView.findViewById(R.id.textView_description);
            //mTextViewTitle.setTypeface(font_robotoslab_regular);
            mTextViewTitle.setTypeface(font_robot_regular);

            mImageViewPetitionerProfilePic = (CircleImageView) mLLCardView.findViewById(R.id.imageView_petitioner_image);

            mTextViewPetitionerName = (TextView) mLLCardView.findViewById(R.id.textView_petitioner_name);
            //mTextViewPetitionerName.setTypeface(font_robotoslab_regular);
            //mTextViewPetitionerName.setTypeface(font_roboto_light);
            //mTextViewPetitionerName.setTypeface(font_roboto_medium);
            //mTextViewPetitionerName.setTypeface(font_roboto_bold);
            mTextViewPetitionerName.setTypeface(font_robot_regular);

            mTextViewPetitionerState = (TextView) mLLCardView.findViewById(R.id.textView_petitioner_state);
            //mTextViewPetitionerState.setTypeface(font_robotoslab_regular);
            mTextViewPetitionerState.setTypeface(font_roboto_light);


            mTextViewPetitionNumber = (TextView) mLLCardView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_bold);

            mImageView = (ImageView) mLLCardView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) itemView.findViewById(R.id.imageView_supported_or_not);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlFavouritePetitions.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT);

            mIMainFavouritePetitionsListener.onFavouriteItemClicked(bundle);
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
            //mTextViewTitle.setTypeface(font_robotoslab_regular);
            mTextViewTitle.setTypeface(font_robot_regular);

            mTextViewPetitionerName = (TextView) mLLMiniView.findViewById(R.id.textView_petitioner_name);
            //mTextViewPetitionerName.setTypeface(font_robotoslab_regular);
            mTextViewPetitionerName.setTypeface(font_robot_regular);

            mTextViewPetitionNumber = (TextView) mLLMiniView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_bold);
            //mTextViewPetitionNumber.setTypeface(font_roboto_thin);

            mImageView = (ImageView) mLLMiniView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) itemView.findViewById(R.id.imageView_supported_or_not);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlFavouritePetitions.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT);

            mIMainFavouritePetitionsListener.onFavouriteItemClicked(bundle);
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
            //mTextViewTitle.setTypeface(font_robotoslab_regular);
            mTextViewTitle.setTypeface(font_robot_regular);

            mTextViewPetitionerName = (TextView) mLLGridView.findViewById(R.id.textView_petitioner_name);
            //mTextViewPetitionerName.setTypeface(font_robotoslab_regular);
            mTextViewPetitionerName.setTypeface(font_robot_regular);

            mTextViewPetitionNumber = (TextView) mLLGridView.findViewById(R.id.textView_petition_number);
            mTextViewPetitionNumber.setTypeface(font_robotoslab_bold);

            mImageView = (ImageView) mLLGridView.findViewById(R.id.imageView_main_image);
            mImageViewSupported = (ImageView) mLLGridView.findViewById(R.id.imageView_supported_or_not);
        }

        @Override
        public void onClick(View v) {

            final ItemPetitionsTable item = mAlFavouritePetitions.get(getAdapterPosition());

            Bundle bundle = new Bundle();
            bundle.putString(Const.Bundle.E_PETITION_NUMBER, item.getE_petition_number());
            bundle.putString(Const.Bundle.PETITION_NUMBER, item.getPetition_number());
            bundle.putString(Const.Bundle.PETITION_TITLE, item.getPetition_title());
            bundle.putInt(Const.Bundle.FROM_FRAGMENT, Const.Bundle.MAIN_FAVOURITE_PETITION_FRAGMENT);

            mIMainFavouritePetitionsListener.onFavouriteItemClicked(bundle);
        }
    }

    private void resetValues() {

        mAlFavouritePetitionIDs.clear();
        mAlFavouritePetitions.clear();

        mAlFavouritePetitionIDs = mFavouritesTableDbAdapter.getFavourites();

        for (int i = 0; i < mAlFavouritePetitionIDs.size(); i++) {

            ItemPetitionsTable item = mPetitionsTableDbAdapter.getPetitionDetailsForID(mAlFavouritePetitionIDs.get(i));

            mAlFavouritePetitions.add(item);
        }

    }


    public void setLayoutManagerForFragment(int column_count) {
        ActivityCompat.invalidateOptionsMenu(activity);
        mStaggeredLayoutManager.setSpanCount(column_count);
        mViewPagerPetitionsRecyclerViewAdapter.notifyDataSetChanged();
    }
}
