package unics.okeffect;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;

import androidx.annotation.AttrRes;
import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
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
 * 使用说明：
 * 1、（尤其重要）要想效果显示出来，效果所显示的区域关联的父级视图必须设置clipChildren = false
 * 2、（最优建议）建议采用.9图做阴影效果，性能更加（不用关闭硬件加速）
 * 3、虽然边框可以跟.9图搭配使用，但是建议如果采用了.9图，边框效果最好是做在图上，没必要在结合绘制的边框
 * 4、采用自定义Drawable绘制的阴影效果，必须关闭硬件加速（这里有个疑惑点：如果多个父级ViewGroup调用setLayerType(View.LAYER_TYPE_SOFTWARE, null)之后，绘制存在异常，待进一步确认；为避免该问题出现，最好就让某个比较合适的父布局设置即可）
 * 5、（最优建议）采用自定义Drawable绘制边框效果时，如果边框很粗，可能会造成边框的绘制效果，建议开启边框绘制优化（默认已开启自动优化：会根据边框的宽度启动优化策略）
 */
public class Effects {

    public static final int[][] FOCUSED_PRESSED_STATES = new int[][]{new int[]{android.R.attr.state_focused}, new int[]{}};

    /**
     * 创建的EffectDrawable默认使用负inset
     */
    static final boolean DEFAULT_EFFECT_DRAWABLE_USE_NEGATIVE_INSET = true;

    /**
     * 边框圆角绘制优化自定处理所判断的边框大小限定值，超过该值才会启用边框（阴影）圆角绘制优化
     */
    @Px
    static final int STOKE_THICK_LIMIT = 6;

    /**
     * 是否启用自动优化边框绘制，默认开启
     *
     * @see #setAutoOptStrokeCorner
     */
    static boolean mAutoOptStrokeCorner = true;

    /**
     * 是否优化边框外圆角半径，默认开启
     * 开启的情况下有如下两种处理规则：
     * 1、如果采用的自定义效果{@link DrawBuilder}，则边框的外圆角半径会增大半个边框大小,如果还存在阴影的话，阴影的绘制区域会往内扩大半个边框区域大小
     * 2、如果采用的是.9图效果{@link NinePathBuilder},则边框的外圆半径不会增大半个边框
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
    public static NinePathBuilder withNinePath(Context context, @DrawableRes int rid) {
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
    public static NinePathBuilder withNinePath(NinePatchDrawable drawable) {
        return new NinePathBuilder(drawable);
    }

    /**
     * 基于自定义绘制的构造器
     */
    @NonNull
    public static DrawBuilder withDraw() {
        return new DrawBuilder();
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性{@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link NinePathBuilder}还是{@link DrawBuilder}
     */
    @NonNull
    public static Builder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs) {
        return withAttrs(context, attrs, R.attr.effectDrawableStyle);
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性 {@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link NinePathBuilder}还是{@link DrawBuilder}
     */
    @NonNull
    public static Builder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        return withAttrs(context, attrs, defStyleAttr, 0);
    }

