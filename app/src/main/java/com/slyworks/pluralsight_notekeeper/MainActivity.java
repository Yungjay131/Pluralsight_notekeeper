package com.slyworks.pluralsight_notekeeper;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.slyworks.pluralsight_notekeeper.Database.NoteKeeperOpenHelper;

import java.util.List;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private NoteRecyclerAdapter mNoteRecyclerAdapter;
    private RecyclerView mRecyclerItems;
    private LinearLayoutManager mNotesLayoutManager;
    private CourseRecyclerAdapter mCourseRecyclerAdapter;
    private GridLayoutManager mCourseLayoutManager;


    //for the database
    private NoteKeeperOpenHelper mDBOpenHelper;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_nav);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, NoteActivity.class));
            }
        });
        DrawerLayout drawer = findViewById(R.id.drawer_layout);


        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);

        //setting the OnNavigationItemSelectedListener
        navigationView.setNavigationItemSelectedListener(this);

        //for the database,(NB, its closed in the onDestroy())
        mDBOpenHelper = new NoteKeeperOpenHelper(this);

        initialiseDisplayContent();
    }


    @Override
    protected void onResume() {
        super.onResume();
        //to handle creation of new notes,resetting the RecyclerAdapter to accommodate new information
        mNoteRecyclerAdapter.notifyDataSetChanged();

        //this is meant for working with small sets of data
        //larger data should use other methods
    }

    private void  initialiseDisplayContent() {
        /*
        final ListView listNotes = findViewById(R.id.list_notes);
        List<NoteInfo> notes = DataManager.getInstance().getNotes();

        mAdapterNotes = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,notes);
        listNotes.setAdapter(mAdapterNotes);
        listNotes.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(NoteListActivity.this, NoteActivity.class);

               NoteInfo note = (NoteInfo) listNotes.getItemAtPosition(position);

                //using intentExtras to pass the values
                intent.putExtra(NoteActivity.NOTE_POSITION, position);
                startActivity(intent);

            }
        });
         */

        //new code using for using recyclerView and database
        DataManager.loadFromDatabase(mDBOpenHelper);

        mRecyclerItems = findViewById(R.id.list_items);

        mNotesLayoutManager = new LinearLayoutManager(this );
        //layout manager for displaying courses i.e from nav_view click
        mCourseLayoutManager = new GridLayoutManager(this, 2);

        List<NoteInfo> notes = DataManager.getInstance().getNotes();
        mNoteRecyclerAdapter = new NoteRecyclerAdapter(this, notes);


        //adding code to display courses
        List<CourseInfo> courses = DataManager.getInstance().getCourses();
        mCourseRecyclerAdapter = new CourseRecyclerAdapter(this, courses);

        displayNotes();
    }


    private void displayCourses(){
        mRecyclerItems.setLayoutManager(mCourseLayoutManager);
        mRecyclerItems.setAdapter(mCourseRecyclerAdapter);

        //to set the clicked nav view menu item
        selectNavigationMenuItem(R.id.nav_courses);
    }
    private void displayNotes() {
        mRecyclerItems.setLayoutManager(mNotesLayoutManager);
        mRecyclerItems.setAdapter(mNoteRecyclerAdapter);


        //adding database functionality
        //checks if database exist(creates it if it doesn't) and returns a reference
       //3 SQLiteDatabase db = mDBOpenHelper.getReadableDatabase();

        //setting notes options checked in navigation view
        selectNavigationMenuItem(R.id.nav_notes);

    }

    private void selectNavigationMenuItem(int id) {
        NavigationView navigationView = findViewById(R.id.nav_view);

        //getting reference to menu within navigation view
        Menu menu  = navigationView.getMenu();
        menu.findItem(id).setChecked(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.nav, menu);
        return true;
    }


    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
      int id = menuItem.getItemId();

        switch(id){
            case R.id.nav_notes:
                displayNotes();
                return true;
            case R.id.nav_courses:
                displayCourses();
                return true;
            case R.id.nav_share:
                handleSelection("Share");
                return true;
            case R.id.nav_send:
                handleSelection("Send");
                return true;
        }
        //DrawerLayout drawer = findViewById(R.id.drawer_layout);
        //drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_settings)
            startActivity(new Intent(this, SettingsActivity.class));

        return true;
    }

    private void handleSelection(String message) {
        Snackbar.make(findViewById(R.id.list_items), message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        //closing the database helper class instance connection
        mDBOpenHelper.close();
        super.onDestroy();
    }
}