package unics.okeffect;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.NinePatchDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;

import androidx.annotation.CallSuper;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Px;

/**
 * Create by luochao
 * on 2023/12/8
 */
public abstract class EffectBuilder<T extends EffectBuilder<?, ?>, P extends EffectParams> {

    //内容间距
    float mContentGap;

    //圆角半径：放在这里是为了后续提供扫光效果可能需要该参数
    float mCornerRadius;
    //圆角四个角半径：放在这里是为了后续提供扫光效果可能需要该参数
    float[] mCornerRadii;

    int mEffectBoundsType = Effects.sDefaultBoundsType;

    /**
     * 从布局中加载参数
     */
    @CallSuper
    public T loadAttrs(@NonNull TypedArray ta) {
        mContentGap = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_contentGap, 0);
        mEffectBoundsType = ta.getInt(R.styleable.EffectDrawable_ed_boundsType, mEffectBoundsType);
        return (T) this;
    }

    /**
     * 设置内容与效果之间的间距
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
     * 设置效果在区域外
     */
    public T setOuterBoundType() {
        mEffectBoundsType = Effects.BOUNDS_TYPE_OUTER;
        return (T) this;
    }

    /**
     * 设置效果在区域为作为padding部分
     */
    public T setPaddingBoundType() {
        mEffectBoundsType = Effects.BOUNDS_TYPE_PADDING;
        return (T) this;
    }

    /**
     * 构建效果参数
     */
    public abstract P buildParams();

    /**
     * 应用效果
     */
    public void into(View view) {
        into(view, view.isClickable() || view.isFocusable());
    }

    public void into(View view, boolean useStateListDrawable) {
        EffectParams params = buildParams();
        Drawable drawable = EffectDrawableFactory.createEffectDrawable(params);
        if (useStateListDrawable) {
            view.setBackground(Effects.createStateListDrawable(drawable));
        } else {
            view.setBackground(drawable);
        }
        //修正clip问题
        if (drawable instanceof EffectDrawable && ((EffectDrawable) drawable).isOutOfBounds()) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                if (((ViewGroup) parent).getClipChildren()) {
                    ((ViewGroup) parent).setClipChildren(false);
                }
            }
        }
    }

    private ViewGroup.MarginLayoutParams resolveLayoutParams(ViewGroup.LayoutParams lp) {
        if (lp instanceof ViewGroup.MarginLayoutParams) {
            return (ViewGroup.MarginLayoutParams) lp;
        } else {
            ViewGroup.MarginLayoutParams mlp = new ViewGroup.MarginLayoutParams(lp.width, lp.height);
            mlp.layoutAnimationParameters = lp.layoutAnimationParameters;
            return mlp;
        }
    }

    /**
     * 基于.9图的构造器
     */
    public static class NinePathBuilder extends EffectBuilder<NinePathBuilder, EffectParams.NinePatchEffectParams> {

        NinePatchDrawable mDrawable;

        NinePathBuilder(NinePatchDrawable drawable) {
            mDrawable = drawable;
        }

        @Override
        public EffectParams.NinePatchEffectParams buildParams() {
            return new EffectParams.NinePatchEffectParamsImpl(this);
        }

    }

    /**
     * 基于自定义绘制的构造器
     */
    public static class DrawBuilder extends EffectBuilder<DrawBuilder, EffectParams.DrawEffectParams> {

        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        int mShadowColor;
        float mStrokeSize;
        int mStrokeColor;
        boolean mOptStrokeCorner;

        @Override
        public EffectParams.DrawEffectParams buildParams() {
            return new EffectParams.DrawEffectParamsImpl(this);
        }

        @Override
        public DrawBuilder loadAttrs(@NonNull TypedArray ta) {
            super.loadAttrs(ta);
            float shadowSize = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSize, 0f);
            mShadowLeft = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeLeft, shadowSize);
            mShadowTop = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeTop, shadowSize);
            mShadowRight = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeRight, shadowSize);
            mShadowBottom = ta.getDimension(R.styleable.EffectDrawable_ed_shadowSizeBottom, shadowSize);
            mShadowColor = ta.getColor(R.styleable.EffectDrawable_ed_shadowColor, Color.BLACK);
            mStrokeSize = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_strokeSize, 0);
            mStrokeColor = ta.getColor(R.styleable.EffectDrawable_ed_strokeColor, Color.WHITE);
            mOptStrokeCorner = ta.getBoolean(R.styleable.EffectDrawable_ed_optStrokeCorner, Effects.mAutoOptStrokeCorner && mStrokeSize > Effects.STOKE_THICK_LIMIT);
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

        /**
         * 设置边框
         *
         * @param width 边框宽度
         * @param color 边框颜色
         * @see #setStrokeWidth(float)
         * @see #setStrokeColor(int)
         */
        public DrawBuilder setStroke(@Px float width, @ColorInt int color) {
            mStrokeSize = width;
            mStrokeColor = color;
            return this;
        }

        /**
         * 设置边框宽度
         *
         * @see #setStroke(float, int)
         */
        public DrawBuilder setStrokeWidth(@Px float width) {
            mStrokeSize = width;
            return this;
        }

        /**
         * 设置边框颜色
         *
         * @see #setStroke(float, int)
         */
        public DrawBuilder setStrokeColor(@ColorInt int color) {
            mStrokeColor = color;
            return this;
        }

        /**
         * 是否设置优化边框圆角处的绘制;
         * 解释：
         * 如果边框存在圆角，绘制的时候，是按照边框的中线进行绘制，即保障的是边框中线的圆角值，这样造成的效果是绘制出来的边框的内边和外边与实际的圆角值不相等，内边框的圆角值小，外边框更大，
         * 造成实际的效果就是边框内边圆角更小，与内部内容圆角不一致，还会形成空隙；如果还存在同样角度的阴影，由于边框外边框圆角更大，也会与绘制的阴影间形成空隙；
         * 因此开启此配置，会保证内外边框圆角均为设定的值，这样边框会与内外内容的圆角一致并贴合；
         * 但是这样也会造成一个问题，就是圆角处的边框线看会比直线部分更厚（更粗）；解决办法就优化圆角处的外边框线圆角半径，目前已默认开启该配置（没理由不开启，二次优化会让整个绘制效果开起来更好），具体查看{@link Effects#DEFAULT_ENABLE_OPT_OUT_CORNER}
         *
         * @param enable 是否开启
         * @see unics.okeffect.EffectParams.DrawEffectParams#optStrokeCorner()
         * @see Effects#setAutoOptStrokeCorner
         */
        public DrawBuilder setOptStrokeCorner(boolean enable) {
            mOptStrokeCorner = enable;
            return this;
        }

    }
}
