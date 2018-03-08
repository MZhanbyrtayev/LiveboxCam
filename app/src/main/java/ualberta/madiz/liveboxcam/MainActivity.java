package ualberta.madiz.liveboxcam;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.CaptureResult;
import android.hardware.camera2.TotalCaptureResult;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.accessibility.AccessibilityRecord;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

    private static final String TAG = "MainActivity";
    private static final int PERMISSION_CODE = 77;
    private static final int BLUETOOTH_CODE = 88;
    private static final int MAX_HEIGHT = 1080;
    private static final int MAX_WIDTH = 1920;
    //Visual Component
    private CameraManager cameraManager;
    private CameraDevice mCamera;
    private CameraCaptureSession mCaptureSession;
    private SurfaceView surfaceView;
    private SurfaceHolder surfaceHolder;
    private Surface cameraSurface;

    //UI
    private FloatingActionButton fab;
    private boolean started;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        started = false;
        fab = (FloatingActionButton) findViewById(R.id.runScanning);
        fab.setOnClickListener(this);
        surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
        surfaceHolder = surfaceView.getHolder();
        surfaceHolder.addCallback(this);
        cameraManager = (CameraManager) getSystemService(CAMERA_SERVICE);
        getPermission();

        try{
            String[] list = cameraManager.getCameraIdList();
            if(ContextCompat.checkSelfPermission(getApplicationContext(),Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED){
                cameraManager.openCamera(list[0], mStateCallback, new Handler());
            } else {
                Log.d(TAG, "Permission is not granted");
            }
            //TODO: determine which one is front facing
        }catch (CameraAccessException e){
            Log.d(TAG, "Exception"+e.getMessage());
        }


    }

    private CameraCaptureSession.CaptureCallback mCaptureCallback =
            new CameraCaptureSession.CaptureCallback() {
        @Override
        public void onCaptureStarted(@NonNull CameraCaptureSession session,
                                     @NonNull CaptureRequest request,
                                     long timestamp,
                                     long frameNumber) {
            super.onCaptureStarted(session, request, timestamp, frameNumber);
            Log.d(TAG, "Capture callback started");
            //TODO: complete processing of images

        }

        @Override
        public void onCaptureProgressed(@NonNull CameraCaptureSession session,
                                        @NonNull CaptureRequest request,
                                        @NonNull CaptureResult partialResult) {
            super.onCaptureProgressed(session, request, partialResult);
            Log.d(TAG, "Capture callback progressed");
        }

        @Override
        public void onCaptureCompleted(@NonNull CameraCaptureSession session,
                                       @NonNull CaptureRequest request,
                                       @NonNull TotalCaptureResult result) {
            super.onCaptureCompleted(session, request, result);
            Log.d(TAG, "Capture callback completed");


        }
    };

    private CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {
        @Override
        public void onOpened(@NonNull CameraDevice cameraDevice) {
            mCamera = cameraDevice;
            Log.d(TAG, "StateCallback opened");
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

    private CameraCaptureSession.StateCallback stateCallback =
            new CameraCaptureSession.StateCallback() {
                @Override
                public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configured");
                    mCaptureSession = cameraCaptureSession;
                    try {
                        CaptureRequest.Builder previewRequestBuilder = mCamera
                                .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);

                        previewRequestBuilder.addTarget(cameraSurface);
                        mCaptureSession.setRepeatingRequest(previewRequestBuilder.build(),
                                mCaptureCallback, null);
                    } catch (CameraAccessException e) {
                        Log.d(TAG, "AccessException" +e.getMessage());
                    }
                }
                @Override
                public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                    Log.d(TAG, "Configure failed");
                }
            };


    private void getPermission(){
        //Check Camera permission
        if(ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA},
                    PERMISSION_CODE);
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
                && !((BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter().isEnabled()){
            Intent enableBLE = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBLE, BLUETOOTH_CODE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == BLUETOOTH_CODE && resultCode == Activity.RESULT_OK){
            Log.d(TAG, "Successful bluetooth enable!");
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

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        cameraSurface = surfaceHolder.getSurface();
        Log.d(TAG, "Surface created");
        List<Surface> surfaceList = new ArrayList<Surface>();
        if(cameraSurface!=null) {
            surfaceList.add(cameraSurface);
            try {
                mCamera.createCaptureSession(surfaceList, stateCallback, null);
            } catch (CameraAccessException e) {
                Log.d(TAG, "Access Exception state call back on opened");
            }
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {
        cameraSurface = surfaceHolder.getSurface();
        Log.d(TAG, "Surface changed");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        cameraSurface = null;
        Log.d(TAG, "Surface destroyed");
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Lifecycle", "start");
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Lifecycle", "pause");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Lifecycle", "resume");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Lifecycle", "stop");
    }


    @Override
    public void onClick(View view) {
        if(!started){
            Intent bleService = new Intent(this, BluetoothDiscoveryService.class);
            startService(bleService);
            started = true;
        } else {
            Toast.makeText(getApplicationContext(),"Already scanning", Toast.LENGTH_SHORT).show();
        }
    }
}
