package com.swenauk.bluetoothseekandpost;

import android.Manifest;
import android.app.Activity;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.ParcelUuid;
import android.util.Base64;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.net.URLEncoder;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class SwenBleScan {
    private Context context;
    private boolean checkBlue;
    private String advertiseID;
    private BluetoothAdapter btAdapter;
    private BluetoothManager btManager;

    private final static int REQUEST_ENABLE_BT = 1;

    public SwenBleScan(Context context, boolean checkBlue, String advertiseID) {
        //To be used for, starting background activity and other context functions.
        this.context = context;

        //If bluetooth enabling will be used.
        this.checkBlue = checkBlue;

        //If Service data is user provided. Can be 13 bytes at most. For example: ahmetumutkurn.
        //Be careful with special chars, since they take up more space when utf-8 encoded. Recommended string length is 10.
        this.advertiseID = advertiseID;

        //We init needed objects to check if bluetooth is enabled
        try {
            btManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
            btAdapter = btManager.getAdapter();
        }catch (Exception ignored){

        }
    }

    public SwenBleScan(Context context, boolean checkBlue) {
        this.context = context;
        this.checkBlue = checkBlue;
    }

    public SwenBleScan(Context context) {
        this.context = context;
    }

    //Checking permissions before starting ble scan
    public void checkPermissions(){
        final Intent intent = new Intent(context, PermissionChecker.class);
        this.context.startActivity(intent);

        //We check continuously for permission and if it is granted we show the main activity.
        Thread r = new Thread() {
            public void run() {
                while(true)
                {
                    System.out.println("Deneme");
                    if(context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                            && context.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED){
                        System.out.println("No permission yet");
                    }else{
                        if(checkBlue){
                            bluetoothCheck();
                        }

                        while (btAdapter != null && !btAdapter.isEnabled()){
                            try {
                                Thread.sleep(1000);
                            }catch (Exception ignored){

                            }
                        }

                        Handler mainHandler = new Handler(Looper.getMainLooper());
                        Runnable myRunnable = new Runnable() {
                            @Override
                            public void run() {
                                startBleService();
                            }
                        };
                        mainHandler.post(myRunnable);
                        break;
                    }
                }
            }
        };

        r.start();
    }

    private boolean bluetoothCheck(){
        //If bluetooth is not enabled we show a alert so user can be notified and enable the bluetooth
        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                if(context instanceof Activity) {
                    Activity activity = (Activity) context;
                    activity.startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
                    return true;
                }
            }catch (Exception ignored){
                return false;
            }
        }

        return true;
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
        String s = "";
        if(advertiseID == null){
            Random random = ThreadLocalRandom.current();
            byte[] r = new byte[8];
            random.nextBytes(r);
            s = Base64.encodeToString(r, Base64.DEFAULT);
        }else{
            try{
                s = URLEncoder.encode(advertiseID, "utf-8");
                //System.out.println(s);
            }catch (Exception e){
                s = "Failed";
            }
        }


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
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "CoroWarnerChannel")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("CoroWarner")
                .setContentText(s)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "CoroWarner";
            String description = "CoroWarner";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CoroWarnerChannel", name, importance);
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
