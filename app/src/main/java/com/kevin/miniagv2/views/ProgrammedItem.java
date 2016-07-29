package com.kevin.miniagv2.views;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;


import com.kevin.miniagv2.R;
import com.kevin.miniagv2.utils.SpinnerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Kevin
 * on 2016/6/28 0028.
 */
public class ProgrammedItem extends LinearLayout {

//    private EditText etLoc, etRFID;
    private TextView tvLoc;
    private TextView tvProgrammedRfid;
    private TextView tvNumber;
    private RadioGroup radioGroup;
    private RadioButton rbSpeed1, rbSpeed2;
    private int rbSpeed = 1;
    private int spinnerSelect = 1;
    private Spinner spinnerProgrammed;
    private SpinnerAdapter spinnerAdapter;
    private List<String> list = new ArrayList<>();
    private String mContent;
    private TextView tvShowProgrammedNumber;
    private int position;

    public ProgrammedItem(Context context) {
        this(context, null);
    }


    public ProgrammedItem(Context context, AttributeSet attrs) {
        super(context, attrs);

        LayoutInflater.from(context).inflate(R.layout.programmed_item_layout, this, true);
//        etLoc = (EditText) findViewById(R.id.etLoc);
//        etRFID = (EditText) findViewById(R.id.etRFID);
        tvLoc = (TextView)findViewById(R.id.tvLoc);
        tvProgrammedRfid = (TextView)findViewById(R.id.tvProgrammedRfid);
        spinnerProgrammed = (Spinner) findViewById(R.id.spinnerProgrammed);
        tvNumber = (TextView) findViewById(R.id.tvNumber);
        radioGroup = (RadioGroup) findViewById(R.id.radioGroupProgrammed);
        rbSpeed1 = (RadioButton) findViewById(R.id.rbSpeed1);
        rbSpeed2 = (RadioButton) findViewById(R.id.rbSpeed2);
        tvShowProgrammedNumber = (TextView)findViewById(R.id.tvShowProgrammedNumber);
        list.add("停止");
        list.add("前进");
        list.add("左转");
        list.add("右转");
        list.add("掉头");
        spinnerAdapter = new SpinnerAdapter(context, list);
        spinnerProgrammed.setAdapter(spinnerAdapter);

        spinnerProgrammed.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                spinnerSelect = position;
                mContent = list.get(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (rbSpeed1.getId() == checkedId) {
                    rbSpeed = 1;
                } else if (rbSpeed2.getId() == checkedId) {
                    rbSpeed = 2;
                }
            }
        });
    }

    public String getEtLoc() {
        return tvLoc.getText().toString();
    }

    public String getEtRFID() {
        return tvProgrammedRfid.getText().toString();
    }

    public int getRbSpeed() {
        return rbSpeed;
    }

    public int getSpinnerSelect() {
        return spinnerSelect;
    }

    public String getmContent() {
        return mContent;
    }



    public int getTvNumber() {
       return position;
    }
    public void setTvNumber(String text) {
        position = Integer.parseInt(text);
        tvNumber.setText(text);
    }

    public void setEtRFID(String rfid) {
        tvProgrammedRfid.setText(rfid);
    }

    public void setEtLoc(String loc) {
        tvLoc.setText(loc);
    }

    public void setmContent(int select){
        if(select>5||select<0){
            select = 0;
        }
        spinnerProgrammed.setSelection(select);
    }

    public void setRbSpeed(int speed){
        if(speed == 1){
            rbSpeed1.setChecked(true);
        }else if(speed == 2){
            rbSpeed2.setChecked(true);
        }
    }

//    public void setOnLongClickListen(View.OnLongClickListener onLongClickListener){
//        tvShowProgrammedNumber.setOnLongClickListener(onLongClickListener);
//    }


}
