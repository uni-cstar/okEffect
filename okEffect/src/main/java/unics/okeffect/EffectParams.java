package unics.okeffect;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;

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
     * 是否优化边框圆角处的绘制：
     */
    boolean optStrokeCorner();

    /**
     * 是否优化边框圆角处外边框圆角（前提{@link #optStrokeCorner}必须为true）
     */
    boolean optStrokeOutCorner();

    /**
     * 获取外边框圆角半径额外的增量；{@link #optStrokeOutCorner()}开启时才有效
     */
    float getIncrementStrokeOutCornerRadius();

    /**
     * 内容与边框之间的间距
     */
    @Px
    float getContentGap();

    /**
     * 圆角半径，在.9中主要作用于边框效果
     *
     * @see android.graphics.drawable.GradientDrawable#setCornerRadius(float)
     */
    float getCornerRadius();

    /**
     * 四角角度的圆角值，一个角两个坐标；在.9中主要作用于边框效果
     * The corners are ordered top-left, top-right, bottom-right, bottom-left.
     *
     * @see android.graphics.drawable.GradientDrawable#setCornerRadii(float[])
     */
    @Nullable
    float[] getCornerRadii();

    /**
     * 创建Drawable的时候是否使用负Inset的Drawable
     */
    boolean useNegativeInsetDrawable();

    /**
     * 获取效果四周的大小
     *
     * @param rect
     */
    default void getEffectRect(Rect rect) {
        float strokeSize = getStrokeSize();
        float contentGap = getContentGap();
        rect.set((int) (getShadowLeft() + strokeSize + contentGap),
                (int) (getShadowTop() + strokeSize + contentGap),
                (int) (getShadowRight() + strokeSize + contentGap),
                (int) (getShadowBottom() + strokeSize + contentGap));
    }

    @NonNull
    default Drawable create() {
        return EffectDrawableFactory.createEffectDrawable(this);
    }

    interface DrawEffectParams extends EffectParams {

        int getShadowColor();

        @Override
        default boolean optStrokeOutCorner() {
            return Effects.DEFAULT_ENABLE_OPT_OUT_CORNER;
        }

        /**
         * .9图默认不开启边框外圆角半径优化
         */
        @Override
        default float getIncrementStrokeOutCornerRadius() {
            if (optStrokeOutCorner()) {
                return getStrokeSize() / 2f;
            } else {
                return 0f;
            }
        }
    }

    interface NinePathEffectParams extends EffectParams {

        NinePatchDrawable getDrawable();

        /**
         * .9图默认不开启外圆角半径优化;（没必要开启，如果开启之后，外圆角半径比实际的大，会可能与.9图的半径不一致）
         */
        @Override
        default boolean optStrokeOutCorner() {
            return false;
        }

        /**
         * .9图默认不开启边框外圆角半径优化
         */
        @Override
        default float getIncrementStrokeOutCornerRadius() {
            return 0;
        }

    }

    abstract class CommonParams implements EffectParams {

        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        float mStrokeSize;
        boolean mOptStrokeCorner;
        int mStrokeColor;
        float mContentGap;
        float mCornerRadius;
        float[] mCornerRadii;
        boolean mUseNegativeInsetDrawable;

        public CommonParams(Effects.Builder<?, ?> builder) {
            mStrokeSize = builder.mStrokeSize;
            mStrokeColor = builder.mStrokeColor;
            //如果设置了优化边框或者设置了自动优化并且边框大小超过了指定的厚度则进行边框优化
            mOptStrokeCorner = builder.mOptStrokeCorner;
            mContentGap = builder.mContentGap;
            mCornerRadius = builder.mCornerRadius;
            mCornerRadii = builder.mCornerRadii;
            mUseNegativeInsetDrawable = builder.mUseNegativeInsetDrawable;
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
        public boolean useNegativeInsetDrawable() {
            return mUseNegativeInsetDrawable;
        }
    }

    /**
     * .9图效果参数
     */
    final class NinePathDrawableEffectParamsImpl extends CommonParams implements NinePathEffectParams {

        private static final Rect NINE_PATH_PADDING_RECT = new Rect();

        NinePatchDrawable mDrawable;

        NinePathDrawableEffectParamsImpl(Effects.NinePathBuilder builder) {
            super(builder);
            mDrawable = builder.mDrawable;
            mDrawable.getPadding(NINE_PATH_PADDING_RECT);
            mShadowLeft = NINE_PATH_PADDING_RECT.left;
            mShadowTop = NINE_PATH_PADDING_RECT.top;
            mShadowRight = NINE_PATH_PADDING_RECT.right;
            mShadowBottom = NINE_PATH_PADDING_RECT.bottom;
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

        DrawEffectParamsImpl(Effects.DrawBuilder builder) {
            super(builder);
            mShadowLeft = builder.mShadowLeft;
            mShadowTop = builder.mShadowTop;
            mShadowRight = builder.mShadowRight;
            mShadowBottom = builder.mShadowBottom;
            mShadowColor = builder.mShadowColor;
        }

        @Override
        public int getShadowColor() {
            return mShadowColor;
        }

    }

}
