package com.apex.icrf.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apex.icrf.R;

public class ProfileExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private LayoutInflater inflater;

    String[] mMainCategories = {"Personal Details",
            "Address Book", "Bank Details"};

    private int group_position = -1;
    private int child_position = -1;


    public ProfileExpandableListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
            return childPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
                             boolean isLastChild, View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater
                    .inflate(R.layout.expandable_navigation_drawer_sub_item, parent, false);
        }

//        ImageView image = (ImageView) convertView.findViewById(R.id.icon);
//        image.setImageDrawable(getImageDrawable(groupPosition, childPosition, false));
//
//        TextView title = (TextView) convertView.findViewById(R.id.title);
//        title.setText(getChild(groupPosition, childPosition).toString());
//
//        if (child_position != -1 && groupPosition == group_position && childPosition == child_position) {
//            image.setImageDrawable(getImageDrawable(groupPosition, childPosition, true));
//            title.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
//            title.setTypeface(null, Typeface.BOLD);
//        } else {
//            image.setImageDrawable(getImageDrawable(groupPosition, childPosition, false));
//            title.setTextColor(ContextCompat.getColor(context, android.R.color.black));
//            title.setTypeface(null, Typeface.NORMAL);
//        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return groupPosition;
    }

    @Override
    public int getChildType(int groupPosition, int childPosition) {
        return super.getChildType(groupPosition, childPosition);
    }

    @Override
    public int getChildTypeCount() {
        return super.getChildTypeCount();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mMainCategories[groupPosition];
    }

    @Override
    public int getGroupCount() {
        return mMainCategories.length;
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
                             View convertView, ViewGroup parent) {

        if (convertView == null) {
            convertView = inflater.inflate(
                    R.layout.expandable_profile_group_item, parent, false);
        }

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setText(getGroup(groupPosition).toString());

        ImageView dropdown = (ImageView) convertView.findViewById(R.id.dropdown);

        if (getChildrenCount(groupPosition) == 0)
            dropdown.setVisibility(View.GONE);

        if (isExpanded) {
            title.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
            title.setTypeface(null, Typeface.BOLD);
            dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.up_arrow_expanded));
        } else {

            if (group_position != -1 && groupPosition == group_position) {
                title.setTextColor(ContextCompat.getColor(context, R.color.default_blue));
                title.setTypeface(null, Typeface.BOLD);
                dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.down_arrow_expanded));
            } else {
                title.setTextColor(ContextCompat.getColor(context, android.R.color.black));
                title.setTypeface(null, Typeface.NORMAL);
                dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.down_arrow_default));
            }

        }

        return convertView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}