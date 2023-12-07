package unics.okeffect;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.util.Log;

/**
 * Create by luochao
 * on 2023/11/15
 * 效果Drawable构建工厂
 */
interface EffectDrawableFactory<T extends EffectParams> {

    Drawable create(T params);

    Rect sEffectInsetRect = new Rect();

    static Drawable createEffectDrawable(EffectParams params) {
        if (params instanceof EffectParams.NinePathEffectParams) {
            return NinePath.getInstance().create((EffectParams.NinePathEffectParams) params);
        } else if (params instanceof EffectParams.DrawEffectParams) {
            return Draw.getInstance().create((EffectParams.DrawEffectParams) params);
        }
        throw new RuntimeException("not support effect params type " + params.getClass().getName());
    }

    /**
     * 创建阴影效果
     */
    static ShadowEffect createShadowEffect(EffectParams.DrawEffectParams params) {
        ShadowEffect drawable = new ShadowEffect(params.getShadowLeft(), params.getShadowTop(), params.getShadowRight(), params.getShadowBottom());
        drawable.setColor(params.getShadowColor());
        //阴影圆角绘制优化
        drawable.setOptShadowCorner(params.optStrokeOutCorner(), params.getIncrementStrokeOutCornerRadius());
        float[] radii = params.getCornerRadii();
        if (radii != null && radii.length > 0) {
            drawable.setCornerRadii(radii);
        } else {
            drawable.setCornerRadius(params.getCornerRadius());
        }
        return drawable;
    }

    /**
     * 创建边框效果
     */
    static StrokeEffect createStrokeEffect(EffectParams params) {
        StrokeEffect drawable = new StrokeEffect(params.getStrokeSize());
        drawable.setOptOutCorner(params.optStrokeOutCorner());
        drawable.setOptCorner(params.optStrokeCorner());
        drawable.setColor(params.getStrokeColor());
        float[] radii = params.getCornerRadii();
        if (radii != null && radii.length > 0) {
            drawable.setCornerRadii(radii);
        } else {
            drawable.setCornerRadius(params.getCornerRadius());
        }
        return drawable;
    }

    /**
     * 创建{@link EffectDrawable}
     *
     * @param content 绘制的内容部分
     * @param params  对应的参数
     */
    static Drawable createEffectDrawable(Drawable content, EffectParams params) {
        params.getEffectRect(sEffectInsetRect);
        return new EffectDrawable(content,
                -sEffectInsetRect.left,
                -sEffectInsetRect.top,
                -sEffectInsetRect.right,
                -sEffectInsetRect.bottom);
    }

    /**
     * .9图构建工厂
     */
    final class NinePath implements EffectDrawableFactory<EffectParams.NinePathEffectParams> {

        private static class SingleTone {
            static EffectDrawableFactory<EffectParams.NinePathEffectParams> mInstance = new NinePath();
        }

        public static EffectDrawableFactory<EffectParams.NinePathEffectParams> getInstance() {
            return SingleTone.mInstance;
        }

        @Override
        public Drawable create(EffectParams.NinePathEffectParams params) {
            Drawable content;
            int strokeSize = (int) params.getStrokeSize();
            Log.i("Stroke", "create: strokeSize=" + strokeSize);
            if (strokeSize > 0) {
                //.9图的边框不用优化外边框圆角，这样子内边框和外边框的圆角是一样大的，就刚好能够跟.9切合
                content = new LayerDrawable(new Drawable[]{params.getDrawable(), createStrokeEffect(params)});
            } else {
                content = params.getDrawable();
            }
            if (params.useNegativeInsetDrawable()) {
                return createEffectDrawable(content, params);
            } else {
                return content;
            }
        }
    }

    /**
     * 自定义绘制工厂
     */
    final class Draw implements EffectDrawableFactory<EffectParams.DrawEffectParams> {

        private static class SingleTone {
            static EffectDrawableFactory<EffectParams.DrawEffectParams> mInstance = new Draw();
        }

        public static EffectDrawableFactory<EffectParams.DrawEffectParams> getInstance() {
            return Draw.SingleTone.mInstance;
        }

        @Override
        public Drawable create(EffectParams.DrawEffectParams params) {
            boolean hasShadow = params.hasShadow();
            boolean hasStroke = params.getStrokeSize() > 0;
            Drawable content;
            if (hasShadow && hasStroke) {
                //有阴影有边框:阴影跟边框同时存在的情况下，阴影根据边框是否绘制优化来决定是否进行绘制优化
                ShadowEffect shadow = createShadowEffect(params);
                StrokeEffect stroke = createStrokeEffect(params);
                content = new LayerDrawable(new Drawable[]{shadow, stroke});
            } else if (hasShadow) {
                //只有阴影
                content = createShadowEffect(params);
            } else if (hasStroke) {
                //只有边框
                content = createStrokeEffect(params);
            } else {
                //什么都没有
                content = new ColorDrawable(Color.TRANSPARENT);
            }
            if (params.useNegativeInsetDrawable()) {
                return createEffectDrawable(content, params);
            } else {
                return content;
            }
        }
    }

}
