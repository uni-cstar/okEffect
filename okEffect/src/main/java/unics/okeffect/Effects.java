package unics.okeffect;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.AttrRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;
import androidx.annotation.StyleRes;
import androidx.core.content.res.ResourcesCompat;

/**
 * Create by luochao
 * on 2023/11/13
 * <p>
 * 最有使用建议：
 * 1、强烈建议：不管动态生成Drawable{@link EffectParams#create()}还是使用自定义的布局{@link EffectLayoutTemplate},都建议使用.9图来做阴影效果，而不是使用自定义属性；.9图的效果性能更高（不用关闭硬件加速）
 * 2、强烈建议：使用{@link EffectLayoutTemplate}优于使用动态Drawable（内部做了一些情况的优化）
 * 3、强烈建议：如果使用了.9做阴影，需要边框效果的话，也强烈建议将边框做在.9图里面
 * 4、注意：.9图做阴影，搭配自定义绘制的边框，如果边框存在圆角并且开启了圆角绘制优化，则在api28版本以前的设备关闭硬件加速，因此在api28以前不建议使用这种方式；.9图搭配无圆角的边框或者设置了不优化边框绘制的不影响，但仍然建议边框做在.9图里
 * 5、最难使用的一种情况：如果您使用{@link EffectBuilder#applyFocusStateListDrawable(View)}等相关方法创建的Drawable，并且该drawable需要关闭硬件加速，那么通常你直接将该drawable作为background设置给view通常不能正常显示；
 * 需要将这个drawable显示区域的viewgroup设置layer_type_software才行（通常是view的parent/或grand parent viewgroup）
 * <p>
 * 使用说明：
 * 1、（尤其重要）要想效果显示出来，效果所显示的区域关联的父级视图必须设置clipChildren = false
 * 2、（最优建议）建议采用.9图做阴影效果，性能更加（不用关闭硬件加速）
 * 3、虽然边框可以跟.9图搭配使用，但是建议如果采用了.9图，边框效果最好是做在图上，没必要在结合绘制的边框
 * 4、采用自定义Drawable绘制的阴影效果，必须关闭硬件加速（这里有个疑惑点：如果多个父级ViewGroup调用setLayerType(View.LAYER_TYPE_SOFTWARE, null)之后，绘制存在异常，待进一步确认；为避免该问题出现，最好就让某个比较合适的父布局设置即可）
 * 5、（最优建议）采用自定义Drawable绘制边框效果时，如果边框很粗，可能会造成边框的绘制效果，建议开启边框绘制优化（默认已开启自动优化：会根据边框的宽度启动优化策略）
 */
public class Effects {

    /**
     * 效果区域在bounds外：即Effect占用的区域不占用控件区域，也不作为padding返回
     */
    public static final int BOUNDS_TYPE_OUTER = EffectDrawable.BOUNDS_TYPE_OUTER;

    /**
     * 效果区域在bounds内：即Effect占用控件区域，作为padding部分
     */
    public static final int BOUNDS_TYPE_PADDING = EffectDrawable.BOUNDS_TYPE_PADDING;

    /**
     * 默认Effect区域不占控件bounds
     */
    static final int sDefaultBoundsType = BOUNDS_TYPE_OUTER;

    static volatile Rect sTmpEffectRect = new Rect();

    /**
     * 边框圆角绘制优化自定处理所判断的边框大小限定值，超过该值才会启用边框（阴影）圆角绘制优化
     */
    @Px
    static final int STOKE_THICK_LIMIT = 5;

    /**
     * 是否启用自动优化边框绘制，默认开启
     *
     * @see #setAutoOptStrokeCorner
     */
    static boolean mAutoOptStrokeCorner = true;

    /**
     * 是否优化边框外圆角半径，默认开启
     * 开启的情况下有如下两种处理规则：
     * 1、如果采用的自定义效果{@link EffectBuilder.DrawBuilder}，则边框的外圆角半径会增大半个边框大小,如果还存在阴影的话，阴影的绘制区域会往内扩大半个边框区域大小
     * 2、如果采用的是.9图效果{@link EffectBuilder.NinePathBuilder},则边框的外圆半径不会增大半个边框
     */
    static final boolean DEFAULT_ENABLE_OPT_OUT_CORNER = true;

    /**
     * 用于配置全局的焦点默认效果Style
     */
    @StyleRes
    public static int defaultEffectDrawableStyleRes = 0;

