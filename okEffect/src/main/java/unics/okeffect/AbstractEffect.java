package unics.okeffect;

import android.content.res.Resources;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.drawable.Drawable;

import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/15
 */
abstract class AbstractEffect<T extends AbstractEffect.AbstractState> extends Drawable {

    @NonNull
    final T mState;

    AbstractEffect(T state) {
        this(state, null);
    }

    AbstractEffect(@NonNull T state, @Nullable Resources resources) {
        super();
        mState = state;
    }

    public void setColor(@ColorInt int color) {
        if (mState.mPaint.getColor() != color) {
            mState.mPaint.setColor(color);
            invalidateSelf();
        }
    }

    public void setCornerRadii(@Nullable float[] radii) {
        mState.setCornerRadii(radii);
        invalidateSelf();
    }

    @Nullable
    public float[] getCornerRadii() {
        float[] radii = mState.mRadiusArray;
        if (radii == null) {
            return null;
        }
        return radii.clone();
    }

    public void setCornerRadius(float radius) {
        mState.setCornerRadius(radius);
        invalidateSelf();
    }

    public float getCornerRadius() {
        return mState.mRadius;
    }

    @Override
    public void setAlpha(int alpha) {
        if (alpha != mState.mAlpha) {
            mState.mAlpha = alpha;
            invalidateSelf();
        }
    }

    @Override
    public int getAlpha() {
        return mState.mAlpha;
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
    public int getOpacity() {
        final Paint p = mState.mPaint;
        if (p.getXfermode() == null) {
            final int alpha = p.getAlpha();
            if (alpha == 0) {
                return PixelFormat.TRANSPARENT;
            }
            if (alpha == 255) {
                return PixelFormat.OPAQUE;
            }
        }
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setColorFilter(@Nullable ColorFilter colorFilter) {
        mState.mPaint.setColorFilter(colorFilter);
        invalidateSelf();
    }

    @Nullable
    @Override
    public ConstantState getConstantState() {
        return mState;
    }

    static abstract class AbstractState extends ConstantState {
        final @NonNull Paint mPaint;
        int mAlpha = 255;
        float mRadius = 0.0f;
        float[] mRadiusArray = null;

        AbstractState() {
            mPaint = new Paint();
        }

        AbstractState(@NonNull AbstractState orig) {
            mPaint = new Paint(orig.mPaint);
            mAlpha = orig.mAlpha;
            mRadius = orig.mRadius;
            mRadiusArray = orig.mRadiusArray;
        }

        public void setCornerRadius(float radius) {
            if (radius < 0) {
                radius = 0;
            }
            mRadius = radius;
            mRadiusArray = null;
        }

        public void setCornerRadii(float[] radii) {
            mRadiusArray = radii;
            if (radii == null) {
                mRadius = 0;
            }
        }

        /**
         * 是否存在圆角
         */
        boolean isRoundedShape() {
            return mRadius > 0 || (mRadiusArray != null && mRadiusArray.length > 0);
        }

        @Override
        public int getChangingConfigurations() {
            return 0;
        }

    }

}
