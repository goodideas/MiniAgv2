package com.kevin.miniagv2.activity;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.kevin.miniagv2.R;
import com.kevin.miniagv2.db.DBCurd;
import com.kevin.miniagv2.entity.ProgrammedBean;
import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.OnReceiveListen;
import com.kevin.miniagv2.utils.SingleUdp;
import com.kevin.miniagv2.utils.SpHelper;
import com.kevin.miniagv2.utils.ToastUtil;
import com.kevin.miniagv2.utils.Util;
import com.kevin.miniagv2.views.ProgrammedItem;

import java.util.ArrayList;
import java.util.List;

public class ProgrammedModeActivity extends AppCompatActivity {

    private static final String TAG = "ProgrammedModeActivity";
    private Button btnProgrammed;
    private LinearLayout.LayoutParams layoutParams = new
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
    private List<ProgrammedItem> programmedItemList = new ArrayList<>();
    private LinearLayout llProgrammedItem;
    private Button btnClearProgrammedItem;
//    private Button btnSubProgrammedItem;
    private int isListSizeEmpty = -1;
    private ScrollView svProgrammedItem;
    private TextView tvCountItem;
    private View emptyView;
    private LinearLayout.LayoutParams params = new
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
    private SingleUdp singleUdp;
    private byte[] sendContent = new byte[11];
    private SpHelper spHelper;
    private DBCurd dbCurd;
    private String revData;
    private MyHandler myHandler;
    private static final int DELAY_SEND_TIME = 200;//50毫秒


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();

        singleUdp = SingleUdp.getUdpInstance();

        if (singleUdp.getIpAddress() == null && spHelper.getSpAgvIp() != null) {
            singleUdp.setUdpIp(spHelper.getSpAgvIp());
            singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
        }
        singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_STOP_TRACK(spHelper.getSpAgvMac()).replace(" ", "")));
        singleUdp.start();
        singleUdp.receiveUdp();
        singleUdp.setOnReceiveListen(new OnReceiveListen() {
            @Override
            public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                String mData = Util.bytes2HexString(data, len);
//                FFAA50C6CF34FE18000002810B00003636393734454541FFFFD9FF55 00~ 512长度 可能出现 前面数据正确，但后面多了0
                if (Util.checkData(mData)||(mData.length()==512&&Util.checkData(mData.substring(0,56)))) {
                    String cmd = mData.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);
//                    Log.e(TAG,"接收的数据="+mData);

                    if (Constant.CMD_PROGRAMMED_CLEAR_RESPOND.equalsIgnoreCase(cmd)) {
                        ToastUtil.customToast(ProgrammedModeActivity.this, "清除成功");
                    }


                    if (Constant.CMD_TRACK_RESPOND.equalsIgnoreCase(cmd)) {
                        ToastUtil.customToast(ProgrammedModeActivity.this, "开始编程");
                    }

                    if (Constant.CMD_SETTING_LOC_RESPOND.equalsIgnoreCase(cmd)) {
                        ToastUtil.customToast(ProgrammedModeActivity.this, "设置成功");
                    }

                    if (Constant.CMD_RFID_CAR_RESPOND.equalsIgnoreCase(cmd)) {
                        revData = mData;
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                int size = programmedItemList.size();
                                boolean isHas = false;
                                if (size != 0) {
                                    for (ProgrammedItem item : programmedItemList) {
                                        if (item.getEtRFID().equalsIgnoreCase(Util.hexStringToAscii(
                                                revData.substring(Constant.DATA_CONTENT_START + 2, Constant.DATA_CONTENT_START + 18)))) {
                                            isHas = true;
                                        }
                                    }
                                }

                                if (!isHas) {

                                    final ProgrammedItem programmedItem = new ProgrammedItem(ProgrammedModeActivity.this);
                                    programmedItem.setLayoutParams(layoutParams);
                                    programmedItem.setTvNumber(String.valueOf(programmedItemList.size() + 1));
                                    programmedItem.setRbSpeed(1);
                                    programmedItem.setEtRFID(Util.hexStringToAscii(
                                            revData.substring(Constant.DATA_CONTENT_START + 2, Constant.DATA_CONTENT_START + 18)));
                                    programmedItem.setEtLoc(revData.substring(Constant.DATA_CONTENT_START, Constant.DATA_CONTENT_START + 2));
//                                    programmedItem.setEtLoc(String.valueOf(programmedItemList.size() + 1));
                                    programmedItem.setmContent(Integer.parseInt(revData.substring(Constant.DATA_CONTENT_START + 18, Constant.DATA_CONTENT_START + 20), 16));
//                                    programmedItem.setOnLongClickListen(new View.OnLongClickListener() {
//                                        @Override
//                                        public boolean onLongClick(View v) {
//                                            for (int i = 0; i < programmedItemList.size(); i++) {
//                                                if (programmedItemList.get(i).getTvNumber() == programmedItem.getTvNumber()) {
//                                                    llProgrammedItem.removeView(programmedItemList.get(i));
//                                                    programmedItemList.remove(i);
//                                                    llProgrammedItem.invalidate();
//                                                    tvCountItem.setText(String.valueOf(programmedItemList.size()));
//                                                    if (programmedItemList.size() == 0) {
//                                                        isListSizeEmpty = -1;
//                                                        llProgrammedItem.addView(emptyView);
//                                                    }
//                                                }
//                                            }
//
//                                            return true;
//                                        }
//                                    });
                                    programmedItemList.add(programmedItem);
                                    llProgrammedItem.addView(programmedItem);
                                    llProgrammedItem.invalidate();
                                    if (isListSizeEmpty == -1) {
                                        llProgrammedItem.removeView(emptyView);
                                        isListSizeEmpty = 1;
                                    }

                                    tvCountItem.setText(String.valueOf(programmedItemList.size()));
                                    svProgrammedItem.smoothScrollTo(0, llProgrammedItem.getBottom());
                                }
                            }
                        });


                    }
                }
            }
        });

