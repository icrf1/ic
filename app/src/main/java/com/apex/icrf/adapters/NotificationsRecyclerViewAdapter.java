package com.apex.icrf.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.apex.icrf.R;
import com.apex.icrf.classes.ItemPushNotificationsTable;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 12/02/16.
 */
public class NotificationsRecyclerViewAdapter extends RecyclerView.Adapter<NotificationsRecyclerViewAdapter.ViewHolder> {
    private String[] mDataset;
    private List<ItemPushNotificationsTable> mItems = new ArrayList<>();
    private Context context;

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {
        // each data item is just a string in this case
        public TextView mTextView;
        public ImageView mImageView;

        public ViewHolder(View v) {
            super(v);
            mTextView = (TextView) v.findViewById(R.id.text_notification);
            mImageView = (ImageView) v.findViewById(R.id.imageView_notification);
        }
    }

    // Provide a suitable constructor (depends on the kind of dataset)
    public NotificationsRecyclerViewAdapter(Context context, List<ItemPushNotificationsTable> items) {
        mItems = items;
        this.context = context;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NotificationsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                                          int viewType) {
        // create a new view
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_fragment_notifications, parent, false);
        // set the view's size, margins, paddings and layout parameters

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        if (mItems.get(position).getPush_image().equalsIgnoreCase("")) {
            holder.mImageView.setVisibility(View.GONE);
            //Picasso.with(context).load(R.drawable.ic_splash_new).into(holder.mImageView);
        } else {
            holder.mImageView.setVisibility(View.VISIBLE);
            Picasso.with(context).load(mItems.get(position).getPush_image()).into(holder.mImageView);
        }

        holder.mTextView.setText(mItems.get(position).getPush_message());

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
