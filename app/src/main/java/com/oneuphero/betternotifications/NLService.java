package com.oneuphero.betternotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.nfc.Tag;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.j256.ormlite.dao.Dao;
import com.oneuphero.betternotifications.helpers.DatabaseHelper;
import com.oneuphero.betternotifications.models.StoredNotification;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Serenity on 28/9/14.
 */
public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    private final int NOTIFICATION_INITIAL = 0;  // initial notification to trigger service
    private boolean mCollectedAllNotifications = false;
    private DatabaseHelper mDbHelper = null;
    private Dao<StoredNotification, Integer> mDao = null;

    @Override
    public void onCreate() {
        super.onCreate();
        // workaround for bug where getActiveNotifications returns null if called before
        // notification callback triggered
        Handler handler = new Handler();
        final Context context = this;
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
                Notification.Builder nBuilder = new Notification.Builder(context);
                nBuilder.setContentTitle("Better Notifications");
                nBuilder.setContentText("starting service...");
                nBuilder.setTicker("starting service...");
                nBuilder.setSmallIcon(R.drawable.ic_launcher);
                nBuilder.setAutoCancel(true);
                nManager.notify(TAG, NOTIFICATION_INITIAL, nBuilder.build());
            }
        }, 1000);
    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "Posted #" + sbn.getId() + " " + sbn.getNotification().tickerText + " " + sbn.getPackageName());
        //TODO: this is throwing null error?
        if(sbn.getTag() != null && sbn.getTag().equals(TAG) && sbn.getId() == NOTIFICATION_INITIAL) {
            cancelNotification(sbn.getPackageName(), TAG, NOTIFICATION_INITIAL);
            Log.i(TAG, "dismissed notification");
        } else {
            storeStatusBarNotification(sbn);
        }
        if (!mCollectedAllNotifications) {
            mCollectedAllNotifications = true;
            StatusBarNotification[] sbns = getActiveNotifications();
            for (StatusBarNotification activeSbn : sbns) {
                Log.i(TAG, "NOTIFICATION: " + activeSbn.getNotification().tickerText);
                storeStatusBarNotification(activeSbn);
            }
        }
    }

    private void storeStatusBarNotification(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        int notificationId = sbn.getId();
        String packageName = sbn.getPackageName();
        try {
            Log.d(TAG, "querying for: " + String.valueOf(notificationId) + " " + packageName);
            List<StoredNotification> notifications = getDao().queryBuilder().where()
                                                               .eq("notification_id", notificationId)
                                                               .and()
                                                               .eq("package_name", packageName).query();
            if (notifications.isEmpty()) {
                String title = n.tickerText == null ? null : n.tickerText.toString();
                getDao().create(new StoredNotification(notificationId, packageName, title, null));
                Log.d(TAG, "stored notification! #" + notificationId + " " + title);
            } else {
                Log.d(TAG, "found notifications: ");
                for(StoredNotification sn : notifications) {
                    Log.d(TAG, sn.toString() + " " + "dismissed: " + String.valueOf(sn.getDismissed()));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "Removed #" + sbn.getId() + " " + sbn.getNotification().tickerText + " " + sbn.getPackageName());
        try {
            List<StoredNotification> notifications = getDao().queryBuilder().where()
                    .eq("notification_id", sbn.getId())
                    .and()
                    .eq("package_name", sbn.getPackageName()).query();
            if(!notifications.isEmpty()) {
                for (StoredNotification n : notifications) {
                    n.setDismissed(true);
                    getDao().createOrUpdate(n);
                    Log.d(TAG, "marked #" + String.valueOf(n.getNotificationId()) + " as dismissed");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DatabaseHelper getDbHelper() {
        if (mDbHelper == null) {
            mDbHelper = new DatabaseHelper(this);
        }
        return mDbHelper;
    }

    private Dao<StoredNotification, Integer> getDao() throws SQLException {
        return getDbHelper().getStoredNotificationDao();
    }
}