    /**
     * 通过属性解析构造
     *
     * @return 如果设置属性{@link R.styleable.EffectDrawable_ed_useDraw}设置为true，则会使用{@link DrawBuilder}，否则会根据是否设置了.9图(设置了{@link R.styleable.EffectDrawable_ed_ninePathSrc}属性,并且指定的图片必须是.9图)而选择使用{@link NinePathBuilder}还是{@link DrawBuilder}
     */
    @NonNull
    public static Builder<?, ?> withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectDrawable, defStyleAttr, defStyleRes);
        Builder<?, ?> builder = withAttrs(ta);
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
    static Builder<?, ?> withAttrs(TypedArray ta) {
        Builder<?, ?> builder;
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
    static Drawable createFocusStateListDrawable(@NonNull Drawable drawable) {
        StateListDrawable sld = new StateListDrawable();
        sld.addState(FOCUSED_PRESSED_STATES[0], drawable);
        return sld;
    }

    public static abstract class Builder<T extends Builder<?, ?>, P extends EffectParams> {

        float mStrokeSize;
        int mStrokeColor;
        boolean mOptStrokeCorner;
        float mContentGap;
        float mCornerRadius;
        float[] mCornerRadii;

        /**
         * 是否使用负InsetDrawable
         */
        boolean mUseNegativeInsetDrawable;

        @CallSuper
        public T loadAttrs(@NonNull TypedArray ta) {
            mStrokeSize = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_strokeSize, 0);
            mStrokeColor = ta.getColor(R.styleable.EffectDrawable_ed_strokeColor, Color.TRANSPARENT);
            mContentGap = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_contentGap, 0);
            mOptStrokeCorner = ta.getBoolean(R.styleable.EffectDrawable_ed_optStrokeCorner, mAutoOptStrokeCorner && mStrokeSize > STOKE_THICK_LIMIT);
            float cornerSize = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSize, 0f);
            float cornerTopLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopLeft, cornerSize);
            float cornerTopRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopRight, cornerSize);
            float cornerBottomRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomRight, cornerSize);
            float cornerBottomLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomLeft, cornerSize);
            if (cornerTopLeft > 0f || cornerTopRight > 0f || cornerBottomLeft > 0f || cornerBottomRight > 0f) {
                setCornerRadii(new float[]{cornerTopLeft, cornerTopLeft, cornerTopRight, cornerTopRight, cornerBottomRight, cornerBottomRight, cornerBottomLeft, cornerBottomLeft});
            } else {
                setCornerRadius(ta.getDimension(R.styleable.EffectDrawable_ed_cornerSize, 0f));
            }
            mUseNegativeInsetDrawable = ta.getBoolean(R.styleable.EffectDrawable_ed_useNegativeInset, DEFAULT_EFFECT_DRAWABLE_USE_NEGATIVE_INSET);
            return (T) this;
        }

        /**
         * 设置边框
         *
         * @param width 边框宽度
         * @param color 边框颜色
         * @see #setStrokeWidth(float)
         * @see #setStrokeColor(int)
         */
        public T setStroke(@Px float width, @ColorInt int color) {
            mStrokeSize = width;
            mStrokeColor = color;
            return (T) this;
        }

        /**
         * 设置边框宽度
         *
         * @see #setStroke(float, int)
         */
        public T setStrokeWidth(@Px float width) {
            mStrokeSize = width;
            return (T) this;
        }

        /**
         * 设置边框颜色
         *
         * @see #setStroke(float, int)
         */
        public T setStrokeColor(@ColorInt int color) {
            mStrokeColor = color;
            return (T) this;
        }

        /**
         * 是否设置优化边框圆角处的绘制;
         * 解释：
         * 如果边框存在圆角，绘制的时候，是按照边框的中线进行绘制，即保障的是边框中线的圆角值，这样造成的效果是绘制出来的边框的内边和外边与实际的圆角值不相等，内边框的圆角值小，外边框更大，
         * 造成实际的效果就是边框内边圆角更小，与内部内容圆角不一致，还会形成空隙；如果还存在同样角度的阴影，由于边框外边框圆角更大，也会与绘制的阴影间形成空隙；
         * 因此开启此配置，会保证内外边框圆角均为设定的值，这样边框会与内外内容的圆角一致并贴合；
         * 但是这样也会造成一个问题，就是圆角处的边框线看会比直线部分更厚（更粗）；解决办法就优化圆角处的外边框线圆角半径，目前已默认开启该配置（没理由不开启，二次优化会让整个绘制效果开起来更好），具体查看{@link #DEFAULT_ENABLE_OPT_OUT_CORNER}
         *
         * @param enable 是否开启
         * @see EffectParams#optStrokeCorner()
         * @see #setAutoOptStrokeCorner
         */
        public T setOptStrokeCorner(boolean enable) {
            mOptStrokeCorner = enable;
            return (T) this;
        }

        /**
         * 设置内容与边框/阴影之间的间距
         */
        public T setContentCap(@Px float gap) {
            mContentGap = gap;
            return (T) this;
        }

        /**
         * 设置四个角圆角半径，调用该方法后会将mCornerRadii重置
         *
         * @see android.graphics.drawable.GradientDrawable#setCornerRadius(float)
         */
        public T setCornerRadius(@Px float radius) {
            mCornerRadius = radius;
            mCornerRadii = null;
            return (T) this;
        }

        /**
         * 设置阴影/边框，调用该设置后会将cornerRadius重置
         * The corners are ordered top-left, top-right, bottom-right, bottom-left.
         *
         * @param radii 四个角的圆角半径：每个角2个坐标
         * @see android.graphics.drawable.GradientDrawable#setCornerRadii(float[])
         */
        public T setCornerRadii(@Px float[] radii) {
            mCornerRadii = radii;
            mCornerRadius = 0f;
            return (T) this;
        }

        public T setUseNegativeInsetDrawable(boolean useNegativeInset) {
            mUseNegativeInsetDrawable = useNegativeInset;
            return (T) this;
        }

        /**
         * 构建效果参数
         */
        public abstract P buildParams();

        /**
         * 创建Drawable
         */
        @NonNull
        public Drawable buildDrawable() {
            return EffectDrawableFactory.createEffectDrawable(buildParams());
        }

        /**
         * 构建焦点selector
         */
        @NonNull
        public Drawable buildFocusStateListDrawable() {
            return createFocusStateListDrawable(buildDrawable());
        }

    }

    /**
     * 基于.9图的构造器
     */
    public static class NinePathBuilder extends Effects.Builder<NinePathBuilder, EffectParams.NinePathEffectParams> {

        NinePatchDrawable mDrawable;

        NinePathBuilder(NinePatchDrawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public EffectParams.NinePathEffectParams buildParams() {
            return new EffectParams.NinePathDrawableEffectParamsImpl(this);
        }

        @Override
        @NonNull
        public Drawable buildDrawable() {
            return EffectDrawableFactory.NinePath.getInstance().create(buildParams());
        }
    }

    /**
     * 基于自定义绘制的构造器
     */
    public static class DrawBuilder extends Effects.Builder<DrawBuilder, EffectParams.DrawEffectParams> {

        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        int mShadowColor;

        @Override
        public EffectParams.DrawEffectParams buildParams() {
            return new EffectParams.DrawEffectParamsImpl(this);
        }

        @Override
        @NonNull
        public Drawable buildDrawable() {
            return EffectDrawableFactory.Draw.getInstance().create(buildParams());
        }

        @Override
        public DrawBuilder loadAttrs(@NonNull TypedArray ta) {
            super.loadAttrs(ta);
            float shadowSize = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSize, 0f);
            mShadowLeft = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeLeft, shadowSize);
            mShadowTop = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeTop, shadowSize);
            mShadowRight = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeRight, shadowSize);
            mShadowBottom = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeBottom, shadowSize);
            mShadowColor = ta.getColor(R.styleable.EffectDrawable_ed_shadowColor, Color.TRANSPARENT);
            return this;
        }

        public DrawBuilder setShadow(@Px float size, @ColorInt int color) {
            setShadowSize(size);
            setShadowColor(color);
            return this;
        }

        public DrawBuilder setShadow(@Px float left, @Px float top, @Px float right, @Px float bottom, @ColorInt int color) {
            setShadowSize(left, top, right, bottom);
            setShadowColor(color);
            return this;
        }

        public DrawBuilder setShadowSize(@Px float size) {
            return setShadowSize(size, size, size, size);
        }

        public DrawBuilder setShadowSize(@Px float left, @Px float top, @Px float right, @Px float bottom) {
            mShadowLeft = left;
            mShadowTop = top;
            mShadowRight = right;
            mShadowBottom = bottom;
            return this;
        }

        public DrawBuilder setShadowColor(@ColorInt int color) {
            mShadowColor = color;
            return this;
        }

    }

    static void log(String message) {
        Log.d("Effect", message);
    }
}
