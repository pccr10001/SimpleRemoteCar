package moe.power.remotecar;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Interpolator;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import app.akexorcist.joystickcontroller.JoyStick;
import com.github.niqdev.mjpeg.DisplayMode;
import com.github.niqdev.mjpeg.Mjpeg;
import com.github.niqdev.mjpeg.MjpegInputStream;
import com.github.niqdev.mjpeg.MjpegView;
import rx.functions.Action1;

import java.util.List;

public class JoystickActivity extends AppCompatActivity {

    RelativeLayout layoutJoystick;
    static int lastDirection = 0;
    static int lastTurn = 128;

    private Intent service;

    SensorManager sensorManager;
    boolean accelerometerPresent;
    Sensor accelerometerSensor;


    MjpegView mjpegView;
    long tap;
    int action;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_joystick);

        service=new Intent();
        service.putExtra("index",getIntent().getIntExtra("index",1));
        service.setClass(JoystickActivity.this,BleService.class);
        startService(service);

        sensorManager = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
        List<Sensor> sensorList = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER);

        mjpegView=(MjpegView)findViewById(R.id.mjpegViewDefault);



        if(sensorList.size() > 0){
            accelerometerPresent = true;
            accelerometerSensor = sensorList.get(0);

        }
        else{
            accelerometerPresent = false;
        }
        layoutJoystick=(RelativeLayout)findViewById(R.id.layout_joystick);

        layoutJoystick.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                //Log.d("QQQ",String.valueOf(motionEvent.getAction()));
                if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
                    int x=Math.round(motionEvent.getX());


                    if(x>1000)
                    {
                        if(action==0)
                            lastDirection=1;
                        else
                        {
                                lastDirection=3;
                        }

                    }
                    else
                    {
                        if(action==0)
                            lastDirection=2;
                        else
                        {
                                lastDirection=4;
                        }
                    }

                    tap=System.currentTimeMillis();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_UP)
                {
                    action=0;
                    if(System.currentTimeMillis()-tap<100)
                    {
                        action=1;
                    }
                    lastDirection=0;
                    tap=System.currentTimeMillis();
                }
                else if(motionEvent.getAction() == MotionEvent.ACTION_MOVE){}
                else
                {
                    lastDirection=0;
                }
                Log.d("QQ",String.valueOf(lastDirection));
               // update();
                return false;
            }
        });
    }

    private DisplayMode calculateDisplayMode() {
        int orientation = getResources().getConfiguration().orientation;
        return orientation == Configuration.ORIENTATION_LANDSCAPE ?
                DisplayMode.FULLSCREEN : DisplayMode.BEST_FIT;
    }

    private void loadIpCam() {

        Mjpeg.newInstance()
                .open("http://192.168.26.108:8080/video", 5)
                .subscribe(
                        new Action1<MjpegInputStream>() {
                            @Override
                            public void call(MjpegInputStream inputStream) {
                                mjpegView.setSource(inputStream);
                                mjpegView.setDisplayMode(JoystickActivity.this.calculateDisplayMode());
                                mjpegView.showFps(true);
                            }
                        },
                        new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                Log.e(JoystickActivity.this.getClass().getSimpleName(), "mjpeg error", throwable);
                                Toast.makeText(JoystickActivity.this, "Error", Toast.LENGTH_LONG).show();
                            }
                        });
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        try {
            stopService(service);
        }catch(Exception e)
        {}
        if(accelerometerPresent){
            sensorManager.unregisterListener(accelerometerListener);
            Toast.makeText(this, "Unregister accelerometerListener", Toast.LENGTH_LONG).show();
        }
        finish();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            stopService(service);

        }catch(Exception e)
        {}
        if(accelerometerPresent){
            sensorManager.unregisterListener(accelerometerListener);
            Toast.makeText(this, "Unregister accelerometerListener", Toast.LENGTH_LONG).show();
        }
        mjpegView.stopPlayback();
        finish();
    }

    void update() {
        Intent intent = new Intent("moe.power.remotecar.update");
        intent.putExtra("dir", lastDirection);
        intent.putExtra("turn", lastTurn);
        sendBroadcast(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if(accelerometerPresent){
            sensorManager.registerListener(accelerometerListener, accelerometerSensor, SensorManager.SENSOR_DELAY_GAME);
            Toast.makeText(this, "Register accelerometerListener", Toast.LENGTH_LONG).show();
        }
        loadIpCam();
    }
    private SensorEventListener accelerometerListener = new SensorEventListener(){

        @Override
        public void onAccuracyChanged(Sensor arg0, int arg1) {
            // TODO Auto-generated method stub

        }

        //TODO Sensor Filter
        @Override
        public void onSensorChanged(SensorEvent event) {

            final double alpha = 0.8;

            double gravity;
            Double y;
            gravity = (1 - alpha) * event.values[1];

            y = event.values[1] - gravity;

            if(y>7.2)
                y=7.2;
            if(y<-7.2)
                y=-7.2;

            y=y*128.0/7.2+128.0;
            lastTurn= y.intValue();
            update();
        }};
}
