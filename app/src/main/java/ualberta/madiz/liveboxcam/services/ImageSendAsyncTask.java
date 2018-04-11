package ualberta.madiz.liveboxcam.services;

import android.media.Image;
import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.params.HttpParams;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.DescriptorExtractor;
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
    private static final String TAG = "newTag";
    private static final String serverURL = "http://192.168.137.1:80/stories/compareImage/";
    // /*"*/;
    private static final String defaultJSON = "{Data: Empty}";
    @Override
    protected void onPostExecute(Boolean finished) {
        super.onPostExecute(finished);
    }

    @Override
    protected Boolean doInBackground(Mat... mats) {
        Log.d(TAG, "Started");
        FeatureDetector featureDetector;
        DescriptorExtractor descriptorExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        MatOfKeyPoint keyPoints = new MatOfKeyPoint();
        Mat descriptors = new Mat();
        featureDetector = FeatureDetector.create(FeatureDetector.ORB);
        featureDetector.detect(mats[0], keyPoints);
        descriptorExtractor.compute(mats[0], keyPoints, descriptors);
        //Log.d("OpenCVops", "Number of KeyPoints: "+keyPoints.toArray().length);
        try{
            URL url = new URL(serverURL);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setDoOutput(true);
            OutputStream stream = new BufferedOutputStream(connection.getOutputStream());

            stream.write(ImageUtils.matToJSON(descriptors).toString().getBytes());
            stream.flush();

            InputStreamReader responseStream = new InputStreamReader(connection.getInputStream());
            BufferedReader br = new BufferedReader(responseStream);
            String response = br.readLine();
            Log.d(TAG, response);
            Log.d(TAG, "finished");
            connection.disconnect();
        } catch (IOException |JSONException e){
            Log.d(TAG, "Not called: " + e.getMessage());
        }

        return true;
    }
}
