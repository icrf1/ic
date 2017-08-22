package com.apex.icrf;

import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.apex.icrf.adapters.CommentsRecyclerViewAdapter;
import com.apex.icrf.classes.ItemComment;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.PetitionsTableDbAdapter;
import com.apex.icrf.diskcache.RequestManager;
import com.apex.icrf.utils.LinearLayoutManager;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.Profile;
import com.apex.icrf.utils.TypeFaceHelper;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.util.ArrayList;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 30/09/15.
 */
public class CommentsActivity extends AppCompatActivity {

    private Toolbar mToolbar;
    private TextView mTextViewTitle;

    RecyclerView mRecyclerView;
    LinearLayoutManager mLayoutManager;

    ListView listview;

    ArrayList<ItemComment> mAlComments = new ArrayList<ItemComment>();

    SharedPreferences prefs;
    Profile mProfile;
    //CommentsAdapter mCommentsAdapter;
    CommentsRecyclerViewAdapter mCommentsRecyclerViewAdapter;
    ProgressDialog progressDialog;

    Button mButtonSubmit;
    EditText mEditTextComment;

    String petition_number, e_petition_number;

    private PetitionsTableDbAdapter mPetitionsTableDbAdapter;

    private boolean isSuccessPetition = false;

    Typeface font_robotoslab_bold, font_robotoslab_regular, font_robot_regular,
            font_roboto_light, font_roboto_medium, font_roboto_bold, font_roboto_thin,
            font_roboto_condensed_bold;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments_2);

