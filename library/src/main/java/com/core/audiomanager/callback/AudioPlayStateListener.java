package com.core.audiomanager.callback;

/**
 * 音频播放状态回调
 * Created by wangzhen on 2018/9/18.
 */
public interface AudioPlayStateListener {
    /**
     * 准备完毕
     */
    void onPrepared();

    /**
     * 播放完毕
     */
    void onComplete();

    /**
     * 播放出错
     *
     * @param error
     */
    void onError(String error);
}
