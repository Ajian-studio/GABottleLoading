# GABottleLoading

## [中文文档](https://github.com/Ajian-studio/GABottleLoading/blob/master/README_CN.md)

### 1 Creative prototype：

#### The effect is shown below：

![](https://github.com/Ajian-studio/GABottleLoading/raw/master/raw/bottleLoading_origin.gif)

#### Through the code to achieve the effect is as follows：

![](https://github.com/Ajian-studio/GABottleLoading/raw/master/raw/totalAnimation.gif)

> Note：
> 
> * 1.The project does not use any image resources, colors of bottle and water can be changed by custom attributes；

> * 2.LoadingView will automatically adjust the width and height；

## 2 How to Use:

### 2.1 How To Obtain Project Resources

step 1. In the project build.gradle add the following code：

```
allprojects {
		repositories {
			...
            // add the follow code
			maven { url 'https://jitpack.io' }
		}
	}
```
step 2. In the module's build.gradle add the following code：

```
dependencies {
	compile 'com.github.Ajian-studio:GABottleLoading:1.0.1'
}

```

### 2.2 Add GABottleLoadingView To The Layout File

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
> * By setting the background of the View to #ff191f26, you can achieve the best visual effect.
### 2.3 Find The View In The Activity
```
((GABottleLoadingView) findViewById(R.id.ga_bottle_loading_view)).performAnimation();
```

## 3 Core Interface And Customize Properties

### 3.1 Core Interface
> * perfromAnimation(): Start the animation;
> * cancel(): Release resources
> * setDebug(): can see the how it work

### 3.2 Custom Properties

### 3.2.1 Add a custom property namespace：

```
    <RelativeLayout xmlns:android="http://schemas.android.com/apk/res/android"
         xmlns:gastudio="http://schemas.android.com/apk/res-auto"
         ... ...
    />
```
### 3.2.2 Add some custom properties

```
    <com.gastudio.gabottleloading.library.GABottleLoadingView
        android:id="@+id/ga_downloading"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:background="#ff191f26"
        gastudio:bottle_color="@android:color/white"
        gastudio:water_color="@android:color/holo_blue_light" />
```

**Finally, if you feel pretty good, please click the Star!**

**Welcome to join the GAStudio exchange qq group: 277582728.**

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

