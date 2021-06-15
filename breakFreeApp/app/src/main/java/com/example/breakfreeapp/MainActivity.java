package com.example.breakfreeapp;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/** Combines the ball and the maze so the game can be played
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public class MainActivity extends AppCompatActivity {
    /** Checks if "about" was clicked
     */
    private boolean aboutAlert = false;
    /** userName, userAudio, userMaze and userTime store the settings
     */
    private String userName, userAudio, userMaze, userTime;
    /** Flags that are useful for the switches in settings and for the mqtt-connection
     */
    private boolean raspberrySwitchOn = false, audioSwitchOn = true,
            connectionFailed = false;

    /** Objects for the maze and the ball
     */
    mazeView drawMaze;
    ballView drawBall;

    /** Makes the ball move
     */
    SensorManager mSensorManager;
    moveBallListener mBallListener;

    /** Buttons to start the game and show the highscore-list
     */
    Button startGameBtn;
    FloatingActionButton highscoreBtn;
    /** Checks if the game has started
     */
    public boolean isGameStarted = false;

    /** Builds the alert-box
     */
    AlertDialog.Builder alertDialogBuilder;

    /** Display timer and variables for seconds and minutes
     */
    TextView timer;
    Handler handler;
    private int second, minute;

    /** Subscription- and publishing-topic
     */
    private static final String sub_topic = "moveBallData";
    private static final String pub_topic = "moveBallMessage";
    /** Important for connection with raspberry pi
     */
    private final int qos = 0;
    private  final MemoryPersistence persistence = new MemoryPersistence();
    private MqttClient client;
    private final String TAG = MainActivity.class.getSimpleName();
    private String BROKER;

    /** Creates main-activity
     * @param savedInstanceState Stores information that won't be lost when activity is recreated
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //get settings from SharedPreferences
        SharedPreferences sh = getSharedPreferences("game settings", MODE_PRIVATE);
        BROKER = sh.getString("broker", "192.168.178.36");
        userAudio = sh.getString("userAudio", "Sound 1");
        userName = sh.getString("userName", "player 1");
        userMaze = sh.getString("userMaze", "maze_1.txt");
        String audioSwitchBuffer = sh.getString("audioOn", "true");
        if(audioSwitchBuffer.equals("false")){
            audioSwitchOn = false;
        } else if(audioSwitchBuffer.equals("true")){
            audioSwitchOn = true;
        }
        String raspberrySwitchBuffer = sh.getString("raspberryOff", "false");
        if(raspberrySwitchBuffer.equals("false")) {
            raspberrySwitchOn = false;
        } else if (raspberrySwitchBuffer.equals("true")){
            raspberrySwitchOn = true;
        }

        //button for highscore-list
        highscoreBtn = findViewById(R.id.highscore);
        highscoreBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, highscoreActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //find views
        timer = findViewById(R.id.timerTextView);
        handler = new Handler();

        drawMaze = findViewById(R.id.mazeLayout);
        drawBall = findViewById(R.id.ballLayout);
        //start-button starts game
        startGameBtn = findViewById(R.id.startGameButton);
        startGameBtn.setOnClickListener(view -> {
            findViewById(R.id.highscore).setVisibility(View.INVISIBLE);
            second = 0;
            handler.postDelayed(runnable,1000);
            readMaze(userMaze);
            startGameBtn.setVisibility(View.INVISIBLE);
            isGameStarted = true;
            drawBall.getStart(isGameStarted);
        });

        //when controlling ball by raspberry the sensors of the smartphone will be unregistered
        if(!raspberrySwitchOn){
            mSensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
            mBallListener = new moveBallListener() {
                @Override
                public void onMove(float x, float y) {
                    startNewGame();
                    MainActivity.this.ballMove(x, y);
                }
            };

            mBallListener.setGravConst(mSensorManager.GRAVITY_EARTH);
        }
    }

    /** Gives the values of x- and y-tilt to the ballView class that draws the ball
     * @param x The x value of the accelerometer
     * @param y The y value of the accelerometer
     */
    //sensor-values will be given to ballView-class
    public void ballMove(float x, float y){
        drawBall.getDirection(x, y);
        drawBall.invalidate();
    }

    /** Disconnects mqtt-connection and unregisters sensorlistener when activity changes
     */
    //when activity changes
    @Override
    protected void onPause() {
        if(raspberrySwitchOn){ disconnect(); }
        else{
            if(!connectionFailed) {
                mSensorManager.unregisterListener(mBallListener);
            }
        }
        super.onPause();
    }

    /** Reconnects mqtt-connection and registers sensorlistener when coming back from another activity
     */
    //when coming back from other activity
    @Override
    protected void onResume(){
        super.onResume();
        if(raspberrySwitchOn){
            connect("tcp://" + BROKER +":1883");
            subscribe(sub_topic);
        } else {
            mSensorManager.registerListener(mBallListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    /** Creates and shows an alert-box
     * @param msg The message the alert-box has to show
     */
    //opens alert-box which appears by clicking "about" in optionsmenu
    public void openAlertBox(String msg){
        alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(msg);
        alertDialogBuilder.setNeutralButton("ok",
                (dialog, which) -> {
                    if (!aboutAlert){
                        drawBall.gameOver = false;
                        drawBall.isWinner = false;
                        if(!raspberrySwitchOn){
                            mSensorManager.registerListener(mBallListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                                    SensorManager.SENSOR_DELAY_NORMAL);
                        }
                        //store information in SharedPreferences
                        SharedPreferences sharedPreferences = getSharedPreferences("game settings", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putString("raspberryOff", String.valueOf(raspberrySwitchOn));
                        editor.apply();
                        Intent intent = new Intent(MainActivity.this, highscoreActivity.class);
                        intent.putExtra("dbName", userName);
                        intent.putExtra("dbTime", userTime);
                        intent.putExtra("dbMaze", userMaze);
                        intent.putExtra("dbAudio", userAudio);
                        intent.putExtra("isAudioOn", String.valueOf(audioSwitchOn));
                        startActivity(intent);
                        finish();
                        dialog.cancel();
                    }
                    else if(aboutAlert){
                        startNewGame();
                        aboutAlert = false;
                        dialog.cancel();
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    /** Puts maze from a text-file in a matrix
     * @param mazeToDraw Text-file which should be read
     */
    //reads selected maze from assert and stores it in "mazeCharArr"
    public void readMaze(String mazeToDraw){
        char[][] mazeCharArr; //the maze will be stored in here
        BufferedReader reader = null;
        try{
            reader = new BufferedReader(new InputStreamReader(getAssets().open(mazeToDraw)));
            String mLine;
            mazeCharArr = new char[21][21];
            int mazeRow = 0;
            while((mLine = reader.readLine()) != null){
                for(int mazeCol = 0; mazeCol != mLine.length(); mazeCol++){
                    char c = mLine.charAt(mazeCol);
                    mazeCharArr[mazeRow][mazeCol] = c;
                }
                mazeRow++;
            }
            //makes sure ballView and mazeView are getting the selected maze
            drawBall.getMaze(mazeCharArr);
            drawMaze.getMaze(mazeCharArr);
            drawMaze.invalidate();
        }
        catch (IOException e) {
            Log.e(TAG, "Problem with opening file!");
        }
        finally {
            if (reader != null){
                try{
                    reader.close();
                }
                catch (IOException e){
                    Log.e(TAG, "Problem with closing file!");
                }
            }
        }
    }

    /** Creates options-menu
     * @param menu The object of the menu-class
     * @return Boolean (true to be shown, false to not be shown
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    /** Fills the menu with actions to go to the settings-screen or to show an alert-box
     * @param item Item that is in the menu
     * @return Boolean (true if menu-item is handled successfully)
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch(id){
            case R.id.settings: //go to game-settings
                startNewGame();
                SharedPreferences sharedPreferences = getSharedPreferences("game settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("broker", BROKER);
                editor.putString("userAudio", userAudio);
                editor.putString("userName", userName);
                editor.putString("userMaze", userMaze);
                editor.putString("audioOn", String.valueOf(audioSwitchOn));
                editor.putString("raspberryOff", String.valueOf(raspberrySwitchOn));
                editor.apply();
                Intent intent = new Intent(MainActivity.this, menuActivity.class);
                startActivity(intent);

                return true;
            case R.id.about: //go to alert-box which shows some information
                aboutAlert = true;
                openAlertBox("© Annika Stadelmann\nVersion: 1.0\n\nJuni 2021");
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /** Creates the timer
     */
    //this is the timer
    public Runnable runnable = new Runnable() {
        public void run() {
            second += 1;
            minute = second/60;
            second = second%60;
            timer.setText("" + minute + ":" +
                    String.format("%02d", second));

            handler.postDelayed(this, 1000);
        }
    };

    /** Resets the timer when game is over or the activity changes
     */
    //resets timer when game is finished or interrupted
    public void resetTimer(){
        handler.removeCallbacks(runnable);
        second = 0;
        minute = 0;
        timer.setText(getResources().getString(R.string.timerText));
    }

    /** Sets all parameters to get the game started
     */
    public void startNewGame(){
        if(drawBall.gameOver || aboutAlert){
            //when opened alert-box, settings or finished one game: start a new one
            userTime = timer.getText().toString();
            resetTimer();
            if(!raspberrySwitchOn) {
                mSensorManager.unregisterListener(mBallListener);
            }
            isGameStarted = false;

            drawBall.getStart(isGameStarted);

            //if you got to the end of the maze: store data in db
            if(drawBall.isWinner){
                drawBall.gameOver = false;
                drawBall.isWinner = false;
                if(!raspberrySwitchOn){
                    mSensorManager.registerListener(mBallListener, mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                            SensorManager.SENSOR_DELAY_NORMAL);
                }
                SharedPreferences sharedPreferences = getSharedPreferences("game settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("raspberryOff", String.valueOf(raspberrySwitchOn));
                editor.apply();
                Intent intent = new Intent(MainActivity.this, highscoreActivity.class);
                intent.putExtra("dbName", userName);
                intent.putExtra("dbTime", userTime);
                intent.putExtra("dbMaze", userMaze);
                intent.putExtra("dbAudio", userAudio);
                intent.putExtra("isAudioOn", String.valueOf(audioSwitchOn));
                if(raspberrySwitchOn){
                    //make sure raspberry gets the information that game is over
                    publish(pub_topic, "finished");
                }
                startActivity(intent);
            }
            findViewById(R.id.highscore).setVisibility(View.VISIBLE);
            startGameBtn.setVisibility(View.VISIBLE);
        }
    }

    /** Connects to raspberry pi
     * @param broker IP-address of raspberry pi
     */
    //connect to broker
    private void connect (String broker) {
        String clientId;
        try {
            clientId = MqttClient.generateClientId();
            client = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            Log.d(TAG, "Connecting to broker: " + broker);
            try {
                client.connect(connOpts);
                Log.d(TAG, "Connected with broker: " + broker);
                connectionFailed = false;
                Context context = getApplicationContext();
                //inform user that connection was successful
                Toast toast = Toast.makeText(context,
                        "Connected with broker " + broker,
                        Toast.LENGTH_LONG);
                toast.show();

            } catch (Exception e) {
                raspberrySwitchOn = false;
                connectionFailed = true;
                SharedPreferences sharedPreferences = getSharedPreferences("game settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString("raspberryOff", String.valueOf(raspberrySwitchOn));
                editor.apply();
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                Context context = getApplicationContext();
                //inform user that connection failed
                Toast toast = Toast.makeText(context,
                        "Connection with broker " + broker + " failed",
                        Toast.LENGTH_LONG);
                toast.show();
                startActivity(intent);
                finish();
            }
        } catch (MqttException me) {
            Log.e(TAG, "Reason: " + me.getReasonCode());
            Log.e(TAG, "Message: " + me.getMessage());
            Log.e(TAG, "localizedMsg: " + me.getLocalizedMessage());
            Log.e(TAG, "cause: " + me.getCause());
            Log.e(TAG, "exception: " + me);
        }
    }

    /** Subscribes to a topic
     * @param topic Topic to subscribe to
     */
    //subscribe to topic
    private void subscribe(String topic) {
        try {
            client.subscribe(topic, qos, (topic1, msg) -> {
                String message = new String(msg.getPayload());

                String[] vals = message.split(", ");

                startNewGame();
                MainActivity.this.ballMove(Float.parseFloat(vals[0]), Float.parseFloat(vals[1]));
            });
            Log.d(TAG, "subscribed to topic " + topic);
            Context context = getApplicationContext();
            //inform user about the topic that the smartphone has subscribed to
            Toast toast = Toast.makeText(context,
                    "Subscribed to topic " + topic,
                    Toast.LENGTH_LONG);
            toast.show();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /** Publishes a message someone has subscribed to
     * @param topic Topic to subscribe to
     * @param msg Message that will be published
     */
    //publish information
    private void publish(String topic, String msg) {
        MqttMessage message = new MqttMessage(msg.getBytes());
        message.setQos(qos);
        try {
            client.publish(topic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /** Discconect from raspberry pi
     */
    //disconnect from broker
    private void disconnect() {
        try {
            client.unsubscribe(sub_topic);
            Context context = getApplicationContext();
            //inform user about unsubscribing the topic
            Toast toast = Toast.makeText(context,
                    "Unsubscribed from topic " + sub_topic,
                    Toast.LENGTH_LONG);
            toast.show();
        } catch (MqttException e) {
            e.printStackTrace();
            Log.e(TAG, e.getMessage());
        }
        try {
            Log.d(TAG, "Disconnecting from broker");
            client.disconnect();
            Log.d(TAG, "Disconnected.");
            Context context = getApplicationContext();
            //inform user about successful disconnection
            Toast toast = Toast.makeText(context,
                    "Disconnected from broker",
                    Toast.LENGTH_LONG);
            toast.show();
        } catch (MqttException me) {
            Log.e(TAG, me.getMessage());
            Context context = getApplicationContext();
            //inform user that disconnection failed
            Toast toast = Toast.makeText(context,
                    "Disconnection failed",
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }
}