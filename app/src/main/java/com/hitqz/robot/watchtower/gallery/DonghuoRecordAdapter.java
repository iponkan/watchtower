package com.hitqz.robot.watchtower.gallery;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hitqz.robot.watchtower.R;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.TimeStruct;

import java.util.List;

public class DonghuoRecordAdapter extends BaseAdapter {

    private List<DonghuoRecord> mData;
    private Context mContext;

    public DonghuoRecordAdapter(List<DonghuoRecord> mData, Context mContext) {
        this.mData = mData;
        this.mContext = mContext;
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return null;
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        convertView = LayoutInflater.from(mContext).inflate(R.layout.item_list_video, parent, false);
        TextView tv = convertView.findViewById(R.id.tv_fileName);
        TimeStruct timeStruct = mData.get(position).struStartTime;

        tv.setText("动火记录 " + (position + 1) + ". " + timeStruct);
        return convertView;
    }
}
