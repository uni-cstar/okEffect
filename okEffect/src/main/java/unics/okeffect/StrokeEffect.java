package unics.okeffect;

import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Xfermode;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/15
 */
final class StrokeEffect extends AbstractEffect<StrokeEffect.StrokeState> {

    private boolean mPathIsDirty = true;

    private Xfermode xfermode = null;

    StrokeEffect(float strokeSize) {
        super(new StrokeState(strokeSize));
    }

    private StrokeEffect(@NonNull StrokeState state, @Nullable Resources resources) {
        super(state, resources);
    }

    public void setStrokeWidth(float width) {
        if (mState.mPaint.getStrokeWidth() == width)
            return;
        mState.mPaint.setStrokeWidth(width);
        mPathIsDirty = true;
        invalidateSelf();
    }

    @Override
    public void setCornerRadii(@Nullable float[] radii) {
        mPathIsDirty = true;
        super.setCornerRadii(radii);
    }

    /**
     * 是否优化圆角绘制
     */
    public void setOptCorner(boolean enable) {
        if (mState.mOptStrokeCorner == enable)
            return;
        mState.mOptStrokeCorner = enable;
        mPathIsDirty = true;
        invalidateSelf();
    }

    /**
     * 是否二次优化圆角绘制；开启的情况下，外圆角半径会增大半个边框大小
     * 默认开启
     */
    public void setOptOutCorner(boolean enable) {
        if (mState.mOptOuterStrokeCorner == enable)
            return;
        mState.mOptOuterStrokeCorner = enable;
        mPathIsDirty = true;
        invalidateSelf();
    }

    @Override
    public void setCornerRadius(float radius) {
        mPathIsDirty = true;
        super.setCornerRadius(radius);
    }

    @Override
    public boolean getPadding(Rect padding) {
        int strokeWidth = (int) mState.mPaint.getStrokeWidth();
        padding.set(strokeWidth, strokeWidth, strokeWidth, strokeWidth);
        return (padding.left | padding.top | padding.right | padding.bottom) != 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (bounds.isEmpty()) {
            return;
        }
        buildPathIfDirty();
        if (mState.mOptStrokeCorner && mState.isRoundedShape()) {
            int layerId = canvas.saveLayer(bounds.left, bounds.top, bounds.right, bounds.bottom, null, Canvas.ALL_SAVE_FLAG);
            canvas.drawPath(mState.mStrokeOuterPath, mState.mPaint);
            if (xfermode == null) {
                xfermode = new PorterDuffXfermode(PorterDuff.Mode.SRC_OUT);
            }
            mState.mPaint.setXfermode(xfermode);
            canvas.drawPath(mState.mStrokeInnerPath, mState.mPaint);
            canvas.restoreToCount(layerId);
        } else {
            canvas.drawPath(mState.mStrokePath, mState.mPaint);
        }
        mState.mPaint.setXfermode(null);
    }

    private void buildPathIfDirty() {
        if (!mPathIsDirty)
            return;

        if (mState.mOptStrokeCorner && mState.isRoundedShape()) {
            buildOptPath();
            mState.mPaint.setStyle(Paint.Style.FILL);
        } else {
            buildCommonPath();
            mState.mPaint.setStyle(Paint.Style.STROKE);
        }
        mPathIsDirty = false;
    }