    /**
     * 是否启用自动边框圆角绘制优化；即当边框的大小超过{@link #STOKE_THICK_LIMIT},则进行绘制优化。
     *
     * @param enable 是否启用，默认开启
     */
    public static void setAutoOptStrokeCorner(boolean enable) {
        mAutoOptStrokeCorner = enable;
    }

    /**
     * 基于.9图创建效果Drawable
     *
     * @param rid .9图资源id
     */
    @NonNull
    public static EffectBuilder.NinePathBuilder withNinePath(Context context, @DrawableRes int rid) {
        Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), rid, context.getTheme());
        if (drawable instanceof NinePatchDrawable) {
            return withNinePath((NinePatchDrawable) drawable);
        }
        throw new RuntimeException(" the drawable of the res #id(=" + rid + ") isn't a nine path drawable.");
    }

    /**
     * 基于.9图的构造器
     */
    @NonNull
    public static EffectBuilder.NinePathBuilder withNinePath(NinePatchDrawable drawable) {
        return new EffectBuilder.NinePathBuilder(drawable);
    }

    /**
     * 基于自定义绘制的构造器
     */
    @NonNull
    public static EffectBuilder.DrawBuilder withDraw() {
        return new EffectBuilder.DrawBuilder();
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性{@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link EffectBuilder.DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link EffectBuilder.NinePathBuilder}还是{@link EffectBuilder.DrawBuilder}
     */
    @NonNull
    public static EffectBuilder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs) {
        return withAttrs(context, attrs, R.attr.effectDrawableStyle);
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性 {@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link EffectBuilder.DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link EffectBuilder.NinePathBuilder}还是{@link EffectBuilder.DrawBuilder}
     */
    @NonNull
    public static EffectBuilder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        return withAttrs(context, attrs, defStyleAttr, 0);
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性{@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link EffectBuilder.DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link EffectBuilder.NinePathBuilder}还是{@link EffectBuilder.DrawBuilder}
     */
    @NonNull
    public static EffectBuilder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectDrawable, defStyleAttr, defStyleRes);
        EffectBuilder<?, ?> builder = withAttrs(ta);
        ta.recycle();
        return builder;
    }

    /**
     * 绑定属性解析工厂；必须在调用super.{@link Activity#onCreate(Bundle)}方法之前调用该方法
     * 使用该工厂支持xml中在任意view上使用自定义的effect相关属性，属性解析之后会生成背景图作为该view的背景
     *
     * @see #applyInjectFactory2(Activity, LayoutInflater.Factory2)
     */
    public static void applyInjectFactory2(@NonNull Activity activity) {
        applyInjectFactory2(activity, null);
    }

    /**
     * 绑定属性解析工厂；必须在调用super.{@link Activity#onCreate(Bundle)}方法之前调用该方法
     * 使用该工厂支持xml中在任意view上使用自定义的effect相关属性，属性解析之后会生成背景图作为该view的背景
     *
     * @param custom 用户的自定义Factory2
     * @see #applyInjectFactory2(Activity)
     */
    public static void applyInjectFactory2(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 custom) {
        EffectInjectFactory2 factory2 = new EffectInjectFactory2(activity, custom);
        activity.getLayoutInflater().setFactory2(factory2);
    }

    @NonNull
    static EffectBuilder<?, ?> withAttrs(TypedArray ta) {
        EffectBuilder<?, ?> builder;
        boolean forceDraw = ta.getBoolean(R.styleable.EffectDrawable_ed_useDraw, false);
        if (forceDraw) {
            builder = withDraw();
        } else {
            Drawable npd = ta.getDrawable(R.styleable.EffectDrawable_ed_ninePathSrc);
            if (!(npd instanceof NinePatchDrawable))
                throw new RuntimeException("ed_ninePathSrc must be a nine patch drawable.");
            builder = withNinePath((NinePatchDrawable) npd);
        }
        builder.loadAttrs(ta);
        return builder;
    }

    /**
     * 构建焦点selector
     */
    @NonNull
    static Drawable createStateListDrawable(@NonNull Drawable drawable) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(new int[]{android.R.attr.state_focused}, drawable);
        sld.addState(new int[]{android.R.attr.state_pressed}, drawable);
        return sld;
    }

    static void log(String message) {
        Log.d("Effect", message);
    }

//    static boolean approximatelyEquals(float self, float other) {
//        return Math.abs(self - other) < 1e-10;
//    }
}
