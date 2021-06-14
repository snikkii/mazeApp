package com.example.breakfreeapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

/** Creates a activity where the settings can be changed
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class menuActivity extends AppCompatActivity {
    /** Values for Sound that is used, the name of the player, the maze that the player
     * wants to play and the IP-address of the raspberry pi
     */
    public String userAudio, userMaze, userName, brokerIP;
    /** Shows if sound is on or off and if ball should be controlled by smartphone or raspberry pi
     */
    public boolean raspberrySwitchOn, audioSwitchOn;

    /** Views where the settings are stored in to be displayed on the screen
     */
    TextView audioInfo, mazeInfo, nameInfo, raspberryIP;
    RadioGroup audioGroup, mazeGroup;
    RadioButton audio1, audio2, maze1, maze2, maze3, maze4, maze5, maze6;
    EditText editPlayerName, editRaspberryIP;
    Switch raspberrySmartphoneSwitch, audioOnOffSwitch;
    Button saveButton;

    /** Creates a menu-activity instance
     */
    public menuActivity() {
    }

    /** Creates the menu to be shown on the screen
     * @param savedInstanceState Stores information that won't be lost when activity is recreated
     */
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);

        //find views to store information in
        audioInfo = findViewById(R.id.audioSettingText);
        audioInfo.setText(R.string.audioText);
        mazeInfo = findViewById(R.id.mazeSettingText);
        mazeInfo.setText(R.string.mazeText);
        nameInfo = findViewById(R.id.nameSettingText);
        nameInfo.setText(R.string.nameText);
        raspberryIP = findViewById(R.id. ipAddress);
        raspberryIP.setText(R.string.ipAddressText);
        raspberryIP.setVisibility(View.INVISIBLE);

        audioGroup = findViewById(R.id.audioRadioGroup);
        mazeGroup = findViewById(R.id.mazeRadioGroup);

        //TODO: Labels
        audio1 = findViewById(R.id.audio1);
        audio1.setText(R.string.aRadio1);
        audio2 = findViewById(R.id.audio2);
        audio2.setText(R.string.aRadio2);
        maze1 = findViewById(R.id.maze1);
        maze1.setText(R.string.mRadio1);
        maze2 = findViewById(R.id.maze2);
        maze2.setText(R.string.mRadio2);
        maze3 = findViewById(R.id.maze3);
        maze3.setText(R.string.mRadio3);
        maze4 = findViewById(R.id.maze4);
        maze4.setText(R.string.mRadio4);
        maze5 = findViewById(R.id.maze5);
        maze5.setText(R.string.mRadio5);
        maze6 = findViewById(R.id.maze6);
        maze6.setText(R.string.mRadio6);

        editPlayerName = findViewById(R.id.editPlayerName);
        editRaspberryIP = findViewById(R.id. setIPAddress);

        raspberrySmartphoneSwitch = findViewById(R.id.RaspberrySmartphoneSwitch);
        raspberrySwitchOn = raspberrySmartphoneSwitch.isChecked();
        raspberrySmartphoneSwitch.setChecked(raspberrySwitchOn);
        editRaspberryIP.setVisibility(View.INVISIBLE);
        raspberrySmartphoneSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                raspberrySwitchOn = false;
                raspberrySmartphoneSwitch.setText(R.string.raspberrySwitchTextOn);
                editRaspberryIP.setVisibility(View.VISIBLE);
                raspberryIP.setVisibility(View.VISIBLE);
            } else {
                raspberrySwitchOn = true;
                raspberrySmartphoneSwitch.setText(R.string.raspberrySwitchTextOff);
                editRaspberryIP.setVisibility(View.INVISIBLE);
                raspberryIP.setVisibility(View.INVISIBLE);
            }
        });
        audioOnOffSwitch = findViewById(R.id.audioOffOnSwitch);
        audioSwitchOn = audioOnOffSwitch.isChecked();
        audioOnOffSwitch.setChecked(audioSwitchOn);
        audioOnOffSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if(isChecked){
                audioSwitchOn = true;
                audioOnOffSwitch.setText(R.string.audioSwitchTextOn);
            } else {
                audioSwitchOn = false;
                audioOnOffSwitch.setText(R.string.audioSwitchTextOff);
            }
        });

        //get broker-ip from shared preferences file
        SharedPreferences sh = getSharedPreferences("IP-Address broker", MODE_PRIVATE);
        brokerIP = sh.getString("broker", "192.168.178.36");

        editRaspberryIP.setHint(brokerIP);

        Bundle extras = getIntent().getExtras();
        if (extras != null) { //get settings that have been taken before
            userName = extras.getString("nameOfUser");
            if (TextUtils.isEmpty(userName)) {
                userName = getResources().getString(R.string.defaultUser);
            }
            editPlayerName.setHint(userName);
            String raspberrySwitchOffText = extras.getString("RaspberrySwitchOnOff");
            if (raspberrySwitchOffText.equals("true")) {
                raspberrySwitchOn = true;
                raspberrySmartphoneSwitch.setChecked(raspberrySwitchOn);
            } else if (raspberrySwitchOffText.equals("false")) {
                raspberrySwitchOn = false;
                raspberrySmartphoneSwitch.setChecked(raspberrySwitchOn);
            }
            String audioSwitchOnText = extras.getString("AudioSwitchOnOff");
            if(audioSwitchOnText.equals("true")){
                audioSwitchOn = true;
                audioOnOffSwitch.setChecked(audioSwitchOn);
            } else if(audioSwitchOnText.equals("false")){
                audioSwitchOn = false;
                audioOnOffSwitch.setChecked(audioSwitchOn);
            }
            userMaze = extras.getString("mazeOfUser");
            if(TextUtils.isEmpty(userMaze)){
                userMaze = getResources().getString(R.string.defaultMaze);
            }
            switch(userMaze){ //check radiobutton with maze that was selected before
                case "maze_1.txt":
                    maze1.setChecked(true);
                    break;
                case "maze_2.txt":
                    maze2.setChecked(true);
                    break;
                case "maze_3.txt":
                    maze3.setChecked(true);
                    break;
                case "maze_4.txt":
                    maze4.setChecked(true);
                    break;
                case "maze_5.txt":
                    maze5.setChecked(true);
                    break;
                case "maze_6.txt":
                    maze6.setChecked(true);
                    break;
            }
        }
        userAudio = extras.getString("audioOfUser");
        if(TextUtils.isEmpty(userAudio)){
            userAudio = getResources().getString(R.string.aRadio1);
        }
        switch(userAudio){ //check radiobutton with sound that was selected before
            case "Sound 1":
                audio1.setChecked(true);
                break;
            case "Sound 2":
                audio2.setChecked(true);
                break;
        }
        saveButton = findViewById(R.id.saveSettingsButton);
        saveButton.setOnClickListener(view -> {
            //save changes and store them in extras or shared preferences
            applyChanges();
            SharedPreferences sharedPreferences = getSharedPreferences("IP-Address broker", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("broker", brokerIP);
            editor.apply();
            Intent intent = new Intent(menuActivity.this, MainActivity.class);
            intent.putExtra("changesMaze", userMaze);
            intent.putExtra("changesName", userName);
            intent.putExtra("changesAudio", userAudio);
            intent.putExtra("changesRaspberrySwitch", String.valueOf(raspberrySwitchOn));
            intent.putExtra("changesAudioSwitch", String.valueOf(audioSwitchOn));
            startActivity(intent);
            finish();
        });
    }

    /** Stores changes in variables to bring them back to main-activity
     */
    public void applyChanges() {
        String playerNameEdited, brokerEdited;
        int selectedAudioID, selectedMazeID;
        //store settings in variables
        selectedAudioID = audioGroup.getCheckedRadioButtonId();
        selectedMazeID = mazeGroup.getCheckedRadioButtonId();
        playerNameEdited = editPlayerName.getText().toString();
        brokerEdited = editRaspberryIP.getText().toString();
        if (selectedAudioID != -1) {
            RadioButton selectedAudio = findViewById(selectedAudioID);
            this.userAudio = setAudio(selectedAudio.getText().toString());
        }
        if (selectedMazeID != -1) {
            RadioButton selectedMaze = findViewById(selectedMazeID);
            this.userMaze = setMaze(selectedMaze.getText().toString());
        }
        if (!TextUtils.isEmpty(playerNameEdited)) {
            this.userName = playerNameEdited;
            if (TextUtils.isEmpty(userName)) {
                userName = getResources().getString(R.string.defaultUser);
            }
        }
        if(!TextUtils.isEmpty(brokerEdited)){
            this.brokerIP = brokerEdited;
            if(TextUtils.isEmpty(brokerIP)){
                brokerIP = getResources().getString(R.string.raspberryIP);
            }
        }
        if (raspberrySwitchOn) {
            raspberrySmartphoneSwitch.setChecked(true);
        } else {
            raspberrySmartphoneSwitch.setChecked(false);
        }
        if(audioSwitchOn){
            audioOnOffSwitch.setChecked(true);
        } else {
            audioOnOffSwitch.setChecked(false);
        }
    }

    /** Sets sound-settings
     * @param audio Sound that the user selected
     * @return The selected sound
     */
    //set sound-settings
    private String setAudio(String audio) {
        String selAudio;
        if (audio.equals("Sound 2")) {
            selAudio = "Sound 2";
        } else {
            selAudio = "Sound 1";
        }
        return selAudio;
    }

    /** Sets maze-settings
     * @param maze Maze that the user selected
     * @return The selected maze
     */
    //set maze-settings
    private String setMaze(String maze) {
        String selMaze;
        switch (maze) {
            case "Maze 2":
                selMaze = "maze_2.txt";
                break;
            case "Maze 3":
                selMaze = "maze_3.txt";
                break;
            case "Maze 4":
                selMaze = "maze_4.txt";
                break;
            case "Maze 5":
                selMaze = "maze_5.txt";
                break;
            case "Maze 6":
                selMaze = "maze_6.txt";
                break;
            default:
                selMaze = "maze_1.txt";
        }
        return selMaze;
    }

    /** Fills the menu with the action to go back to main-activity
     * @param item Item that is in the menu
     * @return Boolean (true if menu-item is handled successfully)
     */
    //go back to main by hitting the arrow
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}