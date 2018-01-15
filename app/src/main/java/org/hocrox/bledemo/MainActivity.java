package org.hocrox.bledemo;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final int REQUEST_ENABLE_BT = 1000;
    private static final long SCAN_PERIOD = 3000;
    private static final int STORAGE_PERMISSION_CODE = 1010;
    private BluetoothAdapter mBluetoothAdapter;
    private boolean mScanning;
    private Handler mHandler;
    BroadcastReceiver mGattUpdateReceiver;
    HashMap<Integer, BluetoothDevice> integerDeviceHashMap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        integerDeviceHashMap = new HashMap<>();
        if (Build.VERSION.SDK_INT > 23) {

            if (isReadStorageAllowed()) {

                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    Toast.makeText(this, "Ble Not Supported", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    final BluetoothManager bluetoothManager =
                            (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                    mBluetoothAdapter = bluetoothManager.getAdapter();
                    if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                        startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                    } else {
                        Toast.makeText(this, "Ble Supported", Toast.LENGTH_SHORT).show();
                        scanLeDevice(true);
                    }
                }

            }
            else{

                Log.e("testing Permissions","Permission Required");
            }

        } else {

            if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                Toast.makeText(this, "Ble Not Supported", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                final BluetoothManager bluetoothManager =
                        (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
                mBluetoothAdapter = bluetoothManager.getAdapter();
                if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                } else {
                    Toast.makeText(this, "Ble Supported", Toast.LENGTH_SHORT).show();
                    scanLeDevice(true);
                }
            }

        }
 /*       mGattUpdateReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                final String action = intent.getAction();

                if (BluetoothLeService.ACTION_GATT_CONNECTED.equals(action)) {
                    mConnected = true;
                    Log.e("upladte", "connected");
                    ;
                    invalidateOptionsMenu();
                } else if (BluetoothLeService.ACTION_GATT_DISCONNECTED.equals(action)) {
                    mConnected = false;
                    Log.e("upladte", "disconnected");
                    ;
                    invalidateOptionsMenu();

                } else if (BluetoothLeService.
                        ACTION_GATT_SERVICES_DISCOVERED.equals(action)) {
                    // Show all the supported services and characteristics on the
                    // user interface.
                    //   displayGattServices(mBluetoothLeService.getSupportedGattServices());
                    Log.e("data", "discrovvev");

                } else if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                    //  displayData(intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                    Log.e("data", "" + intent.getStringExtra(BluetoothLeService.EXTRA_DATA));
                }
            }
        };*/

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {

            case REQUEST_ENABLE_BT:


                if (resultCode == RESULT_OK) {

                    scanLeDevice(true);

                } else {

                    Log.e("Testingg", "" + resultCode);
                }

                break;

        }

    }

    private void scanLeDevice(final boolean enable) {

        // Stops scanning after a pre-defined scan period.

        ScanFilter scanFilter = new ScanFilter.Builder().setServiceUuid(new ParcelUuid(TimeProfile.CLIENT_CONFIG)).build();

        ArrayList<ScanFilter> arrayList = new ArrayList<ScanFilter>();
        arrayList.add(scanFilter);
        ScanSettings scanSettings = new ScanSettings.Builder().setScanMode(ScanSettings.SCAN_MODE_BALANCED).build();

        mBluetoothAdapter.getBluetoothLeScanner().startScan(scanCallback);

          new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mScanning = false;
                    mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);
                }
            }, SCAN_PERIOD);


    }

    ScanCallback scanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
             Log.e("scan callback connected",""+result.getDevice().getName());
               scanResult(result);

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
            Log.e("scan batc connected",""+results.size());

        }

        @Override
        public void onScanFailed(int errorCode) {

            super.onScanFailed(errorCode);
            Log.e("scan errrorr",""+errorCode);

        }
    };
    BluetoothGatt bluetoothGatt;

    public void scanResult(ScanResult scanResult) {

        BluetoothDevice bluetoothDevice = scanResult.getDevice();
        Log.e("New Ble DEvice", "" + bluetoothDevice.getName());
        integerDeviceHashMap.put(bluetoothDevice.hashCode(), bluetoothDevice);

            bluetoothGatt = bluetoothDevice.connectGatt(MainActivity.this, true, new BluetoothLeService().bluetoothGattCallback);
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(scanCallback);


    }

  /*  BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e("Connected State Changed", "" + newState);

            bluetoothGatt.discoverServices();

        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e("On Service Discovered", "Service Discovered");
            for (BluetoothGattService bluetoothGattService : bluetoothGatt.getServices()) {

                Log.e("servicess", "" + bluetoothGattService.getUuid());

                if (TimeProfile.TIME_SERVICE.equals(bluetoothGattService.getUuid())) {


                    bluetoothGatt.readCharacteristic(bluetoothGattService.getCharacteristic(TimeProfile.CURRENT_TIME));
                }

            }


        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            Log.e("On Characteristic Read", "Readingggggg");

            int charValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 1);

            if (TimeProfile.CURRENT_TIME.equals(characteristic.getUuid())) {

            Log.e("chanrachte read value",""+String.valueOf(charValue));

            }else{

                Log.e("read value else",""+String.valueOf(charValue));

            }
            gatt.setCharacteristicNotification(characteristic,true);

        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);

            BluetoothGattCharacteristic bluetoothGattCharacteristic=mBluetoothGatt.getService(TimeProfile.CLIENT_CONFIG).getCharacteristic(TimeProfile.CURRENT_TIME);
            bluetoothGattCharacteristic.setValue("sahil".getBytes());


            gatt.writeCharacteristic(bluetoothGattCharacteristic);


        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            int charValue = characteristic.getIntValue(BluetoothGattCharacteristic.FORMAT_UINT32, 1);
             Log.e("on Characte changed",""+String.valueOf(charValue));
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

        }


    };*/

    private BluetoothGatt mBluetoothGatt;
    // Device scan callback.
    private BluetoothAdapter.LeScanCallback mLeScanCallback =
            new BluetoothAdapter.LeScanCallback() {
                @Override
                public void onLeScan(final BluetoothDevice device, int rssi,
                                     final byte[] scanRecord) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Log.e("sdsdwsdwedwedwed", "" + device + ">>" + device.getName() + ">>>");
                            BluetoothLeService.mBluetoothGatt = device.connectGatt(MainActivity.this, true, new BluetoothLeService().bluetoothGattCallback);

                        }
                    });
                }
            };


    private boolean isReadStorageAllowed() {
        //Getting the permission status
        int result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);

        //If permission is granted returning true
        if (result == PackageManager.PERMISSION_GRANTED)

            return true;
        else
            requestStoragePermission();

        //If permission is not granted returning false
        return false;
    }

    //Requesting permission
    private void requestStoragePermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {
            //If the user has denied the permission previously your code will come to this block
            //Here you can explain why you need this permission
            //Explain here why you need this permission
        }

        //And finally ask for the permission
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION}, STORAGE_PERMISSION_CODE);
    }

    //This method will be called when the user will tap on allow or deny
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        //Checking the request code of our request
        if (requestCode == STORAGE_PERMISSION_CODE) {

            //If permission is granted
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                //Displaying a toast
                Toast.makeText(this, "Permission granted now you can read the storage", Toast.LENGTH_LONG).show();
            } else {
                //Displaying another toast if permission is not granted
                Toast.makeText(this, "Oops you just denied the permission", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
      /*  LocalBroadcastManager.getInstance(this).registerReceiver(mGattUpdateReceiver,
                new IntentFilter("dssd"));*/
    }

    private boolean mConnected;


/* private void displayGattServices(List<BluetoothGattService> gattServices) {
        if (gattServices == null) return;
        String uuid = null;HashM
        String unknownServiceString = getResources().
                getString(R.string.unknown_service);
        String unknownCharaString = getResources().
                getString(R.string.unknown_characteristic);
        ArrayList<HashMap<String, String>> gattServiceData =
                new ArrayList<HashMap<String, String>>();
        ArrayList<ArrayList<HashMap<String, String>>> gattCharacteristicData
                = new ArrayList<ArrayList<HashMap<String, String>>>();
        ArrayList<ArrayList<BluetoothGattCharacteristic>> mGattCharacteristics =
                new ArrayList<ArrayList<BluetoothGattCharacteristic>>();

        // Loops through available GATT Services.
        for (BluetoothGattService gattService : gattServices) {
            HashMap<String, String> currentServiceData =
                    new HashMap<String, String>();
            uuid = gattService.getUuid().toString();
            currentServiceData.put(
                    LIST_NAME, SampleGattAttributes.
                            lookup(uuid, unknownServiceString));
            currentServiceData.put(LIST_UUID, uuid);
            gattServiceData.add(currentServiceData);

            ArrayList<HashMap<String, String>> gattCharacteristicGroupData =
                    new ArrayList<HashMap<String, String>>();
            List<BluetoothGattCharacteristic> gattCharacteristics =
                    gattService.getCharacteristics();
            ArrayList<BluetoothGattCharacteristic> charas =
                    new ArrayList<BluetoothGattCharacteristic>();
            // Loops through available Characteristics.
            for (BluetoothGattCharacteristic gattCharacteristic :
                    gattCharacteristics) {
                charas.add(gattCharacteristic);
                HashMap<String, String> currentCharaData =
                        new HashMap<String, String>();
                uuid = gattCharacteristic.getUuid().toString();
                currentCharaData.put(
                        LIST_NAME, SampleGattAttributes.lookup(uuid,
                                unknownCharaString));
                currentCharaData.put(LIST_UUID, uuid);
                gattCharacteristicGroupData.add(currentCharaData);
            }
            mGattCharacteristics.add(charas);
            gattCharacteristicData.add(gattCharacteristicGroupData);
        }

    }*/


}
