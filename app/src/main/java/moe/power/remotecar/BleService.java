package moe.power.remotecar;

import android.app.Service;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothProfile;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.widget.Toast;

import java.util.TimerTask;
import java.util.UUID;

/**
 * Created by PowerLi on 2016/7/17.
 */
public class BleService extends Service {

    private Handler commandSender;

    BluetoothGatt mGatt;
    private boolean isRunning=false;

    private int direction=0;
    private int turn=128;

    public static final UUID BLE_GATT_SERIAL_SERVICE=UUID.fromString("0000ffe0-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_GATT_SERIAL_CHARACTERISTIC=UUID.fromString("0000ffe1-0000-1000-8000-00805f9b34fb");

    private Runnable sender=new Runnable() {
        @Override
        public void run() {
            sendCommand();
        }
    };

    private BroadcastReceiver mReceiver=new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            direction=intent.getIntExtra("dir",0);
            turn=intent.getIntExtra("turn",128);
        }
    };

    BluetoothGattCallback mGattCallback=new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if(newState== BluetoothProfile.STATE_CONNECTED)
            {
                gatt.discoverServices();
                makeToast("Connected: "+gatt.getDevice().getName());

            }
            else if(newState==BluetoothProfile.STATE_DISCONNECTED)
            {
                isRunning=false;
                makeToast("Disconnected");
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            if(status==BluetoothGatt.GATT_SUCCESS)
            {
                makeToast("Discover OK");
                mGatt=gatt;
                isRunning=true;
                commandSender.postDelayed(sender,50);
            }
            else
            {
                makeToast("Discover Error: "+status);
                isRunning=false;
            }
        }

    };

    @Override
    public boolean onUnbind(Intent intent) {


        mGatt.disconnect();
        mGatt.close();
        stopSelf();
        return super.onUnbind(intent);
    }

    @Override
    public void onCreate() {

        super.onCreate();
        commandSender=new Handler();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        makeToast("Service Stopped");

        mGatt.disconnect();
        mGatt.close();
        stopSelf();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        IntentFilter intentFilter=new IntentFilter("moe.power.remotecar.update");
        registerReceiver(mReceiver,intentFilter);
        BleScan.devList.get(intent.getIntExtra("index",0)).getDevice().connectGatt(getApplicationContext(),false,mGattCallback);

        return 0;
    }



    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    void makeToast(final String text)
    {
        Handler handler=new Handler(Looper.getMainLooper());

        handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(),text,Toast.LENGTH_SHORT).show();
            }
        });
    }



    void sendCommand()
    {

        if(isRunning)
        {
            send();
            commandSender.postDelayed(sender,40);

        }
        else
        {
            direction=0;
            turn=128;
            try {
                Thread.sleep(40);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            send();
        }
    }

    void send()
    {
        BluetoothGattCharacteristic c=mGatt.getService(BLE_GATT_SERIAL_SERVICE).getCharacteristic(BLE_GATT_SERIAL_CHARACTERISTIC);
        StringBuilder sb=new StringBuilder();
        sb.append("S");
        sb.append(direction);
        sb.append(",");
        sb.append(String.format("%03d", turn));
        //sb.append("D");
        c.setValue(sb.toString());
        mGatt.writeCharacteristic(c);
    }
}
