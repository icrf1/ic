package com.apex.icrf;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavLeftListAdapter extends BaseAdapter {

    Context context;
    String[] mTitle = {"Home", /*"ID Card",*/
            "My Petitions", "Petitions Verified By Me", "View All Petitions", "Post A Petition"/*, "Donate"*/};
    SharedPreferences prefs;
    LayoutInflater inflater;

    public NavLeftListAdapter(Context context) {
        this.context = context;
    }

    @Override
    public int getCount() {
        return mTitle.length;
    }

    @Override
    public Object getItem(int position) {
        return mTitle[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        TextView txtTitle;
        ImageView image;

        prefs = PreferenceManager.getDefaultSharedPreferences(context);

        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null)
            convertView = inflater.inflate(R.layout.navdrawer_item, parent,
                    false);

        // View itemView = inflater
        // .inflate(R.layout.navdrawer_item, parent, false);

        image = (ImageView) convertView.findViewById(R.id.icon);


        if (position == Const.NavLeftPositions.HOME_POSITION - 1)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_home_white_24dp));
//        else if(position == Const.NavLeftPositions.IDCARD_POSITION - 1)
//            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_assignment_ind_white_24dp));
        else if(position == Const.NavLeftPositions.VERFITY_MY_PETITIONS_POSITION - 1)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_cancel_white_24dp));
        else if(position == Const.NavLeftPositions.VERIFIED_PETIIONS_BY_ME - 1)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_white_24dp));
        else if(position == Const.NavLeftPositions.VERIFIED_PETITIONS_POSITION - 1)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_done_all_white_24dp));
        else if(position == Const.NavLeftPositions.POST_PETITION_POSITION - 1)
            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_assignment_white_24dp));
//        else if(position == Const.NavLeftPositions.DONATE_POSITION - 1)
//            image.setImageDrawable(context.getResources().getDrawable(R.drawable.ic_attach_money_white_24dp));

        txtTitle = (TextView) convertView.findViewById(R.id.title);
        txtTitle.setText(mTitle[position]);


        // if (prefs.getInt(Const.Prefs.MAININDEX, 0) == position) {
        // itemView.setBackgroundColor(Color.parseColor("#EDEDED"));
        // }

        return convertView;

    }

}