//        btnSubProgrammedItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                final ProgrammedItem programmedItem = new ProgrammedItem(ProgrammedModeActivity.this);
//                programmedItem.setLayoutParams(layoutParams);
//                programmedItem.setEtRFID(String.valueOf(programmedItemList.size() + 1)+"2345678");
//                programmedItem.setTvNumber(String.valueOf(programmedItemList.size() + 1));
//                programmedItem.setEtLoc(String.valueOf(programmedItemList.size() + 1));
//                programmedItem.setOnLongClickListener(new View.OnLongClickListener() {
//                    @Override
//                    public boolean onLongClick(View v) {
//                        for (int i = 0; i < programmedItemList.size(); i++) {
//                            if (programmedItemList.get(i).getTvNumber() == programmedItem.getTvNumber()) {
//                                llProgrammedItem.removeView(programmedItemList.get(i));
//                                programmedItemList.remove(i);
//                                llProgrammedItem.invalidate();
//                                tvCountItem.setText(String.valueOf(programmedItemList.size()));
//                                if (programmedItemList.size() == 0) {
//                                    isListSizeEmpty = -1;
//                                    llProgrammedItem.addView(emptyView);
//                                }
//                            }
//                        }
//
//                        return true;
//                    }
//                });
//                programmedItemList.add(programmedItem);
//                llProgrammedItem.addView(programmedItem);
//                llProgrammedItem.invalidate();
//                if (isListSizeEmpty == -1) {
//                    llProgrammedItem.removeView(emptyView);
//                    isListSizeEmpty = 1;
//                }
//
//                tvCountItem.setText(String.valueOf(programmedItemList.size()));
//                svProgrammedItem.smoothScrollTo(0, llProgrammedItem.getBottom());
//
//            }
//        });


