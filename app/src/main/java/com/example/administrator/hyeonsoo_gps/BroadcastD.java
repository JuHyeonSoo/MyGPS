package com.example.administrator.hyeonsoo_gps;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

/**
 * Created by Administrator on 2017-07-26.
 */

public class BroadcastD extends BroadcastReceiver {
    String INTENT_ACTION = Intent.ACTION_BOOT_COMPLETED;

    @Override
    public void onReceive(Context context, Intent intent) {//알람 시간이 되었을때 onReceive를 호출함
        //NotificationManager 안드로이드 상태바에 메세지를 던지기위한 서비스 불러오고

        String text="Check this Web site :) ";
        String link="https://www.naver.com";

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setAutoCancel(true)
                        .setSmallIcon(R.mipmap.ic_launcher)
                        .setContentTitle(text);



        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);


        // pending implicit intent to view url
        Intent resultIntent = new Intent(Intent.ACTION_VIEW);
        resultIntent.setData(Uri.parse(link));

        //PendingIntent pending1 = PendingIntent.getActivity(context,  98, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        //PendingIntent pending2 = PendingIntent.getActivity(context,  99, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        PendingIntent pending3 = PendingIntent.getActivity(context, 0, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        //notificationBuilder.setContentIntent(pending1);
        //notificationBuilder.setContentIntent(pending2);
        notificationBuilder.setContentIntent(pending3);
        // using the same tag and Id causes the new notification to replace an existing one

        mNotificationManager.notify(String.valueOf(System.currentTimeMillis()),0, notificationBuilder.build());
        //////////////

    }
}