package com.grimaldos.atostest.util;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class JSONparser {
    static JSONObject jObj = null;
    static String json = "";

    // Default constructor
    public JSONparser() {}

    public JSONObject getJSONfromURL(String urlString) {
        try {
            URL url = new URL(urlString);

            // Connect to the url
            Log.d("getJSONfromURL", "Connecting...");

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();

            Log.d("getJSONfromURL", "Connected");

            // Get the data and put it in a String
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");

            BufferedReader reader = new BufferedReader(new InputStreamReader
                    (connection.getInputStream()));

            Log.d("getJSONfromURL", "We have the data");

            StringBuffer buffer = new StringBuffer();
            String line = null;
            Log.d("getJSONfromURL", "Building String");
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }
            Log.d("getJSONfromURL", "String Built");
            json = buffer.toString();
        } catch(MalformedURLException e) {
            e.printStackTrace();
        } catch(IOException e) {
            e.printStackTrace();
        }

        // Parse the String to a JSON object
        try {
            jObj = new JSONObject(json);
        } catch (JSONException e) {
            Log.e("JSON Parser", "Error parsing data " + e.toString());
        }
        return jObj;
    }
}