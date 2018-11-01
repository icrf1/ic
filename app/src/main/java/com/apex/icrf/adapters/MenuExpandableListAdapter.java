package com.apex.icrf.adapters;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.utils.OkHttpClientHelper;
import com.apex.icrf.utils.TypeFaceHelper;

public class MenuExpandableListAdapter extends BaseExpandableListAdapter {

    private Context context;
    private LayoutInflater inflater;

    String[] mMainCategories = {"Home", "My Activity", "Post New Petition", "Success Petitions", "Notifications", "Donate",  "More"};
    String[] mSubCategoryMore = { "Rate Us", "Feedback", "Share", "About ICRF", "How it works", "Check for Update"};
    String[] mSubCategoryNotifications = {"Notice Board", "Latest News"};

    private int group_position = -1;
    private int child_position = -1;

    Typeface font_robot_medium;

    public MenuExpandableListAdapter(Context context) {
        this.context = context;
        inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

//        font_robot_medium = Typeface.createFromAsset(context.getAssets(),
//                "fonts/Roboto-Bold.ttf");

        font_robot_medium = TypeFaceHelper.getTypeFace(context, "Roboto-Bold");
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {

        if (groupPosition == Const.MENULIST.MORE) {
            return mSubCategoryMore[childPosition];
        } else if (groupPosition == Const.MENULIST.NOTIFICATIONS) {
            return mSubCategoryNotifications[childPosition];
        } else {
            return "";
        }
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

        ImageView image = (ImageView) convertView.findViewById(R.id.icon);
        //image.setImageDrawable(getImageDrawable(groupPosition, childPosition, false));

        OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, childPosition, false)).resize(100, 100).into(image);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setTypeface(font_robot_medium);
        title.setText(getChild(groupPosition, childPosition).toString());

