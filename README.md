[uni-cstar/okEffect: 用于TV(手机)的焦点（点击）效果（阴影、边框，自定义绘制、.9图、自定义Layout等） (github.com)](https://github.com/uni-cstar/okEffect)

##### 工程说明
- okEffect包含核心的业务逻辑
- okEffectAnnotation提供了自动生成布局代码的注解
- okEffectCompiler为注解处理器，用于生成代码

###### 体验
运行截图：

![运行截图](https://github.com/uni-cstar/okEffect/blob/master/doc/okEffectSample.gif)

查看运行视频：
[点击观看](https://github.com/uni-cstar/okEffect/blob/master/doc/okEffectSample.mp4)

DemoApk：
[点击下载](https://github.com/uni-cstar/okEffect/blob/master/doc/app-release.apk)

使用手机浏览器扫描二维码下载安装

![二维码](https://github.com/uni-cstar/okEffect/blob/master/doc/qrcode.png)

### 1、下载

`implementation 'io.github.uni-cstar:okEffect:0.0.3'`

### 2、功能特点

- 支持代码创建Drawable
- 支持自适应内容大小(wrap_content)
- 支持.9图和Canvas绘制
- 支持效果（.9图或绘制的效果区域）不占控件位置或作为控件的padding部分
- 支持自定义Layout和自动扩展生成,也支持为任意布局生成扩展类支持相关功能
- 支持自定义布局绘制顺序（效果绘制在内容的底部或者顶部）
- 支持为现有xml布局注入效果
- 支持xml属性
- 支持全局主题
- 边框和阴影圆角绘制优化、支持Canvas绘制优化
- 阴影的绘制不需要关闭硬件加速
- ...

### 3、使用方式

#### 3.1. 代码创建Drawable方式：
##### 3.1.1 **强烈建议使用.9图作为绘制的内容，其效率更高，兼容性更好。**


```java
Effects.withNinePatch(this, R.drawable.bg_shadow).into(view);
```
##### 3.1.2 Canvas绘制方式
```java
//该方式创建了一个四周大小为20的红色阴影并将其作为背景设置给了view
Effects.withDraw().setShadow(20f, Color.RED).into(view);
```
如果您需要使用对应的Drawable对象，可以通过以下方式处理：
```java
// 第一步：创建一个Drawable对象:该方式创建了一个四周大小为20的红色阴影
Drawable drawable = Effects.withDraw().setShadow(20f, Color.RED).buildParams().create();
// 第二步：使用该Drawable对象，您可以直接使用该Drawable，也可以通过该drawable创建StateListDrawable进行使用
view.setBackground(drawable);
```

#### 3.2. 现有的xml布局注入
该方式用于为已经存在的xml布局中的视图节点注入效果，比如已经存在了一个布局文件，想要给其中的某个view增加阴影效果即可使用该方式；

第一步为需要注入属性的view开启`app:ed_inject="true"`属性，（`ed_ninePathSrc`属性配置了一张.9图作为效果）：
```xml
<FrameLayout
    android:id="@+id/layout"
    android:clickable="true"
    android:focusable="true"
    app:ed_inject="true"
    app:ed_ninePathSrc="@drawable/shadow_2"/>
```
第二步在Activity调用super.onCreate()之前，执行如下配置代码：
```java
Effect.applyInjectFactory2(activity);
//如果您的Activity需要使用自定义的LayoutInflater.Factory2，则可以使用如下代码：
Effect.applyInjectFactory2(activity,factory2);//factory2为您的LayoutInflater.Factory2对象

```

以上两步配置即可让id为layout的布局，在创建的时候通过主题或者配置的属性解析出需要的效果生成drawable并设置为背景；

**需要注意的点：默认显示的效果不占用控件的大小，因此绘制的效果在控件所在区域外，因此需要控件的parent设置不裁剪,即调用`parent.setClipChildren(false)`（或xml中配置parent设置属性：`android:clipChildren="false"`）；如果您不想使用这种方式，可以将效果修改为padding模式：`app:ed_boundsType="padding"`,该模式绘制的效果在控件区域内作为padding区域，由于占用了控件的空间，因此实际的布局需要考虑这一点；**


#### 3.3、默认自定义布局方式
目前库提供了两种自定义布局的生成`EffectFrameLayout`(继承自FrameLayout)和`EffectRelativeLayout`（继承自RelateLayout），可直接使用。
```xml
<unics.okeffect.EffectFrameLayout
    android:id="@+id/effectLayout2"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:focusable="true"
    app:ed_cornerSize="12px"
    app:ed_drawingOrderType="bottom"
    app:ed_optStrokeCorner="true">

    <com.google.android.material.imageview.ShapeableImageView
        android:id="@+id/effectImage2"
        android:layout_width="120dp"
        android:layout_height="120dp"
        android:scaleType="centerCrop"
        android:src="@drawable/hellowrold"
        app:shapeAppearanceOverlay="@style/RoundedMedium" />

</unics.okeffect.EffectFrameLayout>
```
#### 3.4、自定义布局扩展
如果内置生成的`EffectFrameLayout`、`EffectRelativeLayout`无法满足您的需求，你需要注入其他布局比如LinearLayout、ConstraintLayout或者你自定义的CustomLayout，则可以通过自动代码生成对应布局使用；以下以生成LinearLayout、ConstraintLayout和CustomLayout对应的自定义布局作为示例。

##### 3.4.1 添加依赖
```
//如果您使用了kapt，则修改为 kapt 'io.github.uni-cstar:okEffectCompiler:1.0.1'
annotationProcessor 'io.github.uni-cstar:okEffectCompiler:1.0.1'
compileOnly 'io.github.uni-cstar:okEffectAnnotation:1.0.1'
```

##### 3.4.2 申明需要自动生成的类
```
@EffectLayout(value = {LinearLayout.class, ConstraintLayout.class,CustomLayout.class})
class MyEffectLayoutInject {//类名无所谓
}
```
增加以上配置后，构建工程即可生成unics.okeffect.EffectLinearLayout、unics.okeffect.EffectConstraintLayout、unics.okeffect.EffectCustomLayout；

*默认生成规则为在包unics.okeffect下根据申明的类名，在前面增加Effect前缀作为最终的生成的文件名*

##### 3.4.3 修改生成的类名
如果你不想使用默认生成的类名，目前也仅支持修改生成的类名前缀和后缀，无法修改包名；
```
@EffectLayout(value = {CustomLayout.class},classPrefix = "MyEffect",classSuffix = "2")
class MyEffectLayoutInject {
}
```
最终生成的类名：unics.okeffect.MyEffectCustomLayout2

### 4. 额外使用说明和建议
- 共识很重要：**绘制的效果支持不占和占控件位置两种模式，不占控件位置由于是在控件之外的区域显示，需要其父级容器设置不裁剪（使用自定义布局或者Drawable的场景已经自动处理，只有inject注入效果的方式目前未处理）；占控件模式会默认将绘制的区域大小作为控件的padding；**
- 不管使用前面的何种方式，建议优先使用.9图作为绘制的效果，如果需要边框之类的，也建议做在.9图上
- 无论生成的Drawable还是自定义布局，均自适应内容大小
- 自定义布局支持绘制在底部（默认）或者顶部，但该控件仍然可能被其他视图遮挡，您需要视情况通过bringToFront解决（这个现象不是本库导致的现象，是Android本身就存在这个现象）
- 建议使用全局主题配置效果属性，当主题中配置`ed_ninePathSrc`属性指定了.9图，由于属性解析时.9图的属性配置高于canvas的属性配置，因此当您需要在xml中使用canvas绘制方式时，需要添加`app:ed_useDraw="true"`修改会canvas绘制方式（该属性值会忽略.9图的配置强制使用canvas绘制）
- 建议开启边框/阴影圆角绘制的优化（默认开启了自动优化，当边框大小超过了4px，并且存在圆角时将进行优化），您可以通过`Effects.setAutoOptStrokeCorner(false)`关闭该功能。
- 支持的xml属性以及其他功能请查看代码，注释已比较详细
