package com.slyworks.pluralsight_notekeeper;

import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperOpenHelper;
import com.slyworks.pluralsight_notekeeper.NoteKeeperProviderContract.Courses;
import com.slyworks.pluralsight_notekeeper.NoteKeeperProviderContract.Notes;

/**
 * created by Joshua Sylvanus
 */
public class NoteKeeperContentProvider extends ContentProvider {
    public static final String MIME_VENDOR_TYPE = "vnd." + NoteKeeperProviderContract.AUTHORITY + ".";
    NoteKeeperOpenHelper mDBOpenHelper;
   public static final int COURSES = 0;
   public static final int NOTES =1;
   public static final int NOTES_EXPANDED = 2;
   public static final int NOTES_ROW = 3;
    //Uri.NO_MATCH->only checks if the Uri is valid(kind of)
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                Courses.PATH,
                COURSES);

        //Uri for noteTable
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                Notes.PATH,
                NOTES);

        //adding for the new ContentProvider joined table
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                Notes.PATH_EXPANDED,
                NOTES_EXPANDED);
        //always add support in the query method

        //for the patterns for matching table row Uri's
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY, Notes.PATH+"/#", NOTES_ROW);
    }
//in the case of a new project, create the contentProvider from the
//"file" menu->"other",that way Android studio does the work
//of writing to the manifest.xml
    public NoteKeeperContentProvider() {
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public String getType(Uri uri) {
        //for arranging the appropriate MIME type
        String mimeType = null;

        //to identify which URI was received
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case COURSES:
                /*
                 *what the mimeType should look like
                 * vnd.android.cursor.dir/vnd.com.slyworks.pluralsight_notekeeper.provider.courses
                 */
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                                   MIME_VENDOR_TYPE +
                                   Courses.PATH;
                break;
            case NOTES:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"+
                                   MIME_VENDOR_TYPE +
                                   Notes.PATH;
                break;
            case NOTES_EXPANDED:
                mimeType = ContentResolver.CURSOR_DIR_BASE_TYPE + "/"+
                                   MIME_VENDOR_TYPE +
                                   Notes.PATH_EXPANDED;
                break;

                //for row URI
            case NOTES_ROW:
                mimeType = ContentResolver.CURSOR_ITEM_BASE_TYPE + "/"+
                                   MIME_VENDOR_TYPE +
                                   Notes.PATH;
                break;
        }
        return mimeType;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //continuation from NoteActivity's createNewNote() method
        SQLiteDatabase sq_db = mDBOpenHelper.getWritableDatabase();
        long rowID = -1;

        //needs to return a Uri, which identifies the newly inserted row
        Uri rowUri = null;

        //checking the uri to see which table it matches with
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case NOTES://notes == 1 and matches the noteTable
                rowID =  sq_db.insert(NoteInfoEntry.TABLE_NAME, null, values);

               /*
                *getting the Uri for the new Row inserted
                *content://com.slyworks.pluralsight_notekeeper/notes/1
                */
                rowUri  =  ContentUris.withAppendedId(Notes.CONTENT_URI, rowID);
                break;

            case COURSES:
                rowID = sq_db.insert(CourseInfoEntry.TABLE_NAME, null, values);
                rowUri = ContentUris.withAppendedId(Courses.CONTENT_URI, rowID);
                break;

             //for the expanded/'joined' table
            case NOTES_EXPANDED:
                //throw new 'TableIsReadOnlyException'
                break;
        }


        return rowUri;
    }

    @Override
    public boolean onCreate() {
       //adding instance of DataBaseOpenHelper
        mDBOpenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase sq_db = mDBOpenHelper.getReadableDatabase();

        //checking which Uri was passed as parameter
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case COURSES:
                cursor = sq_db.query(CourseInfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case NOTES:
                cursor = sq_db.query(NoteInfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case NOTES_EXPANDED:
                cursor = notesExpandedQuery(sq_db, projection,selection, selectionArgs, sortOrder );
                break;

             //adding support for Row uris
            case NOTES_ROW:
                long rowID = ContentUris.parseId(uri);
                String rowSelection = NoteInfoEntry._ID+ " = ?";
                String[] rowSelectionArgs = new String[]{Long.toString(rowID)};

                cursor = sq_db.query(NoteInfoEntry.TABLE_NAME,
                         projection,
                         rowSelection,
                        rowSelectionArgs,
                        null,
                        null,
                        null);
                break;
        }

        return cursor;
    }

    private Cursor notesExpandedQuery(SQLiteDatabase sq_db, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
      //adding code for any column that appears on both tables being joined
      //i.e table-qualifying such columns as is happening in the MainActivity
        String[] columns = new String[projection.length];
        for(int idx=0; idx < projection.length; idx++){
            columns[idx] = projection[idx].equals(BaseColumns._ID)
                                   ||
                           projection[idx].equals(NoteKeeperProviderContract.CoursesIDColumns.COLUMN_COURSE_ID)
                                   ?
                           NoteInfoEntry.getQName(projection[idx])
                                   :
                           projection[idx];
        }

       String tablesWithJoin = NoteInfoEntry.TABLE_NAME+
                               " JOIN "+ CourseInfoEntry.TABLE_NAME +
                               " ON "+ NoteInfoEntry.getQName(NoteInfoEntry.COLUMN_COURSE_ID)+
                               " = "+ CourseInfoEntry.getQName(CourseInfoEntry.COLUMN_COURSE_ID);

       return sq_db.query(tablesWithJoin, columns, selection, selectionArgs, null,null,sortOrder);
    //next modification is in the MainActivity to use this new ContentProvider Uri
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // TODO: Implement this to handle requests to update one or more rows.
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
