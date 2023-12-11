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

    //边界类型
    int mEffectBoundsType = Effects.DEFAULT_BOUNDS_TYPE;

    //内容间距
    float mContentGap;

    //圆角四个角半径：放在这里是为了后续提供扫光效果可能需要该参数
    float[] mCornerRadii;

    /**
     * 加载属性
     */
    @CallSuper
    public T loadAttrs(@NonNull TypedArray ta) {
        mEffectBoundsType = ta.getInt(R.styleable.EffectDrawable_ed_boundsType, mEffectBoundsType);
        mContentGap = ta.getDimensionPixelSize(R.styleable.EffectDrawable_ed_contentGap, 0);

        float cornerSize = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSize, 0f);
        float cornerTopLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopLeft, cornerSize);
        float cornerTopRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeTopRight, cornerSize);
        float cornerBottomRight = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomRight, cornerSize);
        float cornerBottomLeft = ta.getDimension(R.styleable.EffectDrawable_ed_cornerSizeBottomLeft, cornerSize);
        if (cornerTopLeft > 0f || cornerTopRight > 0f || cornerBottomLeft > 0f || cornerBottomRight > 0f) {
            setCornerRadii(new float[]{cornerTopLeft, cornerTopLeft, cornerTopRight, cornerTopRight, cornerBottomRight, cornerBottomRight, cornerBottomLeft, cornerBottomLeft});
        }
        return (T) this;
    }

    /**
     * 设置内容与效果之间的间距
     *
     * @see R.styleable#EffectDrawable_ed_contentGap
     */
    public T setContentCap(@Px float gap) {
        mContentGap = gap;
        return (T) this;
    }

    /**
     * 设置四个角圆角半径
     *
     * @see R.styleable#EffectDrawable_ed_cornerSize
     * @see R.styleable#EffectDrawable_ed_cornerSizeTopLeft
     * @see R.styleable#EffectDrawable_ed_cornerSizeTopRight
     * @see R.styleable#EffectDrawable_ed_cornerSizeBottomRight
     * @see R.styleable#EffectDrawable_ed_cornerSizeBottomLeft
     * @see #setCornerRadii(float[])
     */
    public T setCornerRadius(@Px float radius) {
        if (mCornerRadii == null) {
            setCornerRadii(new float[]{radius, radius, radius, radius, radius, radius, radius, radius});
        } else {
            mCornerRadii[0] = mCornerRadii[1] = mCornerRadii[2] = mCornerRadii[3] = mCornerRadii[4] = mCornerRadii[5] = mCornerRadii[6] = mCornerRadii[7] = radius;
        }
        return (T) this;
    }

    /**
     * 设置四个角圆角半径
     * The corners are ordered top-left, top-right, bottom-right, bottom-left.
     *
     * @param radii 四个角的圆角半径：每个角2个坐标
     * @see R.styleable#EffectDrawable_ed_cornerSize
     * @see R.styleable#EffectDrawable_ed_cornerSizeTopLeft
     * @see R.styleable#EffectDrawable_ed_cornerSizeTopRight
     * @see R.styleable#EffectDrawable_ed_cornerSizeBottomRight
     * @see R.styleable#EffectDrawable_ed_cornerSizeBottomLeft
     * @see #setCornerRadius(float)
     */
    public T setCornerRadii(@Px float[] radii) {
        mCornerRadii = radii;
        return (T) this;
    }

    /**
     * 设置所占位置为控件区域外
     *
     * @see R.styleable#EffectDrawable_ed_boundsType
     * @see Effects#BOUNDS_TYPE_OUTER
     */
    public T setOuterBoundsType() {
        mEffectBoundsType = Effects.BOUNDS_TYPE_OUTER;
        return (T) this;
    }

    /**
     * 设置所占位置为控件区域内
     *
     * @see R.styleable#EffectDrawable_ed_boundsType
     * @see Effects#BOUNDS_TYPE_PADDING
     */
    public T setPaddingBoundsType() {
        mEffectBoundsType = Effects.BOUNDS_TYPE_PADDING;
        return (T) this;
    }

    /**
     * 构建效果参数
     */
    public abstract P buildParams();

    /**
     * 应用效果,如果view可点击或者可获焦，则会创建{@link android.graphics.drawable.StateListDrawable},否则为常规无状态的Drawable。
     */
    public EffectParams into(View view) {
        return into(view, view.isClickable() || view.isFocusable());
    }

    /**
     * 应用效果
     *
     * @param useStateListDrawable 是否使用{@link android.graphics.drawable.StateListDrawable}
     * @see #into(View)
     */
    public EffectParams into(View view, boolean useStateListDrawable) {
        EffectParams params = buildParams();
        Drawable drawable = EffectDrawableFactory.createEffectDrawable(params);
        int pl = view.getPaddingLeft();
        int pt = view.getPaddingTop();
        int pr = view.getPaddingRight();
        int pb = view.getPaddingBottom();
        if (useStateListDrawable) {
            view.setBackground(Effects.createStateListDrawable(drawable));
        } else {
            view.setBackground(drawable);
        }
        view.setPadding(pl, pt, pr, pb);
        //修正clip问题
        if (drawable instanceof EffectDrawable && ((EffectDrawable) drawable).isOutOfBounds()) {
            ViewParent parent = view.getParent();
            if (parent instanceof ViewGroup) {
                if (((ViewGroup) parent).getClipChildren()) {
                    ((ViewGroup) parent).setClipChildren(false);
                }
            }
        }
        return params;
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
     * 基于Canvas绘制的构造器
     */
    public static class DrawBuilder extends EffectBuilder<DrawBuilder, EffectParams.DrawEffectParams> {

        float mShadowLeft;
        float mShadowRight;
        float mShadowTop;
        float mShadowBottom;
        //阴影颜色，默认黑色
        int mShadowColor;
        float mStrokeSize;
        //边框颜色，默认白色
        int mStrokeColor;
        boolean mOptStrokeCorner;

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
            mOptStrokeCorner = ta.getBoolean(R.styleable.EffectDrawable_ed_optStrokeCorner, Effects.sAutoOptStrokeCorner && mStrokeSize > Effects.STOKE_THICK_LIMIT);
            return this;
        }

        @Override
        public EffectParams.DrawEffectParams buildParams() {
            return new EffectParams.DrawEffectParamsImpl(this);
        }

        /**
         * 设置阴影
         *
         * @param size  大小
         * @param color 颜色
         * @see #setShadow(float, float, float, float, int)
         */
        public DrawBuilder setShadow(@Px float size, @ColorInt int color) {
            setShadowSize(size);
            setShadowColor(color);
            return this;
        }

        /**
         * 设置阴影
         *
         * @param left   左侧阴影大小
         * @param top    顶部阴影大小
         * @param right  右侧阴影大小
         * @param bottom 底部阴影大小
         * @param color  阴影颜色
         * @deprecated 作用不大，实际效果可能不符合设计预期，预留后续优化
         */
        @Deprecated
        public DrawBuilder setShadow(@Px float left, @Px float top, @Px float right, @Px float bottom, @ColorInt int color) {
            setShadowSize(left, top, right, bottom);
            setShadowColor(color);
            return this;
        }

        /**
         * 设置阴影大小
         */
        public DrawBuilder setShadowSize(@Px float size) {
            return setShadowSize(size, size, size, size);
        }

        /**
         * 设置阴影大小
         *
         * @param left   左侧阴影大小
         * @param top    顶部阴影大小
         * @param right  右侧阴影大小
         * @param bottom 底部阴影大小
         * @deprecated 作用不大，实际效果可能不符合设计预期，预留后续优化
         */
        @Deprecated
        public DrawBuilder setShadowSize(@Px float left, @Px float top, @Px float right, @Px float bottom) {
            mShadowLeft = left;
            mShadowTop = top;
            mShadowRight = right;
            mShadowBottom = bottom;
            return this;
        }

        /**
         * 设置阴影颜色
         */
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
         * @see R.attr#ed_strokeSize
         */
        public DrawBuilder setStrokeWidth(@Px float width) {
            mStrokeSize = width;
            return this;
        }

        /**
         * 设置边框颜色
         *
         * @see #setStroke(float, int)
         * @see R.attr#ed_strokeSize
         */
        public DrawBuilder setStrokeColor(@ColorInt int color) {
            mStrokeColor = color;
            return this;
        }

        /**
         * 设置边框圆角优化<br>
         * tips:<br>
         * 原因：如果边框存在圆角，绘制的时候，是按照边框的中线进行绘制，即保障的是边框中线的圆角值，这样造成的效果是绘制出来的边框的内边和外边与实际的圆角值不相等，内边框的圆角值小，外边框更大。<br>
         * 结果：因为内边框圆角更小，与内容圆角不一致，因此会与内容之间形成空隙（举个例子，如果边框比较大，内圆角很容易就成直角）；如果还设置了同样角度大小的阴影，由于外边框圆角比设置的大，也会与绘制的阴影间形成空隙；<br>
         * 优化：开启此配置，会保证内外边框圆角均为设定的值，这样边框会与内外内容的圆角一致并贴合；<br>
         * 额外：但是优化后也会造成一个问题，边框线的圆角部分会比直线部分更粗,这是正常现象；通过优化外边框的圆角半径，目前已默认开启该配置（开启边框优化的情况下没理由不开启外边框优化，外边框角度优化会让整个绘制效果看起来更好），具体查看{@link Effects#DEFAULT_ENABLE_OPT_OUT_CORNER}
         *
         * @param enable 是否开启
         * @see Effects#setAutoOptStrokeCorner
         * @see Effects#DEFAULT_ENABLE_OPT_OUT_CORNER
         * @see R.attr#ed_optStrokeCorner
         */
        public DrawBuilder setOptStrokeCorner(boolean enable) {
            mOptStrokeCorner = enable;
            return this;
        }

    }
}
