package com.kevin.miniagv2.db;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;
import android.util.Log;

import com.kevin.miniagv2.entity.AgvBean;
import com.kevin.miniagv2.entity.ProgrammedBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhuangbinbin
 * on 2016/1/5.
 */
public class DBCurd implements InterfaceDBCurd {

    private static final String TAG = "DBCurd";
    private SQLiteDatabase databaseWrite, databaseRead;

    private static DBCurd instance;

    private DBCurd(Context context) {
        DBHelper dbHelper = new DBHelper(context, DBConstant.DB_NAME, null, DBConstant.DB_VERSION);
        //读和写分开处理
        databaseRead = dbHelper.getReadableDatabase();
        databaseWrite = dbHelper.getWritableDatabase();
    }

    public static DBCurd getInstance(Context context){
        if(instance==null){
            instance = new DBCurd(context);
        }
        return instance;
    }


    @Override
    public void addProgrammedData(String mRfid,String mLoc,String mSpeed,String mContent,String isRecord) {
        if (databaseWrite != null) {
            databaseWrite.beginTransaction();//开启事务
            try {

                databaseWrite.execSQL(DBConstant.INSERT_PROGRAMMED_SQL, new Object[]{
                        mRfid, mLoc, mSpeed, mContent,isRecord
                });
                databaseWrite.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                databaseWrite.endTransaction();
            }
        }
    }

    @Override
    public void delProgrammedData(String pid) {
        if (databaseWrite != null) {
            try {
                databaseWrite.beginTransaction();
                databaseWrite.execSQL(DBConstant.DEL_PROGRAMMED_BY_ID_SQL, new Object[]{pid});
                databaseWrite.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                databaseWrite.endTransaction();
            }

        }
    }

    @Override
    public void delALLProgrammedData() {
        if (databaseWrite != null) {
            databaseWrite.execSQL(DBConstant.DEL_ALL_PROGRAMMED_SQL);
        }
    }

    @Override
    public List<ProgrammedBean> getAllProgrammedData() {
        List<ProgrammedBean> programmedBeanList = new ArrayList<>();
        ProgrammedBean programmedBean;
        Cursor cursor;
        if (databaseRead != null) {
            cursor = databaseRead.rawQuery(DBConstant.SELECT_ALL_PROGRAMMED_SQL, null);
            if (cursor.moveToFirst()) {
                do {
                    programmedBean = new ProgrammedBean();
                    programmedBean.setPid(cursor.getInt(cursor.getColumnIndex(DBConstant.PD_ID)));
                    programmedBean.setmRfid(cursor.getString(cursor.getColumnIndex(DBConstant.PD_RFID)));
                    programmedBean.setmLoc(cursor.getString(cursor.getColumnIndex(DBConstant.PD_LOC)));
                    programmedBean.setmSpeed(cursor.getString(cursor.getColumnIndex(DBConstant.PD_SPEED)));
                    programmedBean.setmContent(cursor.getString(cursor.getColumnIndex(DBConstant.PD_CONTENT)));
                    programmedBean.setmIsRecord(cursor.getString(cursor.getColumnIndex(DBConstant.PD_IS_RECORD)));
                    programmedBeanList.add(programmedBean);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return programmedBeanList;
    }

    @Override
    public void upDataProgrammedData(ProgrammedBean programmedBean, String pid) {
        if (databaseWrite != null) {
            try {
                databaseWrite.beginTransaction();
                databaseWrite.execSQL(DBConstant.UP_DATA_PROGRAMMED_SQL, new Object[]{
                        programmedBean.getmRfid(),
                        programmedBean.getmLoc(),
                        programmedBean.getmSpeed(),
                        programmedBean.getmContent(),
                        programmedBean.getmIsRecord(),
                        pid});
                databaseWrite.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e(TAG,"更新失败="+e.toString());
            } finally {
                databaseWrite.endTransaction();
            }

        }
    }

    @Override
    public ProgrammedBean getProgrammedDataById(String pid) {
        ProgrammedBean programmedBean = new ProgrammedBean();
        Cursor cursor;
        if (databaseRead != null) {
            cursor = databaseRead.rawQuery(DBConstant.SELECT_ONE_PROGRAMMED_BY_ID_SQL, new String[]{pid});
            if (cursor.moveToFirst()) {
                do {
                    programmedBean.setmRfid(cursor.getString(cursor.getColumnIndex(DBConstant.PD_RFID)));
                    programmedBean.setmLoc(cursor.getString(cursor.getColumnIndex(DBConstant.PD_LOC)));
                    programmedBean.setmSpeed(cursor.getString(cursor.getColumnIndex(DBConstant.PD_SPEED)));
                    programmedBean.setmContent(cursor.getString(cursor.getColumnIndex(DBConstant.PD_CONTENT)));
                    programmedBean.setmIsRecord(cursor.getString(cursor.getColumnIndex(DBConstant.PD_IS_RECORD)));
                } while (cursor.moveToNext());
            }
            cursor.close();
            if (TextUtils.isEmpty(programmedBean.getmRfid())
                    || TextUtils.isEmpty(programmedBean.getmLoc())
                    || TextUtils.isEmpty(programmedBean.getmSpeed())
                    || TextUtils.isEmpty(programmedBean.getmContent())
                    || TextUtils.isEmpty(programmedBean.getmIsRecord())
                    ) {
                programmedBean =  null;
            }
        }
        return programmedBean;
    }

    @Override
    public void addTempAgvData(String mac, String id, String ip) {
        if (databaseWrite != null) {
            databaseWrite.beginTransaction();//开启事务
            try {

                databaseWrite.execSQL(DBConstant.INSERT_TEMP_AGV_SQL, new Object[]{
                        mac, id, ip
                });
                databaseWrite.setTransactionSuccessful();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                databaseWrite.endTransaction();
            }
        }
    }

    @Override
    public void delALLTempAgvData() {
        if (databaseWrite != null) {
            databaseWrite.execSQL(DBConstant.DEL_ALL_TEMP_AGV_SQL);
        }
    }

    @Override
    public List<AgvBean> getAllTempAgvData() {
        List<AgvBean> agvBeans = new ArrayList<>();
        AgvBean agvBean;
        Cursor cursor;
        if (databaseRead != null) {
            cursor = databaseRead.rawQuery(DBConstant.SELECT_ALL_TEMP_AGV_SQL, null);
            if (cursor.moveToFirst()) {
                do {
                    agvBean = new AgvBean();
                    agvBean.setGavMac(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_MAC)));
                    agvBean.setGavId(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_ID)));
                    agvBean.setGavIp(cursor.getString(cursor.getColumnIndex(DBConstant.TEMP_AGV_IP)));

                    agvBeans.add(agvBean);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return agvBeans;
    }

}