        if (child_position != -1 && groupPosition == group_position && childPosition == child_position) {
            //image.setImageDrawable(getImageDrawable(groupPosition, childPosition, true));
            OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, childPosition, true)).resize(100, 100).into(image);

            title.setTextColor(ContextCompat.getColor(context, R.color.whatsapp_color_modified));
        } else {
            //image.setImageDrawable(getImageDrawable(groupPosition, childPosition, false));
            OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, childPosition, false)).resize(100, 100).into(image);

            title.setTextColor(ContextCompat.getColor(context, R.color.text_grey_color));
        }

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (groupPosition == Const.MENULIST.MORE) {
            return mSubCategoryMore.length;
        } else if (groupPosition == Const.MENULIST.NOTIFICATIONS) {
            return mSubCategoryNotifications.length;
        } else {
            return 0;
        }
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
                    R.layout.expandable_navigation_drawer_group_item, parent, false);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.icon);
        //image.setImageDrawable(getImageDrawable(groupPosition, false));
        OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, false)).resize(100, 100).into(image);

        TextView title = (TextView) convertView.findViewById(R.id.title);
        title.setTypeface(font_robot_medium);
        title.setText(getGroup(groupPosition).toString());

        ImageView dropdown = (ImageView) convertView.findViewById(R.id.dropdown);

        if (getChildrenCount(groupPosition) == 0)
            dropdown.setVisibility(View.GONE);

        if (isExpanded) {
            //image.setImageDrawable(getImageDrawable(groupPosition, true));
            OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, true)).resize(100, 100).into(image);

            title.setTextColor(ContextCompat.getColor(context, R.color.whatsapp_color_modified));
            //title.setTypeface(null, Typeface.BOLD);

            //dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_up_green));
            OkHttpClientHelper.getPicassoBuilder(context).load(R.drawable.ic_arrow_up_green).resize(100, 100).into(dropdown);
        } else {

            if (group_position != -1 && groupPosition == group_position) {
                //image.setImageDrawable(getImageDrawable(groupPosition, true));
                OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, true)).resize(100, 100).into(image);

                title.setTextColor(ContextCompat.getColor(context, R.color.whatsapp_color_modified));
                //title.setTypeface(null, Typeface.BOLD);

                //dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down_green));
                OkHttpClientHelper.getPicassoBuilder(context).load(R.drawable.ic_arrow_down_green).resize(100, 100).into(dropdown);
            } else {
                //image.setImageDrawable(getImageDrawable(groupPosition, false));
                OkHttpClientHelper.getPicassoBuilder(context).load(getImageResource(groupPosition, false)).resize(100, 100).into(image);

                title.setTextColor(ContextCompat.getColor(context, R.color.text_grey_color));
                //title.setTypeface(null, Typeface.NORMAL);
                //dropdown.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_arrow_down_grey));
                OkHttpClientHelper.getPicassoBuilder(context).load(R.drawable.ic_arrow_down_grey).resize(100, 100).into(dropdown);
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


    // set positions for group and child here
    // and use notifyDataSetChanged to change the colors

    public void setGroup_position(int position) {
        group_position = position;
    }

    public void setChild_position(int position) {
        child_position = position;
    }

    private Drawable getImageDrawable(int groupPosition, boolean isExpanded) {

        if (isExpanded) {

            if (groupPosition == Const.MENULIST.HOME)
                return ContextCompat.getDrawable(context, R.drawable.ic_home_green);
            else if (groupPosition == Const.MENULIST.MY_ACTIVITY)
                return ContextCompat.getDrawable(context, R.drawable.ic_my_activity_green);
            else if (groupPosition == Const.MENULIST.POST_NEW_PETITION)
                return ContextCompat.getDrawable(context, R.drawable.drawable_post_petition_green_512);
            else if (groupPosition == Const.MENULIST.SUCCESS_PETITIONS)
                return ContextCompat.getDrawable(context, R.drawable.ic_success_petitions_green_new);
            else if (groupPosition == Const.MENULIST.DONATE)
                return ContextCompat.getDrawable(context, R.drawable.ic_donate_green);
            else if (groupPosition == Const.MENULIST.NOTIFICATIONS)
                return ContextCompat.getDrawable(context, R.drawable.ic_notifications_green);
//            else
//                if (groupPosition == Const.MENULIST.MY_EARNINGS)
//                return ContextCompat.getDrawable(context, R.drawable.ic_my_earnings_green);
            else
                if (groupPosition == Const.MENULIST.MORE)
                return ContextCompat.getDrawable(context, R.drawable.ic_more_green);
        } else {

            if (groupPosition == Const.MENULIST.HOME)
                return ContextCompat.getDrawable(context, R.drawable.ic_home_grey);
            else if (groupPosition == Const.MENULIST.MY_ACTIVITY)
                return ContextCompat.getDrawable(context, R.drawable.ic_my_activity_grey);
            else if (groupPosition == Const.MENULIST.POST_NEW_PETITION)
                return ContextCompat.getDrawable(context, R.drawable.drawable_post_petition_grey_512);
            else if (groupPosition == Const.MENULIST.SUCCESS_PETITIONS)
                return ContextCompat.getDrawable(context, R.drawable.ic_success_petitions_grey_new);
            else if (groupPosition == Const.MENULIST.DONATE)
                return ContextCompat.getDrawable(context, R.drawable.ic_donate_grey);
            else if (groupPosition == Const.MENULIST.NOTIFICATIONS)
                return ContextCompat.getDrawable(context, R.drawable.ic_notifications_grey);
//            else if (groupPosition == Const.MENULIST.MY_EARNINGS)
//                return ContextCompat.getDrawable(context, R.drawable.ic_my_earnings_grey);
            else if (groupPosition == Const.MENULIST.MORE)
                return ContextCompat.getDrawable(context, R.drawable.ic_more_grey);
        }

        return ContextCompat.getDrawable(context, R.drawable.ic_home_grey);
    }

    private Drawable getImageDrawable(int groupPosition, int childPosition, boolean isExpanded) {

        if (isExpanded) {

            if (groupPosition == Const.MENULIST.MORE) {

//                if (childPosition == Const.MENULIST.BANK_DETAILS)
//                    return ContextCompat.getDrawable(context, R.drawable.ic_bank_details_green);
//                else
                    if (childPosition == Const.MENULIST.MORE_RATE_US)
                    return ContextCompat.getDrawable(context, R.drawable.ic_rate_us_green);
                else if (childPosition == Const.MENULIST.MORE_FEEDBACK)
                    return ContextCompat.getDrawable(context, R.drawable.ic_feedback_green);
                else if (childPosition == Const.MENULIST.MORE_INVITE)
                    return ContextCompat.getDrawable(context, R.drawable.ic_share_green);
                else if (childPosition == Const.MENULIST.MORE_ABOUT_ICRF)
                    return ContextCompat.getDrawable(context, R.drawable.ic_about_icrf_green);
                else if (childPosition == Const.MENULIST.MORE_HOW_IT_WORKS)
                    return ContextCompat.getDrawable(context, R.drawable.ic_how_it_works_green);
                else if (childPosition == Const.MENULIST.MORE_CHECK_UPDATE)
                    return ContextCompat.getDrawable(context, R.drawable.ic_check_for_updates_green);
            }

        } else {

            if (groupPosition == Const.MENULIST.MORE) {

//                if (childPosition == Const.MENULIST.BANK_DETAILS)
//                    return ContextCompat.getDrawable(context, R.drawable.ic_bank_details_grey);
//                else
                    if (childPosition == Const.MENULIST.MORE_RATE_US)
                    return ContextCompat.getDrawable(context, R.drawable.ic_rate_us_grey);
                else if (childPosition == Const.MENULIST.MORE_FEEDBACK)
                    return ContextCompat.getDrawable(context, R.drawable.ic_feedback_grey);
                else if (childPosition == Const.MENULIST.MORE_INVITE)
                    return ContextCompat.getDrawable(context, R.drawable.ic_share_grey);
                else if (childPosition == Const.MENULIST.MORE_ABOUT_ICRF)
                    return ContextCompat.getDrawable(context, R.drawable.ic_about_icrf_grey);
                else if (childPosition == Const.MENULIST.MORE_HOW_IT_WORKS)
                    return ContextCompat.getDrawable(context, R.drawable.ic_how_it_works_grey);
                else if (childPosition == Const.MENULIST.MORE_CHECK_UPDATE)
                    return ContextCompat.getDrawable(context, R.drawable.ic_check_for_updates_grey);
            }

        }

        return ContextCompat.getDrawable(context, R.drawable.ic_about_icrf_grey);
    }

    private int getImageResource(int groupPosition, int childPosition, boolean isExpanded) {

        if (isExpanded) {

            if (groupPosition == Const.MENULIST.MORE) {

//                if (childPosition == Const.MENULIST.BANK_DETAILS)
//                    return R.drawable.ic_bank_details_green;
//                else
                    if (childPosition == Const.MENULIST.MORE_RATE_US)
                    return R.drawable.ic_rate_us_green;
                else if (childPosition == Const.MENULIST.MORE_FEEDBACK)
                    return R.drawable.ic_feedback_green;
                else if (childPosition == Const.MENULIST.MORE_INVITE)
                    return R.drawable.ic_share_green;
                else if (childPosition == Const.MENULIST.MORE_ABOUT_ICRF)
                    return R.drawable.ic_about_icrf_green;
                else if (childPosition == Const.MENULIST.MORE_HOW_IT_WORKS)
                    return R.drawable.ic_how_it_works_green;
                else if (childPosition == Const.MENULIST.MORE_CHECK_UPDATE)
                    return R.drawable.ic_check_for_updates_green;
            } else if(groupPosition == Const.MENULIST.NOTIFICATIONS) {
                if (childPosition == Const.MENULIST.NOTIFICATIONS_NOTICE_BOARD)
                    return R.drawable.drawable_notice_board_512_green;
                else if (childPosition == Const.MENULIST.NOTIFICATIONS_NEWS)
                    return R.drawable.drawable_latest_news_512_green;
            }

        } else {

            if (groupPosition == Const.MENULIST.MORE) {

//                if (childPosition == Const.MENULIST.BANK_DETAILS)
//                    return R.drawable.ic_bank_details_grey;
//                else
                    if (childPosition == Const.MENULIST.MORE_RATE_US)
                    return R.drawable.ic_rate_us_grey;
                else if (childPosition == Const.MENULIST.MORE_FEEDBACK)
                    return R.drawable.ic_feedback_grey;
                else if (childPosition == Const.MENULIST.MORE_INVITE)
                    return R.drawable.ic_share_grey;
                else if (childPosition == Const.MENULIST.MORE_ABOUT_ICRF)
                    return R.drawable.ic_about_icrf_grey;
                else if (childPosition == Const.MENULIST.MORE_HOW_IT_WORKS)
                    return R.drawable.ic_how_it_works_grey;
                else if (childPosition == Const.MENULIST.MORE_CHECK_UPDATE)
                    return R.drawable.ic_check_for_updates_grey;
            } else if(groupPosition == Const.MENULIST.NOTIFICATIONS) {
                if (childPosition == Const.MENULIST.NOTIFICATIONS_NOTICE_BOARD)
                    return R.drawable.drawable_notice_board_512_grey;
                else if (childPosition == Const.MENULIST.NOTIFICATIONS_NEWS)
                    return R.drawable.drawable_latest_news_512_grey;
            }

        }

        return R.drawable.ic_about_icrf_grey;
    }

    private int getImageResource(int groupPosition, boolean isExpanded) {

        if (isExpanded) {

            if (groupPosition == Const.MENULIST.HOME)
                return R.drawable.ic_home_green;
            else if (groupPosition == Const.MENULIST.MY_ACTIVITY)
                return R.drawable.ic_my_activity_green;
            else if (groupPosition == Const.MENULIST.POST_NEW_PETITION)
                return R.drawable.drawable_post_petition_green_512;
            else if (groupPosition == Const.MENULIST.SUCCESS_PETITIONS)
                return R.drawable.ic_success_petitions_green_new;
            else if (groupPosition == Const.MENULIST.DONATE)
                return R.drawable.ic_donate_green;
            else if (groupPosition == Const.MENULIST.NOTIFICATIONS)
                return R.drawable.ic_notifications_green;
//            else if (groupPosition == Const.MENULIST.MY_EARNINGS)
//                return R.drawable.ic_my_earnings_green;
            else if (groupPosition == Const.MENULIST.MORE)
                return R.drawable.ic_more_green;
        } else {

            if (groupPosition == Const.MENULIST.HOME)
                return R.drawable.ic_home_grey;
            else if (groupPosition == Const.MENULIST.MY_ACTIVITY)
                return R.drawable.ic_my_activity_grey;
            else if (groupPosition == Const.MENULIST.POST_NEW_PETITION)
                return R.drawable.drawable_post_petition_grey_512;
            else if (groupPosition == Const.MENULIST.SUCCESS_PETITIONS)
                return R.drawable.ic_success_petitions_grey_new;
            else if (groupPosition == Const.MENULIST.DONATE)
                return R.drawable.ic_donate_grey;
            else if (groupPosition == Const.MENULIST.NOTIFICATIONS)
                return R.drawable.ic_notifications_grey;
//            else if (groupPosition == Const.MENULIST.MY_EARNINGS)
//                return R.drawable.ic_my_earnings_grey;
            else if (groupPosition == Const.MENULIST.MORE)
                return R.drawable.ic_more_grey;
        }

        return R.drawable.ic_home_grey;
    }

}