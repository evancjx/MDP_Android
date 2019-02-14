package com.mdp_android;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Set;

public class BluetoothManager extends AppCompatActivity {

    ListView pairedDevicesList;
    ListView discoverDevicesList;
    Button btnDiscover;

    private ArrayAdapter<String> newDeviceAdpater, pairedDevicesAdapter;
    private BluetoothAdapter btAdapter;
    private Set<BluetoothDevice> pairedDevices;

    public static String EXTRA_DEVICE_ADDRESS = "device_address";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);
        getSupportActionBar().setTitle("MDP Group 1 Bluetooth");

        btnDiscover = findViewById(R.id.btnDiscover);
        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
            }
        });

        pairedDevicesAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);
        newDeviceAdpater = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1);

        pairedDevicesList = findViewById(R.id.pairedDevices);
        pairedDevicesList.setAdapter(pairedDevicesAdapter);
        pairedDevicesList.setOnItemClickListener(btDeviceCL);

        discoverDevicesList = findViewById(R.id.discoverDevices);
        discoverDevicesList.setAdapter(newDeviceAdpater);
        discoverDevicesList.setOnItemClickListener(btDeviceCL);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onStart(){
        super.onStart();

        ///* According to Google, to access the hardware identifers of nearby external devices
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},
                1 );
        //*/

        btAdapter = BluetoothAdapter.getDefaultAdapter();
        //checking for bluetooth support
        if(btAdapter==null)
            Log.d("DEBUG", "no Bluetooth support");
        else{
            if(!btAdapter.isEnabled()){
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);
                Toast.makeText(getApplicationContext(),"Turned on", Toast.LENGTH_LONG).show();
            }

            doDiscovery();
            pairedDevices = btAdapter.getBondedDevices();
            //Toast.makeText(getApplicationContext(), "Showing Paired Devices",Toast.LENGTH_SHORT).show();
            pairedDevicesAdapter.clear();
            if(pairedDevices.size() > 0)
                for(BluetoothDevice device: pairedDevices)
                    pairedDevicesAdapter.add(device.getName() + "\n" + device.getAddress());

        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(BluetoothDevice.ACTION_FOUND.equals(action)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                //newDeviceAdpater.add(device.getName() + "\n" + device.getAddress());
                //*
                if(device.getBondState() != BluetoothDevice.BOND_BONDED){
                    newDeviceAdpater.add(device.getName() + "\n" + device.getAddress());
                }
                else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    if(newDeviceAdpater.getCount() == 0){
                        newDeviceAdpater.add("");
                    }
                }
                //*/
            }
        }
    };

    private AdapterView.OnItemClickListener btDeviceCL = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if(btAdapter != null) btAdapter.cancelDiscovery();

            String info = ((TextView) view).getText().toString();
            String address = info.substring(info.length() - 17);

            Intent intent = new Intent();
            intent.putExtra(EXTRA_DEVICE_ADDRESS, address);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private void doDiscovery(){
        if(btAdapter.isDiscovering()) btAdapter.cancelDiscovery();
        newDeviceAdpater.clear();
        btAdapter.startDiscovery();
        Toast.makeText(getApplicationContext(), "discovering",Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }
}
