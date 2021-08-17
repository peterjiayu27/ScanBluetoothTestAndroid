package com.example.bluetoothscan;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelUuid;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    private int devices;

    private HashSet<ScanResult> results = new HashSet<>();
    private HashSet<String> deviceNames = new HashSet<>();

    @SuppressLint("StaticFieldLeak")
    private static TextView resultTextView;

    @SuppressLint("HandlerLeak")
    static Handler scanResultHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            resultTextView.setText("Found" + msg.obj +"device(s).");
        }
    };

    @SuppressLint("HandlerLeak")
    static Handler failedHandler = new Handler() {
        @SuppressLint("SetTextI18n")
        @Override
        public void handleMessage(Message msg) {
            resultTextView.setText("Scan failed. error code: " + msg.obj);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String[] permissions = new String[]{Manifest.permission.BLUETOOTH_ADMIN};
        ActivityCompat.requestPermissions(this, permissions, 1);
        Button button = findViewById(R.id.scanButton);

        checkCoarseLocationPermission();
        checkBluetoothPermission();
        resultTextView = findViewById(R.id.resultTextView);
        button.setOnClickListener(v -> {
            try {
                deviceNames.clear();
                devices = 0;
                Thread.sleep(3000);
                ScanFilter.Builder beaconFilterBuilder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    List<ScanFilter> filters = new ArrayList<>();
                    beaconFilterBuilder = new ScanFilter.Builder();
                    beaconFilterBuilder.setManufacturerData(0x0059, new byte[] {2, 20, -93, -24, -107, -43, 23, -11, 67, 115, -68, -29, -39, -98, -107, 2, -3, 7, 2, 8, -50, 100, -1});
                    ScanFilter beaconFilter = beaconFilterBuilder.build();

                    ParcelUuid uuid = ParcelUuid.fromString("0000180a-0000-1000-8000-00805f9b34fb");
                    ScanFilter.Builder m5000FilterBuilder = new ScanFilter.Builder();
                    m5000FilterBuilder.setServiceUuid(uuid);
                    ScanFilter m5000Filter = m5000FilterBuilder.build();

                    filters.add(beaconFilter);
                    filters.add(m5000Filter);
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                            .build();

                    ScanCallback scanCallback = new ScanCallback() {

                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            results.add(result);
                            String deviceName = result.getDevice().getName() == null ? "Beacon" + result.getDevice().getAddress() + "\n" :result.getDevice().getName() + "\n";
                            deviceNames.add(deviceName);
                            String allNames = "";
                            for(String i : deviceNames){
                                allNames += i;
                            }
                            String finalAllNames = allNames;
                            runOnUiThread(() -> resultTextView.setText(finalAllNames));
                            super.onScanResult(callbackType, result);

                        }

                        @Override
                        public void onBatchScanResults(List<ScanResult> results) {
                            //devices += results.size();
                            //runOnUiThread(() -> resultTextView.setText(getString(R.string.found_devices, devices)));
                            super.onBatchScanResults(results);
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            runOnUiThread(() -> resultTextView.setText(getString(R.string.scan_failed, errorCode)));
                            super.onScanFailed(errorCode);
                        }
                    };

                    bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private boolean checkCoarseLocationPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
            return false;
        }else{
            return true;
        }
    }

    private boolean checkBluetoothPermission(){
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH)!= PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.BLUETOOTH}, 11);
            return false;
        }else{
            return true;
        }
    }

    @Override
    protected void onStart() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION)){
                // You can show your dialog message here but instead I am
                // showing the grant permission dialog box
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        10);
            }
            else{
                //Requesting permission
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION },
                        10);
            }
        }
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}