package com.oneuphero.betternotifications.helpers;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;

import com.j256.ormlite.android.apptools.OrmLiteSqliteOpenHelper;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.TableUtils;
import com.oneuphero.betternotifications.models.StoredNotification;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Created by Serenity on 6/10/14.
 */
public class DatabaseHelper extends OrmLiteSqliteOpenHelper {
    private static final String DATABASE_NAME = "better_notifications.db";
    private static final int DATABASE_VERSION = 1;
    private final String TAG = getClass().getSimpleName();

    private Dao<StoredNotification, Integer> storedNotificationDao;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource) {
        try {
            TableUtils.createTable(connectionSource, StoredNotification.class);
            Log.i(TAG, "Table created for StoredNotification");
        } catch (SQLException e) {
            Log.e(TAG, "Could not create new table for StoredNotification", e);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, ConnectionSource connectionSource, int oldVersion, int newVersion) {
        try {
            // TODO: before releasing to production, change this to proper upgrade procedure
            TableUtils.dropTable(connectionSource, StoredNotification.class, true);
            onCreate(sqLiteDatabase, connectionSource);
        } catch (SQLException e) {
            Log.e(TAG, "Could not upgrade table for StoredNotification", e);
        }
    }

    public Dao<StoredNotification, Integer> getStoredNotificationDao() throws SQLException {
        if (storedNotificationDao == null) {
            storedNotificationDao = getDao(StoredNotification.class);
        }
        return storedNotificationDao;
    }

    @Override
    public void close() {
        super.close();
        storedNotificationDao = null;
    }
}
