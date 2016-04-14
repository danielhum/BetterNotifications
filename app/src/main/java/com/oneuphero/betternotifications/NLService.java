package com.oneuphero.betternotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

import com.getpebble.android.kit.PebbleKit;
import com.j256.ormlite.dao.Dao;
import com.oneuphero.betternotifications.helpers.DatabaseHelper;
import com.oneuphero.betternotifications.models.StoredNotification;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.SQLException;
import java.text.DateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Serenity on 28/9/14.
 */
public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    public static String PACKAGE_NAME;
    private final int NOTIFICATION_INITIAL = 0;  // initial notification to trigger service
    private boolean mCollectedAllNotifications = false;
    private DatabaseHelper mDbHelper = null;
    private Dao<StoredNotification, Integer> mDao = null;

    private Handler mHandler = new Handler();
    private long mLastNotified = 0;
    private static final int mMinWindow = 1000; // time in ms before notifying again
    private Runnable notifyPebble = new Runnable() {
        @Override
        public void run() {
            boolean connected = PebbleKit.isWatchConnected(getApplicationContext());
            if (connected) {
                final Intent i = new Intent("com.getpebble.action.SEND_NOTIFICATION");

                final Map data = new HashMap();
                data.put("title", "Message from Eliza");
                data.put("body", "@ " + DateFormat.getTimeInstance(DateFormat.SHORT).format(System.currentTimeMillis()));
                final JSONObject jsonData = new JSONObject(data);
                final String notificationData = new JSONArray().put(jsonData).toString();

                i.putExtra("messageType", "PEBBLE_ALERT");
                i.putExtra("sender", "BetterNotifications");
                i.putExtra("notificationData", notificationData);

                Log.d(TAG, "About to send a modal alert to Pebble: " + notificationData);
                sendBroadcast(i);
            }
        }
    };
    private int mNotificationsPostedCount = 0;
    private long mLastDismissed = 0;  // last time our notification was dismissed

    @Override
    public void onCreate() {
        super.onCreate();
        // workaround for bug where getActiveNotifications returns null if called before
        // notification callback triggered
        Handler handler = new Handler();
        final Context context = this;
        PACKAGE_NAME = getApplicationContext().getPackageName();
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
        if(isInitialNotification(sbn)) {
            cancelNotification(sbn.getKey());
//            Log.d(TAG, "dismissed notification");
        } else {
            processStatusBarNotification(sbn);
        }
        if (!mCollectedAllNotifications) {
            mCollectedAllNotifications = true;
            StatusBarNotification[] sbns = getActiveNotifications();
            for (StatusBarNotification activeSbn : sbns) {
                processStatusBarNotification(activeSbn);
            }
        }
    }

    private boolean isInitialNotification(StatusBarNotification sbn) {
        return sbn.getTag() != null && sbn.getTag().equals(TAG) && sbn.getId() == NOTIFICATION_INITIAL;
    }

    private void processStatusBarNotification(StatusBarNotification sbn) {
        String packageName = sbn.getPackageName();
        CharSequence ticker = sbn.getNotification().tickerText;
        Bundle extras = sbn.getNotification().extras;
        CharSequence title = extras.getCharSequence("android.title");
        CharSequence text = extras.getCharSequence("android.text");

        if (ticker == null) ticker = "";
        if (title == null) title = "";
        if (text == null) text = "";

        Log.d(TAG, sbn.getPackageName() + ":\t" + sbn.getNotification().tickerText);

        String targetPhrase = "Eliza";

        // DEBUG
        if (packageName.equals("com.whatsapp")) {
            Log.d(TAG, "whatsapp - \tandroid.title: " + title);
            Log.d(TAG, "whatsapp - \tandroid.text: " + text);
            Log.d(TAG, "whatsapp - \tticker: " + ticker);
        }

        if ((packageName.equals("com.whatsapp") || packageName.equals("com.google.android.apps.messaging")) &&
                //(title.toString().contains(targetPhrase) || ticker.toString().contains(targetPhrase) || text.toString().contains(targetPhrase))
                title.toString().contains(targetPhrase)
                ) {

            // check if have previous notifications
            int unreadCount = 0;
            List<StoredNotification> notifications = null;
            try {
                 notifications = getDao().queryBuilder().where()
                        .isNotNull("title")
                        .and()
                        .eq("dismissed", false)
                        .and()
                        .eq("package_name", PACKAGE_NAME).query();
                if (notifications.size() > 0) {
                    unreadCount = notifications.size();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }

            // DEBUG
            NotificationManager nManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Notification.Builder nBuilder = new Notification.Builder(getApplicationContext());
            String msg = text.toString();
            if (unreadCount > 0) {
                for (StoredNotification sn : notifications) {
                    Log.d(TAG, "sn: " + sn.getTitle());
                    msg += "\n" + sn.getTitle();
                }
            }
            nBuilder.setContentTitle(title + " (" + String.valueOf(unreadCount) + "/" + String.valueOf(mNotificationsPostedCount) + ")");
            nBuilder.setContentText(msg);
            nBuilder.setTicker(text);
            nBuilder.setContentIntent(sbn.getNotification().contentIntent);
            nBuilder.setSmallIcon(R.drawable.ic_launcher);
            nBuilder.setStyle(new Notification.BigTextStyle().bigText(msg));
            nBuilder.setAutoCancel(true);
            nManager.notify(TAG, NOTIFICATION_INITIAL+10+unreadCount+mNotificationsPostedCount, nBuilder.build());
            if (++mNotificationsPostedCount > 100) {
                mNotificationsPostedCount = 0;
            };
            Log.d(TAG, "whatsapp - posted custom notification!");

            long now = System.currentTimeMillis();
            if (now-mLastNotified > mMinWindow) {
                mHandler.postDelayed(notifyPebble, mMinWindow);
                mLastNotified = now;
            }

            storeStatusBarNotification(sbn);
        } else if (packageName.equals(PACKAGE_NAME)) {
            storeStatusBarNotification(sbn);
        }

        //storeStatusBarNotification(sbn); TODO: disable for now, restore when implementing this functionality
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
                StoredNotification storedNotification = new StoredNotification(sbn);
                getDao().create(storedNotification);
                Log.d(TAG, "stored notification! #" + notificationId + " " + storedNotification.getTitle());
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

        if (!isInitialNotification(sbn) && sbn.getPackageName().equals(PACKAGE_NAME)) {
            long dismissedTime = System.currentTimeMillis();
            if(dismissedTime - mLastDismissed < 2000) {
                ((NotificationManager) getSystemService(NOTIFICATION_SERVICE)).cancelAll();
            }
            mLastDismissed = dismissedTime;
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
