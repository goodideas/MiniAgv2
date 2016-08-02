package com.kevin.miniagv2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.kevin.miniagv2.R;
import com.kevin.miniagv2.db.DBCurd;
import com.kevin.miniagv2.entity.AgvBean;
import com.kevin.miniagv2.entity.ProgrammedBean;
import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.OnReceiveListen;
import com.kevin.miniagv2.utils.SingleUdp;
import com.kevin.miniagv2.utils.SpHelper;
import com.kevin.miniagv2.utils.ToastUtil;
import com.kevin.miniagv2.utils.Util;
import com.kevin.miniagv2.views.ArrayWheelAdapter;
import com.kevin.miniagv2.views.WheelAdapter;
import com.kevin.miniagv2.views.WheelView;

import java.util.ArrayList;
import java.util.List;


public class AutoModeActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AutoModeActivity";

    private Button btnStartTracking;
    private Button btnStopTracking;
    private Button btnStop;
    private Button btnGetAgvData;
    private Button btnEditProgrammed;

    private TextView tvLeftWheelSpeed, tvDistance, tvRightWheelSpeed, tvAgvMode, tvAgvStatus, tvErrorStatus, tvRFID;
    private EditText etLoc;


    private int rbSpeed = 1;

    private SingleUdp singleUdp;
    private SpHelper spHelper;

    private WheelView wheelProgrammed;
    private DBCurd dbCurd;

    private List<String> sList = new ArrayList<>();
    private ArrayWheelAdapter arrayWheelAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auto_mode);
        init();

        btnStartTracking.setOnClickListener(this);
        btnStopTracking.setOnClickListener(this);
        btnEditProgrammed.setOnClickListener(this);
        btnStop.setOnClickListener(this);
        btnGetAgvData.setOnClickListener(this);
        singleUdp = SingleUdp.getUdpInstance();
        if (singleUdp.getIpAddress() == null) {
            if (!TextUtils.isEmpty(spHelper.getSpAgvIp())) {
                singleUdp.setUdpIp(spHelper.getSpAgvIp());
                singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
            }

        } else {
            singleUdp.start();
            singleUdp.receiveUdp();
            singleUdp.setOnReceiveListen(new OnReceiveListen() {
                @Override
                public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                    final String mData = Util.bytes2HexString(data, len);
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            analysis(mData);
                        }
                    });
                }
            });
        }

        List<ProgrammedBean> pList = dbCurd.getAllProgrammedData();
        if (pList.size() != 0) {
            for (ProgrammedBean programmedBean : pList) {
                sList.add(programmedBean.toString());
            }
        }

        arrayWheelAdapter = new ArrayWheelAdapter(sList);
        wheelProgrammed.setAdapter(arrayWheelAdapter);
        wheelProgrammed.setCyclic(true);
        wheelProgrammed.TEXT_SIZE = (int) (17 * Util.getScreenDensity(this));
        Log.e(TAG, "den=" + Util.getScreenDensity(this));
        wheelProgrammed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ToastUtil.customToast(AutoModeActivity.this, "长按");
            }
        });

    }

    private void init() {
        btnStartTracking = (Button) findViewById(R.id.btnStartTracking);
        btnStopTracking = (Button) findViewById(R.id.btnStopTracking);
        btnEditProgrammed = (Button) findViewById(R.id.btnEditProgrammed);
        btnStop = (Button) findViewById(R.id.btnStop);
        btnGetAgvData = (Button) findViewById(R.id.btnGetAgvData);
        tvLeftWheelSpeed = (TextView) findViewById(R.id.tvLeftWheelSpeed);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvRightWheelSpeed = (TextView) findViewById(R.id.tvRightWheelSpeed);
//        etLoc = (EditText) findViewById(R.id.etAutoLoc);

        tvLeftWheelSpeed = (TextView) findViewById(R.id.tvLeftWheelSpeed);
        tvDistance = (TextView) findViewById(R.id.tvDistance);
        tvRightWheelSpeed = (TextView) findViewById(R.id.tvRightWheelSpeed);
        tvAgvMode = (TextView) findViewById(R.id.tvAgvMode);
        tvAgvStatus = (TextView) findViewById(R.id.tvAgvStatus);
        tvErrorStatus = (TextView) findViewById(R.id.tvErrorStatus);
        tvRFID = (TextView) findViewById(R.id.tvRFID);

        wheelProgrammed = (WheelView) findViewById(R.id.wheelProgrammed);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("自动模式");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("选择功能");
        }

        spHelper = new SpHelper(this);
        dbCurd = DBCurd.getInstance(this);
    }

    private void analysis(String data) {
        if (Util.checkData(data)) {
            Log.e(TAG, "数据=" + data);
            String cmd = data.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);
            //开始循迹或者停止循迹
            if (Constant.CMD_TRACK_RESPOND.equalsIgnoreCase(cmd)) {
                ToastUtil.customToast(AutoModeActivity.this, "开始/停止循迹");
                //急停
            } else if (Constant.CMD_UNLOCK_RESPOND.equalsIgnoreCase(cmd)) {
                ToastUtil.customToast(AutoModeActivity.this, "急停");
                Intent in = new Intent();
                in.putExtra(Constant.INTENT_NAME, Constant.INTENT_VALUE);
                setResult(Constant.AUTO_MODE_TO_FUNCTION_MENU_RESULT_CODE, in);
                AutoModeActivity.this.finish();

                //查询数据
            } else if (Constant.CMD_QUERY_RESPOND.equalsIgnoreCase(cmd)) {
                //18byte
                //agv模式 1byte
                //agv状态 1byte
                //错误状态 1byte
                //左轮速度 2byte
                //右轮速度 2byte
                //光电左 1byte
                //光电右 1byte
                //距离 1byte
                //RFID卡号 8byte
                String agvMode = data.substring(Constant.DATA_CONTENT_START, Constant.DATA_CONTENT_START + 2);
                String agvStatus = data.substring(Constant.DATA_CONTENT_START + 2, Constant.DATA_CONTENT_START + 4);
                String agvErrorStatus = data.substring(Constant.DATA_CONTENT_START + 4, Constant.DATA_CONTENT_START + 6);
                String leftWheelSpeed = data.substring(Constant.DATA_CONTENT_START + 6, Constant.DATA_CONTENT_START + 10);
                String rightWheelSpeed = data.substring(Constant.DATA_CONTENT_START + 10, Constant.DATA_CONTENT_START + 14);

                String distance = data.substring(Constant.DATA_CONTENT_START + 18, Constant.DATA_CONTENT_START + 20);
                String RFID = data.substring(Constant.DATA_CONTENT_START + 20, Constant.DATA_CONTENT_START + 36);

                int modeInt = Integer.parseInt(agvMode, 16);
                String mMode = "";
                switch (modeInt) {
                    case 0:
                        mMode = "停车";
                        break;
                    case 1:
                        mMode = "手动模式";
                        break;
                    case 2:
                        mMode = "循迹模式";
                        break;
                    case 3:
                        mMode = "急停";
                        break;
                }
                tvAgvMode.setText(mMode);
                int statusInt = Integer.parseInt(agvStatus, 16);
                String mStatus = "";
                switch (statusInt) {
                    case 0:
                        mStatus = "停止";
                        break;
                    case 1:
                        mStatus = "前进";
                        break;
                    case 2:
                        mStatus = "左转";
                        break;
                    case 3:
                        mStatus = "右转";
                        break;
                }
                tvAgvStatus.setText(mStatus);

                int errorStatusInt = Integer.parseInt(agvErrorStatus, 16);
                String mErrorStatus = "";
                switch (errorStatusInt) {
                    case 0:
                        mErrorStatus = "无";
                        break;
                    case 1:
                        mErrorStatus = "距离过近";
                        break;
                    case 2:
                        mErrorStatus = "脱轨";
                        break;
                }
                tvErrorStatus.setText(mErrorStatus);

                int leftInt = Integer.parseInt(leftWheelSpeed.substring(0, 2), 16) +
                        Integer.parseInt(leftWheelSpeed.substring(2, 4), 16) * 256;
                tvLeftWheelSpeed.setText(String.valueOf(leftInt));

                int rightInt = Integer.parseInt(rightWheelSpeed.substring(0, 2), 16) +
                        Integer.parseInt(rightWheelSpeed.substring(2, 4), 16) * 256;

                tvRightWheelSpeed.setText(String.valueOf(rightInt));

                int disInt = Integer.parseInt(distance, 16);
                tvDistance.setText(String.valueOf(disInt));

                tvRFID.setText(Util.hexStringToAscii(RFID));

                //自动返回数据RFID
            } else if (Constant.CMD_RFID_CAR_RESPOND.equalsIgnoreCase(cmd)) {

                String rfid = data.substring(Constant.DATA_CONTENT_START + 2, Constant.DATA_CONTENT_START + 18);

                int getI = getLoc(sList, Util.hexStringToAscii(rfid));
                if (getI != -1) {
                    wheelProgrammed.setCurrentItem(getI);
                }


                //错误状态自动返回
            } else if (Constant.CMD_ERROR_STATU_RESPOND.equalsIgnoreCase(cmd)) {
                String autoErrorStatus = data.substring(Constant.DATA_CONTENT_START, Constant.DATA_CONTENT_START + 2);
                int autoErrorStatusInt = Integer.parseInt(autoErrorStatus, 16);
                String mAutoErrorStatus = "";
                switch (autoErrorStatusInt) {
                    case 0:
                        mAutoErrorStatus = "无";
                        break;
                    case 1:
                        mAutoErrorStatus = "距离过近";
                        break;
                    case 2:
                        mAutoErrorStatus = "脱轨";
                        break;
                }

                tvErrorStatus.setText(mAutoErrorStatus);
                //停在指定位置
            } else if (Constant.CMD_STOP_LOC_RESPOND.equalsIgnoreCase(cmd)) {
                ToastUtil.customToast(AutoModeActivity.this, "停在指定位置");
            }


        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "start");
        if(wheelProgrammed!=null&&sList!=null){
            Log.e(TAG, "wheel!=null size=" + sList.size());
            arrayWheelAdapter = new ArrayWheelAdapter(sList);
            wheelProgrammed.setAdapter(arrayWheelAdapter);
        }


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnStartTracking:
                if (singleUdp != null) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_START_TRACK_S1(spHelper.getSpAgvMac()).replace(" ", "")));
                }
                break;
            case R.id.btnStopTracking:
                if (singleUdp != null) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_STOP_TRACK(spHelper.getSpAgvMac()).replace(" ", "")));
                }
                break;

            case R.id.btnGetAgvData:
                if (singleUdp != null) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_QUERY(spHelper.getSpAgvMac()).replace(" ", "")));
                }
                break;

            case R.id.btnStop:
                if (singleUdp != null) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_LOCK(spHelper.getSpAgvMac()).replace(" ", "")));
                }
                break;
            case R.id.btnEditProgrammed:
                startActivity(new Intent(AutoModeActivity.this, ProgrammedModeActivity.class));
                break;
        }
    }

    private int getLoc(List<String> list, String conString) {
        int getLoc = -1;
        int size = list.size();
        for (int i = 0; i < size; i++) {
            if (list.get(i).contains(conString)) {
                getLoc = i;
            }
        }
        return getLoc;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.e(TAG, "onDestroy()");
    }
}
