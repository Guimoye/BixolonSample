package com.bixolon.sample;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.bixolon.sample.PrinterControl.BixolonPrinter;

import java.text.NumberFormat;

public class EtcFragment extends Fragment implements View.OnClickListener {

    public static EtcFragment newInstance() {
        EtcFragment fragment = new EtcFragment();
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
        View view = inflater.inflate(R.layout.fragment_etc, container, false);

        view.findViewById(R.id.buttonCutPaper).setOnClickListener(this);
        view.findViewById(R.id.buttonFormFeed).setOnClickListener(this);
        view.findViewById(R.id.buttonTransactionPrint).setOnClickListener(this);
        view.findViewById(R.id.buttonReceiptSample).setOnClickListener(this);
        view.findViewById(R.id.buttonFarsiSample).setOnClickListener(this);
        view.findViewById(R.id.buttonFarsiSample).setEnabled(false);

        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId())
        {
            case R.id.buttonCutPaper:
                MainActivity.getPrinterInstance().cutPaper();
                break;
            case R.id.buttonFormFeed:
                MainActivity.getPrinterInstance().formFeed();
                break;
            case R.id.buttonTransactionPrint:
                transactionPrint();
                break;
            case R.id.buttonReceiptSample:
                receiptSample();
                break;
            case R.id.buttonFarsiSample:
                farsiSample();
                break;
        }
    }

    private void transactionPrint()
    {
        MainActivity.getPrinterInstance().beginTransactionPrint();

        MainActivity.getPrinterInstance().printText("Transaction mode\n", 0, 0, 1);
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.bixolonlogo);
        MainActivity.getPrinterInstance().printImage(bitmap, 250, MainActivity.getPrinterInstance().ALIGNMENT_CENTER, 65);
        MainActivity.getPrinterInstance().printBarcode("123456789012", MainActivity.getPrinterInstance().BARCODE_TYPE_ITF, 3, 150, 2, MainActivity.getPrinterInstance().BARCODE_HRI_BELOW);

        MainActivity.getPrinterInstance().endTransactionPrint();
    }

    private void receiptSample()
    {
        MainActivity.getPrinterInstance().beginTransactionPrint();

        String data = "";
        data = "Comp: TICKET\n" +
               "Walton, KT12 3BS\n" +
               "Tel: 01932 901 155\n" +
               "123-456-789\n" +
               "VAT No. 123456789\n\n";
        MainActivity.getPrinterInstance().printText("TICKET\n\n", BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_BOLD | BixolonPrinter.ATTRIBUTE_UNDERLINE, 2);
        MainActivity.getPrinterInstance().printBarcode("www.bixolon.com", BixolonPrinter.BARCODE_TYPE_QRCODE, 8, 8, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.BARCODE_HRI_NONE);
        MainActivity.getPrinterInstance().printText(data, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_BOLD, 1);
        MainActivity.getPrinterInstance().printText("Sale:       " + "19-05-2017 16:19:43\n", BixolonPrinter.ALIGNMENT_LEFT, BixolonPrinter.ATTRIBUTE_BOLD, 1);

        data = "Gate:       " + "Xcover kiosk\n" +
                "Operator:   " + "Rob\n" +
                "Order Code: " + "263036991\n";
        MainActivity.getPrinterInstance().printText(data, BixolonPrinter.ALIGNMENT_LEFT, 0, 1);
        MainActivity.getPrinterInstance().printText("Qty Price  Item     Total\n", BixolonPrinter.ALIGNMENT_LEFT, BixolonPrinter.ATTRIBUTE_UNDERLINE, 1);
        MainActivity.getPrinterInstance().printText(" 1  $8.00  PARKING  $8.00\n", BixolonPrinter.ALIGNMENT_LEFT, 0, 1);

        data = "Total (inc VAT):  " + "  $8.00\n" +
                "VAT amount (20%): " + "  $1.33\n" +
                "CARD payment:     " + "  $8.00\n" +
                "Change due:       " + "  $0.00\n\n";
        MainActivity.getPrinterInstance().printText(data, BixolonPrinter.ALIGNMENT_RIGHT, BixolonPrinter.ATTRIBUTE_NORMAL, 1);

        data = "Thank you for your purchase!\n" +
                "Enjoy the show!\n" +
                "Next year visit\n" +
                "www.bixolon.com\n" +
                "to buy discounted tickets.\n\n\n\n";
        MainActivity.getPrinterInstance().printText(data, BixolonPrinter.ALIGNMENT_CENTER, BixolonPrinter.ATTRIBUTE_BOLD, 1);

        MainActivity.getPrinterInstance().endTransactionPrint();
    }

    private void farsiSample()
    {
        int currentCs = MainActivity.getPrinterInstance().getCharacterSet();

        MainActivity.getPrinterInstance().setCharacterSet(BixolonPrinter.CS_FARSI);

        // Farsi option : Mixed
        MainActivity.getPrinterInstance().setFarsiOption(BixolonPrinter.OPT_REORDER_FARSI_MIXED);
        printReceiptforFarsi();

        // Farsi option : Right to Left
        MainActivity.getPrinterInstance().setFarsiOption(BixolonPrinter.OPT_REORDER_FARSI_RTL);
        printReceiptforFarsi();

        MainActivity.getPrinterInstance().setCharacterSet(currentCs);
    }

    private void printReceiptforFarsi(){

        String address = "";
        address = "\nBIXOLON شرکت با مسئولیت محدود . 7 FL،";
        address += "\nMiraeAsset سرمایه گذاری برج 685 ،" ;
        address += "\nSampyeong دونگ، Bundang گو ، سئونگنام - سی،";
        address += "\nگیونگی-دو ، 463-400 ، کره";
        MainActivity.getPrinterInstance().printText("\n" + address, BixolonPrinter.ALIGNMENT_RIGHT, 0, 1);
    }
}
