package com.swenauk.bluetoothseekandpost;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.os.PowerManager;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BluetoothBackgroundService extends Service {
    int calledCount;
    private Looper serviceLooper;
    private ServiceHandler serviceHandler;
    private Boolean isRunning;
    private BluetoothAdapter btAdapter;
    private BluetoothManager btManager;
    private HashMap<String, Integer> interaction;
    private List<String> currentScan;
    private int notifcationCount;
    private BluetoothLeScanner btScanner;
    private Boolean isScanning;
    private PowerManager.WakeLock wakeLock;
    private boolean isPaused;
    private boolean canScan;

    //Creating a separate looper and handler for our background service
    private final class ServiceHandler extends Handler {
        public ServiceHandler(Looper looper) {
            super(looper);
        }
        @Override
        public void handleMessage(Message msg) {
            try {
                //Checking if thread is running
                while(isRunning) {
                    //Sleeping the thread for 1 sec so we know approximately how many seconds has it been
                    Thread.sleep(1000);

                    //Checking bluetooth availability.
                    if(canScan) {
                        System.out.println("Can scan");
                        if ((wakeLock != null) && (!wakeLock.isHeld())) {
                            wakeLock.acquire();
                        }

                        //Every 10 seconds, starting from the 5th second of the run, we clear currentScan so we can get how many times we have interacted with this person in one scan.
                        if (calledCount % 10 == 5 && isScanning) {
                            Thread thread = new Thread() {
                                @Override
                                public void run() {
                                    getUrlContent("deneme");
                                }
                            };
                            thread.start();
                            currentScan.clear();
                        }

                        //Starting and stopping the scan every 100 seconds, starting from the 5th second of the run.
                        if (calledCount % 180000 == 5 || isPaused) {
                            try {
                                if (!isScanning) {
                                    startScanning();
                                    isScanning = true;
                                } else {
                                    stopScanning();
                                    isScanning = false;
                                }
                                isPaused = false;
                            } catch (Exception e) {
                                System.out.println(e);
                            }
                        }
                    }

                    //Increase it every second to track time
                    calledCount++;
                    System.out.println(calledCount);

                }
            } catch (Exception e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    //Just a simple notification shower.
    private void showDialog(String s){
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "CoroWarnerChannel")
                .setSmallIcon(R.drawable.ic_android_black_24dp)
                .setContentTitle("CoroWarner")
                .setContentText(s)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            CharSequence name = "CoroWarner";
            String description = "CoroWarner";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel("CoroWarnerChannel", name, importance);
            channel.setDescription(description);
            notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
            notificationManager.notify(notifcationCount, builder.build());
        }else{
            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);
            notificationManager.notify(notifcationCount, builder.build());
        }

        notifcationCount++;
    }

    //Used for post requests to server
    private String getUrlContent(String strUrl){
        try {
            JSONObject all = new JSONObject();
            all.put("affecting", "AhmetUmut");
            JSONArray others = new JSONArray();

            if(!interaction.isEmpty()){
                for(String s : interaction.keySet()){
                    JSONObject one = new JSONObject();
                    one.put("id", s);
                    one.put("count", interaction.get(s));
                    others.put(one);
                }
            }

            all.put("affected", others);
            Log.d("SwenBleScan", all.toString());
            /*

            String urlParameters  = "userID=1&mac=" + all.toString();
            byte[] postData       = urlParameters.getBytes( StandardCharsets.UTF_8);
            int    postDataLength = postData.length;
            URL url = new URL(strUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects( false );
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            connection.setRequestProperty("User-Agent", "PostmanRuntime/7.22.0");
            connection.setRequestProperty("Accept", "text/html");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString( postDataLength ));

            try(DataOutputStream wr = new DataOutputStream( connection.getOutputStream())) {
                wr.write( postData );
            }

            InputStream stream = connection.getInputStream();

            BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
            */
            String line = "";
            /*
            line = reader.readLine();
            System.out.println(line);
            */
            return line;
        }catch (Exception e){
            System.out.println(e);
        }
        return null;
    }

    @Override
    public void onCreate() {
        //We set isRunning true since we will be starting the thread. isScanning is always false at start since it will start at 5th second of the run.
        isRunning = true;
        isScanning = false;
        isPaused = false;

        //We initialize the interaction and currentScan variables.
        //interaction -> keeps count of interaction between this device and other devices.
        //currentScan -> keeps interaction with in 10 seconds intervals.
        interaction = new HashMap<>();
        currentScan = new ArrayList<>();

        //We need unique id for our notifications so keeping track of notification count too.
        notifcationCount = 1;

        //Creating our thread to run on background and giving it his own looper.
        HandlerThread thread = new HandlerThread("ServiceStartArguments", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        serviceLooper = thread.getLooper();
        serviceHandler = new ServiceHandler(serviceLooper);

        //Using wakeLock for location use in lock screen.
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "bsap:wakeywakey");

        //Initializing bluetooth classes that are needed for scan.
        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        btAdapter = btManager.getAdapter();

        //We set canScan according to bluetooth availability.
        canScan = btAdapter == null || btAdapter.isEnabled();

        btScanner = btAdapter.getBluetoothLeScanner();

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                        BluetoothAdapter.ERROR);
                switch (state) {
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        System.out.println("Bluetooth off");
                        showDialog("Bluetoothunuz ve GPSiniz açık değilse corowarner sizi uyaramaz!");
                        isPaused = true;
                        isScanning = false;
                        canScan = false;
                        break;
                    case BluetoothAdapter.STATE_ON:
                        System.out.println("Bluetooth on");
                        canScan = true;
                        break;
                }
            }
        }
    };

    //This is where we start the background service finally.
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(this, "service starting", Toast.LENGTH_SHORT).show();
        Message msg = serviceHandler.obtainMessage();
        msg.arg1 = startId;
        serviceHandler.sendMessage(msg);
        return START_STICKY;
    }


    //What to do when scan finds someone
    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            try {
                //We get data of other device
                Map<ParcelUuid, byte[]> myMap = result.getScanRecord().getServiceData();
                ParcelUuid pUuid = new ParcelUuid(UUID.fromString("c2cad517-865a-4af1-acba-da65ff888cfb"));
                String uniqueId = new String(myMap.get(pUuid) , StandardCharsets.UTF_8);

                //Getting signal strength of the device.
                Integer rssi = result.getRssi();

                //If currentScan doesn't contain this device we put it in currentScan. If it exists in interaction we increase the amount by one, else we create it with initial value of 1
                if(!currentScan.contains(uniqueId)) {
                    if (interaction.containsKey(uniqueId)) {
                        int x = interaction.get(uniqueId);
                        x++;
                        interaction.remove(uniqueId);
                        interaction.put(uniqueId, x);
                    } else {
                        interaction.put(uniqueId, 1);
                    }
                    System.out.println(uniqueId);
                    currentScan.add(uniqueId);

                    //We show device id and strength as notification
                    showDialog(uniqueId + " -> Strength: " + String.valueOf(rssi));
                }
            }catch (Exception ignored){

            }
        }
    };

    public void startScanning() {
        System.out.println("start scanning");

        //Creating an empty filter so scan can run on lock screen.
        final List<ScanFilter> filters = Collections.singletonList(new ScanFilter.Builder().build());
        final ScanSettings settings = new ScanSettings.Builder()
                .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                .build();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(filters, settings, mScanCallback);
            }
        });
    }

    public void stopScanning() {
        System.out.println("stopping scanning");
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(mScanCallback);
            }
        });
    }

    //Not important for our use
    @Override
    public IBinder onBind(Intent intent) {
        // We don't provide binding, so return null
        return null;
    }

    //On destroy we stop is running so it doesn't keep on going for now.
    @Override
    public void onDestroy() {
        isRunning = false;
    }
}
