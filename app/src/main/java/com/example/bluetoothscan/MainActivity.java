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
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private BluetoothAdapter bluetoothAdapter;

    private int devices;

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

        resultTextView = findViewById(R.id.resultTextView);
        button.setOnClickListener(v -> {
            try {
                devices = 0;
                Thread.sleep(3000);
                ScanFilter.Builder builder = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                    List<ScanFilter> filters = new ArrayList<>();
                    builder = new ScanFilter.Builder();
                    builder.setManufacturerData(0x004c, new byte[] {});
                    ScanFilter filter = builder.build();

                    filters.add(filter);
                    bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
                    ScanSettings settings = new ScanSettings.Builder()
                            .setScanMode( ScanSettings.SCAN_MODE_LOW_LATENCY )
                            .build();
                    Message message = new Message();

                    Message failedMessage = new Message();

                    ScanCallback scanCallback = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            super.onScanResult(callbackType, result);
                            devices += 1;
                            message.obj = devices;
                            scanResultHandler.sendMessage(message);
                        }

                        @Override
                        public void onScanFailed(int errorCode) {
                            super.onScanFailed(errorCode);
                            failedMessage.obj = errorCode;
                            failedHandler.sendMessage(failedMessage);
                        }
                    };

                    bluetoothAdapter.getBluetoothLeScanner().startScan(filters, settings, scanCallback);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }
}