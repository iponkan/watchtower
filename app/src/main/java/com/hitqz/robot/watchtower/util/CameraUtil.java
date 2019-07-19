package com.hitqz.robot.watchtower.util;

import android.util.Log;
import android.view.Surface;

import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.INT_PTR;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_ABILITY;
import com.hikvision.netsdk.NET_DVR_COMPRESSIONCFG_V30;
import com.hikvision.netsdk.NET_DVR_COMPRESSION_INFO_V30;
import com.hikvision.netsdk.NET_DVR_CONFIG;
import com.hikvision.netsdk.NET_DVR_FILECOND;
import com.hikvision.netsdk.NET_DVR_FINDDATA_V30;
import com.hikvision.netsdk.PlaybackControlCommand;
import com.hitqz.robot.watchtower.HCSdkManager;
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
        Log.d(TAG, "查找文件开始时间：" + start.toString() + " 结束时间：" + stop.toString());
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

    public static void COMPRESS_CFG(int playid, int channel) {
        NET_DVR_COMPRESSIONCFG_V30 Compress_Cfg = new NET_DVR_COMPRESSIONCFG_V30();
        if (HCNetSDK.getInstance().NET_DVR_GetDVRConfig(playid, HCNetSDK.NET_DVR_GET_COMPRESSCFG_V30, channel, Compress_Cfg)) {
            NET_DVR_COMPRESSION_INFO_V30 net_para = Compress_Cfg.struNetPara;
            NET_DVR_COMPRESSION_INFO_V30 record_para = Compress_Cfg.struNormHighRecordPara;
            NET_DVR_COMPRESSION_INFO_V30 alarm_para = Compress_Cfg.struEventRecordPara;
			/*
				int[] net_para_video = {0};
				net_para_video[0] = net_para.dwVideoBitrate;
				net_para_video[1] = net_para.dwVideoFrameRate;//			byte[] net_para_array = {0};
				net_para_array[0] = net_para.byAudioEncType;
				net_para_array[1] = net_para.byBitrateType;
				net_para_array[2] = net_para.byIntervalBPFrame;
				net_para_array[3] = net_para.byPicQuality;
				net_para_array[4] = net_para.byResolution;
				net_para_array[5] = net_para.byStreamType;
				net_para_array[6] = net_para.byVideoEncType;
				net_para_array[7] = (byte)net_para.wIntervalFrameI;
			*/
            Log.i(TAG, "Audio Enc Type =	" + net_para.byAudioEncType);                    //0-G722	1-G711_U	2-G711_A	5-MP2L2	6-G726	7-AAC，0xfe- 自动（和源一致），0xff-无效
            Log.i(TAG, "Bit Rate Type = 	" + net_para.byBitrateType);                    //0-变码率	1-定码率
            Log.i(TAG, "Frame Type = 		" + net_para.byIntervalBPFrame);                //0-BBP帧	1-BP帧 	2-P帧	0xff-无效
            Log.i(TAG, "Pic Qua = 			" + net_para.byPicQuality);                    //0 最高————>5最低		0xfe-自动(和源一致)
            Log.i(TAG, "Resolution = 		" + net_para.byResolution);                    //太多了，写不下，用到的时候查文档吧
            Log.i(TAG, "Stream Type = 		" + net_para.byStreamType);                    //0-视屏流	1-复合流	0xfe-自动(和源一致)
            Log.i(TAG, "Video Enc Type = 	" + net_para.byVideoEncType);                    //0-私有264，1-标准h264，2-标准mpeg4，7-M-JPEG，8-MPEG2，0xfe- 自动（和源一致），0xff-无效
            Log.i(TAG, "Video Bit Rate		" + net_para.dwVideoBitrate);                    //视频码率		太多不写	请查询文档
            Log.i(TAG, "Video Frame Rate = 	" + net_para.dwVideoFrameRate);                //视屏帧率		0-全部	1-1/16.....查文档
            Log.i(TAG, "IntervalFrameI = 	" + net_para.wIntervalFrameI);                //I 帧间隔	0xffee	和源一致	0xffff	无效
            channel = 0xFFFFFFFF;
//			Compress_Cfg = null;
        } else {
            Log.e(TAG, "获取视频参数失败,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }

    }

    public static void test(int userId, int channel) {
        NET_DVR_COMPRESSIONCFG_ABILITY Compress_Cfg = new NET_DVR_COMPRESSIONCFG_ABILITY();
        if (HCNetSDK.getInstance().NET_DVR_GetCompressionAbility(userId, channel, Compress_Cfg)) {
            Log.e(TAG, "NET_DVR_GetCompressionAbility,Success:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.e(TAG, "NET_DVR_GetCompressionAbility,Error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
        }

    }
}
