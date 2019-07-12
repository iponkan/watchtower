package com.hitqz.robot.watchtower;

import android.content.Context;
import android.graphics.PixelFormat;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import androidx.annotation.NonNull;

import com.hikvision.netsdk.ExceptionCallBack;
import com.hikvision.netsdk.HCNetSDK;
import com.hikvision.netsdk.NET_DVR_CONFIG;
import com.hikvision.netsdk.NET_DVR_DEVICEINFO_V30;
import com.hikvision.netsdk.NET_DVR_PREVIEWINFO;
import com.hikvision.netsdk.NET_SDK_CALLBACK_TYPE;
import com.hikvision.netsdk.PlaybackCallBack;
import com.hikvision.netsdk.PlaybackControlCommand;
import com.hikvision.netsdk.RealPlayCallBack;
import com.hikvision.netsdk.RemoteConfigCallback;
import com.hitqz.robot.watchtower.bean.FileInfo;
import com.hitqz.robot.watchtower.bean.TimeStruct;
import com.hitqz.robot.watchtower.constant.LoginInfo;
import com.hitqz.robot.watchtower.hcnetw.NET_DVR_REALTIME_THERMOMETRY_COND;
import com.hitqz.robot.watchtower.hcnetw.NET_DVR_THERMOMETRY_BASICPARAM;
import com.hitqz.robot.watchtower.hcnetw.NET_DVR_THERMOMETRY_COND;
import com.hitqz.robot.watchtower.player.PlayerCallback;
import com.hitqz.robot.watchtower.util.CameraUtil;
import com.hitqz.robot.watchtower.util.PathUtil;
import com.orhanobut.logger.Logger;

