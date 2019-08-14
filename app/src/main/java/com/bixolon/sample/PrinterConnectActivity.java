package com.bixolon.sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;

import java.util.ArrayList;
import java.util.Set;

public class PrinterConnectActivity extends AppCompatActivity implements  AdapterView.OnItemClickListener{
    private final int REQUEST_PERMISSION = 0;
    private final String DEVICE_ADDRESS_START = " (";
    private final String DEVICE_ADDRESS_END = ")";

    private final ArrayList<CharSequence> bondedDevices = new ArrayList<>();
    private ArrayAdapter<CharSequence> arrayAdapter;

    private int portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;
    private String logicalName = "";
    private String address = "";
    private static Context context;

    private TextView textViewBluetooth;
    private ListView listView;

    private static ProgressBar mProgressLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_connect);

        textViewBluetooth = findViewById(R.id.textViewBluetoothList);

        context =this;

        mProgressLarge = findViewById(R.id.progressBar1);
        mProgressLarge.setVisibility(ProgressBar.GONE);

        setPairedDevices();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, bondedDevices);
        listView = findViewById(R.id.listViewPairedDevices);
        listView.setAdapter(arrayAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(this);


        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                }
            }
        }

        portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;
        textViewBluetooth.setVisibility(View.VISIBLE);
        listView.setVisibility(View.VISIBLE);
        setPairedDevices();
    }

    private void setPairedDevices()
    {
        logicalName = null;
        bondedDevices.clear();

        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        Set<BluetoothDevice> bondedDeviceSet = bluetoothAdapter.getBondedDevices();

        for(BluetoothDevice device:bondedDeviceSet)
        {
            bondedDevices.add(device.getName() + DEVICE_ADDRESS_START + device.getAddress() + DEVICE_ADDRESS_END);
        }

        if(arrayAdapter != null)
        {
            arrayAdapter.notifyDataSetChanged();
        }
    }



    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String device = ((TextView) view).getText().toString();
        Log.e("position: ",i+" "+device);
        logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));
        address = device.substring(device.indexOf(DEVICE_ADDRESS_START) + DEVICE_ADDRESS_START.length(), device.indexOf(DEVICE_ADDRESS_END));


        mHandler.obtainMessage(0).sendToTarget();
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                //listView.setEnabled(false);
                if(MainActivity.getPrinterInstance().printerOpen(portType, logicalName, address, false))
                {
                    finish();
                }
                else
                {  //listView.setEnabled(true);
                    mHandler.obtainMessage(1, 0, 0,"Fail to printer open!!").sendToTarget();
                }
            }
        }).start();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults)
    {
        switch (requestCode)
        {
            case REQUEST_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED)
                {}
                else {
                    Toast.makeText(getApplicationContext(), "permission denied", Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    public static final Handler mHandler = new Handler(new Handler.Callback()
    {
        @SuppressWarnings("unchecked")
        @Override
        public boolean handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case 0:
                    mProgressLarge.setVisibility(ProgressBar.VISIBLE);
                    break;
                case 1:
                    String data = (String) msg.obj;
                    if(data != null && data.length() > 0) {
                        Toast.makeText(context, data, Toast.LENGTH_LONG).show();
                    }
                    mProgressLarge.setVisibility(ProgressBar.GONE);
                    break;
            }
            return false;
        }
    });
}
