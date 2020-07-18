package com.slyworks.pluralsight_notekeeper;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NoteKeeperProviderContract {
    //to ensure that no one can create an instance of this class
    //kind of like a Singleton
    private NoteKeeperProviderContract(){}

    public static final String AUTHORITY = "com.slyworks.pluralsight_notekeeper.provider";
    public static final Uri AUTHORITY_URI = Uri.parse("content://"+AUTHORITY);

    //creating interfaces for each Tables columns
    //creating interface to implement the columns as constants

    protected interface CoursesIDColumns{
        //creating a 3rd interface for the course_id, since both tables have it
        public static final String COLUMN_COURSE_ID = "course_id";
    }
    protected interface CoursesColumns{
        //public static final String COLUMN_COURSE_ID = "course_id";
        public static final String COLUMN_COURSE_TITLE = "course_title";

    }

    //for the notes
    protected interface NotesColumns{
        public static final String COLUMN_NOTE_TITLE = "note_title";
        public static final String COLUMN_NOTE_TEXT = "note_text";
       // public static final String COLUMN_COURSE_ID = "course_id";
    }
    //creating nested classes for each table i want to expose, implementing
    //the CourseColumns interface exposes the correct column names i want
    //and helps avoid ambiguity(since both tables have similar column names)
    //and implements BaseColumns for "_ID" for row id
    public static final class Courses implements BaseColumns, CoursesColumns, CoursesIDColumns{
        public static final String PATH = "courses";

        //Uri for this class should look like
        //content://com.slyworks.pluralsight_notekeeper.provider/courses

        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);
    }

    //second nested class for notes
    public static final class Notes implements BaseColumns, NotesColumns, CoursesIDColumns,CoursesColumns{
        public static final String PATH = "notes";
        public static final Uri CONTENT_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH);

   //adding constants to provide a level of abstraction for the joined table in
   //the contentProvider
   public static final String PATH_EXPANDED = "notes_expanded";
   public static final Uri CONTENT_EXPANDED_URI = Uri.withAppendedPath(AUTHORITY_URI, PATH_EXPANDED);

    }

}
