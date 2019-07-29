package com.hitqz.robot.watchtower;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.reflect.TypeToken;
import com.hitqz.robot.commonlib.singleton.GsonUtil;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.bean.TimeStruct;
import com.hitqz.robot.watchtower.constant.Constants;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

/***
 * 管理动火记录
 */
public class DonghuoRecordManager {

    public static final String TAG = "DonghuoRecordManager";

    private DonghuoRecordManager() {

    }

    private static DonghuoRecordManager singleton;

    public static DonghuoRecordManager getInstance() {
        if (singleton == null) {
            synchronized (DonghuoRecordManager.class) {
                if (singleton == null) {
                    singleton = new DonghuoRecordManager();
                }
            }
        }
        return singleton;
    }

    private List<DonghuoRecord> donghuoRecords;


    public List<DonghuoRecord> getDonghuoRecords() {
        String gsonString = SPUtils.getInstance(Constants.SP_FILE_NAME).getString(Constants.ROCORD_KEY);
        Logger.t(TAG).i("DonghuoRecords gsonString " + gsonString);
        List<String> list = GsonUtil.getInstance().fromJson(gsonString, new TypeToken<List<String>>() {
        }.getType());

        if (list == null) {
            donghuoRecords = new ArrayList<>();
            return donghuoRecords;
        }

        if (list.size() == 1) {
            DonghuoRecord donghuoRecord = new DonghuoRecord();
            TimeStruct start = TimeStruct.fromMillSeconds(Long.parseLong(list.get(0)));
            TimeStruct stop = TimeStruct.farFeature();
            donghuoRecord.struStartTime = start;
            donghuoRecord.struStopTime = stop;
            donghuoRecords = new ArrayList<>();
            donghuoRecords.add(donghuoRecord);
        } else if (list.size() > 1) {
            donghuoRecords = new ArrayList<>();
            for (int i = 0; i < list.size() - 1; i++) {
                DonghuoRecord donghuoRecord = new DonghuoRecord();
                TimeStruct start = TimeStruct.fromMillSeconds(Long.parseLong(list.get(i)));
                TimeStruct stop = TimeStruct.fromMillSeconds(Long.parseLong(list.get(i + 1)));
                donghuoRecord.struStartTime = start;
                donghuoRecord.struStopTime = stop;
                donghuoRecords.add(donghuoRecord);
            }
        }
        return donghuoRecords;
    }

    public void addTimePoint() {
        String gsonString = SPUtils.getInstance(Constants.SP_FILE_NAME).getString(Constants.ROCORD_KEY);
        Logger.t(TAG).i("before addTimePoint gsonString " + gsonString);
        List<String> list = GsonUtil.getInstance().fromJson(gsonString, new TypeToken<List<String>>() {
        }.getType());
        if (list == null) {
            list = new ArrayList<>();
        }
        String time = String.valueOf(System.currentTimeMillis());
        list.add(time);
        String result = GsonUtil.getInstance().toJson(list);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.ROCORD_KEY, result);
    }

    public void clearRecods() {
        SPUtils.getInstance(Constants.SP_FILE_NAME).remove(Constants.ROCORD_KEY);
    }
}
