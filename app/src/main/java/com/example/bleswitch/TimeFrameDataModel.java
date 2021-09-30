package com.example.bleswitch;

import android.widget.BaseAdapter;

import java.util.ArrayList;

public class TimeFrameDataModel {
    private String Value;
    private int sel;
    public TimeFrameDataModel(int sel, String value) {
        this.Value  = value;
        this.sel    = sel;
    }
    public  void setSel(int value) {
        this.sel = value;
    }

    public void setValue(String value) {
        this.Value = value;
    }

    public int getSel() {
        return this.sel;
    }

    public  String getValue() {
        return Value;
    }
}
