package com.example.myapplication;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;

import com.example.myapplication.GlassGestureDetector.Gesture;
import com.example.myapplication.GlassGestureDetector.OnGestureListener;

import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends Activity implements OnGestureListener {

    private SmartDevice philipsHue;
    private GlassGestureDetector gestureDetector;
    private ScheduledExecutorService executorService;
    private TextView indicator, smallIndicator, currentBrightness, brightnessText;
    private ProgressBar brightnessBar;
    private String state, menuText;
    private boolean dummyMode;
    private int brightness;
    private float brightnessLvl;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        indicator = findViewById(R.id.indicator);
        currentBrightness = findViewById(R.id.brightness);
        brightnessBar = findViewById(R.id.brightnessBar);
        brightnessText = findViewById(R.id.brightness2);

        gestureDetector = new GlassGestureDetector(this, this);

        state = null;
        dummyMode = true;
        brightness = 0;
        brightnessLvl = 0;

        if(dummyMode){
            updateDummy("setUp");
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run(){
                    updateIndicator();
                }
            }, 0, 3, TimeUnit.SECONDS);
        } else{
            setUpDevice();
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIndicator();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev); // || super.dispatchTouchEvent(ev);
    }

    private void setUpDevice(){
        if(!dummyMode){
            //TODO: Change this so it actually is an entity_id like media_player and not the full address
            philipsHue = new SmartDevice(
                    "lights",
                    "http://192.168.1.6:8123/api/states/light.living_room",
                    "http://192.168.1.6:8123/api/services/light/turn_on",
                    "http://192.168.1.6:8123/api/services/light/turn_off",
                    "{\"entity_id\": \"light.living_room\"}",
                    "http://192.168.1.6:8123/api/services/light/turn_on",
                    "http://192.168.1.6:8123/api/services/light/turn_on",
                    null,
                    null);
        }
    }

    private Toast createToast(String choice){
        Toast toast = Toast.makeText(getBaseContext(), choice, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
        return toast;
    }

    private void updateIndicator(){
        if(!dummyMode){
            if(philipsHue.getState() == null){
                return;
            }else{
                state = philipsHue.getState("state");
                if(philipsHue.getLevel() == null){
                    brightness = 0;
                } else{
                    brightness = Integer.parseInt(philipsHue.getLevel());
                    brightnessLvl = (brightness/255f)*100;
                    Log.d("brightness: ", Integer.toString(brightness));
                }
                if(state.equals("off")){
                    Log.d("state: ", "OFF");
                    indicator.setBackgroundResource(R.drawable.off_button);
                    indicator.setText("OFF");
                    brightnessBar.setProgress((int) brightnessLvl);
                    currentBrightness.setText(Integer.toString(brightness));
                    Log.d("ProgressBarLevel:", Float.toString(brightnessLvl));
                } else{
                    Log.d("state: ", "ON");
                    indicator.setBackgroundResource(R.drawable.on_button);
                    indicator.setText("ON");
                    brightnessBar.setProgress((int) brightnessLvl);
                    currentBrightness.setText(Integer.toString(brightness));
                    Log.d("ProgressBarLevel:", Float.toString(brightnessLvl));
                }
            }

        }

    }

    @SuppressLint("SetTextI18n")
    private void updateDummy(String string){
        switch (string) {
            case "increase":
                if(brightnessLvl + 25 <= 100){
                    brightnessLvl += 25;
                    brightnessBar.setProgress((int) brightnessLvl);
                    currentBrightness.setText(String.valueOf(brightnessLvl));
                }
                break;
            case "decrease":
                if(brightnessLvl - 25 >= 0){
                    brightnessLvl -= 25;
                    brightnessBar.setProgress((int) brightnessLvl);
                    currentBrightness.setText(String.valueOf(brightnessLvl));
                }
                break;
            case "on":
                indicator.setBackgroundResource(R.drawable.on_button);
                indicator.setText("ON");
                state = "on";
                break;
            case "off":
                indicator.setBackgroundResource(R.drawable.off_button);
                indicator.setText("OFF");
                state = "off";
                break;
            case "next":
                //Play next song
                break;
            case "previous":
                //Play previous song
                break;
            case "setUp":
                state = "off";
                brightnessLvl = 50;
        }
    }

    //TODO: Implement specific functions to each smartdevice as fragments that you easily can swap between
    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case SWIPE_UP:
                Log.d("App", "Swipe Up!");
                if(dummyMode){
                    updateDummy("increase");
                } else{
                    runOnUiThread(() -> createToast("Increasing brightness...").show());
                    philipsHue.increase();
                }
                return true;

            case SWIPE_DOWN:
                Log.d("App", "Swipe Down!");
                if(dummyMode){
                    updateDummy("decrease");
                } else {
                    runOnUiThread(() -> createToast("Decreasing brightness...").show());
                    philipsHue.decrease();
                }
                return true;

            case TAP:
                if(dummyMode) {
                    if(state.equals("on")){
                        updateDummy("off");
                    } else {
                        updateDummy("on");
                    }
                } else{
                    if (state.equals("off")) {
                        runOnUiThread(() -> createToast("Turning on lights...").show());
                        philipsHue.turnOn();
                    } else {
                        runOnUiThread(() -> createToast("Turning off lights...").show());
                        philipsHue.turnOff();
                    }
                    updateIndicator();
                }
                return true;

            case SWIPE_FORWARD:
                Log.d("App", "Swipe forward");
                return true;

            case SWIPE_BACKWARD:
                Log.d("App", "Swipe backward");
                return true;

            case TWO_FINGER_SWIPE_FORWARD:
                Log.d("App", "Double forward");
                runOnUiThread(() -> createToast("Switching to next device...").show());
                startActivity(new Intent(MainActivity.this, Activity2.class));
                executorService.shutdown();
                finish();
                return true;

            case TWO_FINGER_SWIPE_BACKWARD:
                Log.d("App", "Double backward");
                runOnUiThread(() -> createToast("Switching to next device...").show());
                startActivity(new Intent(MainActivity.this, Activity2.class));
                executorService.shutdown();
                finish();
                return true;

            case TWO_FINGER_SWIPE_DOWN:
                Log.d("App", "Double down");
                runOnUiThread(() -> createToast("Exiting...").show());
                executorService.shutdown();
                finish();

            default:
                return false;
        }
    }
}


