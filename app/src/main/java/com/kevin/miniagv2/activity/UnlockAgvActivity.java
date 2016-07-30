package com.kevin.miniagv2.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.kevin.miniagv2.R;
import com.kevin.miniagv2.entity.AgvBean;
import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.OnReceiveListen;
import com.kevin.miniagv2.utils.SingleUdp;
import com.kevin.miniagv2.utils.SpHelper;
import com.kevin.miniagv2.utils.ToastUtil;
import com.kevin.miniagv2.utils.Util;
import com.kevin.miniagv2.utils.WaitDialog;


public class UnlockAgvActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "UnlockAgvActivity";
    private Button btnUnlockAgv;
    private SingleUdp singleUdp;
    private Toolbar toolbar;
    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    private long exitTime = 0;
    private Button btnAGVList;
    private SpHelper spHelper;
    private TextView tvCurrentAgvId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        init();



        if (btnUnlockAgv != null) {
            btnUnlockAgv.setOnClickListener(this);
        }


        mDrawerToggle = new ActionBarDrawerToggle(UnlockAgvActivity.this, mDrawerLayout, toolbar,
                R.string.app_name, R.string.app_name) {
            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                drawerView.setClickable(true);
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                drawerView.setClickable(false);
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        mDrawerToggle.syncState();
        btnAGVList.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                    mDrawerLayout.closeDrawer(GravityCompat.START);
                }
                startActivity(new Intent(UnlockAgvActivity.this,MainActivity.class));
            }
        });
    }

    private void init() {

        setContentView(R.layout.activity_unlock_agv);
        btnUnlockAgv = (Button)findViewById(R.id.btnUnlockAgv);
        toolbar = (Toolbar) findViewById(R.id.toolBar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawerLayout);
        btnAGVList = (Button)findViewById(R.id.btnAGVList);
        tvCurrentAgvId = (TextView)findViewById(R.id.tvCurrentAgvId);
        toolbar.setTitle(R.string.app_name);
        toolbar.setTitleTextColor(Color.parseColor("#FFFFFF"));
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        spHelper = new SpHelper(UnlockAgvActivity.this);
        if(!TextUtils.isEmpty(spHelper.getSpAgvIp())&&!TextUtils.isEmpty(spHelper.getSpAgvMac())){
            singleUdp = SingleUdp.getUdpInstance();
            singleUdp.setUdpIp(spHelper.getSpAgvIp());
            singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
            singleUdp.start();
            singleUdp.receiveUdp();
            singleUdp.setOnReceiveListen(new OnReceiveListen() {
                @Override
                public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                    String mData = Util.bytes2HexString(data, len);
                    analysisData(mData);
                }
            });
            if(spHelper.getSpAgvId()==null){
                tvCurrentAgvId.setText("没有ID");
            }else{
                tvCurrentAgvId.setText(spHelper.getSpAgvId());
            }

        }

    }

    private void analysisData(String data){
        if(Util.checkData(data)){
            String cmd = data.substring(Constant.DATA_CMD_START,Constant.DATA_CMD_END);
            if(Constant.CMD_UNLOCK_RESPOND.equalsIgnoreCase(cmd)){
                WaitDialog.immediatelyDismiss();
                startActivity(new Intent(UnlockAgvActivity.this,FunctionMenuActivity.class));
            }
        }
    }


    @Override
    protected void onStart() {
        super.onStart();
        Log.e(TAG, "onStart");
        if(!TextUtils.isEmpty(spHelper.getSpAgvIp())&&!TextUtils.isEmpty(spHelper.getSpAgvMac())){
            singleUdp = SingleUdp.getUdpInstance();
            singleUdp.setUdpIp(spHelper.getSpAgvIp());
            singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
            singleUdp.start();
            singleUdp.receiveUdp();
            singleUdp.setOnReceiveListen(new OnReceiveListen() {
                @Override
                public void onReceiveData(byte[] data, int len, @Nullable String remoteIp) {
                    String mData = Util.bytes2HexString(data, len);
                    Log.e(TAG, "mData=" + mData);
                    analysisData(mData);
                }
            });
            if(spHelper.getSpAgvId()==null){
                tvCurrentAgvId.setText("没有ID");
            }else{
                tvCurrentAgvId.setText(spHelper.getSpAgvId());
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
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.btnUnlockAgv:
                if(spHelper==null||TextUtils.isEmpty(spHelper.getSpAgvIp())||TextUtils.isEmpty(spHelper.getSpAgvMac())){
                    ToastUtil.customToast(this, "当前没有选择AGV，请搜索AGV");
                }else{
                    if(singleUdp==null){
                        singleUdp = SingleUdp.getUdpInstance();
                    }else if(TextUtils.isEmpty(singleUdp.getIpAddress())){
                        singleUdp.setUdpIp(spHelper.getSpAgvIp());
                        singleUdp.setUdpRemotePort(Constant.REMOTE_PORT);
                        singleUdp.start();
                    }else{
                        singleUdp.send(Util.HexString2Bytes(Constant.SEND_DATA_UNLOCK(spHelper.getSpAgvMac()).replace(" ", "")));
                        WaitDialog.showDialog(UnlockAgvActivity.this, "正在解锁", Constant.UNLOCK_WAIT_DIALOG_MAX_TIME, null);
                    }

                }

                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK
                && event.getAction() == KeyEvent.ACTION_DOWN) {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else if (System.currentTimeMillis() - exitTime > 2000) {
                ToastUtil.customToast(UnlockAgvActivity.this, "再按一次退出");
                exitTime = System.currentTimeMillis();
            } else {
                finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(singleUdp!=null){
            singleUdp.stop();
        }
    }


}