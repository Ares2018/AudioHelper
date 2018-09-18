package com.core.audiomanager;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;

import com.core.audiomanager.callback.AudioPlayStateListener;
import com.core.audiomanager.callback.AudioRecordStateListener;
import com.core.audiomanager.callback.IAudioCallback;
import com.core.audiomanager.util.PathUtil;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * 音频播放&录制管理类
 * Created by wangzhen on 2018/9/18.
 */
public class AudioManager implements IAudioCallback.IRecordCallback, IAudioCallback.IPlayCallback {

    private static final int MSG_ERROR = 0x1;
    private static final int MSG_COMPLETE = 0x2;
    private static final int MSG_PREPARED = 0x3;

    private static AudioManager mInstance;
    private Context context;
    private MediaPlayer mMediaPlayer;
    private MediaRecorder mMediaRecorder;
    private AudioRecordStateListener mRecordStateListener;
    private AudioPlayStateListener mPlayStateListener;
    //录音保存目录
    private String mDir;
    //是否准备完毕
    private boolean isPrepared;
    //当前录音文件完整路径
    private static String mCurrRecordFilePath;
    //音频播放位置
    private int mCurrPlayPosition;
    //音频url
    private String mAudioUrl;

    public static AudioManager get(Context context) {
        if (mInstance == null) {
            synchronized (AudioManager.class) {
                if (mInstance == null) {
                    mInstance = new AudioManager(context);
                }
            }
        }
        mCurrRecordFilePath = "";
        return mInstance;
    }

    private AudioManager(Context ctx) {
        if (ctx == null)
            throw new NullPointerException("Context不能为null");
        context = ctx.getApplicationContext();
    }

    /**
     * 设置录音存储路径
     *
     * @param dir 目录
     */
    public void setDir(String dir) {
        this.mDir = dir;
    }

    /**
     * 获取音频分贝大小
     * mMediaRecorder.getMaxAmplitude() 1-32767
     *
     * @param maxLevel 最大级别
     * @return 1-maxLevel
     */
    public int getVoiceLevel(int maxLevel) {
        if (isPrepared) {
            try {
                return maxLevel * mMediaRecorder.getMaxAmplitude() / 32768 + 1;
            } catch (IllegalStateException e) {
            }
        }
        return 1;
    }

    @Override
    public void startRecord() {
        if (mMediaRecorder == null) {
            mMediaRecorder = new MediaRecorder();
        } else {
            mMediaRecorder.reset();
        }
        try {
            if (TextUtils.isEmpty(this.mDir))
                this.mDir = PathUtil.getSpeechPath();
            File dir = new File(mDir);
            if (!dir.exists())
                dir.mkdirs();
            String fileName = generateFileName();
            File file = new File(dir, fileName);
            mCurrRecordFilePath = file.getAbsolutePath();
            //设置输出文件
            mMediaRecorder.setOutputFile(file.getAbsolutePath());
            //设置输入源
            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            //设置音频格式
            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
            //设置音频编码
            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
            mMediaRecorder.prepare();
            mMediaRecorder.start();
            isPrepared = true;
            if (mRecordStateListener != null) {
                mRecordStateListener.onPrepared();
            }
        } catch (Exception e) {
            if (mRecordStateListener != null) {
                mRecordStateListener.onError(e.getMessage());
            }
        }
    }

    /**
     * 生成文件名
     *
     * @return 文件名
     */
    private String generateFileName() {
        SimpleDateFormat format = new SimpleDateFormat("yyyyMMddHHmmss", Locale.CHINA);
        Date date = new Date(System.currentTimeMillis());
        return format.format(date) + ".amr";
    }

    @Override
    public void cancelRecord() {
        deleteRecordFile();
        stopRecord();
    }

    /**
     * 删除生成的录音文件
     */
    public void deleteRecordFile() {
        if (TextUtils.isEmpty(mCurrRecordFilePath)) return;
        File file = new File(mCurrRecordFilePath);
        if (file.exists())
            file.delete();
        mCurrRecordFilePath = "";
    }

    @Override
    public void stopRecord() {
        if (mMediaRecorder != null) {
            isPrepared = false;
            try {
                mMediaRecorder.stop();
                mMediaRecorder.release();
                mMediaRecorder = null;
                if (!TextUtils.isEmpty(mCurrRecordFilePath)) {
                    if (mRecordStateListener != null) {
                        mRecordStateListener.onComplete(mCurrRecordFilePath);
                    }
                } else {
                    if (mRecordStateListener != null) {
                        mRecordStateListener.onError("record file does not exist");
                    }
                }
            } catch (RuntimeException e) {
                deleteRecordFile();
                if (mRecordStateListener != null) {
                    mRecordStateListener.onError(e.getMessage());
                }
            }
        } else {
            if (mRecordStateListener != null) {
                mRecordStateListener.onError("mMediaRecorder为null");
            }
        }
    }