//        btnSubProgrammedItem.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (programmedItemList.size() == 0) {
//                    ToastUtil.customToast(ProgrammedModeActivity.this, "列表为空！！！");
//                } else {
//                    llProgrammedItem.removeView(programmedItemList.get(programmedItemList.size() - 1));
//                    programmedItemList.remove(programmedItemList.size() - 1);
//                    llProgrammedItem.invalidate();
//                    if (programmedItemList.size() == 0) {
//                        isListSizeEmpty = -1;
//                        btnSubProgrammedItem.setVisibility(View.GONE);
//                        llProgrammedItem.addView(emptyView);
//                    }
//                    tvCountItem.setText(String.valueOf(programmedItemList.size()));
//
//                }
//
//            }
//        });

        btnClearProgrammedItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(singleUdp!=null&&singleUdp.getIpAddress()!=null&&spHelper!=null&&spHelper.getSpAgvMac()!=null){
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_PROGRAMMED_CLEAR(spHelper.getSpAgvMac()).replace(" ", "")));
                }
                dbCurd.delALLProgrammedData();
                programmedItemList.clear();
                llProgrammedItem.removeAllViews();
                llProgrammedItem.addView(emptyView);
                tvCountItem.setText(String.valueOf(programmedItemList.size()));
                isListSizeEmpty = -1;
            }
        });


        btnProgrammed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int size = programmedItemList.size();
                if (size == 0) {
                    ToastUtil.customToast(ProgrammedModeActivity.this, "数据为空！");
                } else {
                    ProgrammedItem programmedItem;
                    for (int i = 0; i < size; i++) {
                        programmedItem = programmedItemList.get(i);
//                        //判断输入的内容有没有空的
                        if (TextUtils.isEmpty(programmedItem.getEtLoc()) || TextUtils.isEmpty(programmedItem.getEtRFID())) {
                            ToastUtil.customToast(ProgrammedModeActivity.this, "数据不能为空！");
                        } else {
                            byte[] program = Util.HexString2Bytes(Constant.SEND_DATA_SETTING_RFID(spHelper.getSpAgvMac()).replace(" ", ""));

                            try {
                                //位置
                                sendContent[0] = Byte.parseByte(programmedItem.getEtLoc());
                                //RFID卡8字节
                                byte[] rfidByte = Util.HexString2Bytes(Util.asciiToHexString(programmedItem.getEtRFID()));
                                System.arraycopy(rfidByte, 0, sendContent, 1, rfidByte.length);
                                sendContent[9] = Byte.parseByte(String.valueOf(programmedItem.getSpinnerSelect()));
                                sendContent[10] = Byte.parseByte(String.valueOf(programmedItem.getRbSpeed()));
                                System.arraycopy(sendContent, 0, program, 14, sendContent.length);
                                program[25] = Util.CheckCode(Util.bytes2HexString(sendContent, sendContent.length));
                                final byte[] sendByte = program;
                                //如果数据库有这个数据，就不加了，变为更新
                                boolean databaseHas = false;
                                int pid = 0;
                                for (ProgrammedBean bean : dbCurd.getAllProgrammedData()) {
                                    if (programmedItem.getEtRFID().equalsIgnoreCase(bean.getmRfid())) {
                                        databaseHas = true;
                                        pid = bean.getPid();
                                    }
                                }
                                if (databaseHas) {
                                    dbCurd.upDataProgrammedData(new ProgrammedBean(
                                            programmedItem.getEtRFID(),
                                            programmedItem.getEtLoc(),
                                            String.valueOf(programmedItem.getRbSpeed()),
                                            programmedItem.getmContent(),
                                            true + ""
                                    ), String.valueOf(pid));
                                    Log.e(TAG,"update"
                                            +" rfif="+programmedItem.getEtRFID()
                                            +" getEtLoc="+programmedItem.getEtLoc()
                                            +" getRbSpeed="+String.valueOf(programmedItem.getRbSpeed())
                                            +" getmContent="+programmedItem.getmContent()
                                            +" pid="+pid
                                    );
                                } else {
                                    dbCurd.addProgrammedData(
                                            programmedItem.getEtRFID(),
                                            programmedItem.getEtLoc(),
                                            String.valueOf(programmedItem.getRbSpeed()),
                                            programmedItem.getmContent(),
                                            true + ""
                                    );
                                }
                                myHandler.postDelayed(new Runnable() {
                                    @Override
                                    public void run() {
                                        singleUdp.send(sendByte);
                                    }
                                }, i * DELAY_SEND_TIME);

//                                singleUdp.send(program);


                                Log.e(TAG, programmedItem.getEtLoc() + " " + programmedItem.getEtRFID() + " " + programmedItem.getSpinnerSelect() + " " + programmedItem.getRbSpeed() + " ");
//                                ToastUtil.customToast(ProgrammedModeActivity.this, programmedItem.getEtLoc() + " " + programmedItem.getEtRFID() + " " + programmedItem.getmContent() + " " + programmedItem.getRbSpeed() + " ");

                            } catch (Exception e) {
                                ToastUtil.customToast(ProgrammedModeActivity.this, "数据格式错误，请检查数据是否正确");
                            }

                        }

                    }
                }

            }
        });

    }

    private void init() {
        setContentView(R.layout.activity_programmed_mode);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("RFID编程");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("选择功能");
        }

        llProgrammedItem = (LinearLayout) findViewById(R.id.llProgrammedItem);
        btnClearProgrammedItem = (Button) findViewById(R.id.btnAddProgrammedItem);
        svProgrammedItem = (ScrollView) findViewById(R.id.svProgrammedItem);
        tvCountItem = (TextView) findViewById(R.id.tvCountItem);
        btnProgrammed = (Button) findViewById(R.id.btnProgrammed);
