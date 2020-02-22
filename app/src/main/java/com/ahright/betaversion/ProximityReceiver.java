package com.ahright.betaversion;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Build;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.NOTIFICATION_SERVICE;


public class ProximityReceiver extends BroadcastReceiver {
    String TAG = "ProximityReceiver";
    // 근접 이벤트
    NotificationManager manager;
    LocationManager lm;
    // 알람
    private static String CHANNEL_ID = "channel1";
    private static String CHANNEL_NAME = "channel1";
    // DB
    Database database3;
    ArrayList<Integer> check;
    ArrayList<String> todo;
    int id;

    @Override
    public void onReceive(Context context, Intent intent) {

        // DB open
        database3 = Database.getInstance(context);
        boolean isOpen = database3.open();
        if (isOpen) {
            Log.d(TAG, " database is open.");
        } else {
            Log.d(TAG, "database is not open.");
        }
        String alias = intent.getStringExtra("alias");
        String RenameAlias = alias.replace("z", " ");

        // 할일이 모두 체크 되었을 경우 알람 울리지 않기
        todo = database3.selectAllTodo(alias);
        check = database3.selectAllChecked(alias);
        boolean allChecked = true;
        for (int i = 0; i < check.size(); i++) {
            if (check.get(i) == 0)
                allChecked = false;
        }

        // 만료된 날짜거나, 아직 시작하지 않은 날짜일 경우 울리지 않기
        boolean isOver = false;
        boolean isNotYet = false;
        if (alias.equals("집")) Log.e(TAG, "집이므로 날짜 확인하지 않습니다.");
        else {
            String startDate = database3.selectSpecificArea(alias).getStartDate();
            String endDate = database3.selectSpecificArea(alias).getEndDate();
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                Date cmpStartDate = sdf.parse(startDate);
                Date cmpEndDate = sdf.parse(endDate);
                long now = System.currentTimeMillis();
                String curDatStr = sdf.format(new Date(now));
                Date curDate = sdf.parse(curDatStr);
                Log.e(TAG, startDate + " ~ " + cmpEndDate + " VS  " + curDate);
                if (cmpEndDate.compareTo(curDate) < 0) {
                    isOver = true;
                }
                if (cmpStartDate.compareTo(curDate) > 0) {
                    isNotYet = true;
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        // 근접하거나 벗어날 시 알람 주기
        boolean entering = intent.getBooleanExtra(LocationManager.KEY_PROXIMITY_ENTERING, false);
        // ID 구분해서 알람 여러개 주기
        id = intent.getIntExtra("id", 0);

        if (alias != null) {
            if ((allChecked != true && isOver != true) && isNotYet != true) {
                if (entering) {
                    showNoti1(context, RenameAlias, 0, todo, check);
                } else {
                    showNoti1(context, RenameAlias, 1, todo, check);
                }
            }
        }
    }

    // 푸시알람
    public void showNoti1(Context context, String alias, int code, ArrayList<String> todo, ArrayList<Integer> check) {

        manager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        NotificationCompat.Builder builder = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(new NotificationChannel(
                    CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT
            ));

            builder = new NotificationCompat.Builder(context, CHANNEL_ID);
        } else {
            builder = new NotificationCompat.Builder(context);
        }

        Intent intent = new Intent(context, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        PendingIntent pendingIntent = PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        if (code == 0) {
            builder.setContentTitle(alias + "에 도착했습니다. 할 일을 잊지마세요 :)");
        } else if (code == 1) {
            builder.setContentTitle(alias + "에서 벗어났습니다. 할 일을 잊지마세요 :)");
        }

        NotificationCompat.InboxStyle inbox = new NotificationCompat.InboxStyle();

        for (int i = 0; i < todo.size(); i++) {
            if (check.get(i) == 0)
                inbox.addLine("V  " + todo.get(i));
        }

        builder.setSmallIcon(R.drawable.circle);
        builder.setAutoCancel(true);
        builder.setSubText("아래로 당겨 할 일을 확인하세요.");
        builder.setOnlyAlertOnce(true);
        builder.setContentIntent(pendingIntent);
        builder.setSound(android.media.RingtoneManager.getDefaultUri(android.media.RingtoneManager.TYPE_NOTIFICATION));
        builder.setDefaults(Notification.DEFAULT_SOUND | Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);
        builder.setStyle(inbox);
        Notification noti = builder.build();

        manager.notify(id, noti);

    }

}