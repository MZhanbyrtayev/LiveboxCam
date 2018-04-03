package ualberta.madiz.liveboxcam;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

public class PermissionHelper {
    private static final int PERMISSION_CODE = 77;
    private static final int BLUETOOTH_CODE = 66;
    private static final String TAG = "PermissionHelper";


    public static boolean requestCameraPermission(Context context, Activity activity){


        return true;
    }

    public static boolean requestLocationPermission(Context context, Activity activity){



        return true;
    }
}
