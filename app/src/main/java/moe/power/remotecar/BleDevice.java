package moe.power.remotecar;

import android.bluetooth.BluetoothDevice;

/**
 * Created by PowerLi on 2016/7/17.
 */
public class BleDevice {
    private String name;
    private String address;
    private BluetoothDevice device;


    public BleDevice(String name, String address, BluetoothDevice device) {
        this.name = name;
        this.address = address;
        this.device = device;
    }

    public BluetoothDevice getDevice() {
        return device;
    }

    public void setDevice(BluetoothDevice device) {
        this.device = device;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }
}
