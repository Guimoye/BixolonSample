package com.bixolon.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class PageModeFragment extends Fragment implements View.OnClickListener, RadioGroup.OnCheckedChangeListener {

    private RadioGroup radioGroupDirection;
    private CheckBox checkLabel;
    private TextView deviceMessagesTextView;

    private int direction = 1;

    public static PageModeFragment newInstance() {
        PageModeFragment fragment = new PageModeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_page_mode, container, false);

        view.findViewById(R.id.buttonSamplePrint).setOnClickListener(this);

        radioGroupDirection = view.findViewById(R.id.radioGroupDirection);
        radioGroupDirection.setOnCheckedChangeListener(this);

        checkLabel = view.findViewById(R.id.checkBoxLabelMode);

        deviceMessagesTextView = view.findViewById(R.id.textViewDeviceMessages);
        deviceMessagesTextView.setMovementMethod(new ScrollingMovementMethod());
        deviceMessagesTextView.setVerticalScrollBarEnabled(true);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonSamplePrint:
                printPageModeSample();
                break;
        }
    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        // LEFT_TO_RIGHT = 1;
        // BOTTOM_TO_TOP = 2;
        // RIGHT_TO_LEFT = 3;
        // TOP_TO_BOTTOM = 4;
        switch(i)
        {
            case R.id.radioL2R:     direction = 1;      break;
            case R.id.radioB2T:     direction = 2;      break;
            case R.id.radioR2L:     direction = 3;      break;
            case R.id.radioT2B:     direction = 4;      break;
        }
    }

    private void printPageModeSample()
    {
        // Step 1 : Area(Position), Direction
        int xPos = 0, yPos = 0;
        int width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
        int height = 1300;
        MainActivity.getPrinterInstance().startPageMode(xPos, yPos, width, height, direction);

        // Step 2 : Send print item
        switch(direction)
        {
            // LEFT_TO_RIGHT = 1;
            // BOTTOM_TO_TOP = 2;
            // RIGHT_TO_LEFT = 3;
            // TOP_TO_BOTTOM = 4;
            case 1:     printLeftToRight();     break;
            case 2:     printBottomToTop();     break;
            case 3:     printRightToLeft();     break;
            case 4:     printTopToBottom();     break;
        }

        // Step 3 : print start
        MainActivity.getPrinterInstance().endPageMode(checkLabel.isChecked());
    }

    private void printLeftToRight()
    {
        int width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
        int x = 5, y = 50;
        while(true)
        {
            // Position x, y
            MainActivity.getPrinterInstance().setPageModePosition(x, y);
            MainActivity.getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);

            if(y >= 1250 || x >= width)
            {
                break;
            }

            x += 10;
            y += 50;
        }
    }

    private void printBottomToTop()
    {
        int width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
        int x = 5, y = 50;
        while(true)
        {
            // Position x, y
            MainActivity.getPrinterInstance().setPageModePosition(x, y);
            MainActivity.getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);

            if(y >= width || x >= 1250)
            {
                break;
            }

            x += 50;
            y += 50;
        }
    }

    private void printRightToLeft()
    {
        int width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
        int x = 5, y = 1250;
        while(true)
        {
            // Position x, y
            MainActivity.getPrinterInstance().setPageModePosition(x, y);
            MainActivity.getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);

            if(y <= 50 || x >= width)
            {
                break;
            }

            x += 10;
            y -= 50;
        }
    }

    private void printTopToBottom()
    {
        int width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
        int x = 5, y = 50;
        while(true)
        {
            // Position x, y
            MainActivity.getPrinterInstance().setPageModePosition(x, y);
            MainActivity.getPrinterInstance().printText("X : " + x + ", Y : " + y, 0, 0, 1);

            if(y >= width || x >= 1250)
            {
                break;
            }

            x += 50;
            y += 50;
        }
    }

    public void setDeviceLog(String data)
    {
        mHandler.obtainMessage(0, 0, 0, data).sendToTarget();
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
                    deviceMessagesTextView.append((String)msg.obj + "\n");

                    Layout layout = deviceMessagesTextView.getLayout();
                    if (layout != null) {
                        int y = layout.getLineTop(
                                deviceMessagesTextView.getLineCount()) - deviceMessagesTextView.getHeight();
                        if (y > 0) {
                            deviceMessagesTextView.scrollTo(0, y);
                            deviceMessagesTextView.invalidate();
                        }
                    }
                    break;
            }
            return false;
        }
    });
}
