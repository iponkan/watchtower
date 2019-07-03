package com.hitqz.robot.watchtower.net;

/**
 * 文件上传监听
 */

public interface UploadProgressListener {
    void onProgress(long currentCount, long totalCount);
}
