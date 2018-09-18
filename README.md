# AudioManager
> 音频播放&录制管理类

#### 添加依赖

1、主工程build.gradle添加仓库地址
``` gradle
allprojects {
    repositories {
        google()
        jcenter()
        maven { url "http://10.100.62.98:8086/nexus/content/groups/public" }
    }
}
```

2、项目工程build.gradle添加依赖 (点击查看[最新版本](http://10.100.62.98:8086/nexus/#nexus-search;gav~com.core~audiomanager~~~))
``` gradle
compile 'com.core:audiomanager:0.0.1'
```

#### 使用方法

见demo