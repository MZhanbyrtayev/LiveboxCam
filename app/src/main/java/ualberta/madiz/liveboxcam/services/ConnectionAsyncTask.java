package ualberta.madiz.liveboxcam.services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
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

import ualberta.madiz.liveboxcam.R;
import ualberta.madiz.liveboxcam.entities.Beacon;
import ualberta.madiz.liveboxcam.utils.ImageUtils;

public class ConnectionAsyncTask extends AsyncTask<Beacon, Void, JSONObject> {
    private static final String serverURL = "http://192.168.137.1/stories/getData/";
    private static final String TAG = "ConnectionAsyncTask";
    private static final String CHANNEL_ID = "asd";
    private static final int notification_id = 77;
    private Context appContext;
    public ConnectionAsyncTask(Context context){
        this.appContext = context;
    }
    private JSONObject formRequest(Beacon b){
        JSONObject converted = new JSONObject();
        try{
            converted.put("Name",b.getName());
            converted.put("Address",b.getAddress());
        }catch (JSONException jsoe){
            Log.d(TAG, jsoe.getLocalizedMessage());
        }
        return converted;
    }

    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = this.appContext.getString(R.string.channel_name);
            String description = this.appContext.getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = this.appContext.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        Log.d(TAG, "onPost");
        try {
            String content = "Would you like to learn more about "+jsonObject.getString("name")+" "+jsonObject.getString("lname");
            content+=", the livebox has "+jsonObject.getString("box_capacity")+" items";
            createNotificationChannel();
            Intent audioIntent = new Intent(appContext, MediaService.class);
            Log.d(TAG, jsonObject.toString());
            audioIntent.putExtra("boxinf", jsonObject.getString("box"));
            audioIntent.setAction("notification");
            PendingIntent play = PendingIntent.getService(appContext, 0, audioIntent,PendingIntent.FLAG_UPDATE_CURRENT);
            NotificationCompat.Builder builder =
                    new NotificationCompat.Builder(this.appContext, CHANNEL_ID)
                            .setSmallIcon(android.support.v7.appcompat.R.drawable.notification_icon_background)
                            .setContentTitle("Livebox is nearby")
                            .setContentText("Box owner is "+jsonObject.get("name"))
                            .setStyle(new NotificationCompat.BigTextStyle().bigText(content))
                            .setPriority(NotificationCompat.PRIORITY_HIGH)
                            .setContentIntent(play)
                            .setAutoCancel(true)
                            .addAction(R.drawable.ic_launcher_background, "Start", play);
            NotificationManagerCompat managerCompat = NotificationManagerCompat.from(appContext);
            managerCompat.notify(notification_id,builder.build());

        } catch (JSONException jsoe){
            Log.d(TAG, jsoe.getLocalizedMessage());
        }
    }

    @Override
    protected JSONObject doInBackground(Beacon... beacons) {
        JSONObject result = null;
        Log.d(TAG, "Started");
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
            result = new JSONObject(response);
            Log.d(TAG, response);
            Log.d(TAG, "finished");
            connection.disconnect();
        } catch (IOException | JSONException e){
            Log.d(TAG, "Not called: " + e.getMessage());
        }
        return result;
    }
}
