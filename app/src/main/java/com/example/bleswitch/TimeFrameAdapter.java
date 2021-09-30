package com.example.bleswitch;

import android.app.TimePickerDialog;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.TimePicker;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;

public class TimeFrameAdapter extends BaseAdapter {
    ArrayList<TimeFrameDataModel> dataList;
    private Context context;
    private static LayoutInflater inflater = null;
    private String butValue;
    final Calendar cldr = Calendar.getInstance();
    int hour = cldr.get(Calendar.HOUR_OF_DAY);
    int min = cldr.get(Calendar.MINUTE);

    public TimeFrameAdapter(Context context, ArrayList<TimeFrameDataModel> data, String value) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.dataList = data;
        this.butValue = value;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public TimeFrameDataModel getItem(int i) {
        return dataList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        final ViewHolder vHolder;
        View vi = view;
        if (vi == null) {
            vHolder = new ViewHolder();
            inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            vi = inflater.inflate(R.layout.timeframe, null);

            vHolder.selText = (TextView) vi.findViewById(R.id.selText);
            vHolder.fromButton1 = (Button) vi.findViewById(R.id.from1);
            vHolder.fromButton2 = (Button) vi.findViewById(R.id.from2);
            vHolder.toButton1 = (Button) vi.findViewById(R.id.to1);
            vHolder.toButton2 = (Button) vi.findViewById(R.id.to2);

            vi.setTag(vHolder);
        }
        else {
            vHolder = (ViewHolder) vi.getTag();
           // viewGroup.removeView(vi);
        }
        final TimeFrameDataModel mData = getItem(i);
        vHolder.selText.setText("Selection " + String.valueOf(mData.getSel()));
        vHolder.fromButton1.setText(mData.getValue());
        vHolder.fromButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        vHolder.fromButton1.setText(hourOfDay+":"+minute);
                    }
                }, hour, min, true).show();
            }
        });

        vHolder.fromButton2.setText(mData.getValue());
        vHolder.fromButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        vHolder.fromButton2.setText(hourOfDay+":"+minute);
                    }
                }, hour, min, true).show();
            }
        });

        vHolder.toButton1.setText(mData.getValue());
        vHolder.toButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        vHolder.toButton1.setText(hourOfDay+":"+minute);
                    }
                }, hour, min, true).show();
            }
        });

        vHolder.toButton2.setText(mData.getValue());
        vHolder.toButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new TimePickerDialog(context, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        vHolder.toButton2.setText(hourOfDay+":"+minute);
                    }
                }, hour, min, true).show();
            }
        });

        return vi;
    }

    private static class ViewHolder {
        public TextView selText;
        public Button fromButton1;
        public Button fromButton2;
        public Button toButton1;
        public Button toButton2;
    }



}
