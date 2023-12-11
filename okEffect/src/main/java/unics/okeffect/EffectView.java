package unics.okeffect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

/**
 * Create by luochao
 * on 2023/12/5
 * 会在Parent测量的大小下增加自己设置的margin；第一次测量都设置为0，第二次测量才会设置自己实际的大小
 */
class EffectView extends View {

    public EffectView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        setMeasuredDimension(0, 0);
    }

    /**
     * 二次测量才计算实际大小
     *
     * @param widthMeasureSpec     原始的测量size和mode
     * @param heightMeasureSpec    原始的测量size和mode
     * @param parentMeasuredWidth  父布局测量的宽
     * @param parentMeasuredHeight 父布局测量的高
     */
    @SuppressLint("WrongCall")
    void onMeasureSecondary(int widthMeasureSpec, int heightMeasureSpec, int parentMeasuredWidth, int parentMeasuredHeight) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
        final int width = Math.max(0, parentMeasuredWidth - lp.leftMargin - lp.rightMargin);
        final int height = Math.max(0, parentMeasuredHeight - lp.topMargin - lp.bottomMargin);
        int thisWidthMeasureSpec = MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY);
        int thisHeightMeasureSpec = MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY);
        super.onMeasure(thisWidthMeasureSpec, thisHeightMeasureSpec);
        Effects.log("EffectView@" + this.hashCode() + " onMeasureSecondary:expectWidth" + width + " expectHeight" + height + " pwidth=" + parentMeasuredWidth + " pheight=" + parentMeasuredHeight);
    }

}
