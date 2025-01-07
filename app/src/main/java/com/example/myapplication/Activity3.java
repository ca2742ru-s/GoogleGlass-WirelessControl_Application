package com.example.myapplication;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.TextView;

import com.example.myapplication.GlassGestureDetector.Gesture;
import com.example.myapplication.GlassGestureDetector.OnGestureListener;


public class Activity3 extends Activity implements OnGestureListener {

    private SmartDevice TV;
    private GlassGestureDetector gestureDetector;
    private TextView indicator;
    private String state;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_3);
        TextView textView = (TextView) findViewById(R.id.textView01);
        indicator = (TextView) findViewById(R.id.indicator);
        indicator.setBackgroundResource(R.drawable.off_button);
        indicator.setText("OFF");
        state = null;

        //TODO: Change this so it actually is an entity_id like media_player and not the full address
        TV = new SmartDevice(
                "http://192.168.1.86:8123/api/states/media_player.samsung_7_series_55",
                "http://192.168.1.86:8123/api/services/media_player/turn_on",
                "http://192.168.1.86:8123/api/services/media_player/turn_off",
                "{\"entity_id\": \"media_player.samsung_7_series_55\"}",
                null,
                null,
                null,
                null,
                null);

        gestureDetector = new GlassGestureDetector(this, this);
        updateIndicator();

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateIndicator();
    }
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev) || super.dispatchTouchEvent(ev);
    }

    private void updateIndicator(){
        state = TV.getState("state");
        if(state.equals("off")){
            Log.d("state: ", "OFF");
            indicator.setBackgroundResource(R.drawable.off_button);
            indicator.setText("OFF");
        } else{
            Log.d("state: ", "ON");
            indicator.setBackgroundResource(R.drawable.on_button);
            indicator.setText("ON");
        }
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case SWIPE_UP:
                Log.d("App", "Swipe Up!");
                updateIndicator();
                return true;
            case SWIPE_DOWN:
                Log.d("App", "Swipe Down!");
                finish();
                return true;
            case TAP:
                if(state.equals("off")){
                    TV.turnOn();
                    updateIndicator();
                }
                else if(state.equals("on")){
                    TV.turnOff();
                    updateIndicator();
                }
                return true;
            case SWIPE_FORWARD:
                Log.d("App", "Swipe forward");
                startActivity(new Intent(Activity3.this, Activity2.class));
                finish();
                return true;
            case SWIPE_BACKWARD:
                Log.d("App", "Swipe backward");
                startActivity(new Intent(Activity3.this, MainActivity.class));
                finish();
                return true;
            case TWO_FINGER_SWIPE_FORWARD:
                Log.d("App", "Double forward");
                finish();
                return true;
            case TWO_FINGER_SWIPE_BACKWARD:
                Log.d("App", "Double backward");
                return true;
            default:
                return false;
        }
    }
}


