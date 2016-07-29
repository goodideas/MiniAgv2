package com.kevin.miniagv2.entity;

import com.kevin.miniagv2.utils.Util;

/**
 * Created by Administrator
 * on 2016/7/26.
 */
public class ProgrammedBean {



    private int Pid;
    private String mRfid;
    private String mLoc;
    private String mSpeed;
    private String mContent;
    private String mIsRecord;

    public ProgrammedBean(){}
    public ProgrammedBean(String rfid,String loc,String speed,String content,String isRecord){
        this.mRfid = rfid;
        this.mLoc = loc;
        this.mSpeed = speed;
        this.mContent = content;
        this.mIsRecord = isRecord;
    }




    public int getPid() {
        return Pid;
    }

    public void setPid(int pid) {
        Pid = pid;
    }

    public String getmSpeed() {
        return mSpeed;
    }

    public void setmSpeed(String mSpeed) {
        this.mSpeed = mSpeed;
    }

    public String getmRfid() {
        return mRfid;
    }

    public void setmRfid(String mRfid) {
        this.mRfid = mRfid;
    }

    public String getmLoc() {
        return mLoc;
    }

    public void setmLoc(String mLoc) {
        this.mLoc = mLoc;
    }

    public String getmContent() {
        return mContent;
    }

    public void setmContent(String mContent) {
        this.mContent = mContent;
    }

    public String getmIsRecord() {
        return mIsRecord;
    }

    public void setmIsRecord(String mIsRecord) {
        this.mIsRecord = mIsRecord;
    }

    @Override
    public String toString() {
        return "位置:"+mLoc+" 速度:"+mSpeed+" 内容:"+mContent+" RFID:"+ mRfid+"";
    }

}
