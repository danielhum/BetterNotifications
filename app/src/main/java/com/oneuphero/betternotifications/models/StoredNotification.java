package com.oneuphero.betternotifications.models;

import android.app.Notification;
import android.service.notification.StatusBarNotification;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

/**
 * Created by Serenity on 6/10/14.
 */

@DatabaseTable(tableName = "stored_notifications")
public class StoredNotification {

    @DatabaseField(generatedId = true)
    private Integer id;
    @DatabaseField
    private int notification_id;  // id supplied to notify(int, Notification)
    @DatabaseField
    private String package_name;
    @DatabaseField
    private String title;
    @DatabaseField
    private String body;
    @DatabaseField
    private Boolean dismissed = false;
    @DatabaseField
    private String statusBarNotificationKey;

    public StoredNotification() {
        // ORMLite needs a no-arg constructor
        this.dismissed = false;
    }

    public StoredNotification(int notification_id, String package_name, String title, String body) {
        this.notification_id = notification_id;
        this.package_name = package_name;
        this.title = title;
        this.body = body;
    }

    public StoredNotification(StatusBarNotification sbn) {
        this.notification_id = sbn.getId();
        this.package_name = sbn.getPackageName();
        Notification n = sbn.getNotification();
        this.title = n.tickerText == null ? null : n.tickerText.toString();
        this.body = null;
        this.statusBarNotificationKey = sbn.getKey();
    }

    @Override
    public String toString() {
        String printPackageName = package_name == null ? "" : package_name;
        String printTitle = title == null ? "" : title;
        return "[" + String.valueOf(notification_id) + "] " + printPackageName + " " + printTitle;
    }

    public int getNotificationId() {
        return notification_id;
    }

    public Boolean getDismissed() {
        return dismissed;
    }
    public void setDismissed(Boolean dismissed) {
        this.dismissed = dismissed;
    }
    public String getBody(){ return body; }

    public String getTitle() {
        return title;
    }

    public String getPackageName() {
        return package_name;
    }
}
