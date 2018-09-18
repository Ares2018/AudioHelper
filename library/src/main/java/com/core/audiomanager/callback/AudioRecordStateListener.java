package com.core.audiomanager.callback;

/**
 * 音频录制状态回调
 * Created by wangzhen on 2018/9/18.
 */
public interface AudioRecordStateListener {
    /**
     * 准备完毕
     */
    void onPrepared();

    /**
     * 录音完毕
     */
    void onComplete(String path);

    /**
     * 发生错误
     */
    void onError(String error);
}
