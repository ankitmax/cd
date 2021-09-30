package com.example.bleswitch;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

public class infoActivity extends AppCompatActivity {
    public String message = null;
    private TextView Appliances, app1ID, app2ID, app1View, app2View;
    private TextView modeTypeView, modeValue;
    private String week;
    private String[] days = {"Sun","Mon","Tue","Wed","Thu","Fri","Sat"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);

        Bundle bundle   = getIntent().getExtras();
        message         = bundle.getString("Message");
        Log.i("[INFO]", "Message: " + message);
        Appliances = (TextView) findViewById(R.id.interfaceText);
        app1ID = (TextView) findViewById(R.id.app1Text);
        app2ID = (TextView) findViewById(R.id.app2Text);
        app1View = (TextView) findViewById(R.id.app1State);
        app2View = (TextView) findViewById(R.id.app2State);
        modeTypeView = (TextView) findViewById(R.id.modeType);
        modeValue = (TextView) findViewById(R.id.modeValue);
        infoDisplay(message);
    }

    private void infoDisplay(String msg) {
        String[] list = msg.split(",");
        /* List index markings:
            0 - Number of appliances
            1 - Appliance 1 previous state
            2 - Appliance 2 previous state
            3 - Appliance 1 ID
            4 - Appliance 1 current value
            5 - Appliance 2 ID
            6 - Appliance 2 current value
            7 - Mode Type
            8 - ... <Mode Value>
         */
        Appliances.setText("Number of Appliances: " + list[0]);
        app1ID.setText("Appliance 1: 0x" + list[3]);
        app1View.setText(list[4]);
        app2ID.setText("Appliance 2: 0x" + list[5]);
        app2View.setText(list[6]);
        modeTypeView.setText(list[7]);
        switch (list[7]) {
            case "M":
                Log.i("[INFO]", "Manual Mode");
                /*List index marking continuation...
                    8 - Appliance 1 Set Value
                    9 - Appliance 2 set Value
                */
                modeValue.setText("Appliance 1 Set Value: " + list[8] + "\nAppliance 2 Set Value: "+list[9]);
            break;
            case "P":
                Log.i("[INFO]", "Period Mode");
                /* List index marking continuation...
                    8..14 - Days
                    TimeZone 1
                     Appliance 1
                        15 - ON Time
                        16 - OFF Time
                     Appliance 2
                        17 - ON Time
                        18 - OFF Time
                    TimeZone 2
                     Appliance 1
                        19 - ON Time
                        20 - OFF Time
                     Appliance 2
                        21 - ON Time
                        22 - OFF Time
                    TimeZone 3
                     Appliance 1
                        23 - ON Time
                        24 - OFF Time
                     Appliance 2
                        25 - ON Time
                        26 - OFF Time
                    TimeZone 4
                     Appliance 1
                        27 - ON Time
                        28 - OFF Time
                     Appliance 2
                        29 - ON Time
                        30 - OFF Time
                */

                for(int i = 0; i < 7;i++) {
                    week += list[8+i];
                }

                Log.i("Week", week);
                modeValue.setText("Days: ");
                for(int i = 0; i < 7; i++) {
                    if(week.charAt(i) == '1') {
                        modeValue.append(days[i]);
                        modeValue.append(" ");
                    }
                }
                modeValue.append("\n");
                modeValue.append("TimeZone 1\n" +
                                  "Appliance 1: ON for "+list[15]+" min every "+list[16]+" min\n"+
                                  "Appliance 2: ON for "+list[17]+" min every "+list[18]+" min\n"+
                                 "TimeZone 2\n" +
                                  "Appliance 1: ON for "+list[19]+" min every "+list[20]+" min\n"+
                                  "Appliance 2: ON for "+list[21]+" min every "+list[22]+" min\n"+
                                 "TimeZone 3\n" +
                                  "Appliance 1: ON for "+list[23]+" min every "+list[24]+" min\n"+
                                  "Appliance 2: ON for "+list[25]+" min every "+list[26]+" min\n"+
                                 "TimeZone 4\n" +
                                  "Appliance 1: ON for "+list[27]+" min every "+list[28]+" min\n"+
                                  "Appliance 2: ON for "+list[29]+" min every "+list[30]+" min\n");

            break;
            default:
                Log.i("[MODE]", "Wrong Value");
            break;
        }

    }
}