package com.bixolon.sample;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.text.Layout;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;

import jpos.POSPrinterConst;

public class ImageFragment extends Fragment implements View.OnClickListener, SeekBar.OnSeekBarChangeListener, RadioGroup.OnCheckedChangeListener {

    private String PATH = Environment.getExternalStorageDirectory().getAbsolutePath();
    private int REQUEST_CODE_ACTION_PICK = 1;

    private LinearLayout layoutStartPage;
    private LinearLayout layoutEndPage;

    private RadioGroup radioGroupPrintingType;
    private EditText editTextWidth;
    private EditText editTextStartPage;
    private EditText editTextEndPage;
    private TextView textViewBrightness;
    private TextView textViewFilePath;
    private TextView deviceMessagesTextView;
    private SeekBar seekBarBrightness;

    private int spinnerAlignment = 0;

    private String[] pdfItems = null;

    public static ImageFragment newInstance() {
        ImageFragment fragment = new ImageFragment();
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
        View view = inflater.inflate(R.layout.fragment_image, container, false);

        layoutStartPage = view.findViewById(R.id.LinearLayout5);
        layoutEndPage = view.findViewById(R.id.LinearLayout6);
        layoutStartPage.setVisibility(View.GONE);
        layoutEndPage.setVisibility(View.GONE);

        radioGroupPrintingType = view.findViewById(R.id.radioGroupPrint);
        radioGroupPrintingType.setOnCheckedChangeListener(this);

        view.findViewById(R.id.buttonFileSelect).setOnClickListener(this);
        view.findViewById(R.id.buttonPrint).setOnClickListener(this);
        view.findViewById(R.id.buttonGetWidth).setOnClickListener(this);

        editTextWidth = view.findViewById(R.id.editTextWidth);
        editTextStartPage = view.findViewById(R.id.editTextStartPage);
        editTextEndPage = view.findViewById(R.id.editTextEndPage);
        textViewBrightness = view.findViewById(R.id.textViewBrightness);
        textViewFilePath = view.findViewById(R.id.textViewFilePath);

        editTextWidth.setText("384");
        editTextStartPage.setText("0");
        editTextEndPage.setText("0");
        textViewBrightness.setText("Brightness : 50");

        deviceMessagesTextView = view.findViewById(R.id.textViewDeviceMessages);
        deviceMessagesTextView.setMovementMethod(new ScrollingMovementMethod());
        deviceMessagesTextView.setVerticalScrollBarEnabled(true);

        Spinner imageAlignment = view.findViewById(R.id.imageAlignment);

        ArrayAdapter alignmentAdapter = ArrayAdapter.createFromResource(view.getContext(), R.array.Alignment, android.R.layout.simple_spinner_dropdown_item);
        alignmentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        imageAlignment.setAdapter(alignmentAdapter);
        imageAlignment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener()
        {
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
            {
                spinnerAlignment = position;
            }

            public void onNothingSelected(AdapterView<?> parent)
            {}
        });

        seekBarBrightness = view.findViewById(R.id.seekBarBrightness);
        seekBarBrightness.setOnSeekBarChangeListener(this);

