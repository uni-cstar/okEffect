package unics.okeffect;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/23
 */
class EffectLayoutDelegate {

    private ViewGroup mViewGroup;
    private Effects.Builder mEffectBuilder;
    private boolean mUseNegativeMargin = true;

    EffectLayoutDelegate(ViewGroup viewGroup, @NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        this.mViewGroup = viewGroup;
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectLayout, defStyleAttr, 0);
        mEffectBuilder = Effects.withAttrs(ta);
        mUseNegativeMargin = ta.getBoolean(R.styleable.EffectLayout_ed_useNegativeMargin, mUseNegativeMargin);
        if (mUseNegativeMargin) {//采用负margin，只需要view自身关闭硬件加速即可
            mViewGroup.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        }else{
            mViewGroup.setBackground(mEffectBuilder.buildFocusSelectorDrawable());
        }
    }

    /**
     * ViewGroup添加了childView
     */
    void onViewAdded(View child) {
    }

    void onSizeChanged(int w, int h, int oldw, int oldh) {

    }


    void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {

    }

}
