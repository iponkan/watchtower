package com.hitqz.robot.watchtower.util;

import android.content.Context;
import android.media.AudioManager;
import android.media.SoundPool;

import com.hitqz.robot.watchtower.R;

public class SoundPoolUtil {

    private static SoundPoolUtil soundPoolUtil;
    private SoundPool soundPool;

    public SoundPoolUtil(Context context) {
        soundPool = new SoundPool(3, AudioManager.STREAM_SYSTEM, 5);
        //加载音频文件
        soundPool.load(context, R.raw.alert, 1);
//可以添加多个
//   soundPool.load(context, R.raw.yuyin,2);
//   soundPool.load(context, R.raw.yuyin, 3);

    }

    //单例模式
    public static SoundPoolUtil getInstance(Context context) {
        if (soundPoolUtil == null) {
            soundPoolUtil = new SoundPoolUtil(context);
        }
        return soundPoolUtil;
    }

    public void play(int number) {
//        Log.d("tag", "number " + number);
        //播放音频
        soundPool.play(number, 1, 1, 0, 0, 1);
    }
}
