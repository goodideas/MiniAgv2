package com.kevin.miniagv2.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import com.kevin.miniagv2.R;

import java.util.List;

/**
 * Created by Administrator
 * on 2016/7/15.
 */
public class SpinnerAdapter extends BaseAdapter{

    private Context mContext;
    private List<String> mList;

    public SpinnerAdapter(Context context, List<String> list){
        this.mContext = context;
        this.mList = list;
    }


    @Override
    public int getCount() {
        return mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        ViewHolder viewHolder;

        if(convertView == null){
            viewHolder = new ViewHolder();
            view = LayoutInflater.from(mContext).inflate(R.layout.spinner_layout_item,parent,false);
            viewHolder.tvSpinner = (TextView)view.findViewById(R.id.tvSpinner);
            view.setTag(viewHolder);
        }else{
            view = convertView;
            viewHolder = (ViewHolder)view.getTag();
        }

        viewHolder.tvSpinner.setText(mList.get(position));

        return view;
    }


    private final class ViewHolder{
        TextView tvSpinner;
    }



}
