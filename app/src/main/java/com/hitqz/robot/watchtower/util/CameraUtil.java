package com.hitqz.robot.watchtower.util;

import android.util.Log;
import android.view.Surface;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.INT_PTR;
import com.hikvision.netsdk.NET_DVR_COMPRESSION_INFO_V30;
import com.hikvision.netsdk.NET_DVR_FILECOND;
import com.hikvision.netsdk.NET_DVR_FINDDATA_V30;
import com.hikvision.netsdk.PlaybackControlCommand;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.bean.TimeStruct;
import com.orhanobut.logger.Logger;

import java.util.ArrayList;
import java.util.List;

public class CameraUtil {

    public static final String TAG = "CameraUtil";

    public static void test_ScreenConfigCap(int iUserID) {
        byte[] byAbility = new byte[1 * 1024 * 1024];
        INT_PTR iRetLen = new INT_PTR();
        iRetLen.iValue = 0;
        int i = 0;
        if (!HCNetSDK.getInstance().NET_DVR_GetSTDAbility(iUserID, HCNetSDK.NET_DVR_GET_SCREEN_CONFIG_CAP, null, 0, byAbility, 1024 * 1024, iRetLen)) {
            Logger.e("NET_DVR_GET_SCREEN_CONFIG_CAP" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Logger.i("NET_DVR_GET_SCREEN_CONFIG_CAP success");
        }
        i = HCNetSDK.getInstance().NET_DVR_GetLastError();
    }

    // lChannel为1
    public static List<FileInfo> findFile(int iUserID, TimeStruct start, TimeStruct stop) {
        List<FileInfo> list = new ArrayList<>();
        int iFindHandle = -1;
        NET_DVR_FILECOND lpSearchInfo = new NET_DVR_FILECOND();
        lpSearchInfo.lChannel = 1;
        lpSearchInfo.dwFileType = 0xff;
        lpSearchInfo.dwIsLocked = 0xff;
        lpSearchInfo.dwUseCardNo = 0;
        lpSearchInfo.struStartTime = start.toNET_DVR_TIME();
        lpSearchInfo.struStopTime = stop.toNET_DVR_TIME();
//        lpSearchInfo.struStartTime.dwYear = 2019;
//        lpSearchInfo.struStartTime.dwMonth = 6;
//        lpSearchInfo.struStartTime.dwDay = 26;
//        lpSearchInfo.struStopTime.dwYear = 2019;
//        lpSearchInfo.struStopTime.dwMonth = 11;
//        lpSearchInfo.struStopTime.dwDay = 27;
        iFindHandle = HCNetSDK.getInstance().NET_DVR_FindFile_V30(iUserID, lpSearchInfo);
        if (iFindHandle == -1) {
            Logger.e("NET_DVR_FindFile_V30 failed,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return list;
        }
        int findNext = 0;
        NET_DVR_FINDDATA_V30 struFindData = new NET_DVR_FINDDATA_V30();
        while (findNext != -1) {
            findNext = HCNetSDK.getInstance().NET_DVR_FindNextFile_V30(iFindHandle, struFindData);
            if (findNext == HCNetSDK.NET_DVR_FILE_SUCCESS) {
                String fileName = CommonMethod.toValidString(new String(struFindData.sFileName));
                FileInfo fileInfo = new FileInfo();
                fileInfo.fileName = fileName;
                fileInfo.startTime = TimeStruct.cloneFrom(struFindData.struStartTime);
                fileInfo.stopTime = TimeStruct.cloneFrom(struFindData.struStopTime);
                fileInfo.fileSize = struFindData.dwFileSize;
                list.add(fileInfo);
                Log.i(TAG, "~~~~~Find File" + fileName);
                Log.i(TAG, "~~~~~File Size" + struFindData.dwFileSize);
                Log.i(TAG, "~~~~~File Time,from" + struFindData.struStartTime.ToString());
                Log.i(TAG, "~~~~~File Time,to" + struFindData.struStopTime.ToString());
            } else if (HCNetSDK.NET_DVR_FILE_NOFIND == findNext) {
                Log.i(TAG, "No file found");
                break;
            } else if (HCNetSDK.NET_DVR_NOMOREFILE == findNext) {
                Log.i(TAG, "All files are listed");
                break;
            } else if (HCNetSDK.NET_DVR_FILE_EXCEPTION == findNext) {
                Log.e(TAG, "Exception in searching");
                break;
            } else if (HCNetSDK.NET_DVR_ISFINDING == findNext) {
                Log.i(TAG, "NET_DVR_ISFINDING");
            }
        }
        HCNetSDK.getInstance().NET_DVR_FindClose_V30(iFindHandle);
        return list;
    }

    public static void test_PlayBackByName(int iUserID, String fileName, Surface surface) {

        int nHandle = HCNetSDK.getInstance().NET_DVR_PlayBackByName(iUserID, fileName);
        if (-1 == nHandle) {
            System.out.println("NET_DVR_PlayBackByTime failed! error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        NET_DVR_COMPRESSION_INFO_V30 compression = new NET_DVR_COMPRESSION_INFO_V30();
        compression.byResolution = 1;
        compression.dwVideoBitrate = 7;
//        HCNetSDK.getInstance().NET_DVR_PlayBackControl_V50(nHandle, PlaybackControlCommand.NET_DVR_PLAY_CONVERT, compression, null);
        HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(nHandle, PlaybackControlCommand.NET_DVR_PLAYSTART, null, 0, null);
        int nProgress = -1;
        while (true) {
            nProgress = HCNetSDK.getInstance().NET_DVR_GetPlayBackPos(nHandle);
            System.out.println("NET_DVR_GetPlayBackPos:" + nProgress);
            if (nProgress < 0 || nProgress >= 100) {
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        HCNetSDK.getInstance().NET_DVR_StopPlayBack(nHandle);
    }

//      private Button.OnClickListener Playback_Listener = new
//      Button.OnClickListener() {
//
//      public void onClick(View v) {
//      } };
}
