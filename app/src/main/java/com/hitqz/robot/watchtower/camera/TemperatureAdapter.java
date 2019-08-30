package com.hitqz.robot.watchtower.camera;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.hitqz.robot.watchtower.R;

import java.util.List;

public class TemperatureAdapter extends BaseAdapter {

    private List<Float> mData;
    private Context mContext;

    public TemperatureAdapter(List<Float> mData, Context mContext) {
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
        tv.setTextColor(Color.RED);
        tv.setText(position + 1 + ": " + mData.get(position));

        return convertView;
    }
}
