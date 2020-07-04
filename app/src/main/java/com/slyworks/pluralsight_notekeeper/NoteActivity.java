package com.slyworks.pluralsight_notekeeper;

import android.content.Intent;
import android.os.Bundle;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import android.provider.ContactsContract;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

import java.util.Iterator;
import java.util.List;

public class NoteActivity extends AppCompatActivity {
//creating constant to receive intentExtras from NoteListActivity
//using package name as prefix to make identifier unique
    public static final String NOTE_POSITION = "com.slyworks.pluralsight_notekeeper.NOTE_INFO";
    public static final int POSITION_NOT_SET = -1;
    private NoteInfo mNote;
    private boolean mIsNewNote;
    private Spinner mSpinnerCourses;
    private EditText mTextNoteTitle;
    private EditText mTextNoteText;
    private int mNotePosition;
    private boolean mIsCancelling;

    private static NoteActivityViewModel mViewModel;

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

        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        ArrayAdapter<CourseInfo> adapterCourses = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,courses);
        adapterCourses.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mSpinnerCourses.setAdapter(adapterCourses);

        //method to receive intent extras from NoteListActivity
        
        ReadDisplayStateValues();
        saveOriginalNoteValues();

        mTextNoteTitle = findViewById(R.id.text_note_title);
        mTextNoteText = findViewById(R.id.text_note_text);

        if(!mIsNewNote)
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

    }

    private void saveOriginalNoteValues() {
        if(mIsNewNote)
            return;

        mViewModel.mMOriginalCourseId = mNote.getCourse().getCourseId();
        mViewModel.mMOriginalNoteTitle = mNote.getTitle();
        mViewModel.mMOriginalNoteText = mNote.getText();
    }

    private void displayNote(Spinner spinnerCourses, EditText textNoteTitle, EditText textNoteText) {
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        int courseIndex = courses.indexOf(mNote.getCourse());

        //setting the spinner to display the selected note

        spinnerCourses.setSelection(courseIndex);

        textNoteTitle.setText(mNote.getTitle());
        textNoteText.setText(mNote.getText());
    }

    private void ReadDisplayStateValues() {
        Intent intent = getIntent();
        //reference to contain note selected in NoteListActivity
        //,the note's title,course, name were passed as Parcelable IntentExtras

        //mNote = intent.getParcelableExtra(NOTE_POSITION);

        int position = intent.getIntExtra(NOTE_POSITION, POSITION_NOT_SET);

        //checking if its a new note being created
        mIsNewNote = position == POSITION_NOT_SET;

        if(mIsNewNote){
            createNewNote();
        }else{
            mNote = DataManager.getInstance().getNotes().get(position);
            mNotePosition = position;
        }
    }

    private void createNewNote() {
        DataManager dm = DataManager.getInstance();
        mNotePosition = dm.createNewNote();
        mNote = dm.getNotes().get(mNotePosition);
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

       displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

       //calling Overridden class method so that the optionsMenu onPrepareOptionsMenu check can happen again
        invalidateOptionsMenu();
    }


    private void moveToPrevious() {
        //saving the current note first
        saveNote();

        --mNotePosition;

        mNote = DataManager.getInstance().getNotes().get(mNotePosition);

        saveOriginalNoteValues();
        displayNote(mSpinnerCourses, mTextNoteTitle, mTextNoteText);

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
                DataManager.getInstance().removeNote(mNotePosition);
            else
                storePreviousNoteValues();
        }
         else{
            saveNote();
        }

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
        mNote.setCourse((CourseInfo) mSpinnerCourses.getSelectedItem());
        mNote.setTitle(mTextNoteTitle.getText().toString());
        mNote.setText(mTextNoteText.getText().toString());

        //in case it was a new note being created
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
}