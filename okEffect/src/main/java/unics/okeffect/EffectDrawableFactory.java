package unics.okeffect;

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

    static ShadowEffect createShadowEffect(EffectParams.DrawEffectParams params) {
        return createShadowEffect(params, false, 0f);
    }

    static ShadowEffect createShadowEffect(EffectParams.DrawEffectParams params, boolean optShadowCorner, float optShadowSize) {
        ShadowEffect drawable = new ShadowEffect(params.getShadowLeft(), params.getShadowTop(), params.getShadowRight(), params.getShadowBottom());
        drawable.setColor(params.getShadowColor());
        float[] radii = params.getCornerRadii();
        if (radii != null && radii.length > 0) {
            drawable.setCornerRadii(radii);
        } else {
            drawable.setCornerRadius(params.getCornerRadius());
        }
        drawable.setOptShadowCorner(optShadowCorner, optShadowSize);
        return drawable;
    }

    static StrokeEffect createStrokeEffect(EffectParams params) {
        return createStrokeEffect(params, Effects.DEFAULT_ENABLE_OPT_OUT_CORNER);
    }

    static StrokeEffect createStrokeEffect(EffectParams params, boolean optOutCorner) {
        StrokeEffect drawable = new StrokeEffect(params.getStrokeSize());
        drawable.setOptOutCorner(optOutCorner);
        drawable.setOptCorner(isOptStrokeCorner(params));
        drawable.setColor(params.getStrokeColor());
        float[] radii = params.getCornerRadii();
        if (radii != null && radii.length > 0) {
            drawable.setCornerRadii(radii);
        } else {
            drawable.setCornerRadius(params.getCornerRadius());
        }
        return drawable;
    }

    static Drawable createEffectDrawable(Drawable content, EffectParams params) {
        float strokeSize = params.getStrokeSize();
        float contentGap = params.getContentGap();
        return new EffectDrawable(content,
                -(int) (params.getShadowLeft() + strokeSize + contentGap),
                -(int) (params.getShadowTop() + strokeSize + contentGap),
                -(int) (params.getShadowRight() + strokeSize + contentGap),
                -(int) (params.getShadowBottom() + strokeSize + contentGap));
    }

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
                content = new LayerDrawable(new Drawable[]{params.getDrawable(), createStrokeEffect(params, false)});
            } else {
                content = params.getDrawable();
            }
            return createEffectDrawable(content, params);
        }
    }

    final class Draw implements EffectDrawableFactory<EffectParams.DrawEffectParams> {

        private static class SingleTone {
            static EffectDrawableFactory<EffectParams.DrawEffectParams> mInstance = new Draw();
        }

        public static EffectDrawableFactory<EffectParams.DrawEffectParams> getInstance() {
            return Draw.SingleTone.mInstance;
        }

        @Override
        public Drawable create(EffectParams.DrawEffectParams params) {
            boolean hasShadow = params.getShadowLeft() > 0 || params.getShadowTop() > 0 || params.getShadowRight() > 0 || params.getShadowBottom() > 0;
            boolean hasStroke = params.getStrokeSize() > 0;
            Drawable content;
            if (hasShadow && hasStroke) {
                //有阴影有边框:阴影跟边框同时存在的情况下，阴影根据边框是否绘制优化来决定是否进行绘制优化
                ShadowEffect shadow = createShadowEffect(params, isOptStrokeCorner(params), params.getStrokeSize() / 2f);
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
                content = null;
            }
            return createEffectDrawable(content, params);
        }
    }

    static boolean isOptStrokeCorner(EffectParams params) {
        //如果设置了边框圆角优化或者开启了自动优化并且边框超过了阈值，则执行边框优化
        return params.optStrokeCorner() || (Effects.mAutoOptStrokeCorner && params.getStrokeSize() > Effects.STOKE_THICK_LIMIT);
    }
}
