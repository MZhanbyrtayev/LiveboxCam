package ualberta.madiz.liveboxcam;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.util.List;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothDiscoveryService extends IntentService {
    private static final String TAG = "BluetoothDiscoverService";
    private static final int SCAN_PER = 10000; //milliseconds

    private BluetoothAdapter mAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner mScanner;
    private boolean isScanning;
    private Handler mHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        if(getApplicationContext()!=null){
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mAdapter = bluetoothManager.getAdapter();
            mScanner = mAdapter.getBluetoothLeScanner();
        }
    }

    public BluetoothDiscoveryService() {
        super("BluetoothDiscoveryService");

        isScanning = false;
        mHandler = new Handler();
    }
    private ScanCallback myCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.d(TAG, "Callback type: " + callbackType + ", Result: " + result.toString());
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for(ScanResult res : results){
                Log.d(TAG, res.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.d(TAG, "Error:"+errorCode);
        }
    };
    private void scanDevice(final boolean enable){
        if(enable){
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    if(isScanning){
                        isScanning = false;
                        mScanner.stopScan(myCallback);
                    }
                }
            }, SCAN_PER);
            if(!isScanning){
                isScanning = true;
                mScanner.startScan(myCallback);
            }
        }
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            scanDevice(true);
        }
    }
}
