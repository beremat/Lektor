package com.nowakm.lektor;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Marcin on 4/16/2015.
 */
public class LektorSQLite extends SQLiteOpenHelper {
    public static final String DB_NAME = "Lektor.db";
    public static final int DB_VERSION = 1;

    public LektorSQLite (Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE Recordings (\n" +
                "id INT(32) NOT NULL auto_increment,\n" +
                "startTime INT(32) NOT NULL,\n" +
                "endTime INT(32),\n" +
                "filename VARCHAR(255) NOT NULL,\n" +
                "duration INT(32),\n" +
                "numNotes INT(32),\n" +
                "numPhotos TEXT,\n" +
                "courseID INT(32),\n" +
                "lektorVersion VARCHAR(64) NOT NULL,\n" +
                "primary KEY (id));");
        db.execSQL("CREATE TABLE Courses (\n" +
                "id INT(32) NOT NULL auto_increment,\n" +
                "createdTime INT(32) NOT NULL,\n" +
                "name VARCHAR(255) NOT NULL,\n" +
                "shortName VARCHAR(255),\n" +
                "color VARCHAR(255),\n" +
                "lektorVersion VARCHAR(64) NOT NULL,\n" +
                "primary KEY (id));");
        db.execSQL("CREATE TABLE Notes (\n" +
                "id INT(32) NOT NULL auto_increment,\n" +
                "belongsTo INT(32) NOT NULL,\n" +
                "createdTime INT(32) NOT NULL,\n" +
                "data TEXT,\n" +
                "displayTime INT(32),\n" +
                "lektorVersion VARCHAR(64) NOT NULL,\n" +
                "primary KEY (id));");
        db.execSQL("CREATE TABLE Photos (\n" +
                "id INT(32) NOT NULL auto_increment,\n" +
                "belongsTo INT(32) NOT NULL,\n" +
                "createdTime INT(32) NOT NULL,\n" +
                "URI VARCHAR(255) NOT NULL,\n" +
                "displayTime INT(32) NOT NULL,\n" +
                "lektorVersion VARCHAR(64) NOT NULL,\n" +
                "primary KEY (id));");
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //TODO: Write SQL to upgrade database
    }
}
