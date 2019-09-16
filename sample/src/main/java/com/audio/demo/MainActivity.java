package com.audio.demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

import com.core.audiomanager.AudioHelper;
import com.core.audiomanager.callback.AudioPlayStateListener;
import com.core.audiomanager.callback.AudioRecordStateListener;
import com.core.audiomanager.widget.RangeSlider;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, RangeSlider.OnRangeChangeListener {

    private AudioHelper mAudioHelper;
    private RangeSlider mRangeSlider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initAudio();

        findViewById(R.id.btn_play).setOnClickListener(this);
        findViewById(R.id.btn_pause).setOnClickListener(this);
        findViewById(R.id.btn_resume).setOnClickListener(this);
        findViewById(R.id.btn_stop).setOnClickListener(this);
        findViewById(R.id.btn_record).setOnClickListener(this);
        findViewById(R.id.btn_record_stop).setOnClickListener(this);

        mRangeSlider = findViewById(R.id.bgm_range_slider);
        mRangeSlider.setRangeChangeListener(this);
    }

    private void initAudio() {
        mAudioHelper = AudioHelper.create(this);
        mAudioHelper.setPlayStateListener(new AudioPlayStateListener() {
            @Override
            public void onPrepared() {
                Toast.makeText(MainActivity.this, "开始播放", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onComplete() {
                Toast.makeText(MainActivity.this, "播放完毕", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "播放出错 --> " + error, Toast.LENGTH_SHORT).show();
            }
        });
        mAudioHelper.setRecordStateListener(new AudioRecordStateListener() {
            @Override
            public void onPrepared() {

            }

            @Override
            public void onComplete(String path) {
                Toast.makeText(MainActivity.this, "录音完成 --> " + path, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "录音出错 --> " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_play:
                String path = "http://10.100.119.192:8080/wangzhen/audio/hby.mp3";
                mAudioHelper.startPlay(path);
                break;
            case R.id.btn_pause:
                mAudioHelper.pausePlay();
                break;
            case R.id.btn_resume:
                mAudioHelper.resumePlay();
                break;
            case R.id.btn_stop:
                mAudioHelper.stopPlay();
                break;
            case R.id.btn_record:
                mAudioHelper.startRecord();
                break;
            case R.id.btn_record_stop:
                mAudioHelper.stopRecord();
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mAudioHelper != null) {
            mAudioHelper.onDestroy();
        }
    }

    @Override
    public void onKeyDown(int type) {

    }

    @Override
    public void onKeyUp(int type, int leftPinIndex, int rightPinIndex) {
        if (mAudioHelper != null && mAudioHelper.getPlayer() != null) {
            long duration = mAudioHelper.getPlayer().getDuration();
            long leftTime = duration * leftPinIndex / 100;
            long rightTime = duration * rightPinIndex / 100;
            mAudioHelper.rangePlay(leftTime, rightTime);
        }
    }
}
