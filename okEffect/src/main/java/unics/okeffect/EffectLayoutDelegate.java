package unics.okeffect;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/23
 */
class EffectLayoutDelegate {

    interface DI {
        int superGetChildDrawingOrder(int childCount, int drawingPosition);

        void superOnMeasure(int widthMeasureSpec, int heightMeasureSpec);
    }

    static final int EFFECT_VIEW_ORDER_TOP = 0;
    static final int EFFECT_VIEW_ORDER_BOTTOM = 1;
    static final Rect sEffectInsetRect = new Rect();
    private final ViewGroup mViewGroup;
    private EffectView mEffectView;
    private int mEffectViewIndex = -1;
    private Effects.Builder<?, ?> mEffectBuilder;
    private EffectParams mEffectParams;
    private boolean mUseNegativeMargin = Effects.DEFAULT_EFFECT_LAYOUT_USE_NEGATIVE_MARGIN;
    private DI mDI;
    private int mEffectViewOrder = EFFECT_VIEW_ORDER_BOTTOM;

    EffectLayoutDelegate(DI di, @NonNull ViewGroup viewGroup) {
        this.mDI = di;
        this.mViewGroup = viewGroup;
    }

    void setup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectLayout, defStyleAttr, 0);
        mEffectBuilder = Effects.withAttrs(ta);
        mUseNegativeMargin = ta.getBoolean(R.styleable.EffectLayout_ed_useNegativeMargin, mUseNegativeMargin);
        mEffectViewOrder = ta.getInt(R.styleable.EffectLayout_ed_effectOrderType, mEffectViewOrder);
        //如果使用负margin，则就不使用负inset
        mEffectBuilder.mUseNegativeInsetDrawable = mEffectBuilder.mUseNegativeInsetDrawable && !mUseNegativeMargin;
        mEffectParams = mEffectBuilder.buildParams();
        if (mUseNegativeMargin) {//采用负margin，只需要effect view自身关闭硬件加速即可,parent不裁剪
            setupEffectView(context);
        } else {
            //todo 待补全说明
            mViewGroup.setBackground(mEffectBuilder.buildFocusStateListDrawable());
        }
        Log.i("EffectCorner", "" + mEffectParams.getCornerRadius() + " " + mEffectParams.getCornerRadii());
    }

    /**
     * 使用负margin方案，需要额外的view来处理，该方案的好处是由于低版本系统绘制阴影效果需要关闭硬件加速，但是整个视图关闭硬件性能更慢
     * 因此采用负margin，可以将效果的处理转给另外一个view来显示，让该view关闭硬件加速，这样只是局部关闭硬件加速，方案更好
     */
    private void setupEffectView(Context context) {
        if (mEffectView != null) {
            return;
        }
        //由于effectView会超过parent，parent不能裁剪child
        mViewGroup.setClipChildren(false);
        mEffectView = new EffectView(context);
        //跟随父group的状态变化
        mEffectView.setDuplicateParentStateEnabled(true);
        mEffectView.setBackground(Effects.createFocusStateListDrawable(mEffectParams.create()));
        mEffectView.setLayoutParams(generateEffectViewLayoutParams());
        //只需要自身view关闭硬件加速：奇怪的是在一款小米手机上（Android10）没有关闭硬件加速但是效果也绘制出来了
        mEffectView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mViewGroup.addView(mEffectView);
        if (mViewGroup.isFocused()) {
            mEffectView.setVisibility(View.VISIBLE);
        } else {
            mEffectView.setVisibility(View.INVISIBLE);
        }
    }

    private ViewGroup.LayoutParams generateEffectViewLayoutParams() {
        ViewGroup.MarginLayoutParams layoutParams;
        if (mViewGroup instanceof FrameLayout) {
            layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else if (mViewGroup instanceof LinearLayout) {
            layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else if (mViewGroup instanceof RelativeLayout) {
            layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        } else {
            layoutParams = new ViewGroup.MarginLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        }
        mEffectParams.getEffectRect(sEffectInsetRect);
        layoutParams.setMargins(-sEffectInsetRect.left, -sEffectInsetRect.top, -sEffectInsetRect.right, -sEffectInsetRect.bottom);
        return layoutParams;
    }

    /**
     * 是否使用额外的效果视图；负margin方案则需要采用额外的view来处理
     */
    boolean useEffectView() {
        return mUseNegativeMargin;
    }

    /**
     * 获取绘制顺序
     *
     * @param childCount      子view数量
     * @param drawingPosition 绘制位置
     */
    int getChildDrawingOrder(int childCount, int drawingPosition) {
        if (useEffectView()) {
            if (mEffectViewOrder == EFFECT_VIEW_ORDER_BOTTOM) {
                return getChildDrawingOrderBottom(childCount, drawingPosition, mEffectViewIndex);
            } else if (mEffectViewOrder == EFFECT_VIEW_ORDER_TOP) {
                return getChildDrawingOrderTop(childCount, drawingPosition, mEffectViewIndex);
            }
        }
        return mDI.superGetChildDrawingOrder(childCount, drawingPosition);
    }

    private int getChildDrawingOrderBottom(int childCount, int drawingPosition, int effectIndex) {
        //0,1,2,3,4,5,6,7,8
        //target = 4
        if (drawingPosition == 0) {
            // 0 -> 4
            return effectIndex;
        } else if (drawingPosition <= effectIndex) {
            //1,2,3,4 -> 0,1,2,3
            return drawingPosition - 1;
        } else {
            //5,6,7,8
            return drawingPosition;
        }
    }

    private int getChildDrawingOrderTop(int childCount, int drawingPosition, int effectIndex) {
        //0,1,2,3,4,5,6,7,8
        //target = 4
        if (drawingPosition < effectIndex) {
            //0,1,2,3
            return drawingPosition;
        } else if (drawingPosition < childCount - 1) {
            //4,5,6,7 -> 5,6,7,8
            return drawingPosition + 1;
        } else {
            return effectIndex;
        }
    }

    void onViewAdded(View child) {
        setupEffectViewIndex();
    }

    void onViewRemoved(View child) {
        setupEffectViewIndex();
    }

    private void setupEffectViewIndex() {
        if (mEffectView == null) {
            if (useEffectView()) {
                Log.w("okEffect", "EffectLayoutDelegate -> useEffectView,but the effect view is null.");
            }
            return;
        }
        mEffectViewIndex = mViewGroup.indexOfChild(mEffectView);
    }

    void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        if (!useEffectView())
            return;
        //这部分只是预防逻辑，其实可以不用，直接让mEffectView一直显示即可（背景是selector，未选中时没有背景和效果）
        if (gainFocus) {
            mEffectView.setVisibility(View.VISIBLE);
        } else {
            mEffectView.setVisibility(View.INVISIBLE);
        }
    }

    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mDI.superOnMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("EFDelegateG" + this.hashCode(), "onMeasure: gMeasuredWidth=" + mViewGroup.getMeasuredWidth() + " gMeasuredHeight=" + mViewGroup.getMeasuredHeight());
        if (useEffectView() && mEffectView != null) {
            mEffectView.onMeasureSecondary(widthMeasureSpec, heightMeasureSpec, mViewGroup.getMeasuredWidth(), mViewGroup.getMeasuredHeight());
//            Log.d("EFDelegateG" + this.hashCode(), "onMeasure: gMeasuredWidth=" + mViewGroup.getMeasuredWidth() + " gMeasuredHeight=" + mViewGroup.getMeasuredHeight());
        }
    }
}
