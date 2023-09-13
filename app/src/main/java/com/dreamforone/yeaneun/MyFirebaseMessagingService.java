package com.dreamforone.yeaneun;


import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.UUID;

import util.Common;

public class MyFirebaseMessagingService extends FirebaseMessagingService {

    private static final String TAG = "FCM";
    int mLastId = 0;
    ArrayList<Integer> mActiveIdList = new ArrayList<>();
    NotificationManager mManager;

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        //token을 서버로 전송
    }

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
//        super.onMessageReceived(remoteMessage);
        sendNotification(remoteMessage);
        //수신한 메시지를 처리
    }

    private void createNotificationId() {
        int id = ++mLastId;
        mActiveIdList.add(id);
    }

    private void sendNotification(RemoteMessage remoteMessage) {

        String title = remoteMessage.getData().get("title");
        String message = remoteMessage.getData().get("message");
        String goUrl = remoteMessage.getData().get("goUrl");
        String back_url = remoteMessage.getData().get("back_url");
        String chid = remoteMessage.getData().get("chid");
        Log.d("remoteMessage",""+chid);

        Common.savePref(getApplicationContext(),"goUrl",goUrl);
        Common.savePref(getApplicationContext(),"back_url",back_url);

        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        Bundle bundle = new Bundle();
        bundle.putString("goUrl", goUrl);
        bundle.putString("back_url", back_url);
        intent.putExtras(bundle);

        //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        //PendingIntent pendingIntent = PendingIntent.getActivity(this,0, intent , PendingIntent.FLAG_ONE_SHOT);

        // MUTABLE = 변경이 가능하다.
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), UUID.randomUUID().hashCode() , intent,
                PendingIntent.FLAG_MUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        final String CHANNEL_ID = chid;
        mManager = (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        mManager.cancel(mLastId);
        createNotificationId();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            final String CHANNEL_NAME = CHANNEL_ID;
            final String CHANNEL_DESCRIPTION = "Yeaneun_alarm";
            final int importance = NotificationManager.IMPORTANCE_HIGH;

            // add in API level 26
            NotificationChannel mChannel = new NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance);
            mChannel.setDescription(CHANNEL_DESCRIPTION);
            mChannel.enableLights(true);                                            // 빛 조절
            mChannel.enableVibration(true);                                         // 소리 설정
            mChannel.setVibrationPattern(new long[]{100, 200, 100, 200});
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            mManager.createNotificationChannel(mChannel);                           // 채널 생성 등록
        }

        Bitmap mLargeIconForNoti = BitmapFactory.decodeResource(getResources(),R.mipmap.ic_launcher);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID);
        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setLargeIcon(mLargeIconForNoti);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_ALL);
        builder.setWhen(System.currentTimeMillis());
        builder.setContentTitle(title);
        builder.setContentText(message);
        builder.setSound(defaultSoundUri);
        builder.setContentIntent(pendingIntent);
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            builder.setContentTitle(title);
            builder.setVibrate(new long[]{500, 500});
        }
        mManager.notify(mLastId, builder.build());
    }
}
