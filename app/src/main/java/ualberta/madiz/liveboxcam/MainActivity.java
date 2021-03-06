package ualberta.madiz.liveboxcam;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CameraMetadata;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.InstallCallbackInterface;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Mat;


import java.util.Arrays;

import ualberta.madiz.liveboxcam.graphics.CustomGLSurface;
import ualberta.madiz.liveboxcam.services.BluetoothDiscoveryService;
import ualberta.madiz.liveboxcam.services.ImageSendAsyncTask;
import ualberta.madiz.liveboxcam.utils.ImageUtils;

public class MainActivity extends AppCompatActivity implements TextureView.SurfaceTextureListener,
        View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int BLUETOOTH_CODE = 66;
    private static final int PERMISSION_CODE = 77;
    private static final int MAX_HEIGHT = 360;//def 720
    private static final int MAX_WIDTH = 640;//def 1280
    private static final int SCREEN_HEIGHT = 2392;
    private static final int SCREEN_WIDTH = 1440;
    //Visual Component
    private CameraManager cameraManager;
    private CameraDevice mCamera;
    private CameraCharacteristics cameraCharacteristics;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest captureRequest;
    private CaptureRequest.Builder captureBuilder;
    private ImageReader imageReader;
    private TextureView surfaceView;
    private Handler backgroundHandler;
    private HandlerThread backgroundThread;
    private Size imageSize;
    private Image lastImage;
    private String cameraId;
    private Image.Plane blue;
    private Image.Plane green;
    private Image.Plane red;
    private StreamConfigurationMap map;
    private Handler periodicHandler;
    private BaseLoaderCallback callbackInterface = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.d(TAG, "Status: "+status
                    +" Success:"+(status==LoaderCallbackInterface.SUCCESS));
        }

        @Override
        public void onPackageInstall(int operation, InstallCallbackInterface callback) {

        }
    };
    //UI
    private FloatingActionButton fab;
    private boolean started;
    private CustomGLSurface graphicsSurfaceView;
    //Image op-s
    private Mat imageFrame;
    private int imageWidth;
    private int imageHeight;
    private byte[] byteArr;
    private boolean enable;
    private boolean hasStartedTransmission;

    private final Runnable r = new Runnable() {
        @Override
        public void run() {
            enable = true;
            try{
                Log.d(TAG, "Start");
                //imageFrame = ImageUtils.convertToMat(lastImage,  true);
                //Log.d("OpenCVops", "H: "+imageFrame.height()+" , W:"+imageFrame.width());
                new ImageSendAsyncTask(getApplicationContext()).execute(imageFrame).get();

//                Log.d("imageSendAsyncTask", "Enabled:"+enable);

            } catch (Exception e){
                Log.d(TAG, e.getMessage());
            }
            periodicHandler.postDelayed(r,10000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        started = false;
        fab = (FloatingActionButton) findViewById(R.id.runScanning);
        fab.setOnClickListener(this);
        surfaceView = (TextureView) findViewById(R.id.surfaceView);
        graphicsSurfaceView = findViewById(R.id.glsurfaceview);
        graphicsSurfaceView.setVisibility(View.INVISIBLE);

        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        getPermission();
        surfaceView.setSurfaceTextureListener(this);
        enable = false;
        hasStartedTransmission = false;
        //Load OpenCV functions
        if(!OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0,
                this, callbackInterface)){
            Log.d(TAG, "Cannot connect to OpenCV manager");
        }
        //Run periodic scan for object detection
/*
        periodicHandler = new Handler();
        periodicHandler.postDelayed(r,  10000);
*/
        //Run BLE scanning
        if(!started){
            Intent bleService = new Intent(this, BluetoothDiscoveryService.class);
            startService(bleService);
            started = true;
        } else {
            Toast.makeText(getApplicationContext(),"Already scanning", Toast.LENGTH_SHORT).show();
        }
    }
    // Image Reader callback
    private final ImageReader.OnImageAvailableListener imageReaderListener =
            new ImageReader.OnImageAvailableListener() {
        @Override
        public void onImageAvailable(ImageReader imageReader) {
            //TODO:
            lastImage = imageReader.acquireLatestImage();

            try {
                imageFrame = ImageUtils.convertToMat(lastImage, true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            if(lastImage!=null){
                lastImage.close();
            }
        }
    };
    /*
    * Camera Capture Session callbacks
    *
    *
    * */
    private final CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
        }
    };

    private final CameraCaptureSession.StateCallback stateCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configured");
                    mCaptureSession = cameraCaptureSession;
                    try {
                        if(mCamera != null){
                            captureBuilder.set(CaptureRequest.CONTROL_AF_MODE,
                                    CameraMetadata.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            captureBuilder.set(CaptureRequest.FLASH_MODE,
                                    enable? CameraMetadata.FLASH_MODE_TORCH :CameraMetadata.FLASH_MODE_OFF );
                            Log.d(TAG, "Enabled: "+enable);
                            captureBuilder.addTarget(imageReader.getSurface());
                            mCaptureSession.setRepeatingRequest(captureBuilder.build(),
                                    mCaptureCallback, backgroundHandler);
                        }
                    } catch (CameraAccessException e) {
                        Log.d(TAG, "AccessException" +e.getMessage());
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configure failed");
                }
            };


    /*
    * Camera Device Callbacks
    * */
    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCamera = cameraDevice;
            Log.d(TAG, "StateCallback opened");
            try {
                SurfaceTexture mTexture = surfaceView.getSurfaceTexture();
                mTexture.setDefaultBufferSize(imageSize.getWidth(),imageSize.getHeight());
                Surface temp = new Surface(mTexture);
                captureBuilder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                /*captureBuilder.set(CaptureRequest.FLASH_MODE,
                        enable? CameraMetadata.FLASH_MODE_TORCH :CameraMetadata.FLASH_MODE_OFF );*/
                captureBuilder.addTarget(temp);
                mCamera.createCaptureSession(Arrays.asList(temp, imageReader.getSurface()), stateCallback, null);

            }catch (Exception e){
                Log.d(TAG, e.getLocalizedMessage());
            }
        }

        @Override
        public void onDisconnected(@NonNull CameraDevice cameraDevice) {
            cameraDevice.close();
            mCamera = null;
            Log.d(TAG, "StateCallback disconnected");
        }

        @Override
        public void onError(@NonNull CameraDevice cameraDevice, int i) {
            cameraDevice.close();
            mCamera = null;
            Log.d(TAG, "StateCallback error");
            finish();
        }
    };



    //Permissions
    private void getPermission(){
        //Check Camera permission
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CODE);
            Log.d(TAG, "Request Camera Permission");
        }

        //Check location permission
        if(ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(getApplicationContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION},
                    BLUETOOTH_CODE);
        }
        //Enable bluetooth
        if(((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter() != null
                && !((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE))
                .getAdapter().isEnabled()){
            //Request Bluetooth Enable Intent
            Intent enableBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLE, BLUETOOTH_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(requestCode == PERMISSION_CODE && grantResults.length > 0){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Log.d(TAG, "Granted!");
            } else {
                finish();
            }
        }

        if(requestCode == BLUETOOTH_CODE && grantResults.length > 0){
            if(grantResults[0] != PackageManager.PERMISSION_GRANTED
                    || grantResults[1] != PackageManager.PERMISSION_GRANTED){
                Toast.makeText(getApplicationContext(), "Cannot use bluetooth service",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
    /*
    * Android activity lifecycle
    * */

    @Override
    protected void onPause() {
        super.onPause();
        closeCamera();
        stopThreads();
        graphicsSurfaceView.onPause();
        Log.d("Lifecycle", "pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        startThreads();
        if(surfaceView.isAvailable()){
            openCamera();
        } else {
            surfaceView.setSurfaceTextureListener(this);
        }
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_2_0, this, callbackInterface);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            callbackInterface.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
        graphicsSurfaceView.onResume();
        Log.d("Lifecycle", "resume");
    }
    private void openCamera(){
        try{
            Log.d(TAG, "Starting preview");
            if(cameraId == null){
                String[] list = cameraManager.getCameraIdList();
                cameraId = list[0];
            }
            cameraCharacteristics = cameraManager
                    .getCameraCharacteristics(cameraId);
            map = cameraCharacteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            imageSize = map.getOutputSizes(SurfaceTexture.class)[0];

            cameraManager.openCamera(cameraId, mStateCallback, backgroundHandler);
            imageReader = ImageReader.newInstance(MAX_WIDTH,MAX_HEIGHT,ImageFormat.YUV_420_888,2);
            imageReader.setOnImageAvailableListener(imageReaderListener, backgroundHandler);
        }catch (CameraAccessException | SecurityException exceptions){
            Log.d(TAG, "Exception"+exceptions.getMessage());
            Toast.makeText(getApplicationContext(),"Sorry, camera is not permitted.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void closeCamera(){
        if(mCaptureSession != null){
            mCaptureSession.close();
            mCaptureSession = null;
        }

        if(mCamera != null){
            mCamera.close();
            mCamera = null;
        }

        if(imageReader != null){
            imageReader.close();
            imageReader = null;
        }
    }
    /***********************************/
    /*
    * Handle activity lyfecycle
    * Start threads on Resume
    * */
    private void startThreads(){
        if(backgroundThread != null) return;
        backgroundThread = new HandlerThread("CameraBackground");
        backgroundThread.start();
        backgroundHandler = new Handler(backgroundThread.getLooper());
    }
    /*
    * Stop threads on pause
    * */
    private void stopThreads(){
        if(backgroundThread == null) return;
        backgroundThread.quitSafely();
        try {
            backgroundThread.join();
            backgroundThread = null;
            backgroundHandler = null;
        } catch (InterruptedException e){
            Log.d(TAG, "Interrupted Exception");
        }
    }
    /******************************/
    /*
    * On Click listener callback implementation
    * */

    private float[] custom = {
            -0.046875f,0.7f,0.f,
            -0.04375f,0.76666665f,0.f,
            -0.00625f,0.70555556f,0.f
    };
    @Override
    public void onClick(View view) {

        Log.d(TAG, "Check functionality onClick");
//        Intent audioService = new Intent(this, MediaService.class);
//        startService(audioService);

        float[] temp = ImageUtils.getPoints(imageFrame);

        /*if(temp != null){
            //final RectangleFrame frame = new RectangleFrame(ImageUtils.convertToGLcoords(temp));
            graphicsSurfaceView.setVisibility(View.VISIBLE);
            graphicsSurfaceView.getmRenderer().setmTriangle(new Triangle(custom));
            graphicsSurfaceView.render();
            *//*graphicsSurfaceView.getLayoutParams().height = SCREEN_HEIGHT/MAX_HEIGHT*((int)temp[0]);
            graphicsSurfaceView.getLayoutParams().width  = SCREEN_WIDTH/MAX_WIDTH*((int)temp[1]);*//*

            //Log.d(TAG, "size: "+mgr.getDefaultDisplay().getWidth()+", "+ mgr.getDefaultDisplay().getHeight());
            //graphicsSurfaceView.setBackgroundColor(0);
            Log.d(TAG, "Drawing");
        }*/
        new ImageSendAsyncTask(getApplicationContext()).execute(imageFrame);

    }

    /*****************************/
    /*
    * Surface Callback override methods
    * */
    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int i, int i1) {
        //Start Preview with camera
        Log.d(TAG, "Surface Available");
        openCamera();
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surfaceTexture, int i, int i1) {

    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surfaceTexture) {
        return false;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surfaceTexture) {

    }
}