        return view;
    }

    @Override
    public void onClick(View view) {
        int width = 0, alignment = 0, startPage = 0, endPage = 0, brightness = 0;
        String strPath = "";

        switch (view.getId())
        {
            case R.id.buttonFileSelect:
                switch (radioGroupPrintingType.getCheckedRadioButtonId())
                {
                    case R.id.radioImage:
                        showGallery();
                        break;
                    case R.id.radioPDF:
                        showPDFFileList();
                        break;
                }
                break;

            case R.id.buttonGetWidth:
                width = MainActivity.getPrinterInstance().getPrinterMaxWidth();
                editTextWidth.setText(Integer.toString(width));
                break;

            case R.id.buttonPrint:
                strPath = textViewFilePath.getText().toString();
                width = Integer.parseInt(editTextWidth.getText().toString());
                startPage = Integer.parseInt(editTextStartPage.getText().toString());
                endPage = Integer.parseInt(editTextEndPage.getText().toString());
                brightness = seekBarBrightness.getProgress();

                switch(spinnerAlignment)
                {
                    case 0:		alignment = MainActivity.getPrinterInstance().ALIGNMENT_LEFT;		break;
                    case 1:		alignment = MainActivity.getPrinterInstance().ALIGNMENT_CENTER;	    break;
                    case 2:		alignment = MainActivity.getPrinterInstance().ALIGNMENT_RIGHT;		break;
                    default:	alignment = MainActivity.getPrinterInstance().ALIGNMENT_LEFT;		break;
                }

                if(width <= 0)
                {
                    Toast.makeText(getContext(), "Invalid width : " + width, Toast.LENGTH_SHORT).show();
                    break;
                }

                if(strPath.length() == 0)
                {
                    Toast.makeText(getContext(), "Invalid file path!!", Toast.LENGTH_SHORT).show();
                    break;
                }

                switch (radioGroupPrintingType.getCheckedRadioButtonId())
                {
                    case R.id.radioImage:
                        MainActivity.getPrinterInstance().printImage(strPath, width, alignment, brightness);
                        break;
                    case R.id.radioPDF:
                        MainActivity.getPrinterInstance().printPdf(strPath, width, alignment, startPage, endPage, brightness);
                        break;
                }
                break;
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
        textViewBrightness.setText("Brightness : " + Integer.toString(i));
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onCheckedChanged(RadioGroup radioGroup, int i) {
        switch(i)
        {
            case R.id.radioImage:
                layoutStartPage.setVisibility(View.GONE);
                layoutEndPage.setVisibility(View.GONE);
                break;
            case R.id.radioPDF:
                layoutStartPage.setVisibility(View.VISIBLE);
                layoutEndPage.setVisibility(View.VISIBLE);
                break;
        }

        textViewFilePath.setText("");
    }

    private void showPDFFileList()
    {
        File file = new File(PATH);
        File[] files = file.listFiles(new FileFilter()
        {
            @Override
            public boolean accept(File pathname)
            {
                if(pathname.isDirectory())
                {
                    return false;
                }

                String name = pathname.getName();
                int lastIndex = name.lastIndexOf('.');
                if(lastIndex < 0)
                {
                    return false;
                }

                return name.substring(lastIndex).equalsIgnoreCase(".pdf");
            }
        });

        if(files != null && files.length > 0)
        {
            pdfItems = null;
            pdfItems = new String[files.length];
            for(int i = 0; i < pdfItems.length; i++)
            {
                pdfItems[i] = files[i].getAbsolutePath();
            }

            AlertDialog dialog = new AlertDialog.Builder(getContext()).setTitle("PDF List").setItems(pdfItems, new DialogInterface.OnClickListener()
            {
                public void onClick(DialogInterface dialog, int which)
                {
                    textViewFilePath.setText(pdfItems[which]);
                }
            }).create();
            dialog.show();
        }
        else
        {
            Toast.makeText(getContext(), "No PDF file", Toast.LENGTH_SHORT).show();
        }
    }

    private void showGallery()
    {
        String externalStorageState = Environment.getExternalStorageState();

        if(externalStorageState.equals(Environment.MEDIA_MOUNTED))
        {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
            startActivityForResult(intent, REQUEST_CODE_ACTION_PICK);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_ACTION_PICK) {
            if (data != null) {
                Uri uri = data.getData();
                ContentResolver cr = getActivity().getContentResolver();
                Cursor c = cr.query(uri, new String[] {MediaStore.Images.Media.DATA}, null, null, null);
                if (c == null || c.getCount() == 0) {
                    return;
                }

                c.moveToFirst();
                int columnIndex = c.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
                String text = c.getString(columnIndex);

                textViewFilePath.setText(text);
            }
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
