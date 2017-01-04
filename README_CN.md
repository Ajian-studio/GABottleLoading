# GABottleLoading
### 1 创意原型：

#### 原型效果图如下：

![](https://github.com/Ajian-studio/GABottleLoading/raw/master/raw/bottleLoading_origin.gif)

#### 实现效果图如下：

![](https://github.com/Ajian-studio/GABottleLoading/raw/master/raw/totalAnimation.gif)

> Note：
> 
> * 1.动画中的瓶身及水的颜色都可以自我定义；

> * 2.整体宽高自行定义，内部元素均根据整体宽高自动缩放适应；

## 2 如何使用

### 2.1 获取项目资源

step 1. 在项目的build.gradle中加入如下代码：

```
allprojects {
		repositories {
			...
            // add the follow code
			maven { url 'https://jitpack.io' }
		}
	}
```
step 2. 在相应的模块的build.gradle中加入如下代码：

```
dependencies {
	compile 'com.github.Ajian-studio:GABottleLoading:1.0.1'
}

```

### 2.2 在布局文件中添加GABottleLoadingView

```
<com.gastudio.gabottleloading.library.GABottleLoadingView
    android:id="@+id/ga_bottle_loading_view"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    android:background="#ff191f26"
 />
```
> Note
> 
> * 如果View的背景颜色采用#ff191f26色值，将可以达到最佳的视觉效果.

### 2.3 在Activity中找到组件
```
((GABottleLoadingView) findViewById(R.id.ga_bottle_loading_view)).performAnimation();
```

## 3 核心接口和自定义属性

### 3.1 核心接口
> * perfromAnimation(): Start the animation;
> * cancel(): Release resources
> * setDebug(): can see the how it work

### 3.2 自定义属性

### 3.2.1 添加自定义属性命名空间

```
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
         xmlns:gastudio="http://schemas.android.com/apk/res-auto"
         ... ...
    />
```
### 3.2.2 添加自定义属性

```
    <com.gastudio.gabottleloading.library.GABottleLoadingView
        android:id="@+id/ga_downloading"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#ff191f26"
        gastudio:bottle_color="@android:color/white"
        gastudio:water_color="@android:color/holo_blue_light" />
```

**最后，如果你觉得还不错，欢迎Star！**

**欢迎加入GAStudio交流qq群: 277582728。**

## License
    Copyright 2017 GAStudio

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

