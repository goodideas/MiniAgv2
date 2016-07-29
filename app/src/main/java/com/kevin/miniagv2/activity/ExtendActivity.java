package com.kevin.miniagv2.activity;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.kevin.miniagv2.R;

import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.OnReceiveListen;
import com.kevin.miniagv2.utils.SingleUdp;
import com.kevin.miniagv2.utils.SpHelper;
import com.kevin.miniagv2.utils.ToastUtil;
import com.kevin.miniagv2.utils.Util;
import com.kevin.miniagv2.views.ColorPicker;


public class ExtendActivity extends AppCompatActivity implements View.OnClickListener, View.OnLongClickListener {

    private static final String TAG = "ExtendActivity";
    private static final int OFFSET = 20;
    private ColorPicker colorPicker;
    private SingleUdp singleUdp;
    private int colorR, colorG, colorB;
    private byte[] colorBytes = new byte[4];
    private byte[] sendColor;
    private Button btnAdd;
    private Button btnReduce;
    private TextView tvTime;
    private int timeColor = 5;
    private CheckBox checkbox;
    private SpHelper spHelper;
    private int beforeTime = 0;
    private boolean runOnce = true;
    private Handler handler = new Handler();
    private Runnable runnable;
    private static final int SPEED_ADD_TIME = 50;
    private long recordTime = 0;
    private static final int TIME_SEND = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_extend);
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnReduce = (Button) findViewById(R.id.btnReduce);
        tvTime = (TextView) findViewById(R.id.tvTime);
        colorPicker = (ColorPicker) findViewById(R.id.colorPicker);
        checkbox = (CheckBox) findViewById(R.id.checkbox);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("扩展");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("选择功能");
        }

        tvTime.setText(String.valueOf(timeColor) + "s");
        spHelper = new SpHelper(this);

        btnAdd.setOnClickListener(this);
        btnAdd.setOnLongClickListener(this);

        btnReduce.setOnClickListener(this);
        btnReduce.setOnLongClickListener(this);

        tvTime.setOnLongClickListener(this);

