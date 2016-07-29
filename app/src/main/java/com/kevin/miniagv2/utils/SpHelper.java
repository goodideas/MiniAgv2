package com.kevin.miniagv2.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

/**
 * Author zhuangbinbin
 * Time 2016/1/16.
 */
public class SpHelper {


    private final String spFileName = "miniAgv";
    private final String spAgvId = "agvId";
    private final String spAgvIp = "agvIp";
    private final String spAgvMac = "agvMac";

    private SharedPreferences agvShared;
    private SharedPreferences.Editor agvEditor;

    public SpHelper(Context context) {
        agvShared = context.getSharedPreferences(
                spFileName, Activity.MODE_PRIVATE);
    }

    //agvId
    public String getSpAgvId() {
        return agvShared.getString(spAgvId, null);
    }

    public void saveSpAgvId(String agvId) {
        agvEditor = agvShared.edit();
        agvEditor.putString(spAgvId, agvId);
        agvEditor.apply();

    }


    //agvIp
    public String getSpAgvIp() {
        return agvShared.getString(spAgvIp, null);
    }

    public void saveSpAgvIp(String agvIp) {
        agvEditor = agvShared.edit();
        agvEditor.putString(spAgvIp, agvIp);
        agvEditor.apply();

    }

    //agvMac
    public String getSpAgvMac() {
        return agvShared.getString(spAgvMac, null);
    }

    public void saveSpAgvMac(String agvMac) {
        agvEditor = agvShared.edit();
        agvEditor.putString(spAgvMac, agvMac);
        agvEditor.apply();
    }



}
