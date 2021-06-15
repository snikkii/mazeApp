package com.example.breakfreeapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/** Creates highscore-list
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class highscoreActivity extends AppCompatActivity {
    /** Database-access instance to get access to database
     */
    private DBAccess dbAccess;
    /** Adapter to set a cursor
     */
    private SimpleCursorAdapter adapter;
    private Cursor cursor;

    /** Checks if there has to be inserted something in the table
     */
    private boolean newInsert = false;

    /** The ID of the current player
     */
    long currentPlayerID;

    /** Checks if raspberry pi is connected to smartphone
     */
    private String raspberryOn;

    /** Creates the screen that displays the highscore list
     * @param savedInstanceState Stores information that won't be lost when activity is recreated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_highscore);

        ListView highscoreList;

        setTitle("break free highscore");
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        String sql = "CREATE TABLE IF NOT EXISTS highscore (_id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name VARCHAR(20), " +
                "time VARCHAR(50), " +
                "maze VARCHAR(20))";

        //create table if it didn't exist
        dbAccess = new DBAccess(this, "highscore.dat", sql);

        //get sounds from raw-folder
        final MediaPlayer sound1 = MediaPlayer.create(this, R.raw.sound_1);
        final MediaPlayer sound2 = MediaPlayer.create(this, R.raw.sound_2);

        //create cursor
        cursor = dbAccess.createListViewCursor();
        highscoreList  = this.findViewById(R.id.highscoreListView);

        //find views that show the columns of the table
        String[] viewColumns = new String[]{"name", "time", "maze"};
        int[] viewViews = new int[]{R.id.textViewName, R.id.textViewTime, R.id.textViewMaze};
        adapter = new SimpleCursorAdapter(this, R.layout.dataset, cursor, viewColumns, viewViews, CursorAdapter.FLAG_REGISTER_CONTENT_OBSERVER);
        adapter.setViewBinder((view, cursor, columnIndex) -> {
            TextView all = (TextView) view;
            all.setTextColor(Color.parseColor("#FFFFFFFF"));
            //color name of current player blue
            if(columnIndex == 1 && newInsert){
                long changeID = cursor.getLong(0);
                if(currentPlayerID == changeID){
                    TextView currPlayer = (TextView) view;
                    currPlayer.setTextColor(Color.parseColor("#476CFF"));
                }
            }
            return false;
        });

        highscoreList.setAdapter(adapter);

        //get information about how game was played (by smartphone or by raspberry)
        SharedPreferences sh = getSharedPreferences("game settings", MODE_PRIVATE);
        raspberryOn = sh.getString("raspberryOff", "false");
        if(raspberryOn.equals("true")){
            raspberryOn = "false";
        }
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            //get information from just played game
            String dbName = extras.getString("dbName");
            String dbTime = extras.getString("dbTime");
            String dbMaze = extras.getString("dbMaze");
            String dbAudio = extras.getString("dbAudio");
            String audioOn = extras.getString("isAudioOn");
            dbMaze = dbMaze.replace(".txt", "");
            dbMaze = dbMaze.replace("_", " ");
            dbMaze = dbMaze.replace("m", "M");
            //if the audio-switch is neither true or false turn audio off
            if(TextUtils.isEmpty(audioOn)){ audioOn = "false"; }
            //insert new dataset and play sound based on the users decision
            newInsert = true;
            DataSet dataSet = new DataSet(dbName, dbTime, dbMaze);
            currentPlayerID  = dbAccess.insertDataSet(dataSet);
            updateView();
            if(dbAudio.equals("Sound 2") && audioOn.equals("true")){
                sound2.start();
            } else if(dbAudio.equals("Sound 1") && audioOn.equals("true")){
                sound1.start();
            }
        } else {
            updateView();
        }
    }

    /** Creates options-menu
     * @param menu The object of the menu-class
     * @return Boolean (true to be shown, false to not be shown
     */
    //create options-menu to go back to main-activity or delete highscore-list
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_highscore, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /** Fills the menu with the action to go back to main-activity or delete highscore-list
     * @param item Item that is in the menu
     * @return Boolean (true if menu-item is handled successfully)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.home:
                SharedPreferences sharedPreferences = getSharedPreferences("game settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("raspberryOff", raspberryOn);
                editor.apply();
                Intent intent = new Intent(highscoreActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
                return true;
            case R.id.deleteDB:
                dbAccess.onUpgrade(dbAccess.getDB(), 0, 1);
                Intent in = new Intent(highscoreActivity.this, highscoreActivity.class);
                Context context = getApplicationContext();
                Toast toast = Toast.makeText(context,
                        "Highscore list is deleted",
                        Toast.LENGTH_LONG);
                toast.show();
                startActivity(in);
                finish();
        }
        return super.onOptionsItemSelected(item);

    }

    /** Updates the list
     */
    private void updateView(){
        if(cursor != null){
            cursor.close();
        }
        cursor = dbAccess.createListViewCursor();
        adapter.changeCursor(cursor);
    }

    /** Closes the cursor and the access to the database
     */
    @Override
    protected void onDestroy(){
        if(cursor != null && !cursor.isClosed()){
            cursor.close();
        }
        if(dbAccess != null){
            dbAccess.close();
        }
        super.onDestroy();
    }
}