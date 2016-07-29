package com.kevin.miniagv2.entity;

import java.io.Serializable;

/**
 * Created by Administrator
 * on 2016/6/29.
 */
public class AgvBean implements Serializable{

    private String gavId;
    private String gavMac;
    private String gavIp;

    public String getGavIp() {
        return gavIp;
    }

    public void setGavIp(String gavIp) {
        this.gavIp = gavIp;
    }

    public String getGavMac() {
        return gavMac;
    }

    public void setGavMac(String gavMac) {
        this.gavMac = gavMac;
    }

    public String getGavId() {
        return gavId;
    }

    public void setGavId(String gavId) {
        this.gavId = gavId;
    }

}
