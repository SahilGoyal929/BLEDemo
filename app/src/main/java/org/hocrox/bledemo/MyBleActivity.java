package org.hocrox.bledemo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

public class MyBleActivity extends AppCompatActivity {

    private static final long SCAN_PERIOD = 3000;

    BluetoothAdapter bluetoothAdapter;
    BluetoothLeScanner bluetoothLeScanner;
    Button mSendMessage;
    ArrayList<ScanFilter> arrayList = new ArrayList<ScanFilter>();
    BluetoothGatt bluetoothGatt = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_ble);
        mSendMessage = (Button) findViewById(R.id.btnSend);
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();
        mSendMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                try {
                    BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGatt.getService(TimeProfile.TIME_SERVICE).getCharacteristic(TimeProfile.CURRENT_TIME);
                    bluetoothGattCharacteristic.setValue("hey send message".getBytes("UTF-8"));
                    bluetoothGatt.writeCharacteristic(bluetoothGattCharacteristic);
                    Log.e("on send message", "messsage send ");

                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                    Log.e("on send message", "" + e.getMessage());
                }

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (bluetoothAdapter == null || !bluetoothAdapter.isEnabled()) {

            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivity(intent);
            finish();

        }

        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {

            Toast.makeText(MyBleActivity.this, "No Ble Supported", Toast.LENGTH_LONG).show();
            finish();
        }
        startScan();

    }

    private void startScan() {


        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(TimeProfile.TIME_SERVICE)).build();
        Log.e("testing scan result", "" + scanFilter.getServiceUuid());
        arrayList.add(scanFilter);
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY).setReportDelay(1000).build();
        bluetoothLeScanner.startScan(arrayList, scanSettings, scanCallback);
        //bluetoothAdapter.startLeScan(mLeScanCallback);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                bluetoothLeScanner.stopScan(scanCallback);
            }
        }, SCAN_PERIOD);

    }

    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("sdsdwsdwedwedwed", "" + device + ">>" + device.getName() + ">>>");

                        }
                    });
                }
            };
    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            Log.e("tesdsd", "called");
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e("tesdsd", "batch called");


            for (int i = 0; i < results.size(); i++) {


                Log.e("testing ", "" + results.size() + ">>>" + results.get(i));
                scanResult(results.get(i));
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);

            Log.e("eerror in scan callback", "" + errorCode);
        }
    };

    public void scanResult(ScanResult scanResult) {

        BluetoothDevice bluetoothDevice = scanResult.getDevice();
        Log.e("New Ble DEvice", ">>" + scanResult.getScanRecord().getDeviceName());
//        bluetoothGatt = bluetoothDevice.connectGatt(MyBleActivity.this, true,new BluetoothLeService().bluetoothGattCallback);
        bluetoothGatt = bluetoothDevice.connectGatt(MyBleActivity.this, true, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                super.onConnectionStateChange(gatt, status, newState);
                Log.e("On Connection State", ">>" + newState);

                if (newState == BluetoothProfile.STATE_CONNECTED) {

                    gatt.discoverServices();

                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                super.onServicesDiscovered(gatt, status);
                Log.e("On Service Discovered", ">>" + status);
                for (BluetoothGattService bluetoothGatt1 : gatt.getServices()) {

                    Log.e("Testing On", "" + bluetoothGatt1.getUuid());
                    if (bluetoothGatt1.getUuid() == TimeProfile.CURRENT_TIME) {


                        gatt.readCharacteristic(bluetoothGatt1.getCharacteristic(TimeProfile.CURRENT_TIME));

                    }
                }


            }

            @Override
            public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicRead(gatt, characteristic, status);
                Log.e("On Service read", ">>" + status);


                if (TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())) {


                    byte data[] = characteristic.getValue();

                    String value=new String(data);

                    Log.e("On Service read", ">>" + value);
                }
                gatt.setCharacteristicNotification(characteristic, true);

            }

            @Override
            public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
                super.onCharacteristicWrite(gatt, characteristic, status);
                Log.e("On Service write", ">>" + status);

                if (TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())) {

                    try {
                        Log.e("On Service read", ">>" + characteristic.setValue("heyy Server".getBytes("UTF-8")));
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                gatt.setCharacteristicNotification(characteristic, true);
            }

            @Override
            public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
                super.onCharacteristicChanged(gatt, characteristic);
                Log.e("On Service changed", ">>");

                try {
                    byte data[] = characteristic.getValue();

                    String value=new String(data);

                    Log.e("On Service changed", ">>" + value);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

            @Override
            public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorRead(gatt, descriptor, status);
                Log.e("On Service read", ">>" + status);


            }

            @Override
            public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
                super.onDescriptorWrite(gatt, descriptor, status);
                Log.e("On Service write", ">>" + status);

            }
        });
        // bluetoothLeScanner.stopScan(scanCallback);


    }

    @Override
    protected void onPause() {
        super.onPause();

    }

    private void stopScan() {

    }
}
