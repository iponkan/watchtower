package com.hitqz.robot.watchtower;

import com.blankj.utilcode.util.SPUtils;
import com.google.gson.reflect.TypeToken;
import com.hitqz.robot.watchtower.bean.DonghuoRecord;
import com.hitqz.robot.watchtower.constant.Constants;
import com.orhanobut.logger.Logger;
import com.sonicers.commonlib.singleton.GsonUtil;

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

        donghuoRecords = new ArrayList<>();
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                DonghuoRecord donghuoRecord = DonghuoRecord.fromXml(list.get(i));
                donghuoRecords.add(donghuoRecord);
            }
        }
        return donghuoRecords;
    }

    public void addDonghuoRecord() {
        String gsonString = SPUtils.getInstance(Constants.SP_FILE_NAME).getString(Constants.ROCORD_KEY);
        Logger.t(TAG).i("before addDonghuoRecord gsonString " + gsonString);
        List<String> list = GsonUtil.getInstance().fromJson(gsonString, new TypeToken<List<String>>() {
        }.getType());
        if (list == null) {
            list = new ArrayList<>();
        }
        String donghuoRecordXml = DonghuoRecord.newDonghuoRecordXml();
        list.add(donghuoRecordXml);
        String result = GsonUtil.getInstance().toJson(list);
        Logger.t(TAG).i("addDonghuoRecord gsonString " + result);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.ROCORD_KEY, result);
    }

    public void updateDonghuoRecord() {
        String gsonString = SPUtils.getInstance(Constants.SP_FILE_NAME).getString(Constants.ROCORD_KEY);
        List<String> list = GsonUtil.getInstance().fromJson(gsonString, new TypeToken<List<String>>() {
        }.getType());
        if (list != null && list.size() > 0) {
            String str = list.remove(list.size() - 1);
            String[] strings = str.split("～");
            if (strings.length > 0) {
                String newStr = strings[0] + "～" + System.currentTimeMillis();
                list.add(newStr);
                String result = GsonUtil.getInstance().toJson(list);
                Logger.t(TAG).i("updateDonghuoRecord gsonString " + result);
                SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.ROCORD_KEY, result);
            }
        }
    }

    public void removeTimePointBefore(long time) {
        String gsonString = SPUtils.getInstance(Constants.SP_FILE_NAME).getString(Constants.ROCORD_KEY);
        Logger.t(TAG).i("before addTimePoint gsonString " + gsonString);
        List<String> list = GsonUtil.getInstance().fromJson(gsonString, new TypeToken<List<String>>() {
        }.getType());

        if (list != null && list.size() > 0) {
            List<String> remove = new ArrayList<>();
            for (int i = 0; i < list.size(); i++) {
                String str = list.get(i);
                String[] strings = str.split("～");
                if (strings.length > 0) {
                    if (Long.parseLong(strings[0]) < time) {
                        remove.add(str);
                    }
                }
            }
            list.removeAll(remove);
        }
        String result = GsonUtil.getInstance().toJson(list);
        SPUtils.getInstance(Constants.SP_FILE_NAME).put(Constants.ROCORD_KEY, result);
    }

    public void clearRecods() {
        SPUtils.getInstance(Constants.SP_FILE_NAME).remove(Constants.ROCORD_KEY);
    }
}
