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

    static final boolean DEFAULT_ENABLE_OPT_OUT_CORNER = true;

    @StyleRes
    public static int defaultEffectDrawableStyleRes = 0;

    static boolean mAutoOptStrokeCorner = true;

    static final int STOKE_THICK_LIMIT = 6;

    public static void setAutoOptStrokeCorner(boolean enable) {
        mAutoOptStrokeCorner = enable;
    }

    /**
     * 基于.9图创建效果Drawable
     *
     * @param rid .9图资源id
     */
    public static NinePathBuilder withNinePath(Context context, @DrawableRes int rid) {
        Drawable drawable = context.getResources().getDrawable(rid);
        if (drawable instanceof NinePatchDrawable) {
            return withNinePath((NinePatchDrawable) drawable);
        }
        throw new RuntimeException(" the res #id(=" + rid + ") or drawable isn't a nine path drawable.");
    }

    /**
     * 基于.9图创建效果Drawable
     */
    public static NinePathBuilder withNinePath(NinePatchDrawable drawable) {
        return new NinePathBuilder(drawable);
    }

    /**
     * 自定义绘制效果drawable
     */
    public static DrawBuilder withDraw() {
        return new DrawBuilder();
    }

    public static Builder withAttrs(@NonNull Context context, @Nullable AttributeSet attrs) {
        return withAttrs(context, attrs, R.attr.effectDrawableStyle);
    }

    public static Builder withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        return withAttrs(context, attrs, defStyleAttr, 0);
    }

    /**
     * 通过属性构造
     *
     * @param context
     * @param attrs
     * @param defStyleAttr
     */
    public static Builder withAttrs(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr, @StyleRes int defStyleRes) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectDrawable, defStyleAttr, defStyleRes);
        Builder builder = withAttrs(ta);
        ta.recycle();
        return builder;
    }

    static Builder withAttrs(TypedArray ta) {
        Builder builder;
        boolean forceDraw = ta.getBoolean(R.styleable.EffectDrawable_ed_useDraw, false);
        if (forceDraw) {
            builder = withDraw();
        } else {
            Drawable npd = ta.getDrawable(R.styleable.EffectDrawable_ed_ninePathSrc);
            if (!(npd instanceof NinePatchDrawable)) {
                builder = withDraw();
            } else {
                builder = withNinePath((NinePatchDrawable) npd);
            }
        }
        builder.loadAttrs(ta);
        return builder;
    }

    public static void applyInjectFactory2(@NonNull Activity activity) {
        applyInjectFactory2(activity, null);
    }

    /**
     * 绑定自动属性注入工厂；必须在调用super.{@link Activity#onCreate(Bundle)}方法之前调用该方法
     *
     * @param activity
     * @param custom
     */
    public static void applyInjectFactory2(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 custom) {
        EffectInjectFactory2 factory2 = new EffectInjectFactory2(activity, custom);
        activity.getLayoutInflater().setFactory2(factory2);
    }

    public static abstract class Builder<T extends Builder, P extends EffectParams> {

        float mStrokeSize;
        int mStrokeColor;
        boolean mOptStrokeCorner;
        float mContentGap;
        float mCornerRadius;
        float[] mCornerRadii;


        @CallSuper
        public T loadAttrs(TypedArray ta) {
            mStrokeSize = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_strokeSize, 0);
            mStrokeColor = ta.getColor(R.styleable.EffectDrawable_ed_strokeColor, Color.TRANSPARENT);
            mContentGap = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_contentGap, 0);
            float cornerTopLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopLeft, 0f);
            float cornerTopRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopRight, 0f);
            float cornerBottomRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomRight, 0f);
            float cornerBottomLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomLeft, 0f);
            if (cornerTopLeft > 0f || cornerTopRight > 0f || cornerBottomLeft > 0f || cornerBottomRight > 0f) {
                setCornerRadii(new float[]{cornerTopLeft, cornerTopLeft, cornerTopRight, cornerTopRight, cornerBottomRight, cornerBottomRight, cornerBottomLeft, cornerBottomLeft});
            } else {
                setCornerRadius(ta.getDimension(R.styleable.EffectDrawable_ed_cornerSize, 0f));
            }
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
         * 是否设置优化边框圆角处的绘制
         *
         * @param enable
         * @return
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

        /**
         * 构建效果参数
         */
        public abstract P buildParams();

        /**
         * 构建效果Drawable
         */
        public Drawable buildDrawable() {
            EffectParams params = buildParams();
            if (params instanceof EffectParams.NinePathEffectParams) {
                return EffectDrawableFactory.NinePath.getInstance().create((EffectParams.NinePathEffectParams) params);
            } else if (params instanceof EffectParams.DrawEffectParams) {
                return EffectDrawableFactory.Draw.getInstance().create((EffectParams.DrawEffectParams) params);
            } else {
                throw new IllegalArgumentException("un support effect params type.");
            }
        }

        /**
         * 构建焦点selector
         */
        public Drawable buildFocusSelectorDrawable() {
            StateListDrawable sld = new StateListDrawable();
            sld.addState(FOCUSED_PRESSED_STATES[0], buildDrawable());
            return sld;
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
        public DrawBuilder loadAttrs(TypedArray ta) {
            float shadowSize = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSize, 0f);
            mShadowLeft = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeLeft, shadowSize);
            mShadowTop = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeTop, shadowSize);
            mShadowRight = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeRight, shadowSize);
            mShadowBottom = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeBottom, shadowSize);
            mShadowColor = ta.getColor(R.styleable.EffectDrawable_ed_shadowColor, Color.TRANSPARENT);
            return super.loadAttrs(ta);
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

        @Override
        public EffectParams.DrawEffectParams buildParams() {
            return new EffectParams.DrawEffectParamsImpl(this);
        }

    }

    static void log(String message) {
        Log.d("Effect", message);
    }
}
