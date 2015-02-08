package com.oneuphero.betternotifications.models;

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

}
