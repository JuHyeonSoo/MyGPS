package com.example.administrator.hyeonsoo_gps;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import static com.example.administrator.hyeonsoo_gps.MainActivity.Travel_time;
import static com.example.administrator.hyeonsoo_gps.MainActivity.distance_time;
import static com.example.administrator.hyeonsoo_gps.MainActivity.getTime_ON_OFF;
import static com.example.administrator.hyeonsoo_gps.MainActivity.gps;

public class PunctualService extends Service {

    private Handler mHandler;

    //double Travel_t;
    double distance_t;
    public PunctualService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }

    //
    /////////////////////////////////////////////////////////////
    @Override
    public void onCreate(){
        super.onCreate();
        //서비스에서 가장 먼저 호출(최초 한번만)
        gps.Update();
        Log.d("test","서비스의 onCreate");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // 서비스가 호출될 때마다 실행
        Log.d("test", "서비스의 onStartCommand");

        double Travel_t=intent.getDoubleExtra("Travel_time",Travel_time);
        distance_t=intent.getDoubleExtra("distance_time",distance_time);

        gps.Update();
        mHandler = new Handler();
        Log.d("gps location is null",Boolean.toString(gps.location==null));
        if ((getTime_ON_OFF == false)||(Travel_t==-1)){
            Log.d("Tracker_ Click,Again","Please");
            Log.d("getTime_ON_OFF is true?",Boolean.toString(getTime_ON_OFF==true));
            Log.d("예상 소요시간==-1",Boolean.toString(Travel_t==-1));
            Toast.makeText(getApplicationContext(), "Plz, Click the button Again.", Toast.LENGTH_LONG).show();
        }

        else if(getTime_ON_OFF == true && (Travel_t)!=-1) {

            Log.d("getTime_ON_OFF is true?",Boolean.toString(getTime_ON_OFF==true));
            Log.d("예상 소요 시간?(분)",Double.toString(Travel_t));
            Log.d("약속시간까지 남은 시간은?(분)",Double.toString(distance_t));
            //비교: 예상 소요시간 vs 약속 까지 남은 시간
            if (Travel_t == distance_t) {
                Check_Moving();
            } else if (Travel_t > distance_t) {
                Be_hurry();
            } else {
                Log.d("You got Enough Time :) ", "놀고 있어도 돼~!");
            }
        }
        else if(Travel_t==0) {
            Toast.makeText(getApplicationContext(), "도착!", Toast.LENGTH_LONG).show();
            onDestroy();
        }
        else
            Log.d("Something", "Wrong!!");
        return super.onStartCommand(intent, flags, startId);
    }
/////////

    private class ToastRunnable implements Runnable {
        String mText;
        public ToastRunnable(String text) {
            mText = text;
        }

        @Override
        public void run(){
            Toast.makeText(getApplicationContext(), mText, Toast.LENGTH_SHORT).show(); } }



    private void Be_hurry() {
        mHandler.post(new ToastRunnable("Be_hurry for the Schedule!"));
        Toast.makeText(getApplicationContext(), "Be hurry for the Schedule!", Toast.LENGTH_LONG).show();
    }
    private void Check_Moving() {
        mHandler.post(new ToastRunnable("Now You should go. :)"));
        Toast.makeText(getApplicationContext(), "Now You should go. :)", Toast.LENGTH_LONG).show();
    }


    ////////
    @Override
    public void onDestroy() {
        super.onDestroy();
        // 서비스가 종료될 때 실행
        gps.stopUsingGPS(); // gps 종료
        Log.d("test", "서비스의 onDestroy");
    }



    //////////////////////////////////////
    //


}
