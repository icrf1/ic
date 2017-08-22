package com.apex.icrf.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.apex.icrf.R;
import com.apex.icrf.classes.ItemComment;
import com.apex.icrf.utils.OkHttpClientHelper;

import java.util.ArrayList;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by WASPVamsi on 12/02/16.
 */
public class CommentsRecyclerViewAdapter extends RecyclerView.Adapter<CommentsRecyclerViewAdapter.ViewHolder> {
    private String[] mDataset;
    private List<ItemComment> mItems = new ArrayList<>();
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        CircleImageView imageView;
        TextView mCommentText, mCommentDate, mMemberName;

        public ViewHolder(View v) {
            super(v);
            imageView = (CircleImageView) v.findViewById(R.id.imageView);
            mMemberName = (TextView) v.findViewById(R.id.textView_member_name);
            mCommentText = (TextView) v.findViewById(R.id.textView_comment);
            mCommentDate = (TextView) v.findViewById(R.id.textView_comment_date);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public CommentsRecyclerViewAdapter(Context context, List<ItemComment> items) {
        mItems = items;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public CommentsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                     int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_comments_2, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element
        //holder.mTextView.setText(mItems.get(position));

        ItemComment item = mItems.get(position);

        OkHttpClientHelper.getPicassoBuilder(context).load(item.getProfile_url()).resize(100, 100).into(holder.imageView);


        //holder.mMemberName.setTypeface(font_roboto_medium);
        holder.mMemberName.setText(item.getMember_name());


        //mCommentText = (TextView) convertView.findViewById(R.id.textView_comment);
        //mCommentText.setTypeface(font_roboto_thin);
        holder.mCommentText.setText(item.getMessage());

        //mCommentDate = (TextView) convertView.findViewById(R.id.textView_comment_date);
        //mCommentDate.setTypeface(font_roboto_light);
        holder.mCommentDate.setText(item.getDate());


    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void setItems(List<ItemComment> items) {
        mItems = items;
    }
}
