package com.slyworks.pluralsight_notekeeper;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.provider.BaseColumns;

import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperOpenHelper;
/**
 * created by Joshua Sylvanus
 */
public class NoteKeeperContentProvider extends ContentProvider {
    NoteKeeperOpenHelper mDBOPenHelper;
   public static final int COURSES = 0;
   public static final int NOTES =1;
   public static final int NOTES_EXPANDED = 2;
    //Uri.NO_MATCH->only checks if the Uri is valid(kind of)
    private static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    static{
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                NoteKeeperProviderContract.Courses.PATH,
                COURSES);

        //Uri for noteTable
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                NoteKeeperProviderContract.Notes.PATH,
                NOTES);

        //adding for the new ContentProvider joined table
        sUriMatcher.addURI(NoteKeeperProviderContract.AUTHORITY,
                NoteKeeperProviderContract.Notes.PATH_EXPANDED,
                NOTES_EXPANDED);
        //always add support in the query method
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
        // TODO: Implement this to handle requests for the MIME type of the data
        // at the given URI.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // TODO: Implement this to handle requests to insert a new row.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    @Override
    public boolean onCreate() {
       //adding instance of DataBaseOpenHelper
        mDBOPenHelper = new NoteKeeperOpenHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        Cursor cursor = null;
        SQLiteDatabase sq_db = mDBOPenHelper.getReadableDatabase();

        //checking which Uri was passed as parameter
        int uriMatch = sUriMatcher.match(uri);
        switch(uriMatch){
            case COURSES:
                cursor = sq_db.query(NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case NOTES:
                cursor = sq_db.query(NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME,
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
                           NoteKeeperDatabaseContract.NoteInfoEntry.getQName(projection[idx])
                                   :
                           projection[idx];
        }

       String tablesWithJoin = NoteKeeperDatabaseContract.NoteInfoEntry.TABLE_NAME+
                               " JOIN "+ NoteKeeperDatabaseContract.CourseInfoEntry.TABLE_NAME +
                               " ON "+ NoteKeeperDatabaseContract.NoteInfoEntry.getQName(NoteKeeperDatabaseContract.NoteInfoEntry.COLUMN_COURSE_ID)+
                               " = "+ NoteKeeperDatabaseContract.CourseInfoEntry.getQName(NoteKeeperDatabaseContract.CourseInfoEntry.COLUMN_COURSE_ID);

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
