package com.swenauk.bluetooth;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.swenauk.bluetoothseekandpost.SwenBleScan;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //We create the SwenBleScan object at the start of the main activity.
        final SwenBleScan swenBleScan = new SwenBleScan(this);

        //We do permission checks. After permission checks completed BLE Scan and Advertise start automatically
        swenBleScan.checkPermissions();

        Button button1 = findViewById(R.id.butStop);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //BLE Scan Stopper
                swenBleScan.stopBleService();
            }
        });

    }

}
