package ualberta.madiz.liveboxcam.services;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.FastFeatureDetector;
import org.opencv.features2d.FeatureDetector;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import ualberta.madiz.liveboxcam.utils.ImageUtils;

public class ImageSendAsyncTask extends AsyncTask<Mat, Void, Boolean> {
    private static final String TAG = "imageSendAsyncTask";
    private static final String serverURL = "http://172.20.158.84:8000/stories/compareImage/";
    private static final String defaultJSON = "{Data: Empty}";
    @Override
    protected void onPostExecute(Boolean finished) {
        super.onPostExecute(finished);
    }

    @Override
    protected Boolean doInBackground(Mat... mats) {
        Log.d(TAG, "Started");
        FeatureDetector featureDetector;
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(mats[0], keyPoints);
        Log.d("OpenCVops", "Number of KeyPoints: "+keyPoints.toArray().length);
        try{
            JSONObject myObject = new JSONObject();
            URL url = new URL(serverURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            OutputStream stream = new BufferedOutputStream(connection.getOutputStream());

            stream.write(ImageUtils.keypointsToJSON(keyPoints).toString().getBytes());
            stream.flush();

            InputStreamReader responseStream = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(responseStream);
            String response = br.readLine();
            Log.d(TAG, response);
            Log.d(TAG, "finished");
            connection.disconnect();
        } catch (JSONException | IOException e){
            Log.d(TAG, "Not called: " + e.getMessage());
        }

        return true;
    }
}
