package com.oneuphero.betternotifications;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.j256.ormlite.dao.Dao;
import com.oneuphero.betternotifications.helpers.DatabaseHelper;
import com.oneuphero.betternotifications.models.StoredNotification;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by Serenity on 4/11/15.
 */
public class StoredNotificationsAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private DatabaseHelper mDbHelper;
    HashMap<String, List<StoredNotification>> mStoredNotifications = new HashMap<>();
    ArrayList<String> mIndex = new ArrayList<>();

    public StoredNotificationsAdapter(Context context) {
        mContext = context;
        mDbHelper = new DatabaseHelper(mContext);
        List<StoredNotification> storedNotifications = new ArrayList<>();
        try {
            storedNotifications = mDbHelper.getStoredNotificationDao()
                    .queryForAll();
        } catch (SQLException e) {
            e.printStackTrace();
            storedNotifications = new ArrayList<>();
        }
        for(StoredNotification sn : storedNotifications) {
            String packageName = sn.getPackageName();
            List<StoredNotification> nList = mStoredNotifications.get(packageName);
            if (nList == null) {
                nList = new ArrayList<>();
                mStoredNotifications.put(packageName, nList);
                mIndex.add(packageName);
            }
            nList.add(sn);
        }
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_group_main, null);
        }

        TextView titleText = (TextView) convertView.findViewById(R.id.titleText);
        titleText.setText(mIndex.get(groupPosition));

        return convertView;
    }

    @Override
    public int getGroupCount() {
        return mIndex.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mIndex.get(groupPosition);
    }

    @Override
    public View getChildView(int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.list_item_main, null);
        }

        TextView contentView = (TextView) convertView.findViewById(R.id.contentText);
        StoredNotification sn = getStoredNotifications(groupPosition).get(childPosition);
        String contentString = "[" + sn.getNotificationId() + "] " + sn.getTitle();
        if (sn.getDismissed()) {
            contentString += " (dismissed)";
        }
        contentView.setText(contentString);

        return convertView;
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return getStoredNotifications(groupPosition).size();
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public Object getChild(int groupPosition, int  childPosition) {
        return getStoredNotifications(groupPosition).get(childPosition);
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    private List<StoredNotification> getStoredNotifications(int groupPosition) {
        String packageName = mIndex.get(groupPosition);
        List<StoredNotification> notifications = mStoredNotifications.get(packageName);
        if (notifications != null) {
            return notifications;
        } else {
            return new ArrayList<>();
        }
    }

}
