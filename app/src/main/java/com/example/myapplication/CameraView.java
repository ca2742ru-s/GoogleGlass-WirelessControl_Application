package com.example.myapplication;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;

import com.example.myapplication.GlassGestureDetector.Gesture;
import com.example.myapplication.GlassGestureDetector.OnGestureListener;

import com.example.myapplication.util.MathUtils;
import com.google.common.util.concurrent.ListenableFuture;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.SensorManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.ExecutionException;

public class CameraView extends AppCompatActivity implements OnGestureListener{
    ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    PreviewView previewView;
    private GlassGestureDetector gestureDetector;
    private OrientationManager OM;
    private TextView textPitch, header, textDirection, indicator, philipsDeg, sonosDeg, background;
    private float headerDeg, pitchDeg;
    private int targetDeg[], tapCounter, i, version;
    private boolean dummyMode, cameraActive, debugMode, testMode;
    private String choice, hueStatus, sonosStatus, direction;

    private final OrientationManager.OnChangedListener onChangedListener = new OrientationManager.OnChangedListener() {
        @Override
        public void onOrientationChanged(OrientationManager orientationManager) {
            if (tapCounter < 1) {
                header.setText("Please tap to calibrate orientation...");
            } else {
                headerDeg = (int) orientationManager.getHeading();
                pitchDeg = (int) orientationManager.getPitch();
                calculateDirection();
                targetInSight();
            }

                /*
                headerDeg = MathUtils.mod(OM.getHeading(), 360.0f);
                pitchDeg = OM.getPitch();
                orientationManager.getPitch();
                targetInSight();

                 */
        }
        @Override
        public void onLocationChanged(OrientationManager orientationManager) {

        }
        @Override
        public void onAccuracyChanged(OrientationManager orientationManager) {
            tapCounter = 0;

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Choose what version to run
        cameraActive = false;
        dummyMode = false;
        debugMode = false;
        testMode = true;

        version = 0; //Camera view first
        //version = 1; //Only devices

        if(cameraActive){
            setContentView(R.layout.camera_view);
            previewView = findViewById(R.id.previewView);
            indicator = findViewById(R.id.indicator);

            if(dummyMode){
                textPitch = findViewById(R.id.pitch);
                textDirection = findViewById(R.id.direction);
                philipsDeg = findViewById(R.id.PhilipsDegree);
                sonosDeg = findViewById(R.id.SonosDegree);
            }

            cameraProviderFuture = ProcessCameraProvider.getInstance(this);
            cameraProviderFuture.addListener(() -> {
                try {
                    ProcessCameraProvider cameraProvider = cameraProviderFuture.get();
                    bindPreview(cameraProvider);

                } catch (ExecutionException | InterruptedException e) {
                    e.printStackTrace();
                }
            }, ContextCompat.getMainExecutor(this));
        } else if(debugMode){
            setContentView(R.layout.camera_view2);
            indicator = findViewById(R.id.indicator);
            textPitch = findViewById(R.id.pitch);
            textDirection = findViewById(R.id.direction);
            philipsDeg = findViewById(R.id.philipsDegree);
            sonosDeg = findViewById(R.id.sonosDegree);
        } else{
            setContentView(R.layout.camera_view3);
        }

        background = findViewById(R.id.background);
        header = findViewById(R.id.header);

        gestureDetector = new GlassGestureDetector(this, this);

        targetDeg = new int[2];
        headerDeg = 0;
        pitchDeg = 0;
        tapCounter = 0;
        choice = "";
        hueStatus = null;
        sonosStatus = null;
        direction = "Not yet calibrated";
        i = 0;

        if(!cameraActive){
            background = findViewById(R.id.background);
        }

        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        OM = new OrientationManager(sensorManager, locationManager);
        OM.addOnChangedListener(onChangedListener);
        OM.start();
        startActivity(new Intent(CameraView.this, MainActivity.class));


    }

    private Toast createToast(String choice){
        Toast toast = Toast.makeText(this, choice, Toast.LENGTH_SHORT);
        View view = toast.getView();
        view.getBackground().setColorFilter(Color.BLACK, PorterDuff.Mode.SRC_IN);
        TextView text = view.findViewById(android.R.id.message);
        text.setTextColor(Color.WHITE);
        return toast;
    }

    private void calculateDirection() {
        if ((315 < headerDeg && headerDeg <= 360) || (0 <= headerDeg && headerDeg < 45)) {
            direction = "N";
        } else if ((45 <= headerDeg && headerDeg < 135)) {
            direction = "E";
        } else if ((135 <= headerDeg && headerDeg < 225)) {
            direction = "S";
        } else if ((225 <= headerDeg && headerDeg < 315)) {
            direction = "W";
        }
        if(dummyMode){
            textDirection.setText(direction);
            textPitch.setText(Float.toString(pitchDeg));
        }
    }

    private void targetInSight(){
        if(debugMode){
            if(i == 25){
                Log.d("Target: ", Integer.toString(targetDeg[0]));
                Log.d("Target: ", Integer.toString(targetDeg[1]));
                Log.d("viewDirection:", Float.toString(headerDeg));
                Log.d("Compass: ", direction);
                Log.d("In sight: ", choice);
                Log.d("Pitch: ", Float.toString(pitchDeg));
                i = 0;
            }
            i++;
        }

        if(tapCounter < 1){
            background.setBackgroundColor(Color.BLACK);
            header.setTextColor(Color.WHITE);
            header.setText("Please tap to calibrate orientation...");
        }
        else{
            if(Math.abs(pitchDeg) > 70){
                background.setBackgroundColor(Color.RED);
                header.setBackgroundColor(Color.TRANSPARENT);
                header.setTextColor(Color.WHITE);
                header.setText("Please tilt head up or down back agaist the horizon");
            }else{
                if((Math.abs(headerDeg - targetDeg[0]) <= 15)){ //!direction.equals("E") && pitchDeg > 5){
                    if(cameraActive){
                        indicator.setBackgroundColor(Color.GREEN);
                        indicator.setTextColor(Color.GREEN);
                        indicator.setText("Philips Hue Lights");
                    } else{
                        background.setBackgroundColor(Color.WHITE);
                        header.setBackgroundColor(Color.TRANSPARENT);
                        header.setTextColor(Color.BLACK);
                        header.setText("Philips hue lights");
                    }
                    choice = "PhilipsHue";
                }
                else if((Math.abs(headerDeg - targetDeg[1]) <= 15)){// !direction.equals("E") && pitchDeg > 5){
                    if(cameraActive) {
                        indicator.setBackgroundColor(Color.GREEN);
                        indicator.setTextColor(Color.WHITE);
                        indicator.setText("Philips Hue Lights");
                    } else{
                        background.setBackgroundColor(Color.WHITE);
                        header.setBackgroundColor(Color.TRANSPARENT);
                        header.setTextColor(Color.BLACK);
                        header.setText("Sonos speaker");
                    }
                    choice = "Sonos";
                }
                else{
                    if(cameraActive){
                        background.setBackgroundColor(Color.TRANSPARENT);
                        indicator.setBackgroundColor(Color.TRANSPARENT);
                        indicator.setText("");
                    } else {
                        background.setBackgroundColor(Color.BLACK);
                        header.setTextColor(Color.WHITE);
                        if(debugMode){
                            header.setText(Float.toString(headerDeg));
                            textPitch.setText(Float.toString(pitchDeg));
                            textDirection.setText(direction);
                            philipsDeg.setText(Integer.toString(targetDeg[0]));
                            sonosDeg.setText(Integer.toString(targetDeg[1]));
                        } else{
                            header.setText("Please look at smart device to interact");
                        }
                    }
                    choice = "";
                }
            }


        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    void bindPreview(@NonNull ProcessCameraProvider cameraProvider) {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();
        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        cameraProvider.bindToLifecycle(this, cameraSelector, preview);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        return gestureDetector.onTouchEvent(ev); //|| super.dispatchTouchEvent(ev);
    }

    @Override
    public boolean onGesture(Gesture gesture) {
        switch (gesture) {
            case SWIPE_UP:
                Log.d("App", "Swipe Up!");
                if(testMode || debugMode){
                    tapCounter = 0;
                }
                return true;
            case SWIPE_DOWN:
                Log.d("App", "Swipe Down!");
                Toast.makeText(this, "Closing app...", Toast.LENGTH_SHORT).show();
                OM.removeOnChangedListener(onChangedListener);
                OM.stop();
                finish();
                return true;
            case TAP:
                if(tapCounter < 1) {
                    //Before user has calibrated
                    targetDeg[0] = (int) MathUtils.mod(OM.getHeading() + 30, 360.0f);
                    targetDeg[1] = (int) MathUtils.mod(OM.getHeading() -30, 360.0f);
                    if(dummyMode){
                        philipsDeg.setText("PhilipsDegree: " + targetDeg[0]);
                        sonosDeg.setText("SonosDegree: " + targetDeg[1]);
                    }
                    tapCounter++;
                    Toast.makeText(this, "Calibrated!", Toast.LENGTH_SHORT).show();
                } else{
                    //Choices are available after user has calibrated
                    switch (choice){
                        case "PhilipsHue":
                            runOnUiThread(() -> createToast("Entering Philips Hue...").show());
                            //OM.stop();
                            startActivity(new Intent(CameraView.this, MainActivity.class));
                            return true;
                        case "Sonos":
                            runOnUiThread(() -> createToast("Entering Sonos...").show());
                            //OM.stop();
                            startActivity(new Intent(CameraView.this, Activity2.class));
                            return true;
                        default:
                            return false;
                    }
                }
                return true;
            case SWIPE_FORWARD:
                Log.d("App", "Swipe forward");
                Toast.makeText(this, "Swipe forward!", Toast.LENGTH_SHORT).show();
                return true;
            case SWIPE_BACKWARD:
                Log.d("App", "Swipe backward");
                Toast.makeText(this, "Swipe backward!", Toast.LENGTH_SHORT).show();
                return true;
            case TWO_FINGER_SWIPE_FORWARD:
                Log.d("App", "Double forward");
                Toast.makeText(this, "Two finger swipe forward!", Toast.LENGTH_SHORT).show();
                OM.removeOnChangedListener(onChangedListener);
                OM.stop();
                finish();
                return true;
            case TWO_FINGER_SWIPE_BACKWARD:
                Log.d("App", "Double backward");
                Toast.makeText(this, "Two finger swipe backward!", Toast.LENGTH_SHORT).show();
                OM.removeOnChangedListener(onChangedListener);
                OM.stop();
                finish();
                return true;
            case TWO_FINGER_SWIPE_DOWN:
                Log.d("App,","Double down");
                Toast.makeText(this, "Two finger swipe down!", Toast.LENGTH_SHORT).show();
                OM.removeOnChangedListener(onChangedListener);
                OM.stop();
                finish();
            default:
                return false;
        }
    }
}