package unics.okeffect;

import android.annotation.SuppressLint;
import android.content.res.Resources;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/15
 *
 * @apiNote 必须
 * @apiNote 绘制的阴影效果必须关闭硬件加速才有效果；建议对根布局做如下设置：contentView.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
 * 这里有个奇怪问题：即如果多层嵌套的viewgroup均设置了setLayerType(View.LAYER_TYPE_SOFTWARE, null)，可能会导致效果异常，建议是采用
 */
final class ShadowEffect extends AbstractEffect<ShadowEffect.ShadowState> {

    private boolean mPathIsDirty = true;

    ShadowEffect(float shadowLeft, float shadowTop, float shadowRight, float shadowBottom) {
        this(new ShadowState(shadowLeft, shadowTop, shadowRight, shadowBottom), null);
    }

    private ShadowEffect(@NonNull ShadowState state, @Nullable Resources resources) {
        super(state, resources);
    }

    @Override
    public void setCornerRadii(@Nullable float[] radii) {
        mPathIsDirty = true;
        super.setCornerRadii(radii);
    }

    @Override
    public void setCornerRadius(float radius) {
        mPathIsDirty = true;
        super.setCornerRadius(radius);
    }

    /**
     * 设置阴影大小（四周阴影大小相同）
     *
     * @param size
     */
    public void setShadowSize(int size) {
        setShadowSize(size, size, size, size);
    }

    /**
     * 设置四周阴影大小
     */
    public void setShadowSize(int left, int top, int right, int bottom) {
        mState.setShadowSize(left, top, right, bottom);
        mPathIsDirty = true;
        invalidateSelf();
    }

    void setOptShadowCorner(boolean enable, float optSize) {
        if (mState.mOptShadowCorner == enable && mState.mOptShadowSize == optSize)
            return;
        mState.mOptShadowCorner = enable;
        mState.mOptShadowSize = optSize;
        mPathIsDirty = true;
        invalidateSelf();
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            Effects.log("ShadowEffect#draw: empty bounds,return.");
            return;
        }
        buildPathIfDirty();
        Effects.log("ShadowEffect#draw: isHardwareAccelerated=" + canvas.isHardwareAccelerated());
        Effects.log("ShadowEffect#draw: bounds=" + bounds);
        Effects.log("ShadowEffect#draw: rect=" + mState.mRectF);
        canvas.drawPath(mState.mPath, mState.mPaint);
    }

    @Override
    public boolean getPadding(Rect padding) {
        padding.set((int) mState.mShadowLeft, (int) mState.mShadowTop, (int) mState.mShadowRight, (int) mState.mShadowBottom);
        return (padding.left | padding.top | padding.right | padding.bottom) != 0;
    }

    private void buildPathIfDirty() {
        final ShadowState st = mState;
        if (mPathIsDirty) {
            Effects.log("ShadowEffect#draw: buildPathIfDirty=" + st.mRectF);
            st.mPath.rewind();
            Rect bounds = getBounds();
            float optOffset = st.calShadowInset();
            float left = bounds.left + st.mShadowLeft + optOffset;
            float top = bounds.top + st.mShadowTop + optOffset;
            float right = bounds.right - st.mShadowRight - optOffset;
            float bottom = bounds.bottom - st.mShadowBottom - optOffset;
            st.mRectF.set(left, top, right, bottom);
            //必须使用这种方式，否则在某些情况下会出现中间有个阴影色的色块
            if (st.mRadiusArray != null && st.mRadiusArray.length > 0) {
                st.mPath.addRoundRect(st.mRectF, st.mRadiusArray, Path.Direction.CW);
            } else if (st.mRadius > 0) {
                st.mPath.addRoundRect(st.mRectF, st.mRadius, st.mRadius, Path.Direction.CW);
            } else {
                st.mPath.addRoundRect(st.mRectF, 0f, 0f, Path.Direction.CW);
            }
            Effects.log("ShadowEffect#draw: buildPathIfDirty=" + st.mRectF);
            mPathIsDirty = false;
            st.updateMaskFilter();
        }
    }

    static final class ShadowState extends AbstractState {

        Path mPath = new Path();
        RectF mRectF = new RectF();
        float mShadowLeft;
        float mShadowTop;
        float mShadowRight;
        float mShadowBottom;

        boolean mOptShadowCorner;
        float mOptShadowSize;

        ShadowState(float shadowLeft, float shadowTop, float shadowRight, float shadowBottom) {
            super();
            setShadowSize(shadowLeft, shadowTop, shadowRight, shadowBottom);
        }

        void updateMaskFilter() {

            float radius = mShadowLeft;
            if (mShadowTop > radius) {
                radius = mShadowTop;
            }
            if (mShadowRight > radius) {
                radius = mShadowRight;
            }
            if (mShadowBottom > radius) {
                radius = mShadowBottom;
            }
            radius += calShadowInset();
            mPaint.setMaskFilter(new BlurMaskFilter(radius, BlurMaskFilter.Blur.OUTER));
        }

        ShadowState(@NonNull ShadowState orig) {
            super(orig);
            mPath.set(orig.mPath);
            mRectF.set(orig.mRectF);
            mShadowLeft = orig.mShadowLeft;
            mShadowTop = orig.mShadowTop;
            mShadowRight = orig.mShadowRight;
            mShadowBottom = orig.mShadowBottom;
            mOptShadowCorner = orig.mOptShadowCorner;
            mOptShadowSize = orig.mOptShadowSize;
        }

        float calShadowInset() {
            if (mOptShadowCorner) {
                return mOptShadowSize;
            } else {
                return 1f;//偏移1像素，用于修正计算可能造成的精度丢失导致阴影和边框之间可能存在缝隙：没实际测试过，偏移1像素不会造成其他问题
            }
        }

        @SuppressLint("NewApi")
        void setShadowSize(float left, float top, float right, float bottom) {
            mShadowLeft = left;
            mShadowTop = top;
            mShadowRight = right;
            mShadowBottom = bottom;
        }

        @Override
        public Drawable newDrawable() {
            return new ShadowEffect(new ShadowState(this), null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new ShadowEffect(new ShadowState(this), res);
        }

    }

}
