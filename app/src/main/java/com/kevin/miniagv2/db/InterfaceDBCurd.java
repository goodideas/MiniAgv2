package com.kevin.miniagv2.db;

import com.kevin.miniagv2.entity.ProgrammedBean;

import java.util.List;

public interface InterfaceDBCurd {

    //添加数据
    void addProgrammedData(String mRfid,String mLoc,String mSpeed,String mContent,String isRecord);

    //根据id删除数据
    void delProgrammedData(String pid);

    //删除所有数据
    void delALLProgrammedData();

    //得到所有数据
    List<ProgrammedBean> getAllProgrammedData();

    //更新数据
    void upDataProgrammedData(ProgrammedBean programmedBean, String pid);

    //根据ID查找数据
    ProgrammedBean getProgrammedDataById(String pid);

}
