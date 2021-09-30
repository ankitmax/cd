package com.example.bleswitch;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class MainActivity extends AppCompatActivity implements MyRecyclerViewAdapter.ItemClickListener {

    MyRecyclerViewAdapter scanAdapter;
    private Handler mHandler;
    private BluetoothAdapter mBluetoothAdapter;
    private static final long SCAN_PERIOD = 2500;
    private BluetoothLeScanner mBLEScanner;
    private ScanSettings settings;
    private List<ScanFilter> filters;
    private ScanSettings BLEScanSetting;
    private static int REQUEST_ENABLE_BT = 2;
    private static int REQUEST_ENABLE_LOCATION = 2;
    Button scanButton;
    TextView scanText;
    List<String> bleDeviceList;
    RecyclerView recyclerView;
    HashMap<String, BluetoothDevice> mScanResults;
    private Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mHandler = new Handler();

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.ble_not_supported, Toast.LENGTH_SHORT).show();
            finish();
        }

        final BluetoothManager bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            Toast.makeText(this, R.string.error_bluetooth_not_supported, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        scanText = (TextView) findViewById(R.id.scanText);
        scanText.setVisibility(View.INVISIBLE);
        scanButton = (Button) findViewById(R.id.button_scan);
        scanButton.setVisibility(View.VISIBLE);
        scanButton.setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onClick(View view) {
                scanBLE(true);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onPause() {
        super.onPause();
        if (mBluetoothAdapter != null && mBluetoothAdapter.isEnabled()) {
            scanBLE(false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private void enableBT() {
        if (!mBluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != Activity.RESULT_OK) {
                //Bluetooth not enabled.
                enableBT();
                //finish();
                //return;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public void scanBLE(boolean sFlag) {
        if(sFlag) {
            Log.i("SCAN", "here");
            if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                enableBT();
            }
            else if (!(checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED)) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_ENABLE_LOCATION);
                return;
            }
            if(bleDeviceList != null) {
                bleDeviceList.clear();
                displayBleDeviceList();
                scanAdapter.notifyDataSetChanged();
            }

            bleDeviceList = Collections.synchronizedList(new ArrayList<>());
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    mBLEScanner.stopScan(mScanCallback);
                    Log.i("[SCAN]", "Scanning stopped.");
                    scanText.setText("");
                    scanText.setVisibility(View.INVISIBLE);
                    scanButton.setVisibility(View.VISIBLE);
                }
            }, SCAN_PERIOD);
            mScanResults = new HashMap<>();
            mBLEScanner = mBluetoothAdapter.getBluetoothLeScanner();
            BLEScanSetting = new ScanSettings.Builder()
                    .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
                    .build();

            Log.i("[SCAN]", "Scanning for BLE devices...");
            scanText.setText("\n        Scanning...");
            scanText.setVisibility(View.VISIBLE);
            mBLEScanner.startScan(null, BLEScanSetting, mScanCallback);
        }
        else {
            mBLEScanner.stopScan(mScanCallback);
            scanText.setText("");
            scanText.setVisibility(View.INVISIBLE);
            Log.i("[SCAN]", "Scanning disabled.");
        }
    }

    private ScanCallback mScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.i("[ScanCallBack]", "Result: " + result.getDevice().getName());
            mScanResults.put(result.getDevice().getAddress(), result.getDevice());
            if(checkMacAddress(result)) {
                addDevice(result);
            }
            displayBleDeviceList();
            scanAdapter.notifyItemInserted(bleDeviceList.size() - 1);
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            for (ScanResult sr : results) {
                Log.i("## ScanResult - Results", sr.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            Log.e("Scan Failed", "Error Code: " + errorCode);
        }
    };

    private void displayBleDeviceList() {
        recyclerView = (RecyclerView) findViewById(R.id.recyclerID);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        scanAdapter = new MyRecyclerViewAdapter(this, bleDeviceList);
        scanAdapter.setClickListener(this);
        recyclerView.setAdapter(scanAdapter);
    }

    public void addDevice(ScanResult result)
    {
        if(bleDeviceList.size() != 0)
        {
            Iterator<String> it = bleDeviceList.iterator();
            int i = 0;
            while (it.hasNext())
            {
                List<String> oldBle = Collections.singletonList(it.next());
                String oldBleData = String.valueOf(oldBle);
                String[] parts = oldBleData.split("\n");
                String ble_address = parts[1];
                String curr_Address = String.valueOf(result.getDevice().getAddress());
                boolean bool = (ble_address.equals(curr_Address));
                if (bool)
                {
                    it.remove();
                    scanAdapter.notifyItemRemoved(i);
                }
                i++;
            }
        }
        bleDeviceList.add(result.getDevice().getName() + "\n" + result.getDevice().getAddress());
    }

    public boolean checkMacAddress(ScanResult result)
    {
        if(bleDeviceList.size() != 0)
        {
            Iterator<String> it = bleDeviceList.iterator();
            while (it.hasNext())
            {
                String ble = it.next();
                String[] parts = ble.split("\n");
                String ble_address = parts[1];

                boolean bool = (ble_address.equals(String.valueOf(result.getDevice().getAddress())));
                if (bool)
                {
                    return false;
                }
            }
        }
        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onItemClick(View view, int position) {
        //Toast.makeText(this, "You clicked " + scanAdapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        //scanBLE(false);
        String data = scanAdapter.getItem(position);
        String[] parts = data.split("\n");
        String ble_address = parts[1];
        BluetoothDevice device = mScanResults.get(ble_address);
        Intent intent = new Intent(this, BleDeviceActivity.class);
        intent.putExtra("device", device);
        startActivity(intent);
    }
}