//        btnAdd.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                switch (event.getAction() & MotionEvent.ACTION_MASK) {
//                    case MotionEvent.ACTION_DOWN:
//                        runOnce = true;
//                        runnable = new Runnable() {
//                            @Override
//                            public void run() {
//                                runOnUiThread(new Runnable() {
//                                    @Override
//                                    public void run() {
//                                        if (timeColor >= 255) {
//                                            ToastUtil.customToast(ExtendActivity.this, "最大255秒");
//                                        } else {
//                                            tvTime.setText(String.valueOf(++timeColor) + "s");
//                                        }
//                                    }
//                                });
//                                handler.postDelayed(runnable, SPEED_ADD_TIME);
//                            }
//                        };
//                        handler.post(runnable);
//                        break;
//                    case MotionEvent.ACTION_UP:
//
//                        handler.removeCallbacks(runnable);
//                        break;
//                }
//
//
//                return true;
//            }
//        });

        checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if (runOnce) {
                        beforeTime = timeColor;
                        runOnce = false;
                    }

                    timeColor = 255;
                    tvTime.setText("长亮");
                } else {
                    timeColor = beforeTime;
                    tvTime.setText(String.valueOf(beforeTime) + "s");
                }

            }
        });

        singleUdp = SingleUdp.getUdpInstance();
        singleUdp.setUdpIp(spHelper.getSpAgvIp());
        singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
        singleUdp.start();
        singleUdp.receiveUdp();
        singleUdp.setOnReceiveListen(new OnReceiveListen() {
            @Override
            public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {

//                String mData = Util.bytes2HexString(data, len);
//                if (Util.checkData(mData)) {
//                    String cmd = mData.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);
//                    if (Constant.CMD_LIGHTING_RESPOND.equalsIgnoreCase(cmd)) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ToastUtil.customToast(ExtendActivity.this, "LED设置成功");
//                            }
//                        });
//
//                    } else if (Constant.CMD_BUZZER_RESPOND.equalsIgnoreCase(cmd)) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                ToastUtil.customToast(ExtendActivity.this, "蜂鸣器设置成功");
//                            }
//                        });
//
//                    }
//                }

            }
        });
        colorPicker.setOnColorSelectedListener(new ColorPicker.OnColorSelectedListener() {
            @Override
            public void onColorSelected(int color) {
                colorR = Color.red(color);
                colorG = Color.green(color);
                colorB = Color.blue(color);



                colorBytes[0] = (byte) Integer.parseInt(Integer.toHexString(colorR), 16);
                colorBytes[1] = (byte) Integer.parseInt(Integer.toHexString(colorG), 16);
                colorBytes[2] = (byte) Integer.parseInt(Integer.toHexString(colorB), 16);
                colorBytes[3] = (byte) Integer.parseInt(Integer.toHexString(timeColor), 16);
                sendColor = Util.HexString2Bytes(Constant.SEND_DATA_COLOR(spHelper.getSpAgvMac()).replace(" ", ""));
                System.arraycopy(colorBytes, 0, sendColor, 14, colorBytes.length);
                sendColor[18] = Util.CheckCode(Util.bytes2HexString(colorBytes, colorBytes.length));
                singleUdp.send(sendColor);

            }
        });



        colorPicker.setOnColorChangedListener(new ColorPicker.OnColorChangedListener() {
            @Override
            public void onColorChanged(int color) {
                colorR = Color.red(color);
                colorG = Color.green(color);
                colorB = Color.blue(color);


                if(( System.currentTimeMillis() - recordTime ) >=TIME_SEND){
                   recordTime = System.currentTimeMillis();
                   colorBytes[0] = (byte) Integer.parseInt(Integer.toHexString(colorR), 16);
                   colorBytes[1] = (byte) Integer.parseInt(Integer.toHexString(colorG), 16);
                   colorBytes[2] = (byte) Integer.parseInt(Integer.toHexString(colorB), 16);
                   colorBytes[3] = (byte) Integer.parseInt(Integer.toHexString(timeColor), 16);
                   sendColor = Util.HexString2Bytes(Constant.SEND_DATA_COLOR(spHelper.getSpAgvMac()).replace(" ", ""));
                   System.arraycopy(colorBytes, 0, sendColor, 14, colorBytes.length);
                   sendColor[18] = Util.CheckCode(Util.bytes2HexString(colorBytes, colorBytes.length));
                   singleUdp.send(sendColor);

               }else{

               }



            }
        });


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

            case R.id.btnAdd:
                runOnce = true;
                if (timeColor >= 255) {
                    ToastUtil.customToast(ExtendActivity.this, "最大255秒");
                } else {
                    tvTime.setText(String.valueOf(++timeColor) + "s");
                }
                break;
            case R.id.btnReduce:
                runOnce = true;
                if (timeColor <= 0) {
                    ToastUtil.customToast(ExtendActivity.this, "时间要大于0");
                } else {
                    tvTime.setText(String.valueOf(--timeColor) + "s");
                }
                break;
        }
    }

    @Override
    public boolean onLongClick(View v) {
        switch (v.getId()) {
            case R.id.btnAdd:
                runOnce = true;
                if (timeColor >= 255) {
                    ToastUtil.customToast(ExtendActivity.this, "最大255秒");
                } else {
                    timeColor = timeColor + OFFSET;
                    if (timeColor >= 255) {
                        timeColor = timeColor - OFFSET;
                        ToastUtil.customToast(ExtendActivity.this, "最大255秒");
                    } else {
                        tvTime.setText(String.valueOf(timeColor) + "s");
                    }
                }
                break;
            case R.id.btnReduce:
                runOnce = true;
                if (timeColor <= 0) {
                    ToastUtil.customToast(ExtendActivity.this, "时间要大于0");
                } else {
                    timeColor = timeColor - OFFSET;
                    if (timeColor <= 0) {
                        timeColor = timeColor + OFFSET;
                        ToastUtil.customToast(ExtendActivity.this, "时间要大于0");
                    } else {
                        tvTime.setText(String.valueOf(timeColor) + "s");
                    }
                }

                break;

            case R.id.tvTime:
                ToastUtil.customToast(ExtendActivity.this, "time=" + timeColor);
                break;
        }
        return true;
    }
}