import org.MediaPlayer.PlayM4.Player;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class HCSdkManager implements SurfaceHolder.Callback {
    public static final String TAG = "HCSdkManager";
    private LoginInfo loginInfo;

    private static HashMap<LoginInfo, HCSdkManager> hcSdkManagers = new HashMap<>();

    private Context applicationContext;

    private HCSdkManager(Context context, LoginInfo li) {
        this.applicationContext = context.getApplicationContext();
        this.loginInfo = li;
    }

    public static HCSdkManager getHCSdkManager(Context context, LoginInfo loginInfo) {
        HCSdkManager hcSdkManager = hcSdkManagers.get(loginInfo);
        if (hcSdkManager == null) {
            hcSdkManager = new HCSdkManager(context, loginInfo);
            hcSdkManagers.put(loginInfo, hcSdkManager);
        }
        return hcSdkManager;
    }

    public static HCSdkManager getNormalHCSdkManager(Context context) {
        return HCSdkManager.getHCSdkManager(context, LoginInfo.getNormalLogInfo());
    }

    public static HCSdkManager getHotHCSdkManager(Context context) {
        return HCSdkManager.getHCSdkManager(context, LoginInfo.getHotLogInfo());
    }

    // @Override
    public void surfaceCreated(SurfaceHolder holder) {
        holder.setFormat(PixelFormat.TRANSLUCENT);
        Log.i(TAG, "surface is created");
        Surface surface = holder.getSurface();
        if (surface.isValid()) {
            if (!Player.getInstance()
                    .setVideoWindow(m_iPort, 0, holder)) {
                Log.e(TAG, "Player setVideoWindow failed!");
            }
            if (playbackRunnable != null) {
                playbackRunnable.run();
                playbackRunnable = null;
            }

            if (needPreview) {
                startPreviewInternal();
                needPreview = false;
            }
        }


    }

    // @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
    }

    // @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(TAG, "Player setVideoWindow release!" + m_iPort);
    }

    boolean init = false;

    public boolean isInit() {
        return init;
    }

    public boolean init() {
        // init net sdk
//        Logger.i("before initSdk");
        boolean result = HCNetSDK.getInstance().NET_DVR_Init();
//        Logger.i("after initSdk");
        if (!result) {
            Logger.e("HCNetSDK init is failed!");
            return false;
        }

        HCNetSDK.getInstance().NET_DVR_SetLogToFile(3, PathUtil.getSdkLogPath(),
                true);
        init = true;
        return true;
    }

    private int m_iLogID = -1; // return by NET_DVR_Login_v30

    private NET_DVR_DEVICEINFO_V30 m_oNetDvrDeviceInfoV30 = null;
    private int m_iStartChan = 0; // start channel no
    private int m_iChanNum = 0; // channel number

    private int loginNormalDevice() {
        // get instance
        m_oNetDvrDeviceInfoV30 = new NET_DVR_DEVICEINFO_V30();
        String strIP = loginInfo.m_oIPAddr;
        int nPort = Integer.parseInt(loginInfo.m_oPort);
        String strUser = loginInfo.m_oUser;
        String strPsd = loginInfo.m_oPsd;
        // call NET_DVR_Login_v30 to login on, port 8000 as default
        int iLogID = HCNetSDK.getInstance().NET_DVR_Login_V30(strIP, nPort,
                strUser, strPsd, m_oNetDvrDeviceInfoV30);
        if (iLogID < 0) {
            Logger.e("NET_DVR_Login is failed!Err:"
                    + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return -1;
        }
        if (m_oNetDvrDeviceInfoV30.byChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byChanNum;
        } else if (m_oNetDvrDeviceInfoV30.byIPChanNum > 0) {
            m_iStartChan = m_oNetDvrDeviceInfoV30.byStartDChan;
            m_iChanNum = m_oNetDvrDeviceInfoV30.byIPChanNum
                    + m_oNetDvrDeviceInfoV30.byHighDChanNum * 256;
        }
        Logger.i("NET_DVR_Login is Successful!");

        return iLogID;
    }

    private int loginDevice() {
        int iLogID = -1;

        iLogID = loginNormalDevice();

        return iLogID;
    }

    private ExceptionCallBack getExceptiongCbf() {
        ExceptionCallBack oExceptionCbf = new ExceptionCallBack() {
            public void fExceptionCallBack(int iType, int iUserID, int iHandle) {
                System.out.println("recv exception, type:" + iType);
            }
        };
        return oExceptionCbf;
    }

    public boolean isLogin() {
        return !(m_iLogID < 0);
    }

    public boolean isPreviewing() {
        return !(m_iPlayID < 0);
    }

    public boolean isPlayback() {
        return !(m_iPlaybackID < 0);
    }

    public boolean login() {
        if (m_iLogID < 0) {
            // login on the device
            m_iLogID = loginDevice();
            if (m_iLogID < 0) {
                Logger.e("This device logins failed!");
                return false;
            } else {
                Logger.i("m_iLogID=" + m_iLogID);
            }
            // get instance of exception callback and set
            ExceptionCallBack oexceptionCbf = getExceptiongCbf();
            if (oexceptionCbf == null) {
                Logger.e("ExceptionCallBack object is failed!");
                return false;
            }

            if (!HCNetSDK.getInstance().NET_DVR_SetExceptionCallBack(
                    oexceptionCbf)) {
                Logger.e("NET_DVR_SetExceptionCallBack is failed!");
                return false;
            }


            Logger.i(
                    "Login sucess ****************************1***************************");
            return true;
        } else {
            Logger.e("already login !");
            return true;
        }
    }

    public boolean logout() {
        // whether we have logout
        if (!HCNetSDK.getInstance().NET_DVR_Logout_V30(m_iLogID)) {
            Log.e(TAG, " NET_DVR_Logout is failed!");
            return true;
        }
        m_iLogID = -1;
        return false;
    }

    private int m_iPlayID = -1; // return by NET_DVR_RealPlay_V30
    private int m_iPlaybackID = -1; // return by NET_DVR_PlayBackByTime
    private int m_iPort = -1; // play port
    private boolean m_bStopPlayback = false;

    private WeakReference<SurfaceView> surfaceView;

    public void setSurfaceView(@NonNull SurfaceView svView) {
        surfaceView = new WeakReference<>(svView);
        svView.getHolder().addCallback(this);
    }

    private boolean needPreview = false;

    public void startSinglePreview() {
        if (surfaceView == null || surfaceView.get() == null) {
            Log.e(TAG, "must call setSurfaceView before preview");
            return;
        }

        Surface surface = surfaceView.get().getHolder().getSurface();
        if (!surface.isValid()) {
            needPreview = true;
        } else {
            startPreviewInternal();
        }

    }

    private boolean startPreviewInternal() {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on device first");
            return false;
        }

        if (m_iPlaybackID >= 0) {
            Log.i(TAG, "Please stop palyback first");
            return false;
        }
        RealPlayCallBack fRealDataCallBack = getRealPlayerCbf();
        Log.i(TAG, "m_iStartChan:" + m_iStartChan);

        NET_DVR_PREVIEWINFO previewInfo = new NET_DVR_PREVIEWINFO();
        previewInfo.lChannel = m_iStartChan;
        previewInfo.dwStreamType = 0; // substream
        previewInfo.bBlocked = 1;

        m_iPlayID = HCNetSDK.getInstance().NET_DVR_RealPlay_V40(m_iLogID, previewInfo, fRealDataCallBack);
        if (m_iPlayID < 0) {
            Log.e(TAG, "NET_DVR_RealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }

        Log.i(TAG, "NetSdk Play success ***********************3***************************");
        return true;
    }

    public boolean stopSinglePreview() {
        if (m_iPlayID < 0) {
            Log.e(TAG, "m_iPlayID < 0");
            return false;
        }

        // net sdk stop preview
        if (!HCNetSDK.getInstance().NET_DVR_StopRealPlay(m_iPlayID)) {
            Log.e(TAG, "StopRealPlay is failed!Err:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return false;
        }

        m_iPlayID = -1;
        stopSinglePlayer();
        return true;
    }

    private void stopSinglePlayer() {
        Player.getInstance().stopSound();
        // player stop play
        if (!Player.getInstance().stop(m_iPort)) {
            Log.e(TAG, "stop is failed!");
            return;
        }

        if (!Player.getInstance().closeStream(m_iPort)) {
            Log.e(TAG, "closeStream is failed!");
            return;
        }
        if (!Player.getInstance().freePort(m_iPort)) {
            Log.e(TAG, "freePort is failed!" + m_iPort);
            return;
        }
        m_iPort = -1;
    }

    private RealPlayCallBack getRealPlayerCbf() {
        RealPlayCallBack cbf = new RealPlayCallBack() {
            public void fRealDataCallBack(int iRealHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                // player channel 1
                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_REALTIME);
            }
        };
        return cbf;
    }

    public void processRealData(int iPlayViewNo, int iDataType,
                                byte[] pDataBuffer, int iDataSize, int iStreamMode) {
        if (HCNetSDK.NET_DVR_SYSHEAD == iDataType) {
            if (m_iPort >= 0) {
                return;
            }
            m_iPort = Player.getInstance().getPort();
            if (m_iPort == -1) {
                Log.e(TAG, "getPort is failed with: " + Player.getInstance().getLastError(m_iPort));
                return;
            }
            Log.i(TAG, "getPort succ with: " + m_iPort);
            if (iDataSize > 0) {
                if (!Player.getInstance().setStreamOpenMode(m_iPort, iStreamMode)) {// set stream mode
                    Log.e(TAG, "setStreamOpenMode failed");
                    return;
                }
                if (!Player.getInstance().openStream(m_iPort, pDataBuffer, iDataSize, 2 * 1024 * 1024)) {// open stream
                    Log.e(TAG, "openStream failed");
                    return;
                }
                if (surfaceView.get() == null || !Player.getInstance().play(m_iPort, surfaceView.get().getHolder())) {
                    Log.e(TAG, "play failed");
                    return;
                }
                if (!Player.getInstance().playSound(m_iPort)) {
                    Log.e(TAG, "playSound failed with error code:" + Player.getInstance().getLastError(m_iPort));
                }
            }
        } else {
            if (!Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
                Log.e(TAG, "inputData failed with: " +
                        Player.getInstance().getLastError(m_iPort));
//                for (int i = 0; i < 4000 && m_iPlaybackID >= 0 && !m_bStopPlayback; i++) {
//                    if (Player.getInstance().inputData(m_iPort, pDataBuffer, iDataSize)) {
//                        break;
//                    }
//
////                    if (i % 100 == 0) {
////                        Log.e(TAG, "inputData failed with: " + Player.getInstance().getLastError(m_iPort) + ", i:" + i);
////                    }
//
//                    try {
//                        Thread.sleep(10);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//
//                    }
//                }
            }

        }
    }

    private PlaybackCallBack getPlayerbackPlayerCbf() {
        PlaybackCallBack cbf = new PlaybackCallBack() {
            @Override
            public void fPlayDataCallBack(int iPlaybackHandle, int iDataType, byte[] pDataBuffer, int iDataSize) {
                // player channel 1
                processRealData(1, iDataType, pDataBuffer, iDataSize, Player.STREAM_FILE);
            }
        };
        return cbf;
    }

    public static final int STATE_IDLE = 0;
    public static final int STATE_PLAYING = 1;
    public static final int STATE_PAUSE = 2;
    public static final int STATE_STOP = 3;
    private int playState = STATE_IDLE;

    public void playBack(FileInfo fi) {

        fileInfo = fi;
        if (surfaceView == null || surfaceView.get() == null) {
            Log.e(TAG, "must call setSurfaceView before preview");
            return;
        }

        Surface surface = surfaceView.get().getHolder().getSurface();
        if (!surface.isValid()) {
            playbackRunnable = new PlaybackRunnable(fi.fileName);
        } else {
            playBackInternal(fi.fileName);
        }

    }

    private void playBackInternal(String fileName) {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on a device first");
            return;
        }
        if (m_iPlaybackID < 0) {
            if (m_iPlayID >= 0) {
                Log.i(TAG, "Please stop preview first");
                return;
            }

            m_iPlaybackID = HCNetSDK.getInstance().NET_DVR_PlayBackByName(m_iLogID, fileName);
            if (m_iPlaybackID >= 0) {
                PlaybackCallBack fPlaybackCallBack = getPlayerbackPlayerCbf();
                if (!HCNetSDK.getInstance().NET_DVR_SetPlayDataCallBack(m_iPlaybackID, fPlaybackCallBack)) {
                    Log.e(TAG, "Set playback callback failed!");
                    return;
                }
                if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                        PlaybackControlCommand.NET_DVR_PLAYSTART, null, 0, null)) {
                    Log.e(TAG, "net sdk playback start failed!");
                    return;
                }
                m_bStopPlayback = false;

                notifyPlayStart();

            } else {
                Log.i(TAG, "NET_DVR_PlayBackByName failed, error code: " +
                        HCNetSDK.getInstance().NET_DVR_GetLastError());
            }
        }
    }

    public void pausePlayBack() {
        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                PlaybackControlCommand.NET_DVR_PLAYPAUSE, null, 0, null)) {
            Log.e(TAG, "net sdk playback pause failed!");
        }
        Player.getInstance().pause(m_iPort, 1);
        Player.getInstance().stopSound();

        notifyPlayPause();
    }

    public void resumePlayBack() {
        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                PlaybackControlCommand.NET_DVR_PLAYRESTART, null, 0, null)) {
            Log.e(TAG, "net sdk playback pause failed!");
        }
        Player.getInstance().pause(m_iPort, 0);
        Player.getInstance().playSound(m_iPort);

        notifyPlayStart();
    }

    public static byte[] int2byte(int s) {
        byte[] arr = new byte[60];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = (byte) ((s >> i * 8) & 0xff);
        }
        return arr;
    }

    public int getPlayedFrames() {
        return Player.getInstance().getPlayedFrames(m_iPort);
    }

    public void playbackSeekTo(float percent) {

        // 需要把缓冲流清掉，不然播放器在这个时候会继续播放缓冲内容
//        Player.getInstance().resetSourceBuffer(m_iPort);

        int progress = (int) (percent * 100);

        byte[] bytes = int2byte(progress);

        if (!HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(m_iPlaybackID,
                12, bytes, 0, null)) {
            Log.e(TAG, "NET_DVR_PlayBackControl_V40 failed, error code: " +
                    HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.i(TAG, "NET_DVR_PlayBackControl_V40 success");
        }

    }

    public void stopPlayback() {
        m_bStopPlayback = true;
        if (!HCNetSDK.getInstance().NET_DVR_StopPlayBack(m_iPlaybackID)) {
            Log.e(TAG, "net sdk stop playback failed");
        } // player stop play
        stopSinglePlayer();
        notifyPlayStop();

        if (!Player.getInstance().setVideoWindow(m_iPort, 0, null)) {
            Log.e(TAG, "Player setVideoWindow failed!");
        }
        m_iPlaybackID = -1;
    }


    public List<FileInfo> findFile() {
        if (m_iLogID < 0) {
            Log.e(TAG, "please login on a device first");
            return null;
        }
        List<FileInfo> fileList = new ArrayList<>();
        CameraUtil.test_FindFile(m_iLogID, fileList);
        return fileList;
    }

    public void release() {
        stopSinglePreview();
        stopPlayback();
        logout();
    }

    public int getPlayBackTime() {
        int progress = Player.getInstance().getPlayedTime(m_iPort);
        Log.i(TAG, "Player getPlayedTime====" + Player.getInstance().getPlayedTime(m_iPort));
        return progress;
    }

    private FileInfo fileInfo;

    public int getPlaybackDuration() {
        if (fileInfo != null) {
            int duration = TimeStruct.getDuration(fileInfo.startTime, fileInfo.stopTime);
            Log.i(TAG, "duration ==== " + duration);
            return duration;
        }
        return 0;
    }

    class PlaybackRunnable implements Runnable {

        String fileName;

        PlaybackRunnable(String fn) {
            fileName = fn;
        }

        @Override
        public void run() {
            playBackInternal(fileName);
        }
    }

    private PlaybackRunnable playbackRunnable = null;

    private List<PlayerCallback> callbacks = null;

    public void addPlayerCallBack(PlayerCallback callBack) {
        if (callbacks == null) {
            callbacks = new ArrayList<>();
        }
        callbacks.add(callBack);
    }

    public void removePlayerCallBack(PlayerCallback callBack) {
        if (callbacks == null) {
            return;
        }
        callbacks.remove(callBack);
        if (callbacks.size() == 0) {
            callbacks = null;
        }
    }

    private void notifyPlayStart() {
        playState = STATE_PLAYING;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayStart();
            }
        }
    }

    private void notifyPlayPause() {
        playState = STATE_PAUSE;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayPause();
            }
        }
    }

    private void notifyPlayStop() {
        playState = STATE_STOP;
        if (callbacks != null) {
            for (PlayerCallback callback : callbacks) {
                callback.onPlayStop();
            }
        }
    }


    public boolean isPlaying() {
        return playState == STATE_PLAYING;
    }

    public boolean isPause() {
        return playState == STATE_PAUSE;
    }

    public void Test_GetFileByName(String fileName) {
        int nDownloadHandle = HCNetSDK.getInstance().NET_DVR_GetFileByName(m_iLogID, fileName, "/sdcard/RecordFile.mp4");
        if (-1 == nDownloadHandle) {
            System.out.println("NET_DVR_GetFileByName failed! error:" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        HCNetSDK.getInstance().NET_DVR_PlayBackControl_V40(nDownloadHandle, PlaybackControlCommand.NET_DVR_PLAYSTART, null, 0, null);
        int nProgress = -1;
        while (true) {
            nProgress = HCNetSDK.getInstance().NET_DVR_GetDownloadPos(nDownloadHandle);
            System.out.println("NET_DVR_GetDownloadPos:" + nProgress);
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
        HCNetSDK.getInstance().NET_DVR_StopGetFile(nDownloadHandle);
    }

    public void Test_WIRELESSDIAL_CFG() {
        NET_DVR_THERMOMETRY_COND struCardNo = new NET_DVR_THERMOMETRY_COND();
        struCardNo.iValue = 1;
        NET_DVR_THERMOMETRY_BASICPARAM cfg = new NET_DVR_THERMOMETRY_BASICPARAM();
        if (!HCNetSDK.getInstance().NET_DVR_GetSTDConfig(m_iLogID, NET_DVR_THERMOMETRY_BASICPARAM.NET_DVR_GET_THERMOMETRY_BASICPARAM, struCardNo, null, cfg)) {
            Log.e(TAG, "NET_DVR_GET_WIRELESSSERVER_THERMOMETRY_BASICPARAM faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.e(TAG, "NET_DVR_GET_WIRELESSSERVER_THERMOMETRY_BASICPARAM succ!");
        }
        if (!HCNetSDK.getInstance().NET_DVR_SetSTDConfig(m_iLogID, NET_DVR_THERMOMETRY_BASICPARAM.NET_DVR_GET_THERMOMETRY_BASICPARAM, struCardNo, cfg, null)) {
            Log.e(TAG, "NET_DVR_SET_WIRELESSSERVER_THERMOMETRY_BASICPARAM faild!" + " err: " + HCNetSDK.getInstance().NET_DVR_GetLastError());
        } else {
            Log.e(TAG, "NET_DVR_SET_WIRELESSSERVER_THERMOMETRY_BASICPARAM succ!");
        }
    }

    public void test_ScreenCtrl() {
        RemoteConfigCallback fRemoteConfigCallback = getRemoteConfigCbf();
        int lHandle = HCNetSDK.getInstance().NET_DVR_StartRemoteConfig(m_iLogID, 3629, null, fRemoteConfigCallback, null);
        if (lHandle < 0) {
            Log.e(TAG, "NET_DVR_StartRemoteConfig NET_DVR_START_SCREEN_CRTL failed" + HCNetSDK.getInstance().NET_DVR_GetLastError());
            return;
        }
        Log.i(TAG, "NET_DVR_StartRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//
//        NET_DVR_SCREEN_CTRL_CMD struCmd = new NET_DVR_SCREEN_CTRL_CMD();
//        struCmd.byCmdType = 8;
//        NET_DVR_MEDIA_LIST_PARAM struMedia = new NET_DVR_MEDIA_LIST_PARAM();
//        struMedia.byOperateCmd = 1;
//        struCmd.struScreenCtrlParam.struMediaListParam = struMedia;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 5;
//        NET_DVR_REMOTE_CTRL_PARAM struRemoteCtrl = new NET_DVR_REMOTE_CTRL_PARAM();
//        struRemoteCtrl.byRemoteCtrlCmd = 3;
//        struRemoteCtrl.dwCtrlParam = 1;
//        struCmd.struScreenCtrlParam.struRemoteCtrlParam = struRemoteCtrl;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 4;
//        NET_DVR_PPT_PARAM struPPT = new NET_DVR_PPT_PARAM();
//        struPPT.byPPTAction = 1;
//        struCmd.struScreenCtrlParam.struPPTParam = struPPT;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        INT_PTR pState = new INT_PTR();
//        if (!HCNetSDK.getInstance().NET_DVR_GetRemoteConfigState((int) lHandle, pState)) {
//            Log.e(TAG, "NET_DVR_GetRemoteConfigState NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_GetRemoteConfigState NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 7;
//        NET_DVR_TOUCHPAD_PARAM struTouch = new NET_DVR_TOUCHPAD_PARAM();
//        struTouch.byMouseEvent = 1;
//        struTouch.iXDisplacement = 100;
//        struTouch.iYDisplacement = 100;
//        struCmd.struScreenCtrlParam.struTouchPadParam = struTouch;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 6;
//        NET_DVR_SPOTLIGHT_PARAM struSpot = new NET_DVR_SPOTLIGHT_PARAM();
//        struSpot.byCmd = 1;
//        struSpot.dwRadius = 1;
//        NET_DVR_SCREEN_POINT struPoint = new NET_DVR_SCREEN_POINT();
//        struPoint.wX = 200;
//        struPoint.wY = 200;
//        struSpot.struPoint = struPoint;
//        struCmd.struScreenCtrlParam.struSpotLight = struSpot;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 5;
//        NET_DVR_REMOTE_CTRL_PARAM struRemote = new NET_DVR_REMOTE_CTRL_PARAM();
//        struRemote.byRemoteCtrlCmd = 1;
//        struCmd.struScreenCtrlParam.struRemoteCtrlParam = struRemote;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//
//        struCmd.byCmdType = 3;
//        NET_DVR_KEYBOARD_PARAM struKey = new NET_DVR_KEYBOARD_PARAM();
//        struKey.dwKeyValue = 96;
//        struCmd.struScreenCtrlParam.struKeyboardInfo = struKey;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 2;
//        NET_DVR_MARK_PARAM struMark = new NET_DVR_MARK_PARAM();
//        struMark.byMarkEvent = 1;
//        struPoint = new NET_DVR_SCREEN_POINT();
//        struPoint.wX = 200;
//        struPoint.wY = 200;
//        struMark.struPoint = struPoint;
//        NET_DVR_RGB_COLOR struColor = new NET_DVR_RGB_COLOR();
//        struColor.byBlue = 100;
//        struColor.byGreen = 100;
//        struColor.byRed = 100;
//        struMark.struColor = struColor;
//        struCmd.struScreenCtrlParam.struMarkParam = struMark;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 1;
//        NET_DVR_MOUSE_PARAM struMouse = new NET_DVR_MOUSE_PARAM();
//        struPoint = new NET_DVR_SCREEN_POINT();
//        struPoint.wX = 200;
//        struPoint.wY = 200;
//        struMouse.struMousePoint = struPoint;
//        struMouse.byMouseEvent = 1;
//        struCmd.struScreenCtrlParam.struMouseParam = struMouse;
//
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//
//        struCmd.byCmdType = 2;
//
//        struMark.byMarkEvent = 1;
//
//        struPoint.wX = 200;
//        struPoint.wY = 200;
//        struMark.struPoint = struPoint;
//
//        struColor.byBlue = 100;
//        struColor.byGreen = 100;
//        struColor.byRed = 100;
//        struMark.struColor = struColor;
//        struCmd.struScreenCtrlParam.struMarkParam = struMark;
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//        struCmd.byCmdType = 1;
//        struPoint = new NET_DVR_SCREEN_POINT();
//        struPoint.wX = 200;
//        struPoint.wY = 200;
//        struMouse.struMousePoint = struPoint;
//        struMouse.byMouseEvent = 1;
//        struCmd.struScreenCtrlParam.struMouseParam = struMouse;
//
//        if (!HCNetSDK.getInstance().NET_DVR_SendRemoteConfig((int) lHandle, LONG_CFG_SEND_DATA_TYPE_ENUM.ENUM_DVR_SCREEN_CTRL_CMD, struCmd)) {
//            Log.e(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_SendRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");
//
//        if (!HCNetSDK.getInstance().NET_DVR_StopRemoteConfig((int) lHandle)) {
//            Log.e(TAG, "NET_DVR_StopRemoteConfig NET_DVR_START_SCREEN_CRTL failed");
//            return;
//        }
//        Log.i(TAG, "NET_DVR_StopRemoteConfig NET_DVR_START_SCREEN_CRTL succeed");

    }

    private RemoteConfigCallback getRemoteConfigCbf() {
        RemoteConfigCallback cbf = new RemoteConfigCallback() {
            public void fRemoteConfigCallback(int dwType, NET_DVR_CONFIG oConfig, byte[] pUserData) {
                processRemoteConfigData(dwType, oConfig, pUserData);
            }
        };
        return cbf;
    }

    public void processRemoteConfigData(int dwType, NET_DVR_CONFIG oConfig, byte[] pUserData) {
        if (oConfig == null) {
            return;
        }
        switch (dwType) {
            case NET_SDK_CALLBACK_TYPE.NET_SDK_CALLBACK_TYPE_DATA:
                NET_DVR_REALTIME_THERMOMETRY_COND oScreenRespone = (NET_DVR_REALTIME_THERMOMETRY_COND) oConfig;
                Log.i(TAG, "Response parameter callback succeed");
                break;
            default:
                break;
        }
    }
}
