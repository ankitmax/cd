package com.example.bleswitch;

import static java.lang.Integer.numberOfLeadingZeros;
import static java.lang.Integer.parseInt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.InputType;
import android.text.Layout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BleDeviceActivity extends AppCompatActivity {
    private static final UUID SERVICE_UUID = UUID.fromString("208fc8fc-64ed-4423-ba22-2230821ae406");
    private static final UUID CHAR_UUID = UUID.fromString("e462c4e9-3704-4af8-9a20-446fa2eef1d0");
    private static final String TAG = "ERROR";

    private TextView addressText;
    private TextView nameText;
    private BluetoothDevice device;
    private BluetoothGatt mGatt;
    private boolean mConnected = false;
    private Button infoButton;
    private Button modeButton;
    private Button settingsButton;
    private TextView deviceState;
    private static int GATT_ATT_MTU = 100;
    BluetoothGattService switchService;
    BluetoothGattCharacteristic switchChar;
    BluetoothGattDescriptor descriptor;
    private boolean mtuSet = false;
    String messageString = null;
    private boolean chgFlg = false;
    private String sendMsg = null;
    private ScrollView sView;
    private LinearLayout linearLayout;
    private RelativeLayout childRelativeLayout;
    String[] days = new String[]{"0","0","0","0","0","0","0"};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_device);

        Bundle bundle   = getIntent().getExtras();
        device          = (BluetoothDevice) bundle.get("device");

        infoButton      = (Button) findViewById(R.id.infoButton);
        modeButton      = (Button) findViewById(R.id.modeButton);
        settingsButton  = (Button) findViewById(R.id.settingsButton);
        deviceState     = (TextView) findViewById(R.id.deviceState);
        sView = (ScrollView) findViewById(R.id.scrollView);

        linearLayout = (LinearLayout) findViewById(R.id.linearScroll);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.removeAllViews();

        connectToDevice(device);

        infoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                infoFunc();
            }
        });

        modeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                modeFunc();
            }
        });

        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                settingFunc();
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        //if (mGatt != null) {
        //    disconnectGattServer();
        //}
    }

    private void connectToDevice(BluetoothDevice btDevice) {
        if (mGatt == null) {
            Log.i("[BLE Connect]", "Connecting to device: " + btDevice.getName());
            mtuSet = false;
            mGatt = btDevice.connectGatt(this, false, gattCallback);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {

                }
            }, 2000);
            refreshDeviceCache(mGatt);
        }
    }

    private boolean refreshDeviceCache(BluetoothGatt gatt) {
        try {
            BluetoothGatt localBluetoothGatt = gatt;
            Method localMethod = localBluetoothGatt.getClass().getMethod("refresh", new Class[0]);
            if (localMethod != null) {
                boolean bool = ((Boolean) localMethod.invoke(localBluetoothGatt, new Object[0])).booleanValue();
                return bool;
            }
        } catch (Exception localException) {
            Log.e(TAG, "An exception occurred while refreshing device");
        }
        return false;
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            Log.i("onConnectionStateChange", "Status: " + status);
            if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_CONNECTED) {
                Log.i("gattCallback", "STATE_CONNECTED");
                mConnected = true;
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        deviceState.setText("Connected");
                    }
                });

                gatt.requestMtu(GATT_ATT_MTU);
            }
            else if(status == BluetoothGatt.GATT_SUCCESS && newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.i("gattCallback", "STATE_DISCONNECTED");
                disconnectGattServer();
            }
            else if(status != BluetoothGatt.GATT_SUCCESS) {
                Log.i("gattCallback", "STATE_OTHER");
                disconnectGattServer();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i("[GATTService]", "SUCCESS");
                List<BluetoothGattService> services = gatt.getServices();
                Log.d("onServicesDiscovered", "Services count: " + services.size());

                for (BluetoothGattService service : services) {
                    String serviceUUID = service.getUuid().toString();
                    //Log.d("onServicesDiscovered", "Service uuid " + serviceUUID);

                    List<BluetoothGattCharacteristic> characteristics = service.getCharacteristics();
                    for (BluetoothGattCharacteristic characteristic : characteristics) {
                        ///Once you have a characteristic object, you can perform read/write
                        //operations with it
                        String charUUID = characteristic.getUuid().toString();
                        //Log.d("onCharDiscovered", "Characteristic uuid " + charUUID);
                    }
                }
                switchService = gatt.getService(SERVICE_UUID);
                switchChar = switchService.getCharacteristic(CHAR_UUID);
                Log.d("Switch Service", "Service UUID " + switchService.getUuid().toString());
                Log.d("Switch Char", "Characteristic UUID " + switchChar.getUuid().toString());
                gatt.setCharacteristicNotification(switchChar, true);
                UUID uuid = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
                descriptor = switchChar.getDescriptor(uuid);
                descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(descriptor);
            }
        }

        @Override
        public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
            Log.i("MTU", "Setting MTU to " + mtu);
            if (status == gatt.GATT_SUCCESS) {
                Log.i("ATT MTU", "MTU Successful");
            } else {
                Log.i("ATT MTU", "MTU Unsuccessful");
            }
            mtuSet = true;
            gatt.discoverServices();
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt,
                                         BluetoothGattCharacteristic characteristic,
                                         int status) {
            Log.i("onCharacteristicRead", characteristic.toString());
            //if (characteristic.getUuid() == CHAR_UUID) {
                byte[] msgBytes = characteristic.getValue();
                String msgString = null;
                try {
                    msgString = new String(msgBytes, "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    Log.e(TAG, "Unable to convert message bytes to string");
                }
                Log.d("CharRead", "Received message: " + msgString);
                //textView.setText(msgString);
           // }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt,
                                          BluetoothGattCharacteristic characteristic,
                                          int status) {
            Log.i("onCharacteristicWrite", characteristic.getUuid().toString());

        }

        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            //if (characteristic.getUuid().toString() == CHAR_UUID) {
            byte[] messageBytes = characteristic.getValue();
            messageString = null;
            chgFlg = false;
            try {
                messageString = new String(messageBytes, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                Log.e(TAG, "Unable to convert message bytes to string");
            }
            Log.d("CharChange", "Received message: " + messageString);
        }
    };

    public void disconnectGattServer() {
        mConnected = false;
        if (mGatt != null) {
            mGatt.disconnect();
            mGatt.close();
            mGatt = null;
            Log.d("DisconnectGatt", "GATT Server Disconnected");
        }
        deviceState.setText("Disconnected from " + device.getName().toString());
    }
    
    private void infoFunc() {
        linearLayout.removeAllViews();
        sendMsg = "I";
        send(sendMsg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Log.i("[INFO]", "setting display");
                        //infoDisplay(messageString);
                        Intent intent = new Intent(BleDeviceActivity.this, infoActivity.class);
                        intent.putExtra("Message", messageString);
                        startActivity(intent);
                    }
                }, 100);
            }
        });
    }

    private void modeFunc() {
        linearLayout.removeAllViews();

        Button mButton = new Button (this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp1.setMargins( 20, 0, 20, 0 );
        mButton.setLayoutParams(lp1);
        mButton.setTextColor( Color.BLACK );
        mButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        mButton.setText( "Manual" );
        linearLayout.addView(mButton);

        Button pButton = new Button (this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp2.setMargins( 20, 0, 20, 0 );
        pButton.setLayoutParams(lp2);
        pButton.setTextColor( Color.BLACK );
        pButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        pButton.setText( "Period" );
        linearLayout.addView(pButton);

        Button tButton = new Button (this);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp3.setMargins( 20, 0, 20, 0 );
        tButton.setLayoutParams(lp3);
        tButton.setTextColor( Color.BLACK );
        tButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        tButton.setText( "Timeframe" );
        linearLayout.addView(tButton);

        //Manual
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.removeAllViews();
                TextView app1 = new TextView(BleDeviceActivity.this);
                TextView app2 = new TextView(BleDeviceActivity.this);
                app1.setText("Appliance 1");
                app2.setText("Appliance 2");
                Switch app1Switch = new Switch(BleDeviceActivity.this);
                Switch app2Switch = new Switch(BleDeviceActivity.this);
                linearLayout.addView(app1);
                linearLayout.addView(app1Switch);
                linearLayout.addView(app2);
                linearLayout.addView(app2Switch);
                Button submit = new Button(BleDeviceActivity.this);
                submit.setText("Submit");
                linearLayout.addView(submit);
                StringBuilder mCmd = new StringBuilder("S,M,");
                submit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if(app1Switch.isChecked())
                            mCmd.append("1,");
                        else
                            mCmd.append("0,");
                        if(app2Switch.isChecked())
                            mCmd.append("1");
                        else
                            mCmd.append("0");

                        send(mCmd.toString());
                        Toast.makeText(BleDeviceActivity.this, "Manual Mode Set", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
        //Period
        pButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.removeAllViews();
                StringBuilder pCmd = new StringBuilder("S,P,");
                ArrayList<String> min = new ArrayList<>();
                String[] min1 = new String[]{"0","5","10","15","20","30","40","60","90","120","150",
                        "180","210","240","270","300"};
                for(int i = 0; i < min1.length; i++) {
                    min.add(min1[i]);
                }

                ArrayList<AdapterData> dataValues = new ArrayList<>();

                dataValues.add(new AdapterData("[TIMEZONE 1] Appliance 1: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 1] Appliance 1: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 1] Appliance 2: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 1] Appliance 2: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 2] Appliance 1: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 2] Appliance 1: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 2] Appliance 2: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 2] Appliance 2: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 3] Appliance 1: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 3] Appliance 1: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 3] Appliance 2: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 3] Appliance 2: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 4] Appliance 1: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 4] Appliance 1: OFF for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 4] Appliance 2: ON for", "0"));
                dataValues.add(new AdapterData("[TIMEZONE 4] Appliance 2: OFF for", "0"));

                CheckBox sun = new CheckBox(BleDeviceActivity.this);
                CheckBox mon = new CheckBox(BleDeviceActivity.this);
                CheckBox tue = new CheckBox(BleDeviceActivity.this);
                CheckBox wed = new CheckBox(BleDeviceActivity.this);
                CheckBox thu = new CheckBox(BleDeviceActivity.this);
                CheckBox fri = new CheckBox(BleDeviceActivity.this);
                CheckBox sat = new CheckBox(BleDeviceActivity.this);

                sun.setTag("sun");
                mon.setTag("mon");
                tue.setTag("tue");
                wed.setTag("wed");
                thu.setTag("thu");
                fri.setTag("fri");
                sat.setTag("sat");
                sun.setText("S");
                mon.setText("M");
                tue.setText("T");
                wed.setText("W");
                thu.setText("T");
                fri.setText("F");
                sat.setText("S");

                setOnMyViewClick(sun);
                setOnMyViewClick(mon);
                setOnMyViewClick(tue);
                setOnMyViewClick(wed);
                setOnMyViewClick(thu);
                setOnMyViewClick(fri);
                setOnMyViewClick(sat);

                LinearLayout daysLinear = new LinearLayout(BleDeviceActivity.this);
                daysLinear.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams daysLp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                daysLinear.setLayoutParams(daysLp);

                daysLinear.addView(sun);
                daysLinear.addView(mon);
                daysLinear.addView(tue);
                daysLinear.addView(wed);
                daysLinear.addView(thu);
                daysLinear.addView(fri);
                daysLinear.addView(sat);
                linearLayout.addView(daysLinear);

                ListView lView = new ListView(BleDeviceActivity.this);
                lView.setAdapter(new CustomAdapter(BleDeviceActivity.this, dataValues, min));
                Button sButton = new Button(BleDeviceActivity.this);
                sButton.setText("Submit");

                linearLayout.addView(lView);
                linearLayout.addView(sButton);
                sButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Log.i("[PERIOD]", "Message submitted");
                        for(int i = 0; i < 7; i++) {
                            pCmd.append(days[i].toString());
                        }
                        pCmd.append(",");
                        for(int i = 0; i < 16; i++) {
                            Log.i("VAL", dataValues.get(i).getValue());
                            pCmd.append(dataValues.get(i).getValue());
                            pCmd.append(",");
                        }

                        send(pCmd.toString());
                        Toast.makeText(BleDeviceActivity.this, "Period Mode Set", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        //Timeframe
        tButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                linearLayout.removeAllViews();
                final Calendar cldr = Calendar.getInstance();
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int min = cldr.get(Calendar.MINUTE);
                LinearLayout tLay1 = new LinearLayout(BleDeviceActivity.this);
                tLay1.setOrientation(LinearLayout.HORIZONTAL);
                LinearLayout.LayoutParams tlp1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tLay1.setLayoutParams(tlp1);
                RelativeLayout tRelative = new RelativeLayout(BleDeviceActivity.this);
                RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                tRelative.setId(Integer.parseInt("tRel"));
                ArrayList<Integer> selValues = new ArrayList<>();
                for(int i = 0; i < 19; i++) {
                    selValues.add(i);
                }

                Spinner spinSel = new Spinner(BleDeviceActivity.this);
                spinSel.setId(R.id.timeSpin);
                ArrayAdapter<Integer> spinnerAdapter = new ArrayAdapter<Integer>(BleDeviceActivity.this,
                        android.R.layout.simple_spinner_item,selValues);
                spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinSel.setAdapter(spinnerAdapter);
                try {
                    Field popup = Spinner.class.getDeclaredField("mPopup");
                    popup.setAccessible(true);

                    // Get private mPopup member variable and try cast to ListPopupWindow
                    android.widget.ListPopupWindow popupWindow = (android.widget.ListPopupWindow) popup.get(spinSel);

                    // Set popupWindow height to 500px
                    popupWindow.setHeight(400);
                }
                catch (NoClassDefFoundError | ClassCastException | NoSuchFieldException | IllegalAccessException e) {
                    // silently fail...
                }
                Button okButton = new Button(BleDeviceActivity.this);
                okButton.setId(R.id.okButton);
                okButton.setText("OK");

                Button sFButton = new Button(BleDeviceActivity.this);
                sFButton.setId(R.id.submitTF);
                sFButton.setText("Submit");

                TextView selectionText = new TextView(BleDeviceActivity.this);
                selectionText.setId(R.id.selText);
                selectionText.setText("Number of Selection: ");

                RelativeLayout.LayoutParams lp1 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp1.addRule(RelativeLayout.LEFT_OF, R.id.timeSpin);
                selectionText.setLayoutParams(lp1);

                RelativeLayout.LayoutParams lp2 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp2.addRule(RelativeLayout.LEFT_OF, R.id.okButton);
                spinSel.setLayoutParams(lp2);

                ArrayList<TimeFrameDataModel> dataValues = new ArrayList<>();

                ListView lTView = new ListView(BleDeviceActivity.this);
                lTView.setId(R.id.TFlist);
                RelativeLayout.LayoutParams lp3 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp3.addRule(RelativeLayout.BELOW, R.id.selText);
                lTView.setLayoutParams(lp3);

                RelativeLayout.LayoutParams lp4 = new RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.WRAP_CONTENT,
                        RelativeLayout.LayoutParams.WRAP_CONTENT);
                lp3.addRule(RelativeLayout.BELOW, R.id.TFlist);
                sFButton.setLayoutParams(lp3);

                tRelative.addView(selectionText);
                tRelative.addView(spinSel);
                tRelative.addView(okButton);
                tRelative.addView(sFButton);
                linearLayout.addView(tRelative);

                spinSel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        int selNumber = Integer.parseInt(spinSel.getSelectedItem().toString());
                        Log.i("SELECTION", "Value: " + selNumber);
                        dataValues.clear();
                        for(int k = 1; k <= selNumber; k++) {
                            dataValues.add(new TimeFrameDataModel(k, "0"));
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> adapterView) {

                    }
                });

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        //lTView.setAdapter(new TimeFrameAdapter(BleDeviceActivity.this, tModel, "FROM","TO","FROM","TO"));
                        lTView.setAdapter(new TimeFrameAdapter(BleDeviceActivity.this, dataValues,"0" ));
                        if(hasChildren(linearLayout)){
                            Log.i("PARENT", "has childs " + linearLayout.getChildCount());
                            while(linearLayout.getChildCount() > 1) {
                                linearLayout.removeViewAt(1);
                                Log.i("PARENT", "now has childs " + linearLayout.getChildCount());

                            }
                        }

                        linearLayout.removeView(lTView);
                        linearLayout.addView(lTView);
                        //if(dataValues.size() > 0) {
                            linearLayout.addView(sFButton);
                        //}
                    }
                });

            }
        });
    }
    public static boolean hasChildren(ViewGroup viewGroup) {
        return viewGroup.getChildCount() > 0;
    }

    private void setOnMyViewClick(View myView)
    {
        if(myView!= null)
        {
            myView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    try
                    {
                        onCheckBoxClicked(v, v.getResources().getResourceEntryName(v.getId()));
                    }
                    catch (Exception e)
                    {

                    }
                }
            });
        }
    }

    public void onCheckBoxClicked(View view, String viewName) {
        boolean checked = ((CheckBox) view).isChecked();
        switch(view.getTag().toString()) {
            case "sun":
                if (checked) {
                    Log.i("CHECK", "Sunday checked");
                    days[0] = "1";
                }
                else {
                    Log.i("CHECK", "Sunday not checked");
                    days[0] = "0";
                }
            break;
            case "mon":
                if (checked)
                    days[1] = "1";
                else
                    days[1] = "0";
            break;
            case "tue":
                if (checked)
                    days[2] = "1";
                else
                    days[2] = "0";
            break;
            case "wed":
                if (checked)
                    days[3] = "1";
                else
                    days[3] = "0";
            break;
            case "thu":
                if (checked)
                    days[4] = "1";
                else
                    days[4] = "0";
            break;
            case "fri":
                if (checked)
                    days[5] = "1";
                else
                    days[5] = "0";
            break;
            case "sat":
                if (checked)
                    days[6] = "1";
                else
                    days[6] = "0";
            break;
        }
    }

    private void settingFunc() {
        linearLayout.removeAllViews();
        Button restartButton = new Button (this);
        LinearLayout.LayoutParams lp1 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp1.setMargins( 20, 0, 20, 0 );
        restartButton.setLayoutParams(lp1);
        restartButton.setTextColor( Color.BLACK );
        restartButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        restartButton.setText( "Restart" );
        linearLayout.addView(restartButton);

        Button resetButton = new Button (this);
        LinearLayout.LayoutParams lp2 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp2.setMargins( 20, 0, 20, 0 );
        resetButton.setLayoutParams(lp2);
        resetButton.setTextColor( Color.BLACK );
        resetButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        resetButton.setText( "Factory Reset" );
        linearLayout.addView(resetButton);

        Button setRTCButton = new Button (this);
        LinearLayout.LayoutParams lp3 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp3.setMargins( 20, 0, 20, 0 );
        setRTCButton.setLayoutParams(lp3);
        setRTCButton.setTextColor( Color.BLACK );
        setRTCButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        setRTCButton.setText( "Set RTC Time" );
        linearLayout.addView(setRTCButton);

        Button getRTCButton = new Button (this);
        LinearLayout.LayoutParams lp4 = new LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT );
        lp4.setMargins( 20, 0, 20, 0 );
        getRTCButton.setLayoutParams(lp4);
        getRTCButton.setTextColor( Color.BLACK );
        getRTCButton.setGravity( Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL );
        getRTCButton.setText( "Get RTC Time" );
        linearLayout.addView(getRTCButton);
        TextView showRTC = new TextView(this);

        restartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("R");
            }
        });

        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("F");
            }
        });
        TextView dateTime = new TextView(this);
        getRTCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                send("T");
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                dateTime.setText("RTC Time: "+messageString);
                                linearLayout.addView(showRTC);
                            }
                        }, 100);
                    }
                });
            }
        });
        CustomDatePicker date = new CustomDatePicker(this);
        setRTCButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final Calendar cldr = Calendar.getInstance();
                int day = cldr.get(Calendar.DAY_OF_MONTH);
                int month = cldr.get(Calendar.MONTH);
                int year = cldr.get(Calendar.YEAR);
                int hour = cldr.get(Calendar.HOUR_OF_DAY);
                int min = cldr.get(Calendar.MINUTE);
                int mHour, mMin;
                // date picker dialog
                DatePickerDialog picker = new DatePickerDialog(BleDeviceActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                                dateTime.setText(dayOfMonth + "/" + (monthOfYear + 1) + "/" + year+" ");
                                new TimePickerDialog(BleDeviceActivity.this, new TimePickerDialog.OnTimeSetListener() {
                                    @Override
                                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                        dateTime.append(hourOfDay+":"+minute);
                                    }
                                }, hour, min, true).show();
                            }
                        }, year, month, day);
                picker.show();
                linearLayout.addView(dateTime);
            }
        });
    }

    private void send(String command) {
        if (!mConnected) {
            Toast.makeText(this, "BLE Device is not connected. Please try again.",
                    Toast.LENGTH_SHORT).show();
            Log.d(TAG, "Not connected");
            return;
        }

        byte[] messageBytes = new byte[0];
        try {
            messageBytes = command.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            Log.e(TAG, "Failed to convert message string to byte array");
        }
        Log.d(TAG, "Sending message: " + command);
        switchChar.setValue(messageBytes);
        boolean success = mGatt.writeCharacteristic(switchChar);
        if(success) {
            Log.d("[SEND]", "Successful");
        }
        else {
            Log.d("[SEND]", "Unsuccessful");
        }
    }
}