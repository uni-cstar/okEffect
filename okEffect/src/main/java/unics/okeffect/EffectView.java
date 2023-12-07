package unics.okeffect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

/**
 * Create by luochao
 * on 2023/12/5
 * 此view会在viewgroup测量的大小下增加自己设置的margin；第一次测量都设置为0，第二次测量才会设置自己实际的大小
 */
class EffectView extends View {

    public EffectView(Context context) {
        super(context);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
//        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        //每次测量的时候只需要返回0即可，不需要测量自身，等到viewGroup测量完成之后再测量
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
        Log.d("EFDelegateV" + this.hashCode(), "onMeasureSecondary:expectWidth" + width + " expectHeight" + height + " pwidth=" + parentMeasuredWidth + " pheight=" + parentMeasuredHeight);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d("EFDelegateV" + this.hashCode(), "onLayout: measuredWidth" + this.getMeasuredWidth() + " measuredHeight" + this.getMeasuredHeight() + " width=" + this.getWidth() + " height=" + this.getHeight());
        Log.d("EFDelegateV" + this.hashCode(), "onLayout: changed=" + changed + " left=" + left + " top=" + top + " right=" + right + " bottom=" + bottom);
//        ViewGroup.MarginLayoutParams mlp = (ViewGroup.MarginLayoutParams) this.getLayoutParams();
//        Log.d("EFDelegateV" + this.hashCode(), "onLayout: margin left=" + mlp.leftMargin + " top=" + mlp.topMargin + " right=" + mlp.rightMargin + " bottom=" + mlp.bottomMargin);
    }

}
