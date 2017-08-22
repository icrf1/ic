package com.apex.icrf.contacts;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.apex.icrf.Const;
import com.apex.icrf.R;
import com.apex.icrf.classes.IGroupsGroupMembersListener;
import com.apex.icrf.classes.ItemGroupsTable;
import com.apex.icrf.database.DatabaseHelper;
import com.apex.icrf.database.GroupsTableDbAdapter;
import com.apex.icrf.utils.Profile;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by WASPVamsi on 08/10/15.
 */
public class GroupsGroupMembersFragment extends Fragment {

    Activity activity;
    Profile mProfile;
    GroupsTableDbAdapter mGroupsTableDbAdapter;
    GroupMembersAdapter mGroupMembersAdapter;

    ListView listView;

    List<ItemGroupsTable> mAlGroupMembers = new ArrayList<ItemGroupsTable>();
    ArrayList<Boolean> mAlIsItemChecked = new ArrayList<Boolean>();
    ArrayList<Boolean> mAlIsItemCheckedCopy = new ArrayList<Boolean>();
    ArrayList<String> mAlCheckedMembers = new ArrayList<String>();

    IGroupsGroupMembersListener mIGroupsGroupMembersListener;

    private SharedPreferences prefs;
    String group_name;

    Button mButtonGroupMembers;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = activity;

        if (activity instanceof IGroupsGroupMembersListener) {
            mIGroupsGroupMembersListener = (IGroupsGroupMembersListener) activity;
        } else {
            if (Const.DEBUGGING)
                Log.d(Const.DEBUG, "Exception in onAttach");
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_groups_group_members, container, false);
        mAlGroupMembers.clear();
        mAlIsItemCheckedCopy.clear();

        mProfile = new Profile(activity);
        prefs = PreferenceManager.getDefaultSharedPreferences(activity);
        mGroupsTableDbAdapter = DatabaseHelper.get(activity).getGroupsTableDbAdapter();

        Bundle bundle = getArguments();
        if (bundle != null) {
            group_name = bundle.getString(Const.Bundle.GROUP_NAME);
        }

        listView = (ListView) view.findViewById(R.id.listView_group_members);
        mGroupMembersAdapter = new GroupMembersAdapter();
        listView.setAdapter(mGroupMembersAdapter);


        mButtonGroupMembers = (Button) view.findViewById(R.id.button_group_members);
        mButtonGroupMembers.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //getCheckedContacts();
                //mIGroupsGroupMembersListener.onGroupMembersButtonClicked(mAlCheckedMembers);

            }
        });

        return view;
    }

    private void getCheckedContacts() {

        mAlCheckedMembers.clear();

        for (int i = 0; i < mAlIsItemChecked.size(); i++) {

            if (mAlIsItemChecked.get(i)) {

                mAlCheckedMembers.add(mAlGroupMembers.get(i).getContact_number());
            }
        }

    }

    public class GroupMembersAdapter extends BaseAdapter {

        public GroupMembersAdapter() {

        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public int getCount() {
            return mAlGroupMembers.size();
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {

            ItemGroupsTable item = mAlGroupMembers.get(position);

            final ViewHolder holder;

            if (convertView == null) {
                convertView = activity.getLayoutInflater().inflate(R.layout.item_groups_group_member, null);

                holder = new ViewHolder();
                holder.mTextViewName = (TextView) convertView.findViewById(R.id.textView_name);
                holder.mTextViewPhoneNumber = (TextView) convertView.findViewById(R.id.textView_phone_number);
                holder.mCheckBoxGroupMembers = (CheckBox) convertView.findViewById(R.id.checkBox_group_members);
                holder.mImageButtonDelete = (ImageButton) convertView.findViewById(R.id.imageButton_group_members);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mTextViewName.setText(item.getContact_name());
            holder.mTextViewPhoneNumber.setText(item.getContact_number());
            holder.mCheckBoxGroupMembers.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    if (isChecked)
                        mAlIsItemChecked.set(position, true);
                    else
                        mAlIsItemChecked.set(position, false);

                    mButtonGroupMembers.setText("Contacts Selected - " + checkedContacts());

                }
            });

            holder.mCheckBoxGroupMembers.setChecked(mAlIsItemChecked.get(position));

            holder.mImageButtonDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    deleteContactAtPosition(position);
                }
            });

            return convertView;
        }

        private int checkedContacts() {

            int count = 0;
            for (int i = 0; i < mAlIsItemChecked.size(); i++) {
                if (mAlIsItemChecked.get(i))
                    count++;
            }

            return count;
        }


        public void deleteContactAtPosition(int position) {

            String phone = mAlGroupMembers.get(position).getContact_number();
            mGroupsTableDbAdapter.deleteContactFromGroup(phone, group_name);

            mAlGroupMembers.remove(position);

            if (mAlIsItemChecked.size() > position)
                mAlIsItemChecked.remove(position);

            if (mAlIsItemCheckedCopy.size() > position)
                mAlIsItemCheckedCopy.remove(position);

            mGroupMembersAdapter.notifyDataSetChanged();
        }
    }

    public static class ViewHolder {

        TextView mTextViewName, mTextViewPhoneNumber;
        CheckBox mCheckBoxGroupMembers;
        ImageButton mImageButtonDelete;
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_groups_group_members, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_add) {

            mAlIsItemCheckedCopy.clear();

            for (int i = 0; i < mAlIsItemChecked.size(); i++)
                mAlIsItemCheckedCopy.add(mAlIsItemChecked.get(i));

            mIGroupsGroupMembersListener.onAddGroupMembersClicked(prefs.getString(Const.Prefs.CURRENT_GROUP_NAME, ""));
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onResume() {
        super.onResume();

        if (group_name.equalsIgnoreCase(""))
            group_name = prefs.getString(Const.Prefs.CURRENT_GROUP_NAME, "");

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "GroupsGroupMembersFragment -> onResume -> Group Name: " + group_name);

        mAlGroupMembers = mGroupsTableDbAdapter.getDataForGroupName(mProfile.getMemberId(), group_name);

        for (int i = 0; i < mAlGroupMembers.size(); i++)
            mAlIsItemChecked.add(false);

        if (mAlIsItemCheckedCopy.size() > 0) {
            for (int j = 0; j < mAlIsItemCheckedCopy.size(); j++) {
                if (mAlIsItemCheckedCopy.get(j))
                    mAlIsItemChecked.set(j, true);

            }

        }

        if (Const.DEBUGGING)
            Log.d(Const.DEBUG, "Checked List: " + mAlIsItemChecked.toString());

        mGroupMembersAdapter.notifyDataSetChanged();
    }


}
