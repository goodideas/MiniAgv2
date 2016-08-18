package com.kevin.miniagv2.activity;

import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.Vibrator;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.MenuItem;
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
import com.kevin.miniagv2.views.SimpleSpeedSeekBarAdapter;
import com.kevin.miniagv2.views.SpeedSeekBar;
import com.kevin.miniagv2.views.SpeedSeekBarListener;


public class ManualModeActivity extends AppCompatActivity {

    private static final String TAG = "ManualModeActivity";

    private SpeedSeekBar seekBarLeft;
    private SpeedSeekBar seekBarRight;
    private SpeedSeekBar speedSeekBarCenter;
    private Button btnManualStop;
    private SingleUdp singleUdp;
    private byte[] sendData;
    private byte leftWheel = 0x00, rightWheel = 0x00;
    private boolean flag = true;
    private TextView tvManualErrorStatus;
    private String mManualErrorStatus;
    private SpHelper spHelper;
    private CheckBox checkboxDistanceTest;
    private Vibrator vibrator;//震动

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_mode);
        final Resources resources = getResources();
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("手动模式");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("选择功能");
        }
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        seekBarLeft = (SpeedSeekBar) findViewById(R.id.speedSeekBarLeft);
        seekBarRight = (SpeedSeekBar) findViewById(R.id.speedSeekBarRight);
        speedSeekBarCenter = (SpeedSeekBar) findViewById(R.id.speedSeekBarCenter);

        tvManualErrorStatus = (TextView) findViewById(R.id.tvManualErrorStatus);
        btnManualStop = (Button) findViewById(R.id.btnManualStop);
        checkboxDistanceTest = (CheckBox) findViewById(R.id.checkboxDistanceTest);
        spHelper = new SpHelper(this);

        singleUdp = SingleUdp.getUdpInstance();
        if (singleUdp.getIpAddress() == null) {
            if (TextUtils.isEmpty(spHelper.getSpAgvIp())) {
                singleUdp.setUdpIp(spHelper.getSpAgvIp());
                singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
            }

        } else {
            singleUdp.start();
            singleUdp.receiveUdp();
            singleUdp.setOnReceiveListen(new OnReceiveListen() {
                @Override
                public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                    String mData = Util.bytes2HexString(data, len);
                    if (Util.checkData(mData)) {
                        String cmd = mData.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);
                        if (Constant.CMD_MANUAL_RESPOND.equalsIgnoreCase(cmd)) {
                            ToastUtil.customToast(ManualModeActivity.this, "操作成功");
                        } else if (Constant.CMD_UNLOCK_RESPOND.equalsIgnoreCase(cmd)) {
                            Intent in = new Intent();
                            in.putExtra(Constant.INTENT_NAME, Constant.INTENT_VALUE);
                            setResult(Constant.AUTO_MODE_TO_FUNCTION_MENU_RESULT_CODE, in);
                            ManualModeActivity.this.finish();

                            //查询数据
                        } else if (Constant.CMD_ERROR_STATU_RESPOND.equalsIgnoreCase(cmd)) {
                            String manualErrorStatus = mData.substring(Constant.DATA_CONTENT_START, Constant.DATA_CONTENT_START + 2);
                            int manualErrorStatusInt = Integer.parseInt(manualErrorStatus, 16);
                            switch (manualErrorStatusInt) {
                                case 0:
                                    mManualErrorStatus = "无";
                                    break;
                                case 1:
                                    mManualErrorStatus = "距离过近";
                                    break;
                                case 2:
                                    mManualErrorStatus = "脱轨";
                                    break;
                            }
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvManualErrorStatus.setText(mManualErrorStatus);

                                }
                            });
                        }
                    }
                }
            });
        }


        speedSeekBarCenter.setAdapter(new SimpleSpeedSeekBarAdapter(resources, new int[]{
                R.drawable.btn_star3_selector,
                R.drawable.btn_star2_selector,
                R.drawable.btn_star1_selector,
                R.drawable.btn_star0_selector,
                R.drawable.btn_star4_selector,
                R.drawable.btn_star5_selector,
                R.drawable.btn_star6_selector
        }));
        seekBarLeft.setAdapter(new SimpleSpeedSeekBarAdapter(resources, new int[]{
                R.drawable.btn_star3_selector,
                R.drawable.btn_star2_selector,
                R.drawable.btn_star1_selector,
                R.drawable.btn_star0_selector,
                R.drawable.btn_star4_selector,
                R.drawable.btn_star5_selector,
                R.drawable.btn_star6_selector
        }));
        seekBarRight.setAdapter(new SimpleSpeedSeekBarAdapter(resources, new int[]{
                R.drawable.btn_star3_selector,
                R.drawable.btn_star2_selector,
                R.drawable.btn_star1_selector,
                R.drawable.btn_star0_selector,
                R.drawable.btn_star4_selector,
                R.drawable.btn_star5_selector,
                R.drawable.btn_star6_selector
        }));

        seekBarLeft.setPosition(3);
        seekBarRight.setPosition(3);
        speedSeekBarCenter.setPosition(3);

        speedSeekBarCenter.setListener(new SpeedSeekBarListener() {
            @Override
            public void onPositionSelected(int position) {
                final int spd;
                flag = false;
                seekBarLeft.setPosition(position);
                seekBarRight.setPosition(position);
                flag = true;
                vibrator.vibrate(100);
                spd = position > 3 ? position : (3 - position);
//                14 15 16 字节分别是 左轮速度、右轮速度、校验位
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                        sendData[14] = Byte.parseByte(spd + "");
                        sendData[15] = Byte.parseByte(spd + "");
                        String hexData = Util.bytes2HexString(new byte[]{sendData[14], sendData[15]}, 2);
                        sendData[16] = Util.CheckCode(hexData);
                        singleUdp.send(sendData);
                    }
                });


            }
        });
        seekBarLeft.setListener(new SpeedSeekBarListener() {
            @Override
            public void onPositionSelected(int position) {
                final int spd;
                spd = position > 3 ? position : (3 - position);
                leftWheel = Byte.parseByte("" + spd);
                if (flag) {
                    vibrator.vibrate(100);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                            sendData[14] = Byte.parseByte("" + spd);
                            sendData[15] = rightWheel;
                            String hexData = Util.bytes2HexString(new byte[]{sendData[14], sendData[15]}, 2);
                            sendData[16] = Util.CheckCode(hexData);
                            singleUdp.send(sendData);
                        }
                    });
                }
            }
        });

        seekBarRight.setListener(new SpeedSeekBarListener() {
            @Override
            public void onPositionSelected(int position) {
                final int spd;


                spd = position > 3 ? position : (3 - position);

                rightWheel = Byte.parseByte("" + spd);

                if (flag) {
                    vibrator.vibrate(100);
                    new Handler().post(new Runnable() {
                        @Override
                        public void run() {
                            sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                            sendData[14] = leftWheel;
                            sendData[15] = Byte.parseByte("" + spd);
                            String hexData = Util.bytes2HexString(new byte[]{sendData[14], sendData[15]}, 2);
                            sendData[16] = Util.CheckCode(hexData);
                            singleUdp.send(sendData);
                        }
                    });

                }
            }
        });


        //手指弹起
        speedSeekBarCenter.setOnActionUp(new SpeedSeekBar.SpeedSeekBarActionUp() {
            @Override
            public void onActionUp() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                        sendData[14] = (byte)0x00;
                        sendData[15] = (byte)0x00;
                        sendData[16] = (byte)0x00;
                        singleUdp.send(sendData);
                    }
                });
            }
        });
        seekBarLeft.setOnActionUp(new SpeedSeekBar.SpeedSeekBarActionUp() {
            @Override
            public void onActionUp() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                        sendData[14] = (byte)0x00;
                        sendData[15] = (byte)0x00;
                        sendData[16] = (byte)0x00;
                        singleUdp.send(sendData);
                    }
                });
            }
        });
        seekBarRight.setOnActionUp(new SpeedSeekBar.SpeedSeekBarActionUp() {
            @Override
            public void onActionUp() {
                new Handler().post(new Runnable() {
                    @Override
                    public void run() {
                        sendData = Util.HexString2Bytes(Constant.SEND_DATA_SPEED(spHelper.getSpAgvMac()).replace(" ", ""));
                        sendData[14] = (byte)0x00;
                        sendData[15] = (byte)0x00;
                        sendData[16] = (byte)0x00;
                        singleUdp.send(sendData);
                    }
                });
            }
        });



        btnManualStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (singleUdp != null) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_LOCK(spHelper.getSpAgvMac()).replace(" ", "")));
                }
            }
        });


        checkboxDistanceTest.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_DISTANCE_TEST(spHelper.getSpAgvMac()).replace(" ", "")));
                } else {
                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_DISTANCE_NO_TEST(spHelper.getSpAgvMac()).replace(" ", "")));
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

}
