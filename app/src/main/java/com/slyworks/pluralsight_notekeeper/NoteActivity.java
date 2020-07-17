package com.slyworks.pluralsight_notekeeper;

import android.annotation.SuppressLint;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cursoradapter.widget.SimpleCursorAdapter;
import androidx.lifecycle.ViewModelProvider;

import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.CourseInfoEntry;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperDatabaseContract.NoteInfoEntry;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperOpenHelper;

import java.util.List;

import android.app.LoaderManager;
import android.content.CursorLoader;

public class NoteActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{
//creating constant to receive intentExtras from NoteListActivity
//using package name as prefix to make identifier unique
    public static final String NOTE_POSITION = "com.slyworks.pluralsight_notekeeper.NOTE_INFO";
    public static final int POSITION_NOT_SET = -1;
    public static final int LOADER_NOTES = 0;
    public static final int LOADER_COURSES = 1;
    private NoteInfo mNote = new NoteInfo(DataManager.getInstance().getCourses().get(0), "", "");;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;

    private static NoteActivityViewModel mViewModel;


    //for database access
    private NoteKeeperOpenHelper mDBOpenHelper;
    private Cursor mNoteCursor;
    private int mCourseIDPos;
    private int mNoteTitlePos;
    private int mNoteTextPos;

    public static final int ID_NOT_SET = -1;
    public static final String NOTE_ID = "com.slyworks.pluralsight_notekeeper.NOTE_ID";
    public static int mNoteID;

    private SimpleCursorAdapter mAdapterCourses;
    private boolean mCourseQueryFinished;
    private boolean mNoteQueryFinished;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //initialising ViewModel reference object,but its not to
        //be initialised directly,instead the system manages it

        //ViewModelProvider requires a parameter to know where to store
        //ViewModel instances

        //ViewModel class behaves like a singleton(or is a SINGLETON),
        //once it is initially initialised ,all other references to
        //it call that one instance, especially useful,when surviving
        //activity destruction



        ViewModelProvider mViewModelProvider = new ViewModelProvider(getViewModelStore(),
               ViewModelProvider.AndroidViewModelFactory.getInstance(getApplication()));

        mViewModel = mViewModelProvider.get(NoteActivityViewModel.class);

        if(mViewModel.mIsNewlyCreated && savedInstanceState != null) {
            //i want this method to only be called if the Activity got destroyed
            //due to things other than device configuration(rotation)
            mViewModel.restoreState(savedInstanceState);}

            mViewModel.mIsNewlyCreated = false;




        mSpinnerCourses = findViewById(R.id.spinner_courses);

        //editing for database implementation
        //assigning databaseOpenHelper Object
        mDBOpenHelper = new NoteKeeperOpenHelper(this);

        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this,
         //       android.R.layout.simple_spinner_item,courses);

        mAdapterCourses = new SimpleCursorAdapter(this, android.R.layout.simple_spinner_item,null,
                               new String[]{CourseInfoEntry.COLUMN_COURSE_TITLE},
                               new int[]{android.R.id.text1},0);
        mAdapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(mAdapterCourses);
        //loading data to the spinner
        //loadCourseData();

        getLoaderManager().initLoader(LOADER_COURSES,null,this);

        //method to receive intent extras from NoteListActivity
        ReadDisplayStateValues();

        //saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);



