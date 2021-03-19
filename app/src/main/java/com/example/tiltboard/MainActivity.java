package com.example.tiltboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;


public class MainActivity extends Activity {

    private TextView mTextView;
    private boolean typing = false;
    private SensorManager sensorManager;
    private Sensor aSensor;
    private final float downFilter = -2f;
    private final float upFilter = 2f;
    private final float yFilter = 2f;
    private final float xFilter = 2f;
    private final float dFilter = 1.5f;
    private float max = 0;
    private boolean done = true;
    private char[] chars = new char[5];
    private int c = 0;
    private long t1 = 0;
    private long touchT = 0;
    String message = "";
    boolean upper = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = (TextView) findViewById(R.id.text);
        //sets a touch listener for the text view
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getEventTime() - touchT > 250) {
                    touchT = e.getEventTime();
                    del();
                    text(message);
                    done = false;
                    typing = false;
                }
                    return true;
            }
        });
        //initializes the sensor manager, accelerometer, and registers the accelerometer listener
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(aListener, aSensor, SensorManager.SENSOR_DELAY_NORMAL);

    }
    public void text(String s){
        mTextView.setText(s);
    }
    public SensorEventListener aListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(final SensorEvent e) {
            //if not done typing
            if(!done) {
                //assigns the event values to a float array
                float[] values = e.values;
                float x = values[0];
                float y = values[1];
                float z = values[2];
                //checks if the event was large enough to trigger a filter
                if (z > upFilter || y > dFilter || x > dFilter || z < downFilter || y < -dFilter || x < -dFilter) {
                    //prevents events from occurring to rapidly

                    if (e.timestamp - t1 > 250000000) {
                        t1 = e.timestamp;
                        //if the user is ready to type their character
                        if (typing) {
                            aType(values);
                        }
                        //if the user needs to chose their char set
                        else if (!typing) {
                            aRead(values);
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {

        }
    };

    public void aType(float[] values){
        //assigns event values to xyz coordinates
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //down,
        if(z<downFilter && (z<y && z<x)) {
            message += chars[c]+"";
            c = 0;
            for(int i = 0; i<5; i++){
                chars[i]=' ';
            }
            typing = false;
            String str = message + "\n\n";
            for(int i = 0; i < 5; i++){
                str += (chars[i] + " ");
            }
            text(str);
            return;
        }
        //up
        else if(z>upFilter && (z>y && z>x)){
            typing = false;
            for(int i = 0; i<5; i++){
                chars[i]=' ';
            }
            String str = message + "\n\n";
            for(int i = 0; i < 5; i++){
                str += (chars[i] + " ");
            }
            text(str);
            return;
        }
        //forward
        else if(y>yFilter && (y>z && y>x)){
            upper = true;
            for (int i = 0; i<5; i++){
                chars[i] = Character.toUpperCase(chars[i]);
            }
        }
        //backward
        else if(y<-yFilter && (y<z && y<x) && upper){
            upper = false;
            for (int i = 0; i<5; i++){
                chars[i] = Character.toLowerCase(chars[i]);
            }
        }
        //right
        else if(x>xFilter && (x>z && x>y)){
            if(c<4)
                c++;
            else
                c=0;
        }
        //left
        else if(x<-xFilter && (x<z && x<y)){
            if(c>0)
                c--;
            else
                c=4;
        }

        String s = message + "\n";
        s += chars[c] + "\n";
        for(int i = 0; i < 5; i++){
            s += (chars[i] + " ");
        }
        text(s);
    }

    public void aRead(float[] values){

        float x = values[0];
        float y = values[1];
        float z = values[2];

        //Up
        if(z>upFilter && (z>y && z>x)){
            charProc(10);
        }
        //Down
        else if(z<downFilter && (z<y && z<x)){
            charProc(5);
        }
        //Diagonal
        else if(x>dFilter && y<-dFilter){
            charProc(9);
        }
        else if(x<-dFilter && y<-dFilter){
            charProc(7);
        }
        else if(x>dFilter && y>dFilter){
            charProc(3);
        }
        else if(x<-dFilter && y>dFilter){
            charProc(1);
        }
        //Forward
        else if(y>yFilter && (y>z && y>x) && (x<xFilter && x>-xFilter)){
            charProc(2);
        }
        //Backward
        else if(y<(-1*yFilter) && (y<z && y<x) && (x<xFilter && x>-xFilter)){
            charProc(8);
        }
        //Right
        else if(x>xFilter && (x>z && x>y) && (y<yFilter && x>-yFilter)){
            charProc(6);
        }
        //Left
        else if(x<-xFilter && (x<z && x<y) && (y<yFilter && y>-yFilter)){
            charProc(4);
        }

    }

    public void charProc(int q){
        typing = true;
        switch(q){
            case 1:
                chars[0] = 'a';
                chars[1] = 'b';
                chars[2] = 'c';
                chars[3] = '0';
                chars[4] = '1';
                break;
            case 2:
                chars[0] = 'd';
                chars[1] = 'e';
                chars[2] = 'f';
                chars[3] = 'g';
                chars[4] = '2';
                break;
            case 3:
                chars[0] = 'h';
                chars[1] = 'i';
                chars[2] = 'j';
                chars[3] = 'k';
                chars[4] = '3';
                break;
            case 4:
                chars[0] = 'l';
                chars[1] = 'm';
                chars[2] = 'n';
                chars[3] = 'o';
                chars[4] = '4';
                break;
            /*case 5:
                del();
                typing = false;
                chars[0] = ' ';
                chars[1] = ' ';
                chars[2] = ' ';
                chars[3] = ' ';
                chars[4] = ' ';
                break;*/
            case 6:
                chars[0] = 'p';
                chars[1] = 'q';
                chars[2] = 'r';
                chars[3] = 's';
                chars[4] = '5';
                break;
            case 7:
                chars[0] = 't';
                chars[1] = 'u';
                chars[2] = 'v';
                chars[3] = 'w';
                chars[4] = '6';
                break;
            case 8:
                chars[0] = ' ';
                chars[1] = '.';
                chars[2] = '?';
                chars[3] = '!';
                chars[4] = '7';
                break;
            case 9:
                chars[0] = 'x';
                chars[1] = 'y';
                chars[2] = 'z';
                chars[3] = '8';
                chars[4] = '9';
                break;
            case 10:
                typing = false;
            default:
                chars[0] = ' ';
                chars[1] = ' ';
                chars[2] = ' ';
                chars[3] = ' ';
                chars[4] = ' ';
        }

        String s = message + "\n" + chars[0] + "\n";
        for (int i = 0; i < 5; i++) {
            s += (chars[i] + " ");
        }
        text(s);

    }

    public void del(){
        if(message != null && message.length() > 0){
            message = message.substring(0,message.length()-1);
        }
        String str = message + "\n\n";
        text(str);
        typing = false;
    }


}