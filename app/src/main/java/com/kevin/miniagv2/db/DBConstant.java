package com.kevin.miniagv2.db;

/**
 * Created by zhuangbinbin
 * on 2016/1/5.
 */
public class DBConstant {
    public static final String TABLE_NAME = "programmed";
    public static final String DB_NAME = "miniAgv.db";
    public static int DB_VERSION = 2;

    public static final String PD_ID = "pd_id";
    public static final String PD_RFID = "pd_rfid";
    public static final String PD_LOC = "pd_loc";
    public static final String PD_SPEED = "pd_speed";
    public static final String PD_CONTENT = "pd_content";
    public static final String PD_IS_RECORD = "pd_is_record";


    //创建数据表
    public static final String CREATE_PROGRAMMED_DB_SQL = "create table " + TABLE_NAME + "("
            + PD_ID + " integer primary key autoincrement,"
            + PD_RFID + " varchar(20),"
            + PD_LOC + " varchar(20),"
            + PD_SPEED + " varchar(20),"
            + PD_CONTENT + " varchar(20),"
            + PD_IS_RECORD + " varchar(20)"
            + ")";

    //插入数据
    public static final String INSERT_PROGRAMMED_SQL = "insert into " + TABLE_NAME + "("
            + PD_RFID + ","
            + PD_LOC + ","
            + PD_SPEED + ","
            + PD_CONTENT + ","
            + PD_IS_RECORD +
            ") values (?,?,?,?,?)";

    //查找所有数据
    public static final String SELECT_ALL_PROGRAMMED_SQL = "select * from " + TABLE_NAME;

    //更新数据
    public static final String UP_DATA_PROGRAMMED_SQL = "update " + TABLE_NAME
            + " set " + PD_RFID + " = ? ," + PD_LOC + " = ? ," + PD_SPEED + " = ? ," + PD_CONTENT + " = ? ," + PD_IS_RECORD + " = ?"
            + " where " + PD_ID + " = ?";

    //根据ID查找数据
    public static final String SELECT_ONE_PROGRAMMED_BY_ID_SQL = "select * from " + TABLE_NAME + " where " + PD_ID + " = ?";

    //删除所有数据
    public static final String DEL_ALL_PROGRAMMED_SQL = "delete from " + TABLE_NAME;

    //根据ID删除数据
    public static final String DEL_PROGRAMMED_BY_ID_SQL = "delete from " + TABLE_NAME
            + " where " + PD_ID + " = ?";

    /*************************
     * 小车临时列表
     ***************************************/
    public static final String TEMP_AGV_TABLE_NAME = "agv_temp";
    public static final String TEMP_AGV_ID = "agv_id";
    public static final String TEMP_AGV_MAC = "agv_mac";
    public static final String TEMP_AGV_IP = "agv_ip";

    //创建数据表
    public static final String CREATE_TEMP_AGV_DB_SQL = "create table " + TEMP_AGV_TABLE_NAME + "("
            + TEMP_AGV_MAC + " varchar(20) primary key ,"
            + TEMP_AGV_ID + " varchar(20),"
            + TEMP_AGV_IP + " varchar(20)"
            + ")";
    //查找所有数据
    public static final String SELECT_ALL_TEMP_AGV_SQL = "select * from " + TEMP_AGV_TABLE_NAME;
    //删除所有数据
    public static final String DEL_ALL_TEMP_AGV_SQL = "delete from " + TEMP_AGV_TABLE_NAME;
    //插入数据
    public static final String INSERT_TEMP_AGV_SQL = "insert into " + TEMP_AGV_TABLE_NAME + "("
            + TEMP_AGV_MAC + ","
            + TEMP_AGV_ID + ","
            + TEMP_AGV_IP +
            ") values (?,?,?)";

}
