package com.kevin.miniagv2.utils;

import android.support.annotation.Nullable;

/**
 * Created by Administrator
 * on 2016/7/5.
 */
public interface OnReceiveListen {
    void onReceiveData(byte[] data, int len, @Nullable String remoteIp);
}

