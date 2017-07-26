package com.example.administrator.hyeonsoo_gps;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static com.example.administrator.hyeonsoo_gps.R.id.TV_from;
import static com.example.administrator.hyeonsoo_gps.R.id.editText_req;
import static com.example.administrator.hyeonsoo_gps.R.id.editText_to;


/**
 * I start this application on 2017-07-10.
 */
/////////////////////////////////////////////////////////////////
//이 Class는 GeoTask interface를 상속받으며, 실제로 GeoTask와 GPSTracker를 사용하는 Class.★
//도착지 = =Seoulstation 설정.★★★
//도착시간 = .★★★
//주기적으로 위치정보를 확인하며 Activity에 나와야하지만, 그렇지 못하고 있음.★★★
//GPSTracker 수정하면 자동적으로 해결 될 거 같기도 함.★★★

public class MainActivity extends AppCompatActivity implements GeoTask.Geo{
    //변수들//
    String reqDatestr, str_req;
    String str_from,str_to;
    Button btnShowLocation;
    TextView tv_from;
    EditText edttxt_to,edttxt_req;
    TextView tv_result1,tv_result2;
    EditText editText;

    // GPSTracker class
    public static GPSTracker gps=null;  //여기서 gps는 현재 gps                                 //////static안쓰고 잘안됨..
    public Handler mHandler;
    public static int RENEW_GPS = 1;                                     ///static 써도될듯
    public static int SEND_PRINT = 2;                                    ///static 써도될듯
    public static double Travel_time=-1;//default                                                           //////static안쓰고 잘안됨..

    public static boolean getTime_ON_OFF=false;                                                //////static안쓰고 잘안됨..
    public static double distance_time;                                                         //////static안쓰고 잘안됨..

//ALARMTEST 변수 일주일치 초
    private static final long A_WEEK = 1000 * 60 * 60 * 24 * 7;

    //////////함수 시작

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main); //activity 등록

//ALARMTEST 한줄 추가
        new AlarmHATT(getApplicationContext()).Alarm();
//
        initialize(); // activity xml의 각 요소들 등록 Button1개 TextView4개, EditText 1개

        if ( Build.VERSION.SDK_INT >= 24 &&
                ContextCompat.checkSelfPermission( this, android.Manifest.permission.ACCESS_FINE_LOCATION ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions( this, new String[] {  android.Manifest.permission.ACCESS_FINE_LOCATION  },
                    0 );
        }

        mHandler = new Handler(){
            @Override
            public void handleMessage(Message msg){
                if(msg.what==RENEW_GPS){
                    makeNewGpsService();
                }
                if(msg.what==SEND_PRINT){
                    logPrint((String)msg.obj);
                }
            }
        };

        // show location button click event
        btnShowLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {

                str_req = edttxt_req.getText().toString(); //약속시간text받아옴
                //Log.d("str_req:",str_req);
                //reqDatestr=str_req;
                send_distance_time(); //약속시간저장

                // create class object
                makeNewGpsService(); //GPSTracker의 객체(=gps)가 없으면(=null) 만들고, 있으면 gps를 Update함.

//Log.d("Travel_time",Double.toString(Travel_time)); ::-1 나옴

                my_display(); // GPS 위치정보를 알 수 있다면 위도 경도를 얻어 GeoTask를 이용하여 예상 소요시간/거리 구함
                              // 단 초기값의 오류(location ==null)로 위도값과 경도값이 모두 0이면 버튼을 다시 눌러달라고 Toast 띄움

                Log.d("test", "액티비티-서비스 시작버튼클릭");

                Intent intent = new Intent(
                        getApplicationContext(),//현재제어권자
                        PunctualService.class); // 이동할 컴포넌트
                startService(intent); // 서비스 시작
            }
        });
    }

    ///////////앞 서 말한 GPSTracker 객체 gps를 만들거나 Update하는 함수.
    public void makeNewGpsService(){
        if(gps == null) {
            gps = new GPSTracker(MainActivity.this,mHandler);
            Log.d("-1_a gps is null? ",Boolean.toString(gps==null));
        }else{
            gps.Update();
            Log.d("-1_b gps is null? ",Boolean.toString(gps==null));
            Log.d("-1_b location is null? ",Boolean.toString(gps.location==null));
        }
    }

    //////////앞 서 말한 gps의 위도, 경도로 부터 예상 소요시간/거리 구하는 함수
    public void my_display(){
        // check if GPS enabled
        if(gps.canGetLocation()) {

            Log.d("앞: gps is null? ", Boolean.toString(gps == null));
            makeNewGpsService();
            double latitude = gps.getLatitude();
            double longitude = gps.getLongitude();
            // \n is for new line

            if ((latitude ==0)&&(longitude==0)){
                gps.Update();
                Toast.makeText(getApplicationContext(), "Plz, Click the button Again.", Toast.LENGTH_LONG).show();

            }

            if ((latitude != 0) && (longitude != 0)) {
                getTime_Distance(latitude,longitude);

                Log.d("뒤gps.location is null? ",Boolean.toString(gps.location==null));
                Toast.makeText(getApplicationContext(), "Your Location is - \nLat: " + latitude + "\nLong: " + longitude, Toast.LENGTH_LONG).show();
            }
        }
        else{
            // can't get location
            // GPS or Network is not enabled
            // Ask user to enable GPS/network in settings
            gps.showSettingsAlert();
        }
    }


////////////////////////////////////logPrint와 getTimeStr함수: Edit Text에 현재 시각과 위치 정보 도시화 해주는 함수.
    public void logPrint(String str){
        editText.append(getTimeStr()+" "+str+"\n");
    }

    public String getTimeStr(){
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMddHHmmss",Locale.KOREA);
        return sdfNow.format(date);
    }
