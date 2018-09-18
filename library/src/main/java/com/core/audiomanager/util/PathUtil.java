package com.core.audiomanager.util;

import android.os.Environment;

import java.io.File;

public class PathUtil {

    /**
     * 获取录音文件存储路径
     *
     * @return
     */
    public static String getSpeechPath() {
        File path = new File(Environment.getExternalStorageDirectory() + File.separator + "speech");
        if (!path.exists()) {
            path.mkdirs();
        }
        return path.getAbsolutePath();
    }
}
