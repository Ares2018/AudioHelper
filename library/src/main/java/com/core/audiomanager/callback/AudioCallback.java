package com.core.audiomanager.callback;

/**
 * 音频相关接口
 * Created by wangzhen on 2017/8/8.
 */
public interface AudioCallback {
    /**
     * 音频录制接口
     */
    interface RecorderCallback {

        /**
         * 开始录制
         */
        void startRecord();

        /**
         * 取消录制
         */
        void cancelRecord();

        /**
         * 停止录制
         */
        void stopRecord();
    }

    /**
     * 音频播放接口
     */
    interface PlayerCallback {
        /**
         * 开始播放
         */
        void startPlay(String path);

        /**
         * 暂停播放
         */
        void pausePlay();

        /**
         * 恢复播放
         */
        void resumePlay();

        /**
         * 停止播放
         */
        void stopPlay();
    }

    /**
     * 播放状态控制接口
     */
    interface ControlCallback {
        /**
         * 音频停止播放
         */
        void onAudioStop();
    }
}
