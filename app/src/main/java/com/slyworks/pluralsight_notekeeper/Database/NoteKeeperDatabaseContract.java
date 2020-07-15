package com.slyworks.pluralsight_notekeeper.Database;

import android.provider.BaseColumns;

/**
 * created by Joshua Sylvanus
 */
public final class NoteKeeperDatabaseContract {
    private NoteKeeperDatabaseContract(){};

    public static final class CourseInfoEntry implements BaseColumns {
        public static final String TABLE_NAME = "course_info";
        public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

        //for index
        public static final String INDEX1 = TABLE_NAME+"_index1";
        //creating INDEX
        //>>CREATE (UNIQUE) INDEX course_info_index ON course_info (course_title)
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX "+INDEX1+" ON " + TABLE_NAME+
                        "("+COLUMN_COURSE_TITLE+")";
        //commands are in NoteKeeperOpenHelper

        //adding method for getting the qualified name for "JOIN" query
        public static final String getQName(String columnName){
            return TABLE_NAME+"."+columnName;//to avoi doing explicit concatenation
        }
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE "+TABLE_NAME+" ("+
                        _ID+" INTEGER PRIMARY KEY, "+
                        COLUMN_COURSE_ID +" TEXT UNIQUE NOT NULL, "+
                        COLUMN_COURSE_TITLE+" TEXT NOT NULL )";



    }

    public static final class NoteInfoEntry implements BaseColumns{
        public static final String TABLE_NAME = "note_info";
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
        public static final String COLUMN_COURSE_ID = "course_id";

        //for index
        public static final String INDEX1 = TABLE_NAME+"_index1";
        //>>CREATE (UNIQUE) INDEX course_info_index ON course_info (course_title)
        public static final String SQL_CREATE_INDEX1 =
                "CREATE INDEX "+INDEX1+" ON " + TABLE_NAME+
                        "("+COLUMN_NOTE_TITLE+")";
        //commands are in NoteKeeperOpenHelper

        //adding method for getting the qualified name for "JOIN" query
        public static final String getQName(String columnName){
            return TABLE_NAME+"."+columnName;//to avoid doing explicit concatenation
        }
        public static final String SQL_CREATE_TABLE =
                "CREATE TABLE "+TABLE_NAME+" ("+
                        _ID+" INTEGER PRIMARY KEY, "+
                        COLUMN_NOTE_TITLE +" TEXT NOT NULL, "+
                        COLUMN_NOTE_TEXT+" TEXT, "+
                        COLUMN_COURSE_ID+" TEXT NOT NULL )";
    }
}
