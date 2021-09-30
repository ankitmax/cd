package com.example.bleswitch;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListAdapter;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;

public class CustomAdapter extends BaseAdapter {
    ArrayList<AdapterData> dataList;
    private Context context;
    private static LayoutInflater inflater = null;
    ArrayList<String> spinValues;

    public CustomAdapter(Context context, ArrayList<AdapterData> data, ArrayList<String> spinValues) {
        // TODO Auto-generated constructor stub
        this.context = context;
        this.dataList = data;
        this.spinValues = spinValues;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public AdapterData getItem(int i) {
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
            vi = inflater.inflate(R.layout.periodlayout, null);
            vHolder.txtName = (TextView) vi.findViewById(R.id.pLinText);
            vHolder.spinner = (Spinner) vi.findViewById(R.id.pLinSpin);
            vi.setTag(vHolder);
        }
        else {
            vHolder = (ViewHolder) vi.getTag();
        }
        final AdapterData mData = getItem(i);

        vHolder.txtName.setText(mData.getName());
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>
                (context,
                android.R.layout.simple_spinner_item,spinValues);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        vHolder.spinner.setAdapter(spinnerAdapter);

        vHolder.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mData.setValue(vHolder.spinner.getSelectedItem().toString());
                Log.i("ITEM", "item position: " + position + " Selected item: " + mData.getValue());
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return vi;
    }

    private static class ViewHolder {
        TextView txtName;
        Spinner spinner;
    }



}
