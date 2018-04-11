package ualberta.madiz.liveboxcam.services;

import android.app.IntentService;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import java.net.ConnectException;
import java.util.Collections;
import java.util.List;

import ualberta.madiz.liveboxcam.entities.Beacon;


/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class BluetoothDiscoveryService extends IntentService {
    private static final String TAG = "BluetoothDiscoverService";
    private static final long SCAN_PER = 10000; //milliseconds
    private static final long WAIT_PER = 5000;
    private static final String DEVICE_ADDR = "CB:4C:16:08:10:07";
    private BluetoothAdapter mAdapter;
    private BluetoothManager bluetoothManager;
    private BluetoothLeScanner mScanner;
    private boolean isScanning;
    private Handler mHandler;
    private ScanFilter.Builder customFilter;
    private ScanSettings.Builder customSettings;

    @Override
    public void onCreate() {
        super.onCreate();
        if(getApplicationContext()!=null){
            bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
            mAdapter = bluetoothManager.getAdapter();
            mScanner = mAdapter.getBluetoothLeScanner();
            //ADD filter to listen certain mac address
            // TODO: NEEDS to be changed for SERVICE UUID
            // TODO: Service UUID to be managed at the beacon config
            customFilter = new ScanFilter.Builder();
            customFilter.setDeviceAddress(DEVICE_ADDR);
            customSettings = new ScanSettings.Builder();
            customSettings.setCallbackType(1);
            customSettings.build();
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
            //Check if this item was scanned before
            Beacon temp = new Beacon(result.getDevice());
            if(BLECollector.getInstance().updateBeacon(temp)){
                //if yes proceed further
                Log.d(TAG, "Exists in visited");
            } else {
                //if not start data fetch
                Log.d(TAG, "Start request");
                new ConnectionAsyncTask(getApplicationContext()).execute(temp);
            }
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

    private Runnable scan = new Runnable() {
        @Override
        public void run() {
            if(!isScanning){
                isScanning = true;
                mScanner.startScan(Collections.singletonList(customFilter.build()),
                        customSettings.build(), myCallback);
            }
            mHandler.postDelayed(stop, SCAN_PER);
        }
    };

    private Runnable stop = new Runnable() {
        @Override
        public void run() {
            if(isScanning){
                isScanning = false;
                mScanner.stopScan(myCallback);
            }
            mHandler.postDelayed(scan, WAIT_PER);
        }
    };
    private void scanDevice(final boolean enable){
        if(enable){
            mHandler.post(scan);
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
