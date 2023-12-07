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
 * 实现的原理是在内部增加一个负margin的view设置EffectDrawable
 * 该方案的好处：由于阴影部分的绘制需要关闭硬件加速，内部增加一个view来处理，只需要关闭该view的硬件加速即可，不需要关闭整个viewGroup的硬件加速，并且还可以因此提供效果的绘制层级（在所有view的底部或者顶部）
 */
class EffectLayoutDelegate {

    /**
     * 代理需要反向注入调用的方法
     */
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
    private final DI mDI;
    private int mEffectViewOrder = EFFECT_VIEW_ORDER_TOP;

    EffectLayoutDelegate(DI di, @NonNull ViewGroup viewGroup) {
        this.mDI = di;
        this.mViewGroup = viewGroup;
    }

    void setup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectDrawable, defStyleAttr, 0);
        mEffectBuilder = Effects.withAttrs(ta);
        mEffectViewOrder = ta.getInt(R.styleable.EffectDrawable_ed_effectOrderType, mEffectViewOrder);
        //禁用负inset，返回常规drawable
        mEffectBuilder.mUseNegativeInsetDrawable = false;
        mEffectParams = mEffectBuilder.buildParams();
        setupEffectView(context);
    }

    /**
     * 使用负margin方案，需要额外的view来处理，该方案的好处是由于低版本系统绘制阴影效果需要关闭硬件加速，但是整个视图关闭硬件性能更慢
     * 因此采用负margin，可以将效果的处理转给另外一个view来显示，让该view关闭硬件加速，这样只是局部关闭硬件加速，方案更好
     */
    private void setupEffectView(Context context) {
        if (mEffectView != null) {
            return;
        }
        //关键点1：由于effectView会超过parent（负margin区域），parent不能裁剪child
        mViewGroup.setClipChildren(false);
        mEffectView = new EffectView(context);
        //关键点2：跟随父group的状态变化（获焦点时显示，没获取焦点时隐藏）
        mEffectView.setDuplicateParentStateEnabled(true);
        mEffectView.setBackground(Effects.createFocusStateListDrawable(mEffectParams.create()));
        mEffectView.setLayoutParams(generateEffectViewLayoutParams());
        //关键点3：阴影的绘制需要关闭硬件加速：奇怪的是在一款小米手机上（Android10）没有关闭硬件加速但是效果也绘制出来了
        mEffectView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mViewGroup.addView(mEffectView);
    }

    /**
     * 用于版本兼容：在Android 7之前，如果childView设置的LayoutParams与{@link #mViewGroup}类型不同，会导致添加到ViewGroup中时margin被忽略
     *
     * @see View.sPreserveMarginParamsInLayoutParamConversion （包访问）Android 7即以后的源码中有该变量的定义
     */
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
     * 获取绘制顺序
     *
     * @param childCount      子view数量
     * @param drawingPosition 绘制位置
     */
    @CallBy
    int getChildDrawingOrder(int childCount, int drawingPosition) {
        if (mEffectViewOrder == EFFECT_VIEW_ORDER_BOTTOM) {
            return getChildDrawingOrderBottom(childCount, drawingPosition, mEffectViewIndex);
        } else if (mEffectViewOrder == EFFECT_VIEW_ORDER_TOP) {
            return getChildDrawingOrderTop(childCount, drawingPosition, mEffectViewIndex);
        } else {
            return mDI.superGetChildDrawingOrder(childCount, drawingPosition);
        }
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

    @CallBy
    void onViewAdded(View child) {
        setupEffectViewIndex();
    }

    @CallBy
    void onViewRemoved(View child) {
        setupEffectViewIndex();
    }

    private void setupEffectViewIndex() {
        if (mEffectView == null) {
            Log.w("okEffect", "EffectLayoutDelegate -> useEffectView,but the effect view is null.");
            return;
        }
        mEffectViewIndex = mViewGroup.indexOfChild(mEffectView);
    }

    @CallBy
    void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
//        //这部分只是预防逻辑，其实可以不用，直接让mEffectView一直显示即可（背景是selector，未选中时没有背景和效果）
//        if (gainFocus) {
//            mEffectView.setVisibility(View.VISIBLE);
//        } else {
//            mEffectView.setVisibility(View.INVISIBLE);
//        }
    }

    @CallBy
    void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mDI.superOnMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("EFDelegateG" + this.hashCode(), "onMeasure: gMeasuredWidth=" + mViewGroup.getMeasuredWidth() + " gMeasuredHeight=" + mViewGroup.getMeasuredHeight());
        if (mEffectView != null) {
            mEffectView.onMeasureSecondary(widthMeasureSpec, heightMeasureSpec, mViewGroup.getMeasuredWidth(), mViewGroup.getMeasuredHeight());
//            Log.d("EFDelegateG" + this.hashCode(), "onMeasure: gMeasuredWidth=" + mViewGroup.getMeasuredWidth() + " gMeasuredHeight=" + mViewGroup.getMeasuredHeight());
        }
    }

}
