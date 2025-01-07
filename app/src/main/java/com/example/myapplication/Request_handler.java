package com.example.myapplication;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketPermission;
import java.net.URL;

public class Request_handler implements Runnable{
    private static HttpURLConnection urlCon;
    private String requestMethod;
    private String payload;
    private String autorization = "Bearer " + "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJjZjUyNTZiMDlhNzI0YTMwYTMwYTUwYjFhYmY2MTFiZSIsImlhdCI6MTY3MjkyOTM0OSwiZXhwIjoxOTg4Mjg5MzQ5fQ.LiCL6PyRb_hd12Ia-AUFj73jMKUqc2Sj_3zgGmEN6oQ";
    private boolean post;
    private String url;

    public void setupPost(String connection, String input){
        setPost();
        setUrl(connection);
        setPayload(input);
        Thread t1 = new Thread(() -> run());
        t1.start();
    }
    public void setupGet(String connection){
        setGet();
        setUrl(connection);
    }
    private void setUrl(String url){
        this.url = url;
    }
    private void setPost(){
        requestMethod = "POST";
        post = true;
    }
    private void setGet(){
        requestMethod = "GET";
        post = false;
    }
    private void setRequestProperty(String property){
        requestMethod = property;
    }
    private void setPayload(String inputPayload){
        payload = inputPayload;
    }

    public void run() {
        BufferedReader reader;
        String line;
        StringBuffer responseContent = new StringBuffer();
        //final String tvInformation = "{\"entity_id\": \"media_player.samsung_7_series_55\"}";
        try{
            urlCon = (HttpURLConnection) new URL(url).openConnection();

            //Setup the request
            urlCon.setRequestMethod(requestMethod);
            urlCon.setConnectTimeout(5000);
            urlCon.setReadTimeout(5000);
            urlCon.setRequestProperty("Authorization", autorization);
            if(post = true){
                urlCon.setRequestProperty("Content-Type", "application/json");
            }
            urlCon.setDoOutput(true);
            OutputStream os = urlCon.getOutputStream();
            if(post = true){
                os.write(payload.getBytes());
            }
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
                reader = new BufferedReader(new InputStreamReader(urlCon.getInputStream()));
                while((line = reader.readLine()) != null){
                    responseContent.append(line);
                }
                reader.close();
            }
            Log.d("status", Integer.toString(responseCode));
            Log.d("message: ", responseContent.toString());


        } catch (MalformedURLException e){
            e.printStackTrace();
        } catch (IOException e){
            e.printStackTrace();
        } finally{
            urlCon.disconnect();
        }
    }
}
