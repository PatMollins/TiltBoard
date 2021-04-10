package com.example.tiltboard;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.util.TimeUnit;
import android.os.Bundle;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannedString;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import static com.google.android.gms.wearable.DataMap.TAG;


public class MainActivity extends Activity {
    private TextView mTextView;
    private TextView mTextView_menu;
    private TextView mTextView_typing;
    private TextView mTextView_characters;
    private ImageView mRelativeLayout;
    private boolean typing = false;
    private SensorManager sensorManager;
    private Sensor aSensor;
    private final float downFilter = -3f;
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
        typing = false;
        //sets a touch listener for the text view
        mTextView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                    returnToMain();
                    done = false;
                    return true;
            }
        });

        //initializes the sensor manager, accelerometer, and registers the accelerometer listener
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        aSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
        sensorManager.registerListener(aListener, aSensor, SensorManager.SENSOR_DELAY_GAME);

    }
    public void text(String s){
       mTextView_menu.setText(s);
    }
    public void textTyping(String s){
        mTextView_typing.setText(s);
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

                    if ((e.timestamp - t1) > 500000000L) {
                        t1 = e.timestamp;

                        //if the user is ready to type their character
                        if (typing) {
                            setContentView(R.layout.key_menu);
                            mTextView_typing= (TextView) findViewById(R.id.userMessage_typing);
                            mTextView_characters = (TextView) findViewById(R.id.characters);
                            aType(values);
                        }
                        //if the user needs to chose their char set
                        else if (!typing) {
                            //returnToMain();
                            aRead(values);
                        }
                    }
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {}
    };


    //User chooses specific character
    public void aType(float[] values){
        textTyping(message);

        setChars();

        //assigns event values to xyz coordinates
        float x = values[0];
        float y = values[1];
        float z = values[2];

        //up
        if(z>upFilter && (z>y && z>x)){
            typing = false;
            c = 0;
            for(int i = 0; i<5; i++){
                chars[i]=' ';
            }
            returnToMain();
        }
        //down,
        else if(z<downFilter && (z<y && z<x)) {
            message += chars[c];
            c = 0;
            for(int i = 0; i<5; i++){
                chars[i]=' ';
            }
            typing = false;
            returnToMain();
        }
        //forward
        else if(y>yFilter && (y>z && y>x) && !upper){
            upper = true;
            for (int i = 0; i<5; i++){
                chars[i] = Character.toUpperCase(chars[i]);
            }
            setChars();
        }
        //backward
        else if(y<-yFilter && (y<z && y<x) && upper){
            upper = false;
            for (int i = 0; i<5; i++){
                chars[i] = Character.toLowerCase(chars[i]);
            }
            setChars();
        }
        //right
        else if(x>xFilter && (x>z && x>y)){
            if(c<4)
                c++;
            else
                c=0;
            setChars();
        }
        //left
        else if(x<-xFilter && (x<z && x<y)){
            if(c>0)
                c--;
            else
                c=4;
            setChars();
        }
    }

    public void aRead(float[] values){
        mTextView = (TextView) findViewById(R.id.userMessage_Menu);
        text(message);

        float x = values[0];
        float y = values[1];
        float z = values[2];



        //Diagonal
        if(x>dFilter && y<-dFilter){
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
        setContentView(R.layout.key_menu);
        mTextView_typing= (TextView) findViewById(R.id.userMessage_typing);
        mTextView_characters = (TextView) findViewById(R.id.characters);
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
        }

        textTyping(message);
        setChars();
    }

    public void returnToMain(){
        setContentView(R.layout.keyboard_menu);
        mRelativeLayout = (ImageView) findViewById(R.id.arrows);
        mRelativeLayout.setOnTouchListener(new View.OnTouchListener(){
            @Override
            public boolean onTouch(View v, MotionEvent e) {
                if (e.getEventTime() - touchT > 250) {
                    touchT = e.getEventTime();
                    del();
                    done = false;
                    typing = false;
                }
                return true;
            }
        });
        mTextView_menu = (TextView) findViewById(R.id.userMessage_Menu);
        text(message);
    }

    public void del(){
        if(message != null && message.length() > 0){
            message = message.substring(0,message.length()-1);
        }
        text(message);
        typing = false;
    }

    public void setChars(){
        String temp = "" + chars[0];
        for(int i = 1; i < chars.length; i++){
            temp += " " + chars[i];
        }

        SpannableString hoveredChar = new SpannableString(temp);
        hoveredChar.setSpan(new BackgroundColorSpan(Color.GREEN), c*2, (c*2)+1, 0);

        mTextView_characters.setText(hoveredChar);
    }


}