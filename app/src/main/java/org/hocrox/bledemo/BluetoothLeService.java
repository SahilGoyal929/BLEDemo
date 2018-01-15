package org.hocrox.bledemo;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import java.util.UUID;

public class BluetoothLeService extends Service {
    private final static String TAG = BluetoothLeService.class.getSimpleName();

    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private String mBluetoothDeviceAddress;
    public static BluetoothGatt mBluetoothGatt;
    private int mConnectionState = STATE_DISCONNECTED;
    public static String action;
    private static final int STATE_DISCONNECTED = 0;
    private static final int STATE_CONNECTING = 1;
    private static final int STATE_CONNECTED = 2;

    public final static String ACTION_GATT_CONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_CONNECTED";
    public final static String ACTION_GATT_DISCONNECTED =
            "com.example.bluetooth.le.ACTION_GATT_DISCONNECTED";
    public final static String ACTION_GATT_SERVICES_DISCOVERED =
            "com.example.bluetooth.le.ACTION_GATT_SERVICES_DISCOVERED";
    public final static String ACTION_DATA_AVAILABLE =
            "com.example.bluetooth.le.ACTION_DATA_AVAILABLE";
    public final static String EXTRA_DATA =
            "com.example.bluetooth.le.EXTRA_DATA";

    public final static UUID UUID_HEART_RATE_MEASUREMENT =
            UUID.fromString(SampleGattAttributes.HEART_RATE_MEASUREMENT);

    // Various callback methods defined by the BLE API.
    BluetoothGattCallback bluetoothGattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            Log.e("Connected State Changed", "" + newState);

            if(newState==BluetoothLeService.STATE_CONNECTED){

                gatt.discoverServices();

            }


        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            Log.e("On Service Discovered", "Service Discovered"+">>>"+gatt.getServices().size());
            for (BluetoothGattService bluetoothGattService : gatt.getServices()) {

                Log.e("servicess", "" + bluetoothGattService.getUuid());

                if (TimeProfile.TIME_SERVICE.equals(bluetoothGattService.getUuid())) {

                    gatt.readCharacteristic(bluetoothGattService.getCharacteristic(TimeProfile.CURRENT_TIME));
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
            byte[] data = characteristic.getValue();
            String s=new String(data);
            Log.e("on Characte changed",""+s);
        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

        }

        @Override
        public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorWrite(gatt, descriptor, status);

        }


    };
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void broadcastUpdate(final String action) {
        BluetoothLeService.action=action;
        final Intent intent = new Intent(action);
        sendBroadcast(intent);
    }

    private void broadcastUpdate(final String action,
                                 final BluetoothGattCharacteristic characteristic) {
        BluetoothLeService.action=action;
        final Intent intent = new Intent(action);

        // This is special handling for the Heart Rate Measurement profile. Data
        // parsing is carried out as per profile specifications.
        if (UUID_HEART_RATE_MEASUREMENT.equals(characteristic.getUuid())) {
            int flag = characteristic.getProperties();
            int format = -1;
            if ((flag & 0x01) != 0) {
                format = BluetoothGattCharacteristic.FORMAT_UINT16;
                Log.d(TAG, "Heart rate format UINT16.");
            } else {
                format = BluetoothGattCharacteristic.FORMAT_UINT8;
                Log.d(TAG, "Heart rate format UINT8.");
            }
            final int heartRate = characteristic.getIntValue(format, 1);
            Log.d(TAG, String.format("Received heart rate: %d", heartRate));
            intent.putExtra(EXTRA_DATA, String.valueOf(heartRate));
        } else {
            // For all other profiles, writes the data formatted in HEX.
            final byte[] data = characteristic.getValue();
            if (data != null && data.length > 0) {
                final StringBuilder stringBuilder = new StringBuilder(data.length);
                for (byte byteChar : data)
                    stringBuilder.append(String.format("%02X ", byteChar));
                intent.putExtra(EXTRA_DATA, new String(data) + "\n" +
                        stringBuilder.toString());
            }
        }
        sendBroadcast(intent);

    }
}