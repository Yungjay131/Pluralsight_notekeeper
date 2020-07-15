package com.slyworks.pluralsight_notekeeper.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

/**
 * created by Joshua Sylvanus
 */
public class NoteKeeperOpenHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "Notekeeper.db";

    //this version is added once serious changes are made to the
    //database e.g adding Indexes to the database tables
    public static final int DATABASE_VERSION = 2;
    public NoteKeeperOpenHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
     db.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_TABLE);
     db.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_TABLE);

     //adding indexes to the 2 tables
     db.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
     db.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);

    //adding sample initial data to database
     DatabaseDataWorker worker = new DatabaseDataWorker(db);
     worker.insertCourses();
     worker.insertSampleNotes();
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
             if(oldVersion < 2){
                 //adding indexes to the 2 tables
                 db.execSQL(NoteKeeperDatabaseContract.CourseInfoEntry.SQL_CREATE_INDEX1);
                 db.execSQL(NoteKeeperDatabaseContract.NoteInfoEntry.SQL_CREATE_INDEX1);
             }

    }
}
