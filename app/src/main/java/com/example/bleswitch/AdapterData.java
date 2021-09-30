package com.example.bleswitch;

public class AdapterData {
    private String textName;
    private String value;

    public AdapterData(String textName, String value) {
        this.textName = textName;
        this.value = value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public void setName(String name) {
        this.textName = name;
    }

    public String getName() {
        return textName;
    }
}