//        btnSubProgrammedItem = (Button) findViewById(R.id.btnSubProgrammedItem);


        spHelper = new SpHelper(this);
        btnProgrammed.getLayoutParams().width = Util.getScreenWidth(this) / 3;
        dbCurd = DBCurd.getInstance(this);

        List<ProgrammedBean> beanList = dbCurd.getAllProgrammedData();
        int beanListLength = beanList.size();
        for (int i = 0; i < beanListLength; i++) {
            ProgrammedItem item = new ProgrammedItem(this);
            item.setLayoutParams(layoutParams);
            item.setEtLoc(beanList.get(i).getmLoc());
            item.setEtRFID(beanList.get(i).getmRfid());
            item.setTvNumber(String.valueOf(i + 1));
            item.setmContent(Util.getSpinnerSelect(beanList.get(i).getmContent()));
            item.setRbSpeed(Integer.parseInt(beanList.get(i).getmSpeed()));
            programmedItemList.add(item);
            llProgrammedItem.addView(item);
            llProgrammedItem.invalidate();

            tvCountItem.setText(String.valueOf(programmedItemList.size()));
            svProgrammedItem.smoothScrollTo(0, llProgrammedItem.getBottom());
        }

        params.gravity = Gravity.CENTER;
        emptyView = LayoutInflater.from(this).inflate(R.layout.programmed_list_empty_layout, null);
        if (programmedItemList.size() == 0) {
            llProgrammedItem.addView(emptyView, params);
        }
        myHandler = new MyHandler(Looper.myLooper());
    }

    @Override
    protected void onStart() {
        super.onStart();
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


    private class MyHandler extends Handler {
        public MyHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    break;
                case 2:
                    break;
                case 3:
                    break;
                case 4:
                    break;
                case 5:
                    break;
            }


        }
    }

}


//FFAA 22E9167FCF5C0000 0600 0B00       01 3436303037363338 02 01       A6 FF55
//FFAA 22E9167FCF5C0000 0600 0B00       02 3139303036393232 02 01       A2 FF55



//FFAA 22E9167FCF5C0000 0281 0B00      01 3436303037363338 02 01    A6FF55


//FFAA 22E9167FCF5C0000 0281 0B00 00 3139303036393232 FF FF        9BFF55
//FFAA 22E9167FCF5C0000 0281 0B00 01 3436303037363338 02 01        A6FF55
//FFAA 22E9167FCF5C0000 0281 0B00 02 3436303037363235 03 01        A4FF55
//FFAA 22E9167FCF5C0000 0281 0B00 00 3341303033323337 FF FF        A1FF55