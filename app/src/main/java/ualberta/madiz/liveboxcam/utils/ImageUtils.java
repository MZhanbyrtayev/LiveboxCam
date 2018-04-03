package ualberta.madiz.liveboxcam.utils;

import android.graphics.ImageFormat;
import android.media.Image;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.opencv.core.CvType;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class ImageUtils {
    private static final String TAG = "ImageUtils";
    public static JSONObject keypointsToJSON(MatOfKeyPoint keyPoints) throws JSONException {

        JSONObject result = new JSONObject();
        Map<Integer,Integer> map = new HashMap<>();
        JSONArray array = new JSONArray();
        JSONObject temp;
        for(KeyPoint k : keyPoints.toList()){
            temp = new JSONObject();
            temp.put("angle", k.angle);
            temp.put("class_id", k.class_id);
            temp.put("octave", k.octave);
            temp.put("point", "["+k.pt.x+","+k.pt.y+"]");
            temp.put("response", k.response);
            temp.put("size", k.size);
            array.put(temp);
        }
//        Log.d(TAG, "Map Size:" + map.get(map.keySet().toArray()[0]));
        Log.d(TAG, "kp: "+array.toString());
        result.put("Keypoints", array);
        return result;
    }
    public static JSONObject matToJSON(Mat input){
        JSONObject result = new JSONObject();
        try{
            Size shape = input.size();
            int height = (int)shape.height;
            int width = (int) shape.width;
            JSONObject matrix = new JSONObject();
            double[] blue = new double[width];
            for(int row = 0; row < height; row++){
                for(int col = 0; col< width; col++){
                    blue[col] = input.get(row, col)[0];
                }
                matrix.put("Row"+row, new JSONArray(Arrays.asList(blue)));
            }
            result.put("Matrix", matrix);
        }catch (JSONException jsonEx){
            Log.d(TAG, "Something wrong with the JSON");
        }

        return result;
    }
    /*
    * The function converts captured frame from YUV_420_888 format
    * to OpenCV compatible Mat format
    * */
    public static Mat convertToMat(Image image, boolean isGray) throws Exception{
        int height = image.getHeight();
        int width =  image.getWidth();
        int offset = 0;
        int rowStride;
        int pixelStride;
        ByteBuffer mBuffer;
        Image.Plane yPlane = image.getPlanes()[0];
        Image.Plane uPlane = image.getPlanes()[1];
        Image.Plane vPlane = image.getPlanes()[2];
        int planeSize = yPlane.getBuffer().remaining();
        int uPlaneSize = uPlane.getBuffer().remaining();
        int vPlaneSize = vPlane.getBuffer().remaining();

        if(isGray){
            byte[] data = new byte[planeSize];
            yPlane.getBuffer().get(data, 0, planeSize);
            Mat grayMat = new Mat(height,width,CvType.CV_8UC1);
            grayMat.put(0,0,data);
            return grayMat;
        } else {
            if(uPlane.getPixelStride() == 1){
                byte[] data = new byte[planeSize+(planeSize/2)];
                yPlane.getBuffer().get(data, 0, planeSize);

                ByteBuffer uBuff = uPlane.getBuffer();
                ByteBuffer vBuff = vPlane.getBuffer();

                uBuff.get(data, planeSize, uPlaneSize);
                vBuff.get(data, planeSize+uPlaneSize, vPlaneSize);

                Mat yuvMat = new Mat(height+(height/2), width, CvType.CV_8UC1);
                yuvMat.put(0,0,data);
                Mat bgrMat = new Mat(height,width, CvType.CV_8UC3);
                Imgproc.cvtColor(yuvMat, bgrMat, Imgproc.COLOR_YUV2BGR);
                yuvMat.release();
                return bgrMat;
            } else {
                throw new Exception("The image cannot be converted");
            }
        }

       /* for(int i = 0; i < colorPlanes.length; i++){
            mBuffer = colorPlanes[i].getBuffer();
            rowStride = colorPlanes[i].getRowStride();
            pixelStride = colorPlanes[i].getPixelStride();
            int w = (i==0) ? width : (width/2);
            int h = (i==0) ? height : (height/2);
            for(int r = 0; r < h; r++){
                int bytesPerPixel =  ImageFormat.getBitsPerPixel(image.getFormat())/8;
                if(pixelStride == bytesPerPixel){
                    int length = w*bytesPerPixel;
                    mBuffer.get(data, offset, length);

                    if( h - r != 1){
                        mBuffer.position(mBuffer.position()+rowStride-length);
                    }
                    offset += length;
                } else {
                    if( h - r == 1){
                        mBuffer.get(rowData, 0, width-pixelStride+1);
                    } else {
                        mBuffer.get(rowData, 0, rowStride);
                    }
                    for(int c = 0; c < w; c++){
                        data[offset++] = rowData[c*pixelStride];
                    }
                }
            }
        }
        Mat result = new Mat(height+height/2, width, CvType.CV_8UC3);
        result.put(0,0,data);
        return result;
        */
    }
}