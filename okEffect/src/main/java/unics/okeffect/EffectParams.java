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
     * 内容与边框之间的间距
     */
    @Px
    float getContentGap();

    /**
     * 圆角半径，在.9中则需要设置为.9图的圆角值，以便后续做其他绘制（比如扫光区域）
     *
     * @see android.graphics.drawable.GradientDrawable#setCornerRadius(float)
     */
    float getCornerRadius();

    /**
     * 四角角度的圆角值，一个角两个坐标；在.9中则需要设置为.9图的圆角值，以便后续做其他绘制（比如扫光区域）
     * The corners are ordered top-left, top-right, bottom-right, bottom-left.
     *
     * @see android.graphics.drawable.GradientDrawable#setCornerRadii(float[])
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
     * 获取效果四周的大小
     *
     * @param rect 用于保存四周大小（四周大小都是正值）
     */
    void getEffectRect(Rect rect);

    /**
     * 是否包含软件绘制图层：比如绘制的阴影、边框优化的处理等均需要关闭硬件加速；但.9图一般不需要关闭硬件加速
     * <a href="https://developer.android.com/guide/topics/graphics/hardware-accel?hl=zh-cn#layers">硬件加速的官方资料</a>
     */
    boolean containSoftwareLayer();

    @NonNull
    default Drawable create() {
        return EffectDrawableFactory.createEffectDrawable(this);
    }

    /**
     * 自定义绘制参数
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
         * 是否设置了阴影（绘制的阴影需要关闭硬件加速）
         */
        default boolean hasShadow() {
            return getShadowLeft() > 0 || getShadowTop() > 0 || getShadowRight() > 0 || getShadowBottom() > 0;
        }

        /**
         * 是否优化边框圆角处的绘制：
         */
        boolean optStrokeCorner();

        /**
         * 是否优化边框圆角处外边框圆角（前提{@link #optStrokeCorner}必须为true）
         */
        default boolean optStrokeOutCorner() {
            return Effects.DEFAULT_ENABLE_OPT_OUT_CORNER;
        }

        /**
         * 获取外边框圆角半径额外的增量；{@link #optStrokeOutCorner()}开启时才有效
         */
        default float getIncrementStrokeOutCornerRadius() {
            if (optStrokeOutCorner()) {
                return getStrokeSize() / 2f;
            } else {
                return 0f;
            }
        }

        /**
         * 获取效果四周的大小
         *
         * @param rect 用于保存四周大小（四周大小都是正值）
         */
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
            float[] cornerRadii = getCornerRadii();
            //需要优化边框圆角
            boolean isOptStrokeCorner = this.optStrokeCorner() && this.getStrokeSize() > 0 && (getCornerRadius() > 0f || (cornerRadii != null && cornerRadii.length > 0));
            return (Build.VERSION.SDK_INT < 28 && isOptStrokeCorner) //边框的圆角优化采用了Xfermode，在28以前需要关闭硬件加速
                    || this.hasShadow();//使用了阴影
        }
    }

    /**
     * .9图参数
     */
    interface NinePatchEffectParams extends EffectParams {

        NinePatchDrawable getDrawable();

        /**
         * 获取效果四周的大小
         *
         * @param rect 用于保存四周大小（四周大小都是正值）
         */
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

        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        float mContentGap;
        float mCornerRadius;
        float[] mCornerRadii;
        int mEffectBoundsType;

        public CommonParams(EffectBuilder<?, ?> builder) {
            mContentGap = builder.mContentGap;
            mEffectBoundsType = builder.mEffectBoundsType;
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

        @Override
        public float getCornerRadius() {
            return mCornerRadius;
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
            //如果设置了优化边框或者设置了自动优化并且边框大小超过了指定的厚度则进行边框优化
            mOptStrokeCorner = builder.mOptStrokeCorner;
            mCornerRadius = builder.mCornerRadius;
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
