package com.example.bleswitch;

import android.widget.BaseAdapter;

import java.util.ArrayList;

public class TimeFrameDataModel {
    private String Value;
    private int sel;
    private String app1data;
    private String app2data;

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

    public void setApp1data(String app1data) {
        this.app1data = app1data;
    }

    public void setApp2data(String app2data) {
        this.app2data = app2data;
    }

    public String getApp1data() {
        return this.app1data;
    }

    public String getApp2data() {
        return this.app2data;
    }
}
