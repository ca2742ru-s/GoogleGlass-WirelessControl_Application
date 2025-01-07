package com.example.myapplication;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.example.myapplication.GlassGestureDetector.Gesture;
import com.example.myapplication.GlassGestureDetector.OnGestureListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


public class Activity2 extends Activity implements OnGestureListener {

    private SmartDevice sonosSpeaker;
    private GlassGestureDetector gestureDetector;
    private ScheduledExecutorService executorService;
    private String state, currentSong;
    private Boolean dummyMode;
    private int volume, tapCounter, build;
    private TextView indicator, currentlyPlaying, currentVolume;
    private ProgressBar volumeIndicator;
    private List<String> songs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_2);

        indicator = findViewById(R.id.indicator);
        currentlyPlaying = findViewById(R.id.currentSong);
        currentVolume = findViewById(R.id.volume);
        volumeIndicator = findViewById(R.id.volumeBar);

        volume = 0;
        volumeIndicator.setProgress(volume);
        currentVolume.setText(String.valueOf(volume));
        state = "paused";
        dummyMode = true;
        gestureDetector = new GlassGestureDetector(this, this);

        build = 1;

        songs = new ArrayList<>();
        songs.add("I'm every woman - Chaka Khan");
        songs.add("Somewhat Damaged - Nine Inch Nails");
        songs.add("Positions - Ariana Grande");

        tapCounter = 0;

        currentSong = songs.get(0);

        if(dummyMode){
            setUpDummy();
        } else{
            setUpDevice();
            executorService = Executors.newSingleThreadScheduledExecutor();
            executorService.scheduleAtFixedRate(new Runnable() {
                @Override
                public void run(){
                    updateIndicator();
                }
            }, 0, 3, TimeUnit.SECONDS);
        }



    }

    @Override
    protected void onResume() {
        super.onResume();
        if(!dummyMode){
            updateIndicator();
        }
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev); // || super.dispatchTouchEvent(ev);
    }

    private void setUpDevice(){
        if(!dummyMode){
            //TODO: Change this so it actually is an entity_id like media_player and not the full address
            sonosSpeaker = new SmartDevice(
                    "speaker",
                    "http://192.168.1.6:8123/api/states/media_player.den",
                    "http://192.168.1.6:8123/api/services/media_player/media_play",
                    "http://192.168.1.6:8123/api/services/media_player/media_pause",
                    "{\"entity_id\": \"media_player.den\"}",
                    "http://192.168.1.6:8123/api/services/media_player/volume_up",
                    "http://192.168.1.6:8123/api/services/media_player/volume_down",
                    "http://192.168.1.6:8123/api/services/media_player/media_next_track",
                    "http://192.168.1.6:8123/api/services/media_player/media_previous_track");
        }
    }

    private void setUpDummy(){
        state = "paused";
        volumeIndicator.setProgress(volume);
        updateDummy("setUp");
    }

    private Toast createToast(String choice){
        Toast toast = Toast.makeText(this, choice, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
        return toast;
    }
    @SuppressLint("SetTextI18n")
    private void updateIndicator() {
        if(sonosSpeaker.getState("state") == null){
            return;
        } else{
            state = sonosSpeaker.getState("state");
            currentSong = sonosSpeaker.getCurrent();

            if(sonosSpeaker.getLevel() != null){
                volume = (int) (100*Float.parseFloat(sonosSpeaker.getLevel()));
            } else{
                volume = 0;
            }

            Log.d("Integer level volume: ", Integer.toString(volume));
            volumeIndicator.setProgress(volume);
            currentVolume.setText(Integer.toString(volume));
            Log.d("Status: ", "Updated!");

            switch(state) {
                case "idle":
                    Log.d("state: ", "Idle");
                    indicator.setBackgroundResource(R.drawable.off_button);
                    indicator.setText("OFF");
                    currentlyPlaying.setText("No song currently playing");
                    break;
                case "playing":
                    Log.d("state: ", "PLAYING");
                    indicator.setBackgroundResource(R.drawable.on_button);
                    indicator.setText("PLAYING");
                    currentlyPlaying.setText(currentSong);
                    break;
                case "paused":
                    Log.d("state: ", "PAUSED");
                    indicator.setBackgroundResource(R.drawable.off_button);
                    indicator.setText("PAUSED");
                    currentlyPlaying.setText(currentSong);
                    break;

            }
        }

    }

    @SuppressLint("SetTextI18n")
    private void updateDummy(String string){
        switch (string) {
            case "volumeUp":
                if(volume + 25 <= 100){
                    volume += 25;
                    volumeIndicator.setProgress(volume);
                    currentVolume.setText(String.valueOf(volume));
                }
                break;
            case "volumeDown":
                if(volume - 25 >= 0){
                    volume -= 25;
                    volumeIndicator.setProgress(volume);
                    currentVolume.setText(String.valueOf(volume));
                }
                break;
            case "play":
                indicator.setBackgroundResource(R.drawable.on_button);
                indicator.setText("PLAYING");
                state = "playing";
                currentlyPlaying.setText("Now playing: Somewhat Damaged - Nine Inch Nails");
                break;
            case "pause":
                indicator.setBackgroundResource(R.drawable.off_button);
                indicator.setText("PAUSED");
                state = "paused";
                break;
            case "next":
                //Play next song
                break;
            case "previous":
                //Play previous song
                break;
            case "setUp":
                currentlyPlaying.setText("No song currently playing");
        }
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case SWIPE_UP:
                if(!dummyMode) {
                    runOnUiThread(() -> createToast("Increasing volume...").show());
                    sonosSpeaker.increase();
                } else {
                    updateDummy("volumeUp");
                }
                Log.d("App", "Swipe Up!");
                Log.d("App", "Volume: " + volume);
                return true;

            case SWIPE_DOWN:
                if(!dummyMode){
                    runOnUiThread(() -> createToast("Decreasing volume...").show());
                    sonosSpeaker.decrease();
                } else{
                    updateDummy("volumeDown");
                }
                Log.d("App", "Swipe Down!");
                Log.d("App", "Volume: " + volume);
                return true;

            case TAP:
                if(!dummyMode){
                    tapCounter++;

                    if(state.equals("idle") || state.equals("paused")){
                        runOnUiThread(() -> createToast("Starting song...").show());
                        sonosSpeaker.turnOn();
                    } else if(state.equals("playing")) {
                        runOnUiThread(() -> createToast("Pausing song...").show());
                        sonosSpeaker.turnOff();
                    }

                } else{
                    if(state.equals("paused")){
                        updateDummy("play");
                    } else if(state.equals("playing")){
                        updateDummy("pause");
                    }
                }
                tapCounter = 0;
                return true;

            case SWIPE_FORWARD:
                Log.d("App", "Swipe forward");
                if(build == 1){
                    runOnUiThread(() -> createToast("Next song...").show());
                    sonosSpeaker.next();
                } else{
                    if(!dummyMode){
                        executorService.shutdown();
                    }
                    startActivity(new Intent(Activity2.this, MainActivity.class));
                    finish();
                }
                return true;

            case SWIPE_BACKWARD:
                Log.d("App", "Swipe backward");
                if(build == 1){
                    runOnUiThread(() -> createToast("Previous song...").show());
                    sonosSpeaker.previous();
                } else{
                    if(!dummyMode){
                        executorService.shutdown();
                    }
                    Intent i = new Intent(Activity2.this, MainActivity.class);
                    finish();
                }

                return true;

            case TWO_FINGER_SWIPE_FORWARD:
                Log.d("App", "Double forward");
                runOnUiThread(() -> createToast("Next device...").show());
                if(!dummyMode){
                    executorService.shutdown();
                }

                startActivity(new Intent(Activity2.this, MainActivity.class));
                finish();
                return true;

            case TWO_FINGER_SWIPE_BACKWARD:
                Log.d("App", "Double backward");
                runOnUiThread(() -> createToast("Next device...").show());
                if(!dummyMode){
                    executorService.shutdown();
                }
                startActivity(new Intent(Activity2.this, MainActivity.class));
                finish();
                return true;

            case TWO_FINGER_SWIPE_DOWN:
                Log.d("App", "Double downwards");
                if(!dummyMode){
                    executorService.shutdown();
                }
                runOnUiThread(() -> createToast("Exiting...").show());
                finish();
                return true;
            case TWO_FINGER_SWIPE_UP:
                Log.d("App", "Double upwards");
                runOnUiThread(() -> createToast("Opening orientation...").show());
                if(!dummyMode){
                    executorService.shutdown();
                }
                startActivity(new Intent(Activity2.this, CameraView.class));
                finish();
                return true;

            default:
                return false;
        }
    }

}


