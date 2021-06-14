package com.example.breakfreeapp;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

/** Creates a new move-listener
 * @author Annika Stadelmann
 * @version 1.0
 * @since 1.0
 */
public abstract class moveBallListener implements SensorEventListener {
    /** Current and last value of accelerometer
     */
    float mAccelCurrent, mAccelLast;

    /** Get values for x- and y-tilt
     * @param x The x value of the accelerometer
     * @param y The y value of the accelerometer
     */
    //get values for x-axis and y-axis
    public abstract void onMove(float x, float y);

    /** Set gravitational constant
     * @param gravConst Gravitational constant
     */
    //set gravitational constant
    public void setGravConst(float gravConst){
        mAccelCurrent = gravConst;
        mAccelLast = gravConst;
    }

    /** Stores sensor values in x or y
     * @param se Sensor-event when smartphone is moved
     */
    //store sensor values in x or y
    public void onSensorChanged(SensorEvent se){
        float x = se.values[0];
        float y = se.values[1];

        onMove(x, y);
    }

    /** Checks if accuracy changed
     * @param sensor Sensor that is affected
     * @param accuracy Accuracy of the sensor-data
     */
    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }
}