package unics.okeffect;

import android.graphics.Color;
import android.graphics.Rect;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;

/**
 * Create by luochao
 * on 2023/11/15
 * Drawable构建工厂
 */
interface EffectDrawableFactory<T extends EffectParams> {

    /**
     * 根据配置参数创建Drawable
     */
    Drawable create(T params);

    /**
     * 根据配置参数创建Drawable
     */
    static Drawable createEffectDrawable(EffectParams params) {
        if (params instanceof EffectParams.NinePatchEffectParams) {
            return NinePath.getInstance().create((EffectParams.NinePatchEffectParams) params);
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
        drawable.setCornerRadii(params.getCornerRadii());
        return drawable;
    }

    /**
     * 创建边框效果
     */
    static StrokeEffect createStrokeEffect(EffectParams.DrawEffectParams params) {
        StrokeEffect drawable = new StrokeEffect(params.getStrokeSize());
        drawable.setOptOutCorner(params.optStrokeOutCorner());
        drawable.setOptCorner(params.optStrokeCorner());
        drawable.setColor(params.getStrokeColor());
        float[] radii = params.getCornerRadii();
        drawable.setCornerRadii(radii);
        return drawable;
    }

    /**
     * 创建{@link EffectDrawable}
     *
     * @param content 绘制的内容部分
     * @param params  对应的参数
     */
    static Drawable createEffectDrawable(Drawable content, EffectParams params) {
        if (params.getBoundsType() == Effects.BOUNDS_TYPE_OUTER) {
            Rect rect = Effects.sTmpEffectRect;
            rect.setEmpty();
            params.getEffectRect(rect);
            return new EffectDrawable(content, rect.left, rect.top, rect.right, rect.bottom, params.getBoundsType(), params.containSoftwareLayer());
        } else {
            //在bounds内，则直接返回原始的drawable即可
            return content;
        }
    }

    /**
     * .9图构建工厂
     */
    final class NinePath implements EffectDrawableFactory<EffectParams.NinePatchEffectParams> {

        private static class SingleTone {
            static EffectDrawableFactory<EffectParams.NinePatchEffectParams> mInstance = new NinePath();
        }

        public static EffectDrawableFactory<EffectParams.NinePatchEffectParams> getInstance() {
            return SingleTone.mInstance;
        }

        @Override
        public Drawable create(EffectParams.NinePatchEffectParams params) {
            return createEffectDrawable(params.getDrawable(), params);
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
            return createEffectDrawable(content, params);
        }
    }

}
