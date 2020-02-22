package com.ahright.betaversion;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import java.util.Calendar;

import static android.content.ContentValues.TAG;

public class UndeadService extends Service {
    LocationManager mLocationManager;
    LocationListener locationListener;

    public UndeadService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    public static Intent serviceIntent = null;
    // ...

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        serviceIntent = intent;

        initializeNotification();

        //
        // Todo.
        //
        StartLocation();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {

            }
        }, 3000);
        return START_STICKY;
    }

    public void initializeNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "30");
        builder.setSmallIcon(R.drawable.blackcircle);

        NotificationCompat.BigTextStyle style = new NotificationCompat.BigTextStyle();
        style.setBigContentTitle(null);
        style.setSummaryText("아맞다 앱이 실행 중입니다.");

        builder.setContentText(null);
        builder.setContentTitle(null);
        builder.setOngoing(true);

        builder.setStyle(style);
        builder.setWhen(0);
        builder.setShowWhen(false);

        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
        builder.setContentIntent(pendingIntent);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel("30", "undead_service", NotificationManager.IMPORTANCE_NONE));
        }

        Notification notification = builder.build();
        startForeground(30, notification);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 800,intent,0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

        final Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND, 3);
        Intent intent = new Intent(this, AlarmReceiver.class);
        PendingIntent sender = PendingIntent.getBroadcast(this, 800,intent,0);

        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), sender);
    }

    private void StartLocation(){
        mLocationManager=(LocationManager) getSystemService(Context.LOCATION_SERVICE);


        if(mLocationManager != null){

            boolean isGPSEnable = mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
            boolean isNetworkEnable = mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);

            Log.e(TAG, "isGPSEnable : "  + isGPSEnable);
            Log.e(TAG, "isNetworkEnable : " + isNetworkEnable);

            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    double lat = location.getLatitude();
                    double lng = location.getLongitude();

                    Log.e(TAG, "위도 : " + lat + " 경도 : " + lng);
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {
                    Log.e(TAG, "onStatusChanged : ");
                }

                @Override
                public void onProviderEnabled(String provider) {
                    Log.e(TAG, "onProviderEnabled : ");
                }

                @Override
                public void onProviderDisabled(String provider) {
                    Log.e(TAG, "onProviderDisabled : ");
                }
            };

            //아래 코드를 실행시키기 위해서 임의적으로 한번 더 권한 체크를 하여야함. 그렇지 않으면 error
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            if(isGPSEnable && isNetworkEnable){

                //시스템 위치마저 허용되어있을경우
                mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10, 0, locationListener);
                mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10, 0, locationListener);

            }else{

                //시스템 위치 셋팅으로 넘겨야함
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
            }
        }
        else{

        }
    }




}