        if(!mIsNewNote)
        //displayNote();
        //now loading from database
           // loadNoteData();
            getLoaderManager().initLoader(LOADER_NOTES,null,this);

    }

    private void loadCourseData() {
        SQLiteDatabase sq_db = mDBOpenHelper.getReadableDatabase();
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID//uniquely identifies rows in a table
        };

        Cursor cursor = sq_db.query(CourseInfoEntry.TABLE_NAME, courseColumns,
                        null, null, null, null,CourseInfoEntry.COLUMN_COURSE_TITLE);//as the ordering column

        //associating adapter with this cursor
        mAdapterCourses.changeCursor(cursor);
    }

    private void loadNoteData() {
        SQLiteDatabase sq_db = mDBOpenHelper.getReadableDatabase();

        //assuming i'm always finding the same note
        String courseID = "android_intents";
        String titleStart = "dynamic";

        //now setting the selection to general
        String selection = NoteInfoEntry._ID+ " = ?";
        String[] selectionArgs  = {Integer.toString(mNoteID)};

        String[] noteColumns = {
                NoteInfoEntry.COLUMN_COURSE_ID,
                NoteInfoEntry.COLUMN_NOTE_TITLE,
                NoteInfoEntry.COLUMN_NOTE_TEXT
        };

        mNoteCursor = sq_db.query(NoteInfoEntry.TABLE_NAME,
                noteColumns, selection, selectionArgs,
                null, null, null);
        mCourseIDPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

       //since Cursor object start at -1
        mNoteCursor.moveToNext();//now at 0

        displayNote();
    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;

        mViewModel.mMOriginalCourseId = mNote.getCourse().getCourseId();
        mViewModel.mMOriginalNoteTitle = mNote.getTitle();
        mViewModel.mMOriginalNoteText = mNote.getText();
    }

    private void displayNote() {
        //getting the values from the Cursor object returned from Database query
        String courseID = mNoteCursor.getString(mCourseIDPos);
        String noteTitle = mNoteCursor.getString(mNoteTitlePos);
        String noteText = mNoteCursor.getString(mNoteTextPos);


        //implementing database way
        //List<CourseInfo> courses = DataManager.getInstance().getCourses();
        //CourseInfo course = DataManager.getInstance().getCourse(courseID);

        int courseIndex = getIndexOfCourseId(courseID);

        //setting the spinner to display the selected note
        mSpinnerCourses.setSelection(courseIndex);

        //mTextNoteTitle.setText(mNote.getTitle());
        //mTextNoteText.setText(mNote.getText());

        //now done using database values
        mTextNoteTitle.setText(noteTitle);
        mTextNoteText.setText(noteText);
    }

    private int getIndexOfCourseId(String courseID) {
        Cursor cursor = mAdapterCourses.getCursor();
        int courseIDPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);

        //to get row Index which is what we actually need
        int courseRowIndex = 0;

       boolean more = cursor.moveToFirst();
       while(more){
           //looping through the loop until the required row is found
           String cursorCourseID = cursor.getString(courseIDPos);
           if(courseID.equals(cursorCourseID))
               //meaning the required row is found
               break;

           courseRowIndex++;
           more = cursor.moveToNext();
       }
       return courseRowIndex;
    }

    private void ReadDisplayStateValues() {

        //reference to contain note selected in NoteListActivity
        //,the note's title,course, name were passed as Parcelable IntentExtras

        //mNote = intent.getParcelableExtra(NOTE_POSITION);

        //int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);

        //adding database implementation
        //checking if its a new note being created
        Intent intent = getIntent();
        mNoteID = intent.getIntExtra(NOTE_ID, ID_NOT_SET);
        mIsNewNote = mNoteID == ID_NOT_SET;

        if(mIsNewNote){
            createNewNote();
        }
        //else{
            //no longer needed 'cause of database
            //mNote = DataManager.getInstance().getNotes().get(mNoteID);
            //mNotePosition = mNoteID;
        //}
    }

    private void createNewNote() {
        //DataManager dm = DataManager.getInstance();
        //mNotePosition = dm.createNewNote();
       //mNote = dm.getNotes().get(mNotePosition);

        //changing to use database for saving, the concept here is that
        //once the FAB is clicked a new row with empty column is created
        //and on saving the row is updated with the new(correct) values
        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID,"");
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE,"");
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, "");

        SQLiteDatabase sq_db = mDBOpenHelper.getWritableDatabase();
        //returns row ID
       mNoteID = (int) sq_db.insert(NoteInfoEntry.TABLE_NAME, null, values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        switch(id){
        case R.id.action_send_mail:
            sendEmail();
            return true;
        case R.id.action_cancel:
            mIsCancelling = true;
            //dismissing the activity
            finish();
            return true;
        case R.id.action_next:
             moveToNext();
             return true;
        case R.id.action_previous:
             moveToPrevious();
             return true;
        default:
        return super.onOptionsItemSelected(item);
    }
    }



    private void moveToNext() {
        //final Iterator<NoteInfo> iterator = DataManager.getInstance().getNotes().iterator();
        //since its leaving the current note,save current value
        saveNote();

        //to get next, incrementing mNotePosition
        //checking if its the last note in the menu

        //if(mNotePosition == DataManager.getInstance().getNotes().size() -1)
        //if(!iterator.hasNext())
         // return;

        ++mNotePosition;
        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        //in case the user is editing but decided to cancel
        //saving the original note values
       saveOriginalNoteValues();

       displayNote();

       //calling Overridden class method so that the optionsMenu onPrepareOptionsMenu check can happen again
        invalidateOptionsMenu();
    }


    private void moveToPrevious() {
        //saving the current note first
        saveNote();

        --mNotePosition;

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        saveOriginalNoteValues();
        displayNote();

        invalidateOptionsMenu();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item_prev = menu.findItem(R.id.action_previous);
        MenuItem item_next = menu.findItem(R.id.action_next);

        int first_note_index  = 0;
        int last_note_index = DataManager.getInstance().getNotes().size()-1;

        //to set both menu options to be enabled as long as its not the first(for previous) or last(for next)
        boolean isPrevEnabled = mNotePosition > first_note_index;
        boolean isNextEnabled = mNotePosition < last_note_index ;

        item_prev.setEnabled(isPrevEnabled);
         if(!isPrevEnabled)
            item_prev.setVisible(false);

        item_next.setEnabled(isNextEnabled);
        if(!isNextEnabled)
             item_next.setVisible(false);

        return super.onPrepareOptionsMenu(menu);
        //need to call invalidateOptionsMenu() in the moveToPrevious() and moveToNext methods
        //to ensure the onPrepareOptionsMenu() is called again and the checking can occur
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(mIsCancelling){
            if(mIsNewNote)
                //switching to database
                //DataManager.getInstance().removeNote(mNotePosition);

            //situation where User created note but is leaving without saving the note
            //deleting the backingStore(empty row inserted) for the would-hav-been entry
                deleteNoteFromDatabase();
                else
                storePreviousNoteValues();
        }
         else{
            saveNote();
        }

    }

    @SuppressLint("StaticFieldLeak")
    private void deleteNoteFromDatabase() {
        final String selection = NoteInfoEntry._ID + " = ?";
        final String[] selectionArgs = {Integer.toString(mNoteID)};

        //adding a bit of Threading to run tasks in background
        AsyncTask task = new AsyncTask() {
            @Override
            protected Object doInBackground(Object[] objects) {
                SQLiteDatabase sq_db = mDBOpenHelper.getWritableDatabase();
                sq_db.delete(NoteInfoEntry.TABLE_NAME,selection,selectionArgs);
                return null;
            }
    };
        task.execute();
}

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
//method for saving InstanceState, in case of Activity destruction
//not handled by the ViewModel class

            mViewModel.saveState(outState);

    }

    private void storePreviousNoteValues() {
        CourseInfo course = DataManager.getInstance().getCourse(mViewModel.mMOriginalCourseId);
        mNote.setCourse(course);
        mNote.setTitle(mViewModel.mMOriginalNoteTitle);
        mNote.setText(mViewModel.mMOriginalNoteText);
    }

    private void saveNote() {
       // mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        //mNote.setTitle(mTextNoteTitle.getText().toString());
        //mNote.setText(mTextNoteText.getText().toString());

        //switching to database
        String courseID = selectedCourseID();
        String noteTitle = mTextNoteTitle.getText().toString();
        String noteText = mTextNoteText.getText().toString();

        //courseID is in a spinner
        saveNoteToDatabase(courseID, noteTitle, noteText);
    }

    private String selectedCourseID() {
        //courseId corresponding to the one selected in the spinner
        int selectedPosition  = mSpinnerCourses.getSelectedItemPosition();
        Cursor cursor = mAdapterCourses.getCursor();
        cursor.moveToPosition(selectedPosition);

        //reading data from the cursor
        int courseIDPos = cursor.getColumnIndex(CourseInfoEntry.COLUMN_COURSE_ID);
        String courseID = cursor.getString(courseIDPos);

        return courseID;
    }

    //for database updating of entries
    public void  saveNoteToDatabase(String courseID, String noteTitle, String noteText){
        String selection = NoteInfoEntry._ID+" = ?";
        String[] selectionArgs = {Integer.toString(mNoteID)};

        ContentValues values = new ContentValues();
        values.put(NoteInfoEntry.COLUMN_COURSE_ID, courseID );
        values.put(NoteInfoEntry.COLUMN_NOTE_TITLE, noteTitle );
        values.put(NoteInfoEntry.COLUMN_NOTE_TEXT, noteText );

        SQLiteDatabase sq_db = mDBOpenHelper.getWritableDatabase();
        sq_db.update(NoteInfoEntry.TABLE_NAME, values, selection, selectionArgs);
    }
    private void sendEmail() {
        //method for sending a Note as an Email
        //with the Note's title as the Subject of the mail
        //and Note's text as Body of the Email
     CourseInfo course = (CourseInfo)mSpinnerCourses.getSelectedItem();
     String subject = mTextNoteTitle.getText().toString();
     String text = "Check out what i learned in the PluralSight course \""+
            course.getTitle()+"\"\n" + mTextNoteText.getText();

    Intent intent = new Intent(Intent.ACTION_SEND);
    //associating type with the implicit intent
    //"message/rfc2822" is the standard mime corresponding to email
    intent.setType("message/rfc2822");

    intent.putExtra(Intent.EXTRA_SUBJECT, subject);
    intent.putExtra(Intent.EXTRA_TEXT, text);

    startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        //closing DatabaseOpenHelper object
        mDBOpenHelper.close();
        super.onDestroy();
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        CursorLoader loader = null;
        if(id == LOADER_NOTES)
            loader = createLoaderNotes();
        else if(id == LOADER_COURSES)
            loader = createLoaderCourses();
          return loader;
    }

    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderCourses() {
        //adding flag to know when the 'other' loading is done
        mCourseQueryFinished = false;

        //migrating app to make use of ContentProvider
        //providing authority for the ContentProvider
        Uri uri = Uri.parse("content://com.slyworks.pluralsight_notekeeper.provider");
        String[] courseColumns = {
                CourseInfoEntry.COLUMN_COURSE_TITLE,
                CourseInfoEntry.COLUMN_COURSE_ID,
                CourseInfoEntry._ID//uniquely identifies rows in a table
        };
        return new CursorLoader(this, uri, courseColumns,null,null, CourseInfoEntry.COLUMN_COURSE_TITLE);

    }


    @SuppressLint("StaticFieldLeak")
    private CursorLoader createLoaderNotes() {
        //boolean flag to know exactly when the loading is done
        mNoteQueryFinished = false;

        return new mCursorLoader(this){
            @SuppressLint("StaticFieldLeak")
            @Override
            public Cursor loadInBackground() {
                SQLiteDatabase sq_db = mDBOpenHelper.getReadableDatabase();


                //assuming i'm always finding the same note
                String courseID = "android_intents";
                String titleStart = "dynamic";

                //now setting the selection to general
                String selection = NoteInfoEntry._ID+ " = ?";
                String[] selectionArgs  = {Integer.toString(mNoteID)};

                String[] noteColumns = {
                        NoteInfoEntry.COLUMN_COURSE_ID,
                        NoteInfoEntry.COLUMN_NOTE_TITLE,
                        NoteInfoEntry.COLUMN_NOTE_TEXT
                };

                return sq_db.query(NoteInfoEntry.TABLE_NAME,
                        noteColumns, selection, selectionArgs,
                        null, null, null);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
 //for when the results are returned
        if(loader.getId()==LOADER_NOTES){
            loadFinishedNotes(data);
        }else if(loader.getId() == LOADER_COURSES){
            mAdapterCourses.changeCursor(data);
            mCourseQueryFinished = true;
            displayNoteWhenQueriesFinished();
        }
    }

    private void loadFinishedNotes(Cursor data) {
        mNoteCursor = data;
        mCourseIDPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_COURSE_ID);
        mNoteTitlePos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TITLE);
        mNoteTextPos = mNoteCursor.getColumnIndex(NoteInfoEntry.COLUMN_NOTE_TEXT);

        //since Cursor object start at -1
        mNoteCursor.moveToNext();//now at 0

        //setting the boolean flag
        mNoteQueryFinished = true;

        //called from 2 places to ensure that at one of the 2 points the loading would be done
        displayNoteWhenQueriesFinished();
    }

    private void displayNoteWhenQueriesFinished() {
   if(mNoteQueryFinished && mCourseQueryFinished)
       displayNote();

    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
       if(loader.getId() == LOADER_NOTES){
           if(mNoteCursor != null)
              mNoteCursor.close();
       }else if(loader.getId() == LOADER_COURSES){
           mAdapterCourses.changeCursor(null);
       }
    }

    //nested class
    public static class mCursorLoader extends CursorLoader{

        public mCursorLoader(Context context) {
            super(context);
        }

        @Override
        public Cursor loadInBackground() {
            NoteActivity mNoteActivity = new NoteActivity();
            SQLiteDatabase sq_db = mNoteActivity.mDBOpenHelper.getReadableDatabase();

            //assuming i'm always finding the same note
            String courseID = "android_intents";
            String titleStart = "dynamic";

            //now setting the selection to general
            String selection = NoteInfoEntry._ID+ " = ?";
            String[] selectionArgs  = {Integer.toString(mNoteID)};

            String[] noteColumns = {
                    NoteInfoEntry.COLUMN_COURSE_ID,
                    NoteInfoEntry.COLUMN_NOTE_TITLE,
                    NoteInfoEntry.COLUMN_NOTE_TEXT
            };

            return sq_db.query(NoteInfoEntry.TABLE_NAME,
                    noteColumns, selection, selectionArgs,
                    null, null, null);
        }
    }
}