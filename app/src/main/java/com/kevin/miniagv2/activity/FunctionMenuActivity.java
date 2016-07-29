package com.kevin.miniagv2.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.kevin.miniagv2.R;
import com.kevin.miniagv2.entity.AgvBean;
import com.kevin.miniagv2.utils.Constant;
import com.kevin.miniagv2.utils.Util;


public class FunctionMenuActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "FunctionMenuActivity";
    private Button btnManualMode;
    private Button btnRFIDProgrammed;
    private Button btnAutoMode;
    private Button btnExtend;
    private Intent mIntent = new Intent();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_function_menu);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle("选择功能");
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setSubtitle("解锁AGV");
        }
        int screenWidth = Util.getScreenWidth(this);
        int btnMeasure = (int) (screenWidth * 0.4);

        btnManualMode = (Button) findViewById(R.id.btnManualMode);
        btnRFIDProgrammed = (Button) findViewById(R.id.btnRFIDProgrammed);
        btnAutoMode = (Button) findViewById(R.id.btnAutoMode);
        btnExtend = (Button) findViewById(R.id.btnExtend);

        btnManualMode.getLayoutParams().width = btnMeasure;
        btnManualMode.getLayoutParams().height = btnMeasure;
        btnRFIDProgrammed.getLayoutParams().width = btnMeasure;
        btnRFIDProgrammed.getLayoutParams().height = btnMeasure;
        btnAutoMode.getLayoutParams().width = btnMeasure;
        btnAutoMode.getLayoutParams().height = btnMeasure;
        if (btnExtend != null) {
            btnExtend.getLayoutParams().width = btnMeasure;
            btnExtend.getLayoutParams().height = btnMeasure;
        }

        btnManualMode.setOnClickListener(this);
        btnRFIDProgrammed.setOnClickListener(this);
        btnAutoMode.setOnClickListener(this);
        btnExtend.setOnClickListener(this);
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
            case R.id.btnAutoMode:
                mIntent.setClass(FunctionMenuActivity.this, AutoModeActivity.class);
                startActivityForResult(mIntent, Constant.FUNCTION_REQUEST_CODE);
                break;
            case R.id.btnManualMode:
                mIntent.setClass(FunctionMenuActivity.this, ManualModeActivity.class);
                startActivityForResult(mIntent, Constant.FUNCTION_REQUEST_CODE);
                break;
            case R.id.btnRFIDProgrammed:
                startActivity(new Intent(FunctionMenuActivity.this, ProgrammedModeActivity.class));
                break;
            case R.id.btnExtend:
                startActivity(new Intent(FunctionMenuActivity.this, ExtendActivity.class));
                break;
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constant.FUNCTION_REQUEST_CODE && data != null) {
            int value = data.getIntExtra(Constant.INTENT_NAME, -1);
            Log.e(TAG, "resultCode=" + resultCode + " value=" + value);
            if (value == Constant.INTENT_VALUE && resultCode == Constant.AUTO_MODE_TO_FUNCTION_MENU_RESULT_CODE) {
                finish();
            }
        }

    }
}