//        font_robotoslab_bold = Typeface.createFromAsset(getAssets(),
//                "fonts/RobotoSlab-Bold.ttf");
//
//        font_robotoslab_regular = Typeface.createFromAsset(getAssets(),
//                "fonts/RobotoSlab-Regular.ttf");
//
//        font_robot_regular = Typeface.createFromAsset(getAssets(),
//                "fonts/Roboto-Regular.ttf");
//
//        font_roboto_light = Typeface.createFromAsset(getAssets(),
//                "fonts/Roboto-Light.ttf");
//
//        font_roboto_medium = Typeface.createFromAsset(getAssets(),
//                "fonts/Roboto-Medium.ttf");
//
//        font_roboto_bold = Typeface.createFromAsset(getAssets(),
//                "fonts/Roboto-Bold.ttf");
//
//        font_roboto_thin = Typeface.createFromAsset(getAssets(),
//                "fonts/Roboto-Thin.ttf");
//
//        font_roboto_condensed_bold = Typeface.createFromAsset(getAssets(),
//                "fonts/RobotoCondensed-Bold.ttf");

        font_robotoslab_bold = TypeFaceHelper.getTypeFace(this, "RobotoSlab-Bold");
        font_robotoslab_regular = TypeFaceHelper.getTypeFace(this, "RobotoSlab-Regular");
        font_robot_regular = TypeFaceHelper.getTypeFace(this, "Roboto-Regular");
        font_roboto_light = TypeFaceHelper.getTypeFace(this, "Roboto-Light");
        font_roboto_medium = TypeFaceHelper.getTypeFace(this, "Roboto-Medium");
        font_roboto_bold = TypeFaceHelper.getTypeFace(this, "Roboto-Bold");
        font_roboto_thin = TypeFaceHelper.getTypeFace(this, "Roboto-Thin");
        font_roboto_condensed_bold = TypeFaceHelper.getTypeFace(this, "RobotoCondensed-Bold");

        mAlComments.clear();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mTextViewTitle = (TextView) mToolbar.findViewById(R.id.toolbar_title);
        mTextViewTitle.setText("Comments");

        setSupportActionBar(mToolbar);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setTitle("");

        Bundle bundle = getIntent().getExtras();
        petition_number = bundle.getString("petition_no");
        e_petition_number = bundle.getString("e_petition_no");

        if (bundle.containsKey("isSuccessPetition")) {
            isSuccessPetition = true;
        } else
            isSuccessPetition = false;

        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPetitionsTableDbAdapter = DatabaseHelper.get(this).getPetitionsTableDbAdapter();
        mProfile = new Profile(this);
        progressDialog = new ProgressDialog(this);
        progressDialog.setIndeterminate(true);
        progressDialog.setCancelable(true);

        //listview = (ListView) findViewById(R.id.listView_comments);
        //mCommentsAdapter = new CommentsAdapter();
        //listview.setAdapter(mCommentsAdapter);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerview_comments);
        mRecyclerView.setHasFixedSize(true);

        mLayoutManager = new LinearLayoutManager(this, android.support.v7.widget.LinearLayoutManager.VERTICAL, false);
        mLayoutManager.setOrientation(android.support.v7.widget.LinearLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mCommentsRecyclerViewAdapter = new CommentsRecyclerViewAdapter(this, mAlComments);
        mRecyclerView.setAdapter(mCommentsRecyclerViewAdapter);

        mButtonSubmit = (Button) findViewById(R.id.comments_button_submit);

        if (mPetitionsTableDbAdapter.canComment(e_petition_number)) {
            mButtonSubmit.setEnabled(true);
            mButtonSubmit.setText("Comment");
        } else {
            mButtonSubmit.setEnabled(false);

            if (!isSuccessPetition)
                mButtonSubmit.setText("Already Commented");
            else
                mButtonSubmit.setText("Comments Disabled");
        }

        mButtonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                postComment(mEditTextComment.getText().toString());
            }
        });

        mEditTextComment = (EditText) findViewById(R.id.comments_edittext_comment);

        getDataFromServer(e_petition_number);
    }


    private void postComment(String comment) {

        if (comment.equalsIgnoreCase("")) {
            Toast.makeText(this, "Empty comments cannot be posted", Toast.LENGTH_LONG).show();
        } else {

            String member_id = mProfile.getMemberId();
            String member_id_type = mProfile.getMemberIdType();
            String comment_msg = comment;

            String url = Const.FINAL_URL + Const.URLs.POST_COMMENT_TO_PETITION;
            url = url + "epetno=" + e_petition_number;
            url = url + "&memberid=" + member_id;
            url = url + "&comments_msg=" + URLEncoder.encode(comment_msg);
            url = url + "&memberid_type=" + member_id_type;

            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Post Comment Url = " + url);

            JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                    new Response.Listener<JSONObject>() {

                        @Override
                        public void onResponse(JSONObject response) {

                            if (response != null) {

                                Log.d(Const.DEBUG, "Response: " + response.toString());

                                try {

                                    if (response.getString("responce").equalsIgnoreCase("success")) {
                                        Toast.makeText(CommentsActivity.this, "Comment Posted", Toast.LENGTH_SHORT).show();

                                        mButtonSubmit.setEnabled(false);
                                        mButtonSubmit.setText("Already Commented");
                                        mEditTextComment.setText("");

                                        mPetitionsTableDbAdapter.updateCommentCheck(e_petition_number);

                                        getDataFromServer(e_petition_number);
                                    } else {
                                        Toast.makeText(CommentsActivity.this, "Comment posting failed", Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    e.printStackTrace();
                                    Toast.makeText(CommentsActivity.this, "Comment posting failed", Toast.LENGTH_SHORT).show();
                                }

                            }
                        }
                    }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    error.printStackTrace();
                }
            });

            jsonObjectRequest.setTag(Const.VOLLEY_TAG);
            jsonObjectRequest.setRetryPolicy(new DefaultRetryPolicy(
                    Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
            RequestManager.getRequestQueue().add(jsonObjectRequest);
        }
    }

    private class CommentsAdapter extends BaseAdapter {

        CircleImageView imageView;
        TextView mCommentText, mCommentDate, mMemberName;

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mAlComments.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {

            ItemComment item = mAlComments.get(position);

            if (convertView == null)
                convertView = getLayoutInflater().inflate(R.layout.item_comments_2, null);

            imageView = (CircleImageView) convertView.findViewById(R.id.imageView);
            OkHttpClientHelper.getPicassoBuilder(CommentsActivity.this).load(item.getProfile_url()).resize(100, 100).into(imageView);


            mMemberName = (TextView) convertView.findViewById(R.id.textView_member_name);
            mMemberName.setTypeface(font_roboto_medium);
            mMemberName.setText(item.getMember_name());


            mCommentText = (TextView) convertView.findViewById(R.id.textView_comment);
            mCommentText.setTypeface(font_roboto_thin);
            mCommentText.setText(item.getMessage());

            mCommentDate = (TextView) convertView.findViewById(R.id.textView_comment_date);
            mCommentDate.setTypeface(font_roboto_light);
            mCommentDate.setText(item.getDate());

            return convertView;
        }
    }


    private void getDataFromServer(final String e_pno) {

        if (e_pno == null)
            return;

        //String url = Const.BASE_URL + Const.URLs.GET_COMMENTS;
        String url = Const.FINAL_URL + Const.URLs.GET_COMMENTS;
        url = url + "epetno=" + e_pno;

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Get Comments Url = " + url);

        showProgressDialog("Loading Comments...");

        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(url,
                new Response.Listener<JSONArray>() {

                    @Override
                    public void onResponse(JSONArray response) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG,
                                    "Response => " + response.toString());
                            Log.d(Const.DEBUG, "Length = " + response.length());
                        }

                        parseResponse(e_pno, response);
                    }
                },
                new Response.ErrorListener() {

                    @Override
                    public void onErrorResponse(VolleyError error) {

                        if (Const.DEBUGGING) {
                            Log.d(Const.DEBUG, "Volley Error");
                            Log.d(Const.DEBUG, "Error = " + error.toString());
                        }

                        dismissProgressDialog();

                        String errorMessage = error.getClass().toString();
                        if (errorMessage
                                .equalsIgnoreCase("class com.android.volley.NoConnectionError")) {
                            Toast.makeText(
                                    CommentsActivity.this,
                                    "Cannot detect active internet connection. "
                                            + "Please check your network connection.",
                                    Toast.LENGTH_LONG).show();
                        }
                    }
                });

        jsonArrayRequest.setTag(Const.VOLLEY_TAG);
        jsonArrayRequest.setRetryPolicy(new DefaultRetryPolicy(
                Const.VOLLEY_TIME_OUT, DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestManager.getRequestQueue().add(jsonArrayRequest);
    }


    private void parseResponse(String e_pno, JSONArray response) {

        mAlComments.clear();
        mCommentsRecyclerViewAdapter.setItems(mAlComments);
        mCommentsRecyclerViewAdapter.notifyDataSetChanged();

        if (response.length() == 0) {
            Toast.makeText(this, "No comments posted.", Toast.LENGTH_LONG).show();
            dismissProgressDialog();
        } else {

            ItemComment item;

            try {

                for (int i = 0; i < response.length(); i++) {

                    JSONObject object = response.getJSONObject(i);

                    item = new ItemComment();
                    item.setMember_name(object.getString("membername").trim());
                    item.setMessage(object.getString("message").trim());
                    item.setDate(object.getString("dt").trim());
                    item.setProfile_url(object.getString("profile_img_url").trim());

                    mAlComments.add(item);
                }

                mCommentsRecyclerViewAdapter.setItems(mAlComments);
                mCommentsRecyclerViewAdapter.notifyDataSetChanged();

                dismissProgressDialog();

            } catch (Exception e) {
                e.printStackTrace();
                dismissProgressDialog();
            }
        }

    }


    private void dismissProgressDialog() {

        if (progressDialog != null && progressDialog.isShowing())
            progressDialog.dismiss();
    }

    private void showProgressDialog(String message) {

        if (progressDialog != null && !progressDialog.isShowing()) {
            progressDialog.setMessage(message);
            progressDialog.show();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home)
            finish();

        return super.onOptionsItemSelected(item);
    }
}
