package com.slyworks.pluralsight_notekeeper;

import android.os.Bundle;

import androidx.lifecycle.ViewModel;

public class NoteActivityViewModel extends ViewModel {
    //creating constants for saveState() method
    public static final String ORIGINAL_NOTE_COURSE_ID = "com.slyworks.pluralsight_notekeeper.ORIGINAL_NOTE_COURSE_ID";
    public static final String ORIGINAL_NOTE_TITLE = "com.slyworks.pluralsight_notekeeper.ORIGINAL_NOTE_TITLE";
    public  static  final String ORIGINAL_NOTE_TEXT = "com.slyworks.pluralsight_notekeeper.ORIGINAL_NOTE_TEXT";
    public String mMOriginalCourseId;
    public String mMOriginalNoteTitle;
    public String mMOriginalNoteText;

    //used to check if the Activity is being recreated, hence requires the use of the
    //Bundle and restoreState() method, else its just the View Model field values to be used
    public boolean mIsNewlyCreated = true;
    public void saveState(Bundle outState) {
        //values have to be saved in same order as they would be gotten
        //using Bundle() class method to save the fields
        //hence writing the values to our Bundle so it can be restored
        outState.putString(ORIGINAL_NOTE_COURSE_ID, mMOriginalCourseId);
        outState.putString(ORIGINAL_NOTE_TITLE, mMOriginalNoteTitle);
        outState.putString(ORIGINAL_NOTE_TEXT, mMOriginalNoteText);

    }

    //method for restoring state
    public void restoreState(Bundle instate){
//restoring the values entered in the saveState() method in a Map-like manner

        mMOriginalCourseId = instate.getString(ORIGINAL_NOTE_COURSE_ID);
        mMOriginalNoteTitle = instate.getString(ORIGINAL_NOTE_TITLE);
        mMOriginalNoteText = instate.getString(ORIGINAL_NOTE_TEXT);
    }
}
