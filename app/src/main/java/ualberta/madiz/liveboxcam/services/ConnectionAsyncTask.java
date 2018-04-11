package ualberta.madiz.liveboxcam.services;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import ualberta.madiz.liveboxcam.entities.Beacon;
import ualberta.madiz.liveboxcam.utils.ImageUtils;

public class ConnectionAsyncTask extends AsyncTask<Beacon, Void, JSONObject> {
    private static final String serverURL = "http://172.20.153.207:8000/stories/getData/";
    private static final String TAG = "ConnectionAsyncTask";
    private JSONObject formRequest(Beacon b){
        JSONObject converted = new JSONObject();
        try{
            converted.put("Name",b.getName());
            converted.put("Address",b.getAddress());
        }catch (JSONException jsoe){
            Log.d(TAG, jsoe.getLocalizedMessage());
        }
        return new JSONObject();
    }
    @Override
    protected JSONObject doInBackground(Beacon... beacons) {
        JSONObject result = null;

        try{
            URL url = new URL(serverURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            OutputStream stream = new BufferedOutputStream(connection.getOutputStream());
            stream.write(formRequest(beacons[0]).toString().getBytes());
            stream.flush();

            InputStreamReader responseStream = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(responseStream);
            String response = br.readLine();
            Log.d(TAG, response);
            Log.d(TAG, "finished");
            connection.disconnect();
        } catch (IOException e){
            Log.d(TAG, "Not called: " + e.getMessage());
        }
        return result;
    }
}