    @Override
    public void startPlay(String audioUrl) {
        mAudioUrl = audioUrl;
        innerPlayer();
    }

    /**
     * 播放音频
     */
    private void innerPlayer() {
        if (TextUtils.isEmpty(mAudioUrl)) {
            onMainError("invalid music url");
            return;
        }
        if (mMediaPlayer == null) {
            try {
                mMediaPlayer = new MediaPlayer();
            } catch (Exception e) {
                //高通平台直接new MediaPlayer()可能会报错，暂时catch
                onMainError(e.getMessage());
            }
        } else {
            mMediaPlayer.reset();
        }
        if (mMediaPlayer == null) {
            onMainError("MediaPlayer inner create failed.");
            return;
        }
        mCurrPlayPosition = 0;

        try {
            mMediaPlayer.setAudioStreamType(android.media.AudioManager.STREAM_MUSIC);
            mMediaPlayer.setDataSource(mAudioUrl);
            mMediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    onMainComplete();
                    stopPlay();
                }
            });
            mMediaPlayer.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                @Override
                public boolean onError(MediaPlayer mp, int what, int extra) {
                    onMainError("播放失败");
                    return true;
                }
            });
            mMediaPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    AudioManager.this.onMainPrepared();
                    mMediaPlayer.start();
                    obtainFocus();
                }
            });
            mMediaPlayer.prepareAsync();
        } catch (Exception e) {
            onMainError(e.getMessage());
        }
    }

    @Override
    public void pausePlay() {
        if (mMediaPlayer != null) {
            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.pause();
                mCurrPlayPosition = mMediaPlayer.getCurrentPosition();
                releaseFocus();
            }
        }
    }

    @Override
    public void resumePlay() {
        if (mMediaPlayer != null) {
            mMediaPlayer.seekTo(mCurrPlayPosition);
            mMediaPlayer.start();
            obtainFocus();
        }
    }

    @Override
    public void stopPlay() {
        if (mMediaPlayer != null) {
            try {
                releaseFocus();
                mMediaPlayer.stop();
                mMediaPlayer.release();
                mMediaPlayer = null;
            } catch (RuntimeException e) {
                onMainError(e.getMessage());
            }
        }
    }

    /**
     * 全部销毁
     */
    public void onDestroy() {
        stopRecord();
        stopPlay();
    }

    /**
     * 音频是否正在播放
     *
     * @return 播放状态
     */
    public boolean isAudioPlaying() {
        return mMediaPlayer != null && mMediaPlayer.isPlaying();
    }

    /**
     * 获取当前播放的音频url
     *
     * @return 音频url
     */
    public String getAudioUrl() {
        return mAudioUrl;
    }

    public void setRecordStateListener(AudioRecordStateListener listener) {
        this.mRecordStateListener = listener;
    }

    public void setPlayStateListener(AudioPlayStateListener listener) {
        this.mPlayStateListener = listener;
    }

    private void obtainFocus() {
        android.media.AudioManager am = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null)
            am.requestAudioFocus(null, android.media.AudioManager.STREAM_MUSIC, android.media.AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
    }

    private void releaseFocus() {
        android.media.AudioManager am = (android.media.AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        if (am != null)
            am.abandonAudioFocus(null);
    }

    private void onMainError(String error) {
        Message message = Message.obtain();
        message.what = MSG_ERROR;
        message.obj = error;
        mainHandler.sendMessage(message);
    }

    private void onMainPrepared() {
        Message message = Message.obtain();
        message.what = MSG_PREPARED;
        mainHandler.sendMessage(message);
    }

    private void onMainComplete() {
        Message message = Message.obtain();
        message.what = MSG_COMPLETE;
        mainHandler.sendMessage(message);
    }

    private Handler mainHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_ERROR:
                    if (mPlayStateListener != null) {
                        String error = msg.obj != null ? String.valueOf(msg.obj) : "";
                        mPlayStateListener.onError(error);
                    }
                    break;
                case MSG_COMPLETE:
                    if (mPlayStateListener != null) {
                        mPlayStateListener.onComplete();
                    }
                    break;
                case MSG_PREPARED:
                    if (mPlayStateListener != null) {
                        mPlayStateListener.onPrepared();
                    }
                    break;
            }
        }
    };
}
