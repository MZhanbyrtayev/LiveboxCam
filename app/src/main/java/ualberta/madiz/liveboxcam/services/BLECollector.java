package ualberta.madiz.liveboxcam.services;

import android.util.Log;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import ualberta.madiz.liveboxcam.entities.Beacon;

class BLECollector {
    private static final String TAG = "BluetoothCollector";
    private static final BLECollector ourInstance = new BLECollector();
    private static final long ACTIVE_PERIOD = 60000;
    private List<Beacon> visitedBeacons;
    static BLECollector getInstance() {
        return ourInstance;
    }

    private BLECollector() {
        visitedBeacons = new ArrayList<>();
    }

    private int contains(Beacon discoveredDevice){
        for(Beacon b : visitedBeacons){
            if(b.getAddress().equalsIgnoreCase(discoveredDevice.getAddress())){
                Log.d(TAG, "Present");
                return visitedBeacons.indexOf(b);
            }
        }
        return -1;
    }
    /*
    return true if exists
    */
    public boolean updateBeacon(Beacon discoveredDevice){
        int containsIndex = -1;
        Log.d(TAG, "Update");

        if((containsIndex = contains(discoveredDevice)) >= 0){
            Beacon temp = visitedBeacons.get(containsIndex);
            Log.d(TAG, containsIndex+"-> Index");
            if(temp.isSleeping()){
                temp.setTime(Calendar.getInstance().getTimeInMillis());
                temp.setToSleep(false);
                return false;
            } else {
                long timeSeen = temp.getTime();
                long now = Calendar.getInstance().getTimeInMillis();
                if (timeSeen - now > ACTIVE_PERIOD) {
                    temp.setToSleep(true);
                } else {
                    visitedBeacons.get(containsIndex).setTime(now);
                }
                return true;
            }
        } else {
            visitedBeacons.add(discoveredDevice);
        }
        return false;
    }

}
