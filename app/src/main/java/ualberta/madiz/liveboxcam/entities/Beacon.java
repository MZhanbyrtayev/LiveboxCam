package ualberta.madiz.liveboxcam.entities;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.sql.Time;
import java.util.Calendar;

public class Beacon {
    private String address;
    private ParcelUuid[] serviceUuids;
    private String name;
    private int type;
    private int isPaired;
    private boolean isSleeping;
    private long lastSeenTime;
    public Beacon(BluetoothDevice discoveredDevice){
        this.address = discoveredDevice.getAddress();
        this.name = discoveredDevice.getName();
        this.serviceUuids = discoveredDevice.getUuids();
        this.type = discoveredDevice.getType();
        this.isPaired = discoveredDevice.getBondState();
        this.isSleeping = false;
        this.lastSeenTime = Calendar.getInstance().getTimeInMillis();
    }

    public String getAddress() {
        return address;
    }

    public ParcelUuid[] getServiceUuids() {
        return serviceUuids;
    }

    public String getName() {
        return name;
    }

    public int getType() {
        return type;
    }

    public int getIsPaired() {
        return isPaired;
    }
    public boolean isSleeping(){
        return isSleeping;
    }
    public void setToSleep(boolean value){
        isSleeping = value;
    }
    public long getTime(){
        return lastSeenTime;
    }
    public void setTime(long newTime){
        lastSeenTime = newTime;
    }
}