    private void buildOptPath() {
        final StrokeEffect.StrokeState st = mState;
        st.mStrokePath.rewind();
        Rect bounds = getBounds();
        float strokeWidth = st.mPaint.getStrokeWidth();
        st.mRectF.set(bounds);

        float incRadius = strokeWidth;
        if (st.mRadiusArray != null && st.mRadiusArray.length > 0) {
            if (st.mOptOuterStrokeCorner) {
                if (st.mOutRadiusArray == null) {
                    st.mOutRadiusArray = new float[8];
                }
                st.mOutRadiusArray[0] = isFloat0(st.mRadiusArray[0]) ? 0f : st.mRadiusArray[0] + incRadius;
                st.mOutRadiusArray[1] = isFloat0(st.mRadiusArray[1]) ? 0f : st.mRadiusArray[1] + incRadius;
                st.mOutRadiusArray[2] = isFloat0(st.mRadiusArray[2]) ? 0f : st.mRadiusArray[2] + incRadius;
                st.mOutRadiusArray[3] = isFloat0(st.mRadiusArray[3]) ? 0f : st.mRadiusArray[3] + incRadius;
                st.mOutRadiusArray[4] = isFloat0(st.mRadiusArray[4]) ? 0f : st.mRadiusArray[4] + incRadius;
                st.mOutRadiusArray[5] = isFloat0(st.mRadiusArray[5]) ? 0f : st.mRadiusArray[5] + incRadius;
                st.mOutRadiusArray[6] = isFloat0(st.mRadiusArray[6]) ? 0f : st.mRadiusArray[6] + incRadius;
                st.mOutRadiusArray[7] = isFloat0(st.mRadiusArray[7]) ? 0f : st.mRadiusArray[7] + incRadius;
                st.mStrokeOuterPath.addRoundRect(st.mRectF, st.mOutRadiusArray, Path.Direction.CW);
            } else {
                st.mStrokeOuterPath.addRoundRect(st.mRectF, st.mRadiusArray, Path.Direction.CW);
            }
            st.mRectF.set(bounds.left + strokeWidth, bounds.top + strokeWidth, bounds.right - strokeWidth, bounds.bottom - strokeWidth);
            st.mStrokeInnerPath.addRoundRect(st.mRectF, st.mRadiusArray, Path.Direction.CW);
        } else if (!isFloat0(st.mRadius)) {
            if (st.mOptOuterStrokeCorner) {
                st.mStrokeOuterPath.addRoundRect(st.mRectF, st.mRadius + incRadius, st.mRadius + incRadius, Path.Direction.CW);
            } else {
                st.mStrokeOuterPath.addRoundRect(st.mRectF, st.mRadius, st.mRadius, Path.Direction.CW);
            }
            st.mRectF.set(bounds.left + strokeWidth, bounds.top + strokeWidth, bounds.right - strokeWidth, bounds.bottom - strokeWidth);
            st.mStrokeInnerPath.addRoundRect(st.mRectF, st.mRadius, st.mRadius, Path.Direction.CW);
        } else {
            st.mStrokeOuterPath.addRect(st.mRectF, Path.Direction.CW);
            st.mRectF.set(bounds.left + strokeWidth, bounds.top + strokeWidth, bounds.right - strokeWidth, bounds.bottom - strokeWidth);
            st.mStrokeInnerPath.addRect(st.mRectF, Path.Direction.CW);
        }
    }

    private boolean isFloat0(float value) {
        return Math.abs(value) < 0.0000001f;
    }

    private void buildCommonPath() {
        final StrokeEffect.StrokeState st = mState;
        st.mStrokePath.rewind();
        Rect bounds = getBounds();
        float inset = st.mPaint.getStrokeWidth() * 0.5f;
        float left = bounds.left + inset;
        float top = bounds.top + inset;
        float right = bounds.right - inset;
        float bottom = bounds.bottom - inset;
        st.mRectF.set(left, top, right, bottom);
        if (st.mRadiusArray != null && st.mRadiusArray.length > 0) {
            st.mStrokePath.addRoundRect(st.mRectF, st.mRadiusArray, Path.Direction.CW);
        } else if (st.mRadius > 0) {
            st.mStrokePath.addRoundRect(st.mRectF, st.mRadius, st.mRadius, Path.Direction.CW);
        } else {
            st.mStrokePath.addRect(st.mRectF, Path.Direction.CW);
        }
    }

    static final class StrokeState extends AbstractEffect.AbstractState {

        Path mStrokePath = new Path();
        Path mStrokeOuterPath = new Path();
        Path mStrokeInnerPath = new Path();
        RectF mRectF = new RectF();
        boolean mOptStrokeCorner;

        boolean mOptOuterStrokeCorner = Effects.DEFAULT_ENABLE_OPT_OUT_CORNER;
        float[] mOutRadiusArray = null;

        StrokeState(float strokeSize) {
            super();
            mPaint.setStrokeWidth(strokeSize);
            mPaint.setAntiAlias(true);
            mPaint.setStyle(Paint.Style.STROKE);
        }

        StrokeState(@NonNull StrokeState orig) {
            super(orig);
            mStrokePath.set(orig.mStrokePath);
            mStrokeOuterPath.set(orig.mStrokeOuterPath);
            mStrokeInnerPath.set(orig.mStrokeInnerPath);
            mRectF.set(orig.mRectF);
            mOptStrokeCorner = orig.mOptStrokeCorner;
            mOptOuterStrokeCorner = orig.mOptOuterStrokeCorner;
            mOutRadiusArray = orig.mOutRadiusArray;
        }

        @Override
        public Drawable newDrawable() {
            return new StrokeEffect(new StrokeState(this), null);
        }

        @Override
        public Drawable newDrawable(Resources res) {
            return new StrokeEffect(new StrokeState(this), res);
        }

    }

}
