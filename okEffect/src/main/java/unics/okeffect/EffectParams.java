package unics.okeffect;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.os.Build;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Px;

/**
 * Create by luochao
 * on 2023/11/15
 * 效果参数
 */
public interface EffectParams {

    /**
     * 绘制效果与内容之间的间距
     */
    @Px
    float getContentGap();

    /**
     * 左侧阴影大小
     */
    @Px
    float getShadowLeft();

    /**
     * 顶部阴影大小
     */
    @Px
    float getShadowTop();

    /**
     * 右侧阴影大小
     */
    @Px
    float getShadowRight();

    /**
     * 底部阴影大小
     */
    @Px
    float getShadowBottom();

    /**
     * 四角角度的圆角值，一个角两个坐标；在.9中则需要设置为.9图的圆角值（只是预留），以便后续做其他绘制（比如扫光区域）
     * The corners are ordered top-left, top-right, bottom-right, bottom-left.
     */
    @Nullable
    float[] getCornerRadii();

    /**
     * 效果视图的布局类型
     *
     * @see Effects#BOUNDS_TYPE_OUTER
     * @see Effects#BOUNDS_TYPE_PADDING
     */
    int getBoundsType();

    /**
     * 获取四周绘制区域大小
     *
     * @param rect 用于保存大小（大小都是正值）
     */
    void getEffectRect(Rect rect);

    /**
     * 是否包含软件绘制支持的图层：比如Canvas绘制阴影、边框优化的处理等均需要关闭硬件加速；但.9图一般不需要关闭硬件加速
     * <a href="https://developer.android.com/guide/topics/graphics/hardware-accel?hl=zh-cn#layers">硬件加速的官方资料</a>
     */
    boolean containSoftwareLayer();

    /**
     * 是否有圆角
     */
    default boolean hasCorner() {
        float[] cornerRadii = getCornerRadii();
        if (cornerRadii == null || cornerRadii.length == 0)
            return false;
        for (float cornerRadius : cornerRadii) {
            if (cornerRadius > 0) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    default Drawable create() {
        return EffectDrawableFactory.createEffectDrawable(this);
    }

    /**
     * Canvas绘制所需参数
     */
    interface DrawEffectParams extends EffectParams {

        /**
         * 边框大小
         */
        @Px
        float getStrokeSize();

        /**
         * 边框颜色
         */
        @ColorInt
        int getStrokeColor();

        /**
         * 阴影颜色
         */
        int getShadowColor();

        /**
         * 是否有阴影区域
         */
        default boolean hasShadow() {
            return getShadowLeft() > 0 || getShadowTop() > 0 || getShadowRight() > 0 || getShadowBottom() > 0;
        }

        /**
         * 是否优化边框圆角处的绘制：
         */
        boolean optStrokeCorner();

        /**
         * 是否优化外边框圆角；（前提{@link #optStrokeCorner}必须为true）
         */
        default boolean optStrokeOutCorner() {
            return Effects.DEFAULT_ENABLE_OPT_OUT_CORNER;
        }

        /**
         * 获取外边框圆角优化的圆角增量值；{@link #optStrokeOutCorner()}开启时才有效
         */
        default float getIncrementStrokeOutCornerRadius() {
            if (optStrokeOutCorner()) {
                return getStrokeSize() / 2f;
            } else {
                return 0f;
            }
        }

        @Override
        default void getEffectRect(Rect rect) {
            float strokeSize = getStrokeSize();
            float contentGap = getContentGap();
            rect.set((int) (getShadowLeft() + strokeSize + contentGap),
                    (int) (getShadowTop() + strokeSize + contentGap),
                    (int) (getShadowRight() + strokeSize + contentGap),
                    (int) (getShadowBottom() + strokeSize + contentGap));
        }

        @Override
        default boolean containSoftwareLayer() {
            return (Build.VERSION.SDK_INT < 28 && this.optStrokeCorner() && this.getStrokeSize() > 0 && hasCorner()) //边框的圆角优化采用了Xfermode，在28以前需要关闭硬件加速
                    || this.hasShadow();//使用了阴影:阴影采用了MaskFilter，需要关闭硬件加速
        }
    }

    /**
     * .9图参数
     */
    interface NinePatchEffectParams extends EffectParams {

        /**
         * 获取.9图
         */
        NinePatchDrawable getDrawable();

        @Override
        default void getEffectRect(Rect rect) {
            float contentGap = getContentGap();
            rect.set((int) (getShadowLeft() + contentGap),
                    (int) (getShadowTop() + contentGap),
                    (int) (getShadowRight() + contentGap),
                    (int) (getShadowBottom() + contentGap));
        }

        /**
         * .9图默认不需要使用软件绘制
         */
        @Override
        default boolean containSoftwareLayer() {
            return false;
        }

    }

    /**
     * 通用参数
     */
    abstract class CommonParams implements EffectParams {
        int mEffectBoundsType;
        float mContentGap;
        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        float[] mCornerRadii;

        public CommonParams(EffectBuilder<?, ?> builder) {
            mEffectBoundsType = builder.mEffectBoundsType;
            mContentGap = builder.mContentGap;
        }

        @Override
        public float getShadowLeft() {
            return mShadowLeft;
        }

        @Override
        public float getShadowTop() {
            return mShadowTop;
        }

        @Override
        public float getShadowRight() {
            return mShadowRight;
        }

        @Override
        public float getShadowBottom() {
            return mShadowBottom;
        }

        @Override
        public float getContentGap() {
            return mContentGap;
        }

        @Nullable
        @Override
        public float[] getCornerRadii() {
            return mCornerRadii;
        }

        @Override
        public int getBoundsType() {
            return mEffectBoundsType;
        }

    }

    /**
     * .9图效果参数
     */
    final class NinePatchEffectParamsImpl extends CommonParams implements NinePatchEffectParams {

        private static final Rect sNinePatchPaddingRect = new Rect();

        NinePatchDrawable mDrawable;

        NinePatchEffectParamsImpl(EffectBuilder.NinePathBuilder builder) {
            super(builder);
            mDrawable = builder.mDrawable;
            mDrawable.getPadding(sNinePatchPaddingRect);
            mShadowLeft = sNinePatchPaddingRect.left;
            mShadowTop = sNinePatchPaddingRect.top;
            mShadowRight = sNinePatchPaddingRect.right;
            mShadowBottom = sNinePatchPaddingRect.bottom;
        }

        @Override
        public NinePatchDrawable getDrawable() {
            return mDrawable;
        }

    }

    /**
     * 自定义绘制参数
     */
    final class DrawEffectParamsImpl extends CommonParams implements DrawEffectParams {

        int mShadowColor;
        float mStrokeSize;
        boolean mOptStrokeCorner;
        int mStrokeColor;

        DrawEffectParamsImpl(EffectBuilder.DrawBuilder builder) {
            super(builder);
            mShadowLeft = builder.mShadowLeft;
            mShadowTop = builder.mShadowTop;
            mShadowRight = builder.mShadowRight;
            mShadowBottom = builder.mShadowBottom;
            mShadowColor = builder.mShadowColor;
            mStrokeSize = builder.mStrokeSize;
            mStrokeColor = builder.mStrokeColor;
            mOptStrokeCorner = builder.mOptStrokeCorner;
            mCornerRadii = builder.mCornerRadii;
        }

        @Override
        public int getShadowColor() {
            return mShadowColor;
        }

        @Override
        public float getStrokeSize() {
            return mStrokeSize;
        }

        @Override
        public int getStrokeColor() {
            return mStrokeColor;
        }

        @Override
        public boolean optStrokeCorner() {
            return mOptStrokeCorner;
        }

    }

}
