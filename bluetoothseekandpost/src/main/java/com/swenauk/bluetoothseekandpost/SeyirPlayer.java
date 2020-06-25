package com.swenauk.bluetoothseekandpost;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Binder;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;


public class SeyirPlayer extends Service {
/*
    private IBinder mBinder = new MyBinder();
    private SimpleExoPlayer player;

    public class MyBinder extends Binder {
        public SeyirPlayer getService() {
            return SeyirPlayer.this;
        }

        public void setPlayer(SimpleExoPlayer player1){
            player = player1;
            playPause();
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();

        String channelID = "";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            channelID = getChannelID();
        }

        //This is the intent of PendingIntent
        Intent intentAction = new Intent(this, ActionReceiver.class);

        //This is optional if you have more than one buttons and want to differentiate between two
        intentAction.putExtra("action","ppButton");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(this,1, intentAction, PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new NotificationCompat.Builder(this, channelID).setOngoing(true)
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setPriority(NotificationManager.IMPORTANCE_HIGH)
                .setCategory(Notification.CATEGORY_SERVICE)
                .addAction(R.drawable.ic_play_arrow_black_24dp, "PlayPause", pendingIntent)
                .build();
        startForeground(44, notification);

    }

    public class ActionReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            String action=intent.getStringExtra("action");
            if(action.equals("ppButton")){
                performAction1();
            }
            else if(action.equals("action2")){
                performAction2();

            }
            //This is used to close the notification tray
            Intent it = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(it);
        }

        public void performAction1(){
            player.setPlayWhenReady(false);
        }

        public void performAction2(){

        }

    }

    private void playPause(){
        showDialog("Player paused");
    }


    @RequiresApi(api = Build.VERSION_CODES.O)
    private String getChannelID(){
        NotificationChannel chan = new NotificationChannel("seyService", "seyBackService", NotificationManager.IMPORTANCE_HIGH);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.createNotificationChannel(chan);
        return "seyService";
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //showDialog("Service Has Started");

        return START_NOT_STICKY;
    }

    //Just a simple notification shower.
    private void showDialog(String s){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "Deneme")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("Deneme Title")
                .setContentText(s)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "Deneme Name";
            String description = "Deneme Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Deneme", name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(1, builder.build());
        }else{
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(1, builder.build());
        }

    }
*/
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        //return mBinder;
        return null;
    }
}
