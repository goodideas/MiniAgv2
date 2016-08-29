package com.kevin.miniagv2.activity;


import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;


import com.kevin.miniagv2.R;
import com.kevin.miniagv2.db.DBCurd;
import com.kevin.miniagv2.entity.AgvBean;
import com.kevin.miniagv2.utils.AgvAdapter;
import com.kevin.miniagv2.utils.BroadcastUdp;
import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.OnReceiveListen;
import com.kevin.miniagv2.utils.SingleUdp;
import com.kevin.miniagv2.utils.SpHelper;
import com.kevin.miniagv2.utils.ToastUtil;
import com.kevin.miniagv2.utils.Util;
import com.kevin.miniagv2.utils.WaitDialog;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "MainActivity";
    private Button btnConnectAgv;
    private Button btnSearchAgv;
    private ListView lvAgv;
    private List<AgvBean> list = new ArrayList<>();
    private AgvAdapter agvAdapter;
    private int lastSelect = -1;
    private boolean isSelect = false;
    private int selected = -1;
    private LinearLayout.LayoutParams params = new
            LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

    private SingleUdp singleUdp;
    //    private UdpHelper udpHelper = UdpHelper.getUdpInstance(this);
    private BroadcastUdp broadcastUdp;
    private SpHelper spHelper;
    private Handler handler = new Handler();
    private Runnable broadCastSendRunnable;
    private int sendTimes = 0;
    private DBCurd dbCurd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        init();


        btnConnectAgv.setOnClickListener(this);
        btnSearchAgv.setOnClickListener(this);
        agvAdapter = new AgvAdapter(this, list);
        lvAgv.setAdapter(agvAdapter);

        View emptyView = LayoutInflater.from(this).inflate(R.layout.programmed_list_empty_layout, null);


        params.gravity = Gravity.CENTER;
        ((ViewGroup) lvAgv.getParent()).addView(emptyView, params);
        lvAgv.setEmptyView(emptyView);

        lvAgv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {


                if (lastSelect == position) {
                    isSelect = !isSelect;
                    agvAdapter.setSelected(position, isSelect);
                    agvAdapter.notifyDataSetChanged();
                } else {
                    isSelect = true;
                    agvAdapter.setSelected(position, true);
                    agvAdapter.notifyDataSetChanged();
                }

//                ToastUtil.customToast(MainActivity.this, "setOnItemClickListener=" + String.valueOf(position));
                selected = isSelect ? position : -1;
                lastSelect = position;
            }
        });

        lvAgv.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                list.remove(position);
                agvAdapter.notifyDataSetChanged();
                return true;
            }
        });

    }

    private void init() {
        setContentView(R.layout.activity_main);
        btnConnectAgv = (Button) findViewById(R.id.btnConnectAgv);
        btnSearchAgv = (Button) findViewById(R.id.btnSearchAgv);
        lvAgv = (ListView) findViewById(R.id.lvAgv);


        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("AGV列表");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("主页面");
        }
        spHelper = new SpHelper(MainActivity.this);
        dbCurd = DBCurd.getInstance(MainActivity.this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btnConnectAgv:
                if (selected == -1) {
                    ToastUtil.customToast(this, "没有选择！！！");
                } else {
//                    ToastUtil.customToast(this, "选择了" + String.valueOf(selected));

                    final AgvBean agvBean = (AgvBean) agvAdapter.getItem(selected);
                    spHelper.saveSpAgvId(agvBean.getGavId());
                    spHelper.saveSpAgvIp(agvBean.getGavIp());
                    spHelper.saveSpAgvMac(agvBean.getGavMac());

                    singleUdp = SingleUdp.getUdpInstance();
                    singleUdp.stop();
                    singleUdp = SingleUdp.getUdpInstance();
                    singleUdp.setUdpIp(agvBean.getGavIp());
                    singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
                    singleUdp.start();

                    singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SHAKE(agvBean.getGavMac()).replace(" ", "")));
                    WaitDialog.showDialog(MainActivity.this, "正在连接", Constant.CONNECT_WAIT_DIALOG_MAX_TIME, null);
                    singleUdp.receiveUdp();
                    singleUdp.setOnReceiveListen(new OnReceiveListen() {
                        @Override
                        public void onReceiveData(byte[] data, int len, String ip) {
                            String mString = Util.bytes2HexString(data, len);
                            if (Util.checkData(mString)) {
                                String cmd = mString.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);
                                if (Constant.CMD_SHAKE_RESPOND.equalsIgnoreCase(cmd)) {
                                    WaitDialog.immediatelyDismiss();
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            agvAdapter.setSelected(selected, false);
                                            agvAdapter.notifyDataSetChanged();
                                        }
                                    });
                                    selected = -1;
                                    isSelect = false;
                                    finish();
                                }
                            }

                        }
                    });


                }

                break;
            case R.id.btnSearchAgv:

                agvAdapter.setSelected(selected, false);
                selected = -1;
                isSelect = false;
                agvAdapter.notifyDataSetChanged();
                list.clear();
                dbCurd.delALLTempAgvData();
                agvAdapter.notifyDataSetChanged();
                if (broadcastUdp == null) {
                    broadcastUdp = new BroadcastUdp();
                }
                sendTimes = 0;
                broadcastUdp.stop();
                broadcastUdp.init();
                broadcastUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SEARCH.replace(" ", "")));
                broadCastSendRunnable = new Runnable() {
                    @Override
                    public void run() {
                        sendTimes++;
                        Log.e(TAG, "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111");
                        broadcastUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_SEARCH.replace(" ", "")));
                        handler.postDelayed(broadCastSendRunnable, 2000);
                        if (sendTimes == 2) {
                            handler.removeCallbacks(broadCastSendRunnable);
                        }

                    }
                };
                handler.postDelayed(broadCastSendRunnable, 2000);
                broadcastUdp.setReceiveListen(new OnReceiveListen() {
                    @Override
                    public void onReceiveData(byte[] data, int len, String remoteIp) {
                        String da = Util.bytes2HexString(data, len);
                        Log.e(TAG, "da = " + da);
                        analysisData(da, remoteIp);
                    }
                });
                WaitDialog.immediatelyDismiss();
                WaitDialog.showDialog(MainActivity.this, "正在搜索。。。", Constant.SEARCH_WAIT_DIALOG_TIME, broadcastUdp);
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        list = dbCurd.getAllTempAgvData();
                        agvAdapter = new AgvAdapter(MainActivity.this, list);
                        lvAgv.setAdapter(agvAdapter);
                        lvAgv.smoothScrollToPosition(list.size());
                    }
                }, Constant.SEARCH_WAIT_DIALOG_TIME);
                break;
        }
    }

    private void analysisData(String data, String ip) {
        if (Util.checkData(data)) {
            String cmd = data.substring(Constant.DATA_CMD_START, Constant.DATA_CMD_END);

            if (Constant.CMD_SEARCH_RESPOND.equalsIgnoreCase(cmd)) {
                String agvMac = data.substring(Constant.DATA_MAC_START, Constant.DATA_MAC_END);
//                if(list!=null){
//                    for (AgvBean aBean:list) {
//                        if(agvMac.equalsIgnoreCase(aBean.getGavMac())){
//                            return;
//                        }
//                    }
//                }
                Log.e(TAG, "agvMac=" + agvMac);


                int dataLength = Integer.parseInt(data.substring(Constant.DATA_CONTENT_LENGTH_START_0, Constant.DATA_CONTENT_LENGTH_END_0), 16) +
                        Integer.parseInt(data.substring(Constant.DATA_CONTENT_LENGTH_START_1, Constant.DATA_CONTENT_LENGTH_END_1), 16) * 256;
                String agvId = data.substring(Constant.DATA_CONTENT_START, Constant.DATA_CONTENT_END(dataLength * 2));
//                final AgvBean agvBean = new AgvBean();
//                agvBean.setGavId(agvId);
//                agvBean.setGavIp(ip);
//                agvBean.setGavMac(agvMac);
                dbCurd.addTempAgvData(agvMac, agvId, ip);
//                runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
////                        list.add(agvBean);
//                        list = dbCurd.getAllTempAgvData();
//                        Log.e(TAG,""+list.size());
////                        agvAdapter.notifyDataSetChanged();
//                        agvAdapter = new AgvAdapter(MainActivity.this, list);
//                        lvAgv.setAdapter(agvAdapter);
//                        lvAgv.smoothScrollToPosition(list.size());
//                    }
//                });

            }
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
    protected void onDestroy() {
        super.onDestroy();
    }
}
