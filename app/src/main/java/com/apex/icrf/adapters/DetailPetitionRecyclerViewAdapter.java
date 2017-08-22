package com.apex.icrf.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.utils.OkHttpClientHelper;

import java.util.ArrayList;

/**
 * Created by WASPVamsi on 30/10/15.
 */
public class DetailPetitionRecyclerViewAdapter extends RecyclerView.Adapter<DetailPetitionRecyclerViewAdapter.ViewHolder> {

    Context context;
    ArrayList<String> mUrls = new ArrayList<String>();

    public DetailPetitionRecyclerViewAdapter(Context context, ArrayList<String> urls) {
        this.context = context;
        this.mUrls.clear();
        this.mUrls = urls;

        if (Const.DEBUGGING) {
            Log.d(Const.DEBUG, "Urls Size: " + urls.size());
            Log.d(Const.DEBUG, urls.toString());
        }
    }

    @Override
    public DetailPetitionRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(context).inflate(R.layout.item_horizontal_recyclerview, parent, false);

        WindowManager windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        int width = windowManager.getDefaultDisplay().getWidth();
        //int height = windowManager.getDefaultDisplay().getHeight();
        view.setLayoutParams(new RecyclerView.LayoutParams(width, RecyclerView.LayoutParams.MATCH_PARENT));

        //view.setLayoutParams(new RecyclerView.LayoutParams(RecyclerView.LayoutParams.MATCH_PARENT, RecyclerView.LayoutParams.MATCH_PARENT));

        ViewHolder holder = new ViewHolder(view);
        return holder;
    }

    @Override
    public void onBindViewHolder(DetailPetitionRecyclerViewAdapter.ViewHolder holder, int position) {
        //Picasso.with(context).load(mUrls.get(position)).into(holder.mImageView);
        OkHttpClientHelper.getPicassoBuilder(context).load(mUrls.get(position)).into(holder.mImageView);

        if (position == 0) {
            holder.mImageViewLeft.setVisibility(View.GONE);
        } else {
            holder.mImageViewLeft.setVisibility(View.VISIBLE);
        }

        if (position == mUrls.size() - 1) {
            holder.mImageViewRight.setVisibility(View.GONE);
        } else {
            holder.mImageViewRight.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public int getItemCount() {
        return mUrls.size();
    }


    public class ViewHolder extends RecyclerView.ViewHolder {

        public ImageView mImageView, mImageViewLeft, mImageViewRight;


        public ViewHolder(View v) {
            super(v);
            this.mImageView = (ImageView) v.findViewById(R.id.imageView_petition_image);
            this.mImageViewLeft = (ImageView) v.findViewById(R.id.imageView_left);
            this.mImageViewRight = (ImageView) v.findViewById(R.id.imageView_right);
        }
    }

}
