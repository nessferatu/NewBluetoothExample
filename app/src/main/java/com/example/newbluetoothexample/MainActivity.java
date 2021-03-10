package com.example.newbluetoothexample;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    Button b1,b2,b3,b4,b5;
    BluetoothManager btManager;

    BluetoothLeScanner btScanner;
    private BluetoothAdapter BA;
    private BluetoothDevice device;
    private Set<BluetoothDevice> pairedDevices;
    private ArrayAdapter<String>  mAdapter;
    //private BroadcastReceiver mReceiver;
    ListView lv;
    private final static int REQUEST_ENABLE_BT = 1;
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 1;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        b1 = findViewById(R.id.button);
        b2 = findViewById(R.id.button2);
        b3 = findViewById(R.id.button3);
        b4 = findViewById(R.id.button4);
        b5 = findViewById(R.id.button5);

        b3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                stopScanning();
            }
        });

        b3.setVisibility(View.INVISIBLE);

        btManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        BA = btManager.getAdapter();
        btScanner = BA.getBluetoothLeScanner();

        b5.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startScanning();
            }
        });


        BA = BluetoothAdapter.getDefaultAdapter();
        ArrayList list = new ArrayList();
        lv = findViewById(R.id.listView);
        mAdapter = new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,list);

        if (BA != null && !BA.isEnabled()) {
            Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent,REQUEST_ENABLE_BT);
        }

        // Make sure we have access coarse location enabled, if not, prompt the user to enable it
        if (this.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs fine location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_FINE_LOCATION);
                }
            });
            builder.show();
        }

        if (this.checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("This app needs coarse location access");
            builder.setMessage("Please grant location access so this app can detect peripherals.");
            builder.setPositiveButton(android.R.string.ok, null);
            builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);
                }
            });
            builder.show();
        }

    }

    private ScanCallback leScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {

            mAdapter.add("Name: "+result.getDevice().getAlias()+"\nRSSI: "+result.getRssi()+"dbm " +"|"+" TXPOWER: "+result.getTxPower() +"\nMAC: "+ result.getDevice().getAddress());



            Log.i("txPower","txPower is "+result.getTxPower());

        }
    };


    ///////////////////////////////
    public void startScanning(){
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.clear();
        Toast.makeText(getApplicationContext(),"Scanning...",Toast.LENGTH_SHORT).show();

        b5.setVisibility(View.INVISIBLE);
        b3.setVisibility(View.VISIBLE);

        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.startScan(leScanCallback);
            }
        });


    }



    //Find paired devices first
    // pairedDevices = BA.getBondedDevices();

    //ArrayList list = new ArrayList();


        /*for(BluetoothDevice bt: pairedDevices){

            list.add(bt.getName()+"\nSignal: dBm"+"\nMAC: "+bt.getAddress());
        }  */



    // If another discovery is in progress, cancels it before starting the new one.
       /* if (BA.isDiscovering()) {
            Toast.makeText(getApplicationContext(),"Already scanning.",Toast.LENGTH_SHORT).show();
            BA.cancelDiscovery();
        }

        //Start scanning for devices.
        BA.startDiscovery();
        Toast.makeText(getApplicationContext(),"Scanning for devices.",Toast.LENGTH_SHORT).show();
         //Create BroadcastReceiver
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                 String action = intent.getAction();
                //Finding unpaired devices
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // Get the BluetoothDevice object from the Intent
                    device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // Get the signal strength
                    int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);

                    String deviceName;

                    if(device.getName()!= null){
                        deviceName = device.getName();
                    }else{
                        deviceName = "Unknown Device";
                    }


                    mAdapter.add(deviceName +"\nSignal: "+rssi+ "dBm" +"\nMAC: "+ device.getAddress());


                }

                if(BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)){
                    Toast.makeText(getApplicationContext(),"Scanning complete.",Toast.LENGTH_SHORT).show();
                    unregisterReceiver(mReceiver);


                }
            }
        };




        mAdapter = new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,list);
        lv.setAdapter(mAdapter);
        mAdapter.notifyDataSetChanged();
        mAdapter.clear();



        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);


        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);   */


    public void stopScanning() {

        Toast.makeText(getApplicationContext(),"Stopping...",Toast.LENGTH_SHORT).show();

        b5.setVisibility(View.VISIBLE);
        b3.setVisibility(View.INVISIBLE);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                btScanner.stopScan(leScanCallback);
            }
        });
    }



    public void on(View v){
        if(!BA.isEnabled()){
            Intent turnOn = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(turnOn,0);
            Toast.makeText(getApplicationContext(),"Turned On", Toast.LENGTH_LONG).show();
        } else{
            Toast.makeText(getApplicationContext(),"Already On", Toast.LENGTH_LONG).show();
        }
    }

    public void off(View v){
        BA.disable();
        Toast.makeText(getApplicationContext(),"Turned Off", Toast.LENGTH_LONG).show();
    }

    public void visible(View v) {
        Intent getVisible = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        startActivityForResult(getVisible,0);
    }


   /* public void list(View v) {
       pairedDevices = BA.getBondedDevices();

        ArrayList list = new ArrayList();

        for(BluetoothDevice bt: pairedDevices){
            list.add(bt.getName()+"\n"+bt.getAddress());
        }

        Toast.makeText(getApplicationContext(),"Showing Paired Devices", Toast.LENGTH_LONG).show();

        mAdapter = new ArrayAdapter(this,android.R.layout.simple_expandable_list_item_1,list);
        lv.setAdapter(mAdapter);

    }   */
}
