package com.oneuphero.betternotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Handler;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Log;

/**
 * Created by Serenity on 28/9/14.
 */
public class NLService extends NotificationListenerService {
    private String TAG = this.getClass().getSimpleName();
    private final int NOTIFICATION_INITIAL = 0;  // initial notification to trigger service
    private boolean mCollectedAllNotifications = false;

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
                nBuilder.setContentText("starting service...");
                nBuilder.setSmallIcon(R.drawable.ic_launcher);
                nBuilder.setAutoCancel(true);
                nManager.notify(TAG, NOTIFICATION_INITIAL, nBuilder.build());
            }
        }, 1000);    }

    public void onNotificationPosted(StatusBarNotification sbn) {
        Log.i(TAG, "Posted ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
        //TODO: this is throwing null error?
        if(sbn.getTag() != null && sbn.getTag().equals(TAG) && sbn.getId() == NOTIFICATION_INITIAL) {
            cancelNotification(sbn.getPackageName(), TAG, NOTIFICATION_INITIAL);
            Log.i(TAG, "dismissed notification");
        }
        if (!mCollectedAllNotifications) {
            mCollectedAllNotifications = true;
            StatusBarNotification[] sbns = getActiveNotifications();
            for (StatusBarNotification activeSbn : sbns) {
                Log.i(TAG, "NOTIFICATION: " + activeSbn.getNotification().tickerText);
            }
        }
    }

    public void onNotificationRemoved(StatusBarNotification sbn) {
        Log.i(TAG, "Removed ID :" + sbn.getId() + "\t" + sbn.getNotification().tickerText + "\t" + sbn.getPackageName());
    }
}
