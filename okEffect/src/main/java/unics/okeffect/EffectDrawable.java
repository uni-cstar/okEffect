package unics.okeffect;

import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BlendMode;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Outline;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

/**
 * Create by luochao
 * on 2023/12/8
 * 设计理念：让本Drawable的bounds跟view的bounds一样，但是让内部的drawable的bounds超过区域,其实用InsetDrawable传递负inset也可实现
 */
class EffectDrawable extends Drawable {

    /**
     * 绘制效果在控件bounds外，不占用控件区域，也不作为padding返回
     */
    public static final int BOUNDS_TYPE_OUTER = 0;

    /**
     * 绘制效果在控件bounds内，占用控件区域，作为padding部分
     */
    public static final int BOUNDS_TYPE_PADDING = 1;

    private final Drawable mDrawable;

    //是否使用drawingCache，建议开启
    private boolean mDrawingCacheEnable = true;

    //绘制缓存
    private Bitmap mDrawingCaching = null;

    //缓存是否需要重建
    private boolean mDrawingCacheIsDirty = true;

    //是否兼容硬件加速
    private boolean mHardwareAccelerated = false;

    //效果是否占Bounds区域
    private int mEffectBoundsType = BOUNDS_TYPE_OUTER;

    //四周大小
    private final int mEffectLeft, mEffectTop, mEffectRight, mEffectBottom;

    /**
     * @param content             实际绘制的drawable
     * @param boundsType          绘制位置类型
     * @param hardwareAccelerated 是否启用硬件加速优化
     */
    public EffectDrawable(@NonNull Drawable content, int effectLeft, int effectTop, int effectRight, int effectBottom, int boundsType, boolean hardwareAccelerated) {
        mDrawable = content;
        mEffectLeft = effectLeft;
        mEffectTop = effectTop;
        mEffectRight = effectRight;
        mEffectBottom = effectBottom;
        mEffectBoundsType = boundsType;
        mHardwareAccelerated = hardwareAccelerated;
    }

    /**
     * 是否会超过布局区域
     */
    public boolean isOutOfBounds() {
        return mEffectBoundsType == BOUNDS_TYPE_OUTER && (mEffectLeft | mEffectTop | mEffectRight | mEffectBottom) != 0;
    }

    /**
     * 是否启用图像缓存；该配置生效的情况下，只要图像的大小没有发生变化，则会复用之前的缓存图像，否则每次绘制都会创建新的图像。<br>
     * tips:{@link #mHardwareAccelerated}为true时，该配置才生效；<br>
     */
    public void setDrawingCacheEnable(boolean enable) {
        if (mDrawingCacheEnable == enable)
            return;
        mDrawingCacheEnable = enable;
    }

    //创建缓存图像
    private Bitmap createDrawingCache(int width, int height) {
        //释放之前的缓存
        if (mDrawingCaching != null) {
            mDrawingCaching.recycle();
            mDrawingCaching = null;
        }
        return Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Effects.log("draw: ");
        //兼容硬件加速并且图层也是硬件加速的图层，则执行优化
        if (mHardwareAccelerated && canvas.isHardwareAccelerated()) {
            final Rect bounds = mDrawable.getBounds();
            if (!mDrawingCacheEnable || mDrawingCacheIsDirty || mDrawingCaching == null) {
                //如果不允许使用缓存、或者缓存已脏、或者图片未创建
                mDrawingCaching = createDrawingCache(bounds.width(), bounds.height());
                Canvas cc = new Canvas(mDrawingCaching);
                cc.translate(-bounds.left, -bounds.top);
                mDrawable.draw(cc);
            }
            mDrawingCacheIsDirty = false;
            canvas.drawBitmap(mDrawingCaching, bounds.left, bounds.top, null);
        } else {
            mDrawable.draw(canvas);
        }
    }

    @Override
    public boolean getPadding(Rect padding) {
        //需要主动调用内部Drawable的方法，否则可能存在某些Drawable内部的padding计算未生效
        mDrawable.getPadding(padding);
        if (mEffectBoundsType == BOUNDS_TYPE_OUTER) {
            return super.getPadding(padding);
        } else {
            padding.set(mEffectLeft, mEffectTop, mEffectRight, mEffectBottom);
            return (padding.left | padding.top | padding.right | padding.bottom) != 0;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void getOutline(Outline outline) {
        outline.setRect(mDrawable.getBounds());
        outline.setAlpha(mDrawable.getAlpha() / 255.0f);
    }

    @Override
    protected void onBoundsChange(@NonNull Rect bounds) {
        super.onBoundsChange(bounds);
        mDrawingCacheIsDirty = true;
        if (mEffectBoundsType == BOUNDS_TYPE_OUTER) {
            //让drawable在实际的bounds基础上往外扩区域
            mDrawable.setBounds(bounds.left - mEffectLeft, bounds.top - mEffectTop, bounds.right + mEffectRight, bounds.bottom + mEffectBottom);
        } else {
            mDrawable.setBounds(bounds);
        }
    }

    @Override
    public int getIntrinsicWidth() {
        return getBounds().width();
    }

    @Override
    public int getIntrinsicHeight() {
        return getBounds().height();
    }

    @Override
    public void setAlpha(int alpha) {
        mDrawable.setAlpha(alpha);
    }

    @Override
    public int getAlpha() {
        return mDrawable.getAlpha();
    }

    @Override
    public void setColorFilter(ColorFilter colorFilter) {
        mDrawable.setColorFilter(colorFilter);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void setTintList(ColorStateList tint) {
        mDrawable.setTintList(tint);
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void setTintBlendMode(@NonNull BlendMode blendMode) {
        mDrawable.setTintBlendMode(blendMode);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public boolean onLayoutDirectionChanged(int layoutDirection) {
        mDrawable.onLayoutDirectionChanged(layoutDirection);
        return super.onLayoutDirectionChanged(layoutDirection);
    }

    @Override
    public int getOpacity() {
        return mDrawable.getAlpha();
    }

}
