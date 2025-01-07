package com.example.myapplication;

import android.util.Log;


import com.fasterxml.jackson.databind.JsonNode;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;
import java.util.concurrent.*;

public class SmartDevice {
    //private final String name;
    private final String device, entity_id, turnOn, turnOff, payload, increase, decrease, next, previous;
    private final Semaphore s1;
    private String target, url, state, level, currently;
    private Boolean valChange;
    private int changeVal;

    private static HttpURLConnection urlCon;

    //TODO: Make constructor read from a text file to access commands and addresses for a specific device
       public SmartDevice(String device, String entity_id, String turnOn, String turnOff, String payload, String increase, String decrease, String next, String previous){
           this.device = device;
           this.entity_id = entity_id;
           this.turnOn = turnOn;
           this.turnOff = turnOff;
           this.payload = payload;
           this.increase = increase;
           this.decrease = decrease;
           this.s1 = new Semaphore(1);
           this.state = null;
           this.level = "25";
           this.currently = "test";
           this.next = next;
           this.previous = previous;
           valChange = false;
           changeVal = 0;
           getStatus("state");
    }

    public void turnOn(){
        callPostSetup(turnOn);
        Thread t1 = new Thread(new urlCallPost());
        t1.start();
        try {
            t1.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void turnOff(){
        callPostSetup(turnOff);
        Thread t2 = new Thread(new urlCallPost());
        t2.start();
        try {
            t2.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    public void increase(){
        callPostSetup(increase);
        if(device.equals("lights")){
            valChange = true;
            changeVal = 55;
        }
        Thread t3 = new Thread(new urlCallPost());
        t3.start();
        try {
            t3.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        valChange = false;
    }

    public void decrease(){
        callPostSetup(decrease);
        if(device.equals("lights")){
            valChange = true;
            changeVal = -55;
        }
        Thread t4 = new Thread(new urlCallPost());
        t4.start();
        try {
            t4.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        valChange = false;
    }

    public void next(){
           callPostSetup(next);
           Thread t5 = new Thread(new urlCallPost());
           t5.start();
           try{
               t5.join();
           } catch(InterruptedException e){
               e.printStackTrace();
           }
    }

    public void previous(){
        callPostSetup(previous);
        Thread t6 = new Thread(new urlCallPost());
        t6.start();
        try{
            t6.join();
        } catch(InterruptedException e){
            e.printStackTrace();
        }
    }

    /**
     * Get every state from entity
     * @return state
     */
    public String getState(){
        getStatus(null);
        return state;
    }

    /**
     * Get a specific state (target) from entity
     * @return state
     */
    public String getState(String target){
        getStatus(target);
        return state;
    }

    public String getCurrent(){
        return currently;
    }

    public String getLevel(){
        Log.d("Float level volume: ", level);
        return level;
    }

    private void getStatus(String target){
        callGetSetup(target, entity_id);
        Thread t5 = new Thread(new urlCallGet());
        t5.start();
        try {
            t5.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    private void callPostSetup(String url){
        Log.d("Request method: ", "Setting up call...");
        this.url = url;
        Log.d("url: ", url);
    }

    private void callGetSetup(String target, String url){
        Log.d("Status: ", "Setting up call...");
        this.target = target;
        this.url = url;
        Log.d("Request method: ", "GET");
        if(target==null){
            Log.d("Target: ", "all");
        }
        else{
            Log.d("Target: ", target);
        }
        Log.d("url: ", url);
    }

    private class urlCallGet implements Runnable{
        @Override
        public void run() {
            Log.d("Status: ", "Thread starting (GET)");

            try {
                s1.acquire();
                Log.d("Semaphore: ", "Acquired");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            BufferedReader reader;
            String line;
            StringBuffer responseContent = new StringBuffer();
            try{
                URL temp = new URL(url);
                urlCon = (HttpURLConnection) temp.openConnection();

                //Setup the request
                urlCon.setRequestMethod("GET");
                urlCon.setConnectTimeout(5000);
                urlCon.setReadTimeout(5000);
                urlCon.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjZjUyNTZiMDlhNzI0YTMwYTMwYTUwYjFhYmY2MTFiZSIsImlhdCI6MTY3MjkyOTM0OSwiZXhwIjoxOTg4Mjg5MzQ5fQ.LiCL6PyRb_hd12Ia-AUFj73jMKUqc2Sj_3zgGmEN6oQ");
                urlCon.setDoInput(true);

                //Try to get response from server
                int responseCode = urlCon.getResponseCode();
                SocketPermission sockPer = (SocketPermission) urlCon.getPermission();
                Log.d("Socket Permission", sockPer.toString());

                //If response code returns error message
                if(responseCode > 299){
                    reader = new BufferedReader(new InputStreamReader(urlCon.getErrorStream()));
                    while((line = reader.readLine()) != null){
                        responseContent.append(line);
                    }
                }

                //When response code returns successful response
                else{
                    reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                    while((line = reader.readLine()) != null){
                        responseContent.append(line);
                    }
                }
                reader.close();

                Log.d("status", Integer.toString(responseCode));
                Log.d("message: ", responseContent.toString());

                try{
                    JsonNode node = Json.parse(responseContent.toString());

                    //Grab info and place into status which is to be returned
                    //If target == null the full status of the entity will be grabbed
                    if(target != null){
                        Log.d("Current target status: ", (node.get(target).asText()));
                        state = node.get(target).asText();
                        if(device.equals("speaker")){
                            if(node.get("attributes").get("volume_level") != null){
                                level = node.get("attributes").get("volume_level").asText();
                            } else{
                                level = "0"; //"No volume available";
                            }
                            if(node.get("attributes").get("media_title") != null) {
                                currently = node.get("attributes").get("media_title").asText() + " - " + node.get("attributes").get("media_artist").asText();
                            } else {
                                currently = "No current song playing";
                            }
                            Log.d("Current volume: ", level);
                            Log.d("Currently playing: ", currently);

                        } else if(device.equals("lights")){
                            if(state.equals("off")){
                                level = "0";
                                currently = "0";
                            } else{
                                if(node.get("attributes").get("brightness") == null){
                                    level = "0";
                                } else{
                                    level = node.get("attributes").get("brightness").asText();
                                }
                                if(node.get("attributes").get("rgb_color") == null){
                                    currently = "0";
                                } else{
                                    currently = node.get("attributes").get("rgb_color").asText();
                                }

                            }
                            Log.d("Current brightness: ", level);
                            Log.d("Current rgb color: ", currently);
                        }
                    }
                    else{
                        Log.d("State: ", (node.asText()));
                        state = node.asText();
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }


            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } finally{
                urlCon.disconnect();
                System.out.println("Successful close of connection");
            }
            s1.release();
            Log.d("Status: ", "Thread closing");
        }
    }
    private class urlCallPost implements Runnable{
        @Override
        public void run(){
            Log.d("Status: ", "Thread starting (POST)");

            try {
                s1.acquire();
                Log.d("Semaphore: ", "Acquired");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            BufferedReader reader;
            String line;
            StringBuffer responseContent = new StringBuffer();
            Log.d("Device: ", device);
            Log.d("valChange:" , Boolean.toString(valChange));
            String payloadInput;
            if(device.equals("lights") && valChange){
                int val = Integer.parseInt(level) + changeVal;
                if(val < 0){
                    val = 0;
                } else if(val > 255){
                    val = 255;
                }
                payloadInput = "{\"entity_id\": \"light.living_room\",\"brightness\": " + val + "}";
            } else {
                payloadInput = SmartDevice.this.payload;
            }

            try{
                Log.d("Message", url);
                URL temp = new URL(url);
                urlCon = (HttpURLConnection) temp.openConnection();

                //Setup the request
                urlCon.setRequestMethod("POST");
                urlCon.setConnectTimeout(5000);
                urlCon.setReadTimeout(5000);
                urlCon.setRequestProperty("Authorization", "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjZjUyNTZiMDlhNzI0YTMwYTMwYTUwYjFhYmY2MTFiZSIsImlhdCI6MTY3MjkyOTM0OSwiZXhwIjoxOTg4Mjg5MzQ5fQ.LiCL6PyRb_hd12Ia-AUFj73jMKUqc2Sj_3zgGmEN6oQ");
                urlCon.setRequestProperty("Content-Type", "application/json");
                urlCon.setDoOutput(true);
                OutputStream os = urlCon.getOutputStream();

                os.write(payloadInput.getBytes());
                Log.d("Payload: ", payload);
                os.flush();
                os.close();

                //Try to get response from server
                int responseCode = urlCon.getResponseCode();
                SocketPermission sockPer = (SocketPermission) urlCon.getPermission();
                Log.d("Socket Permission", sockPer.toString());

                //If response code returns error message
                if(responseCode > 299){
                    reader = new BufferedReader(new InputStreamReader(urlCon.getErrorStream()));
                    while((line = reader.readLine()) != null){
                        responseContent.append(line);
                    }
                    reader.close();
                }
                //When response code returns successful response
                else{
                    try{
                        reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                        while((line = reader.readLine()) != null){
                            responseContent.append(line);
                        }
                        reader.close();
                    } catch (NullPointerException e){
                        Log.d("Nullpointer exception: ", "Tried to read an empty input stream");
                    }

                }
                Log.d("status", Integer.toString(responseCode));
                Log.d("message: ", responseContent.toString());

            } catch (MalformedURLException e){
                e.printStackTrace();
            } catch (IOException e){
                e.printStackTrace();
            } finally{
                urlCon.disconnect();
                System.out.println("Successful close of connection");
            }
            s1.release();
            Log.d("Status: ", "Thread closing");
        }
    }

}