package com.pojosontheweb.selenium;

public enum Browsr {

    Chrome("chrome"),
    Firefox("firefox");

    private final String sysProp;

    Browsr(String sysProp) {
        this.sysProp = sysProp;
    }

    public String getSysProp() {
        return sysProp;
    }
}
