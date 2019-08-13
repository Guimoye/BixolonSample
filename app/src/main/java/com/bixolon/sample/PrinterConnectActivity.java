package com.bixolon.sample;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bxl.config.editor.BXLConfigLoader;
import com.bxl.config.util.BXLBluetooth;
import com.bxl.config.util.BXLBluetoothLE;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import jpos.JposException;

public class PrinterConnectActivity extends AppCompatActivity implements RadioGroup.OnCheckedChangeListener, AdapterView.OnItemClickListener, View.OnTouchListener, View.OnClickListener {
    private final int REQUEST_PERMISSION = 0;
    private final String DEVICE_ADDRESS_START = " (";
    private final String DEVICE_ADDRESS_END = ")";

    private final ArrayList<CharSequence> bondedDevices = new ArrayList<>();
    private ArrayAdapter<CharSequence> arrayAdapter;

    private int portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;
    private String logicalName = "";
    private String address = "";

    private LinearLayout layoutModel;
    private LinearLayout layoutIPAddress;

    private RadioGroup radioGroupPortType;
    private TextView textViewBluetooth;
    private ListView listView;
    private EditText editTextIPAddress;
    private CheckBox checkBoxAsyncMode;

    private Button btnPrinterOpen;

    private ProgressBar mProgressLarge;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer_connect);

        layoutModel = findViewById(R.id.LinearLayout2);
        layoutIPAddress = findViewById(R.id.LinearLayout3);
        layoutModel.setVisibility(View.GONE);
        layoutIPAddress.setVisibility(View.GONE);

        radioGroupPortType = findViewById(R.id.radioGroupPortType);
        radioGroupPortType.setOnCheckedChangeListener(this);

        textViewBluetooth = findViewById(R.id.textViewBluetoothList);
        editTextIPAddress = findViewById(R.id.editTextIPAddr);
        editTextIPAddress.setText("192.168.0.1");

        checkBoxAsyncMode = findViewById(R.id.checkBoxAsyncMode);

        btnPrinterOpen = findViewById(R.id.btnPrinterOpen);
        btnPrinterOpen.setOnClickListener(this);

        mProgressLarge = findViewById(R.id.progressBar1);
        mProgressLarge.setVisibility(ProgressBar.GONE);

        setPairedDevices();

        arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_single_choice, bondedDevices);
        listView = findViewById(R.id.listViewPairedDevices);
        listView.setAdapter(arrayAdapter);

        listView.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        listView.setOnItemClickListener(this);
        listView.setOnTouchListener(this);

        Spinner modelList = findViewById(R.id.spinnerModelList);

        ArrayAdapter modelAdapter = ArrayAdapter.createFromResource(this, R.array.modelList, android.R.layout.simple_spinner_dropdown_item);
        modelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        modelList.setAdapter(modelAdapter);
        modelList.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                if(portType == BXLConfigLoader.DEVICE_BUS_USB || portType == BXLConfigLoader.DEVICE_BUS_WIFI) {
                    logicalName = (String) parent.getItemAtPosition(position);
                }
            }

            public void onNothingSelected(AdapterView<?> parent)
            {}
        });

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_COARSE_LOCATION)) {
                    requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
                }
            }
        }
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

    private void setBleDevices()
    {
        mHandler.obtainMessage(0).sendToTarget();
        BXLBluetoothLE.setBLEDeviceSearchOption(5, 1);
        new searchBLEPrinterTask().execute();
    }

    private class searchBLEPrinterTask extends AsyncTask<Integer, Integer, Set<BluetoothDevice>>
    {
        private String message;

        @Override
        protected void onPreExecute()
        {
        }

        @Override
        protected void onPostExecute(Set<BluetoothDevice> bluetoothDeviceSet)
        {
            bondedDevices.clear();

            if(bluetoothDeviceSet.size() > 0)
            {
                for(BluetoothDevice device:bluetoothDeviceSet)
                {
                    bondedDevices.add(device.getName() + DEVICE_ADDRESS_START + device.getAddress() + DEVICE_ADDRESS_END);
                }
            }
            else
            {
                Toast.makeText(getApplicationContext(), "Can't found BLE devices. ", Toast.LENGTH_SHORT).show();
            }

            if(arrayAdapter != null)
            {
                arrayAdapter.notifyDataSetChanged();
            }

            if(message != null)
            {
                Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
            }

            mHandler.obtainMessage(1).sendToTarget();
        }

        @Override
        protected Set<BluetoothDevice> doInBackground(Integer... params)
        {
            try
            {
                return BXLBluetoothLE.getBLEPrinters(PrinterConnectActivity.this, BXLBluetoothLE.SEARCH_BLE_ALWAYS);
            }
            catch(NumberFormatException | JposException e)
            {
                message = e.getMessage();
                return new HashSet<>();
            }
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch(i)
        {
            case R.id.radioBT:
                portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH;

                textViewBluetooth.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);

                layoutModel.setVisibility(View.GONE);
                layoutIPAddress.setVisibility(View.GONE);

                setPairedDevices();
                break;
            case R.id.radioBLE:
                portType = BXLConfigLoader.DEVICE_BUS_BLUETOOTH_LE;
                textViewBluetooth.setVisibility(View.VISIBLE);
                listView.setVisibility(View.VISIBLE);

                layoutModel.setVisibility(View.GONE);
                layoutIPAddress.setVisibility(View.GONE);

                setBleDevices();
                break;
            case R.id.radioWifi:
                portType = BXLConfigLoader.DEVICE_BUS_WIFI;
                layoutModel.setVisibility(View.VISIBLE);
                layoutIPAddress.setVisibility(View.VISIBLE);

                textViewBluetooth.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                break;
            case R.id.radioUSB:
                portType = BXLConfigLoader.DEVICE_BUS_USB;
                layoutModel.setVisibility(View.VISIBLE);

                layoutIPAddress.setVisibility(View.GONE);
                textViewBluetooth.setVisibility(View.GONE);
                listView.setVisibility(View.GONE);
                break;
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent motionEvent) {
        if(motionEvent.getAction() == MotionEvent.ACTION_UP) listView.requestDisallowInterceptTouchEvent(false);
        else listView.requestDisallowInterceptTouchEvent(true);
        return false;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
        String device = ((TextView) view).getText().toString();
        logicalName = device.substring(0, device.indexOf(DEVICE_ADDRESS_START));
        address = device.substring(device.indexOf(DEVICE_ADDRESS_START) + DEVICE_ADDRESS_START.length(), device.indexOf(DEVICE_ADDRESS_END));
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.btnPrinterOpen:
                mHandler.obtainMessage(0).sendToTarget();
                new Thread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // TODO Auto-generated method stub
                        if(portType == BXLConfigLoader.DEVICE_BUS_WIFI)
                        {
                            address = editTextIPAddress.getText().toString();
                        }

                        if(MainActivity.getPrinterInstance().printerOpen(portType, logicalName, address, checkBoxAsyncMode.isChecked()))
                        {
                            finish();
                        }
                        else
                        {
                            mHandler.obtainMessage(1, 0, 0,"Fail to printer open!!").sendToTarget();
                        }
                    }
                }).start();

                break;
        }
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

    public final Handler mHandler = new Handler(new Handler.Callback()
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
                        Toast.makeText(getApplicationContext(), data, Toast.LENGTH_LONG).show();
                    }
                    mProgressLarge.setVisibility(ProgressBar.GONE);
                    break;
            }
            return false;
        }
    });
}
