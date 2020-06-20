package com.swenauk.bluetoothseekandpost;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.ParcelUuid;
import android.util.Base64;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SwenBleScan {
    private Context context;

    public SwenBleScan(Context context) {
        this.context = context;
    }

    //Checking permissions before starting ble scan
    public void checkPermissions(){
        Intent intent = new Intent(context, PermissionChecker.class);
        this.context.startActivity(intent);

        //We check continuously for permission and if it is granted we show the main activity. Not working for now.
        /*while(true)
        {
            if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                System.out.println("No permission yet");
            }else{
                break;
            }
        }*/

        //If Permission is granted we start BLE Service
        startBleService();
    }

    public void startBleService(){
        //First we start advertising
        startAdvertise();

        //Then we start the service
        Intent intent = new Intent(context, BluetoothBackgroundService.class);
        context.startService(intent);
    }

    private void startAdvertise(){
        BluetoothLeAdvertiser advertiser = BluetoothAdapter.getDefaultAdapter().getBluetoothLeAdvertiser();

        //AdvertiseSettings are set here. AdvertiseMode and TxPowerLevel info can be found on: https://medium.com/@martijn.van.welie/making-android-ble-work-part-1-a736dcd53b02
        //Info is under the title -> Defining your ScanSettings
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode( AdvertiseSettings.ADVERTISE_MODE_BALANCED )
                .setTxPowerLevel( AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM )
                .setConnectable( false )
                .build();

        //We give our application a UUID
        ParcelUuid pUuid = new ParcelUuid(UUID.fromString("c2cad517-865a-4af1-acba-da65ff888cfb"));

        //This part creates a random string(which is unique for every device and doesn't change)
        Random random = ThreadLocalRandom.current();
        byte[] r = new byte[8];
        random.nextBytes(r);
        String s = Base64.encodeToString(r, Base64.DEFAULT);

        //We show device's generated id as notification
        showDialog("Your id is " + s);

        //We put UUID of our app and device's generated id to be advertised
        AdvertiseData data = new AdvertiseData.Builder()
                .addServiceData(pUuid, s.getBytes())
                .build();

        System.out.println(s);

        //This part is just so we can see fail and start of advertise
        AdvertiseCallback advertisingCallback = new AdvertiseCallback() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                super.onStartSuccess(settingsInEffect);
                System.out.println("Advert Started");
            }

            @Override
            public void onStartFailure(int errorCode) {
                System.out.println( "Advertising onStartFailure: " + errorCode );
                super.onStartFailure(errorCode);
            }
        };

        //We start advertising with the settings, data and callback function we created above
        advertiser.startAdvertising(settings, data, advertisingCallback);
    }

    //This is just a simple notification builder so we can see our results without the need of console.log
    private void showDialog(String s){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "Deneme")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("Deneme Title")
                .setContentText(s)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "Deneme Name";
            String description = "Deneme Description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("Deneme", name, importance);
            channel.setDescription(description);
            notificationManager = context.getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(0, builder.build());
        }else{
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(0, builder.build());
        }
    }

    public void stopBleService(){
        //We stop the service
        Intent intent = new Intent(context, BluetoothBackgroundService.class);
        this.context.stopService(intent);
    }
}