///////////////////////////////////////////////////////////////////////////////////////////////

    /////////////앞 서 말한 화면 구성요소 등록
    public void initialize()
    {
        btnShowLocation = (Button) findViewById(R.id.btnShowLocation);
        tv_from= (TextView) findViewById(TV_from);
        edttxt_to= (EditText) findViewById(editText_to);
        edttxt_req=(EditText) findViewById(editText_req);
        tv_result1= (TextView) findViewById(R.id.textView_result1);
        tv_result2=(TextView) findViewById(R.id.textView_result2);
        editText = (EditText) findViewById(R.id.editText);
    }


    /////////////////////////////////////////////////////////////////////////////////
    ////////////getTime_Distance함수와 setDouble함수
    ///GeoTask를 이용하여 예상 소요시간과 거리를 TextView에 도시화해주는 함수.
    public void getTime_Distance(double lati, double longi) {

        tv_from.setText("Here is " + lati + ", " + longi + "");
        str_from = "" + lati + "," + "+" + longi + "";//edttxt_from.getText().toString();
        str_to = edttxt_to.getText().toString();

        String url = "https://maps.googleapis.com/maps/api/distancematrix/json?origins=" + str_from + "&destinations=" + str_to + "&mode=transit&language=fr-FR&avoid=tolls&key=AIzaSyDepLWIg1PnSeYnMkMwPoTJRzS4VpMr7go";
        new GeoTask(MainActivity.this).execute(url);
        Log.d("1Here's latitude is ", Double.toString(lati));
        Log.d("1Here's longitude is ", Double.toString(longi));
        getTime_ON_OFF=true;
    }
    @Override
    public void setDouble(String result) {
        String res[]=result.split(",");
        Double min=Double.parseDouble(res[0])/60;         //Double min
        int dist=Integer.parseInt(res[1])/1000;
        tv_result1.setText("Duration= " + (int) (min / 60) + " hr " + (int) (min % 60) + " mins");
        tv_result2.setText("Distance= " + dist + " kilometers");

        //예상 소요시간 넘겨주려고 setDobule에 2줄 추가
        Log.d("setDouble.min 값 : ", Double.toString(min));
        Travel_time=min;
    }
        //예상 소요시간 구한거 다른 class로 넘겨 주는 함수.
    public void send_Travel_time(){
        //return Travel_time;
        Intent intent = new Intent(getApplicationContext(),PunctualService.class);
        intent.putExtra("Travel_time",Travel_time);
        Log.d("setDouble.min 값 : ", Double.toString(Travel_time));
    }



    //간략한 현재 시각 정보를 넘겨주는 함수.
    public void send_distance_time(){
        try{
            //요청 시간 String
            reqDatestr = str_req; // 내가 설정

            //현재 시간 date
            Date curDate = new Date();
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
            //요청시간을 Date로 parsing 후 time 가져오기
            Date reqDate = sdf.parse(reqDatestr); //str_req=reqDatestr
            long reqDateTime =reqDate.getTime();
            //현재시간을 요청 시간의 형태로 format 후 time 가져오기
            curDate = sdf.parse(sdf.format(curDate));
            long curDateTime = curDate.getTime();
            //분으로 표현
            distance_time= (reqDateTime-curDateTime)/60/1000;
            Intent intent = new Intent(getApplicationContext(),PunctualService.class);
            intent.putExtra("distance_time",distance_time);
        }catch ( Exception e ){
            e.printStackTrace();
        }
        //return distance_time;
    }


    ///////////////ALARMTEST 추가
    public class AlarmHATT {
        private Context context;

        public AlarmHATT(Context context) {
            this.context = context;
        }

        public void Alarm() {

            Calendar calendar1 = Calendar.getInstance();
            Calendar calendar2 = Calendar.getInstance();
            Calendar calendar3 = Calendar.getInstance();
            //알람시간 calendar에 set해주기

            //내가 정했음:
            calendar1.setTimeInMillis(System.currentTimeMillis());
            calendar1.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
            calendar1.set(Calendar.HOUR_OF_DAY, 7);
            calendar1.set(Calendar.MINUTE, 40);
            calendar1.set(Calendar.SECOND, 00);

            calendar2.setTimeInMillis(System.currentTimeMillis());
            calendar2.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
            calendar2.set(Calendar.HOUR_OF_DAY, 10);
            calendar2.set(Calendar.MINUTE, 10);
            calendar2.set(Calendar.SECOND, 00);

            calendar3.setTimeInMillis(System.currentTimeMillis());
            calendar3.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
            calendar3.set(Calendar.HOUR_OF_DAY, 9);
            calendar3.set(Calendar.MINUTE, 10);
            calendar3.set(Calendar.SECOND, 00);

            Intent intent = new Intent(getApplicationContext(), BroadcastD.class);

            PendingIntent sender1 = PendingIntent.getBroadcast(getApplicationContext(), 98,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent sender2 = PendingIntent.getBroadcast(getApplicationContext(), 99,  intent, PendingIntent.FLAG_UPDATE_CURRENT);
            PendingIntent sender3 = PendingIntent.getBroadcast(getApplicationContext(), 100, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            AlarmManager am1 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager am2 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            AlarmManager am3 = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
            //알람 예약
            am1.setRepeating(AlarmManager.RTC_WAKEUP, calendar1.getTimeInMillis(), A_WEEK, sender1);
            am2.setRepeating(AlarmManager.RTC_WAKEUP, calendar2.getTimeInMillis(), A_WEEK, sender2);
            am3.setRepeating(AlarmManager.RTC_WAKEUP, calendar3.getTimeInMillis(), A_WEEK, sender3);

        }
    }
}