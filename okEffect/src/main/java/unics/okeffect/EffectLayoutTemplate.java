package unics.okeffect;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/23
 * 代码模板
 */
public class EffectLayoutTemplate extends FrameLayout implements EffectLayoutDelegate.DI {

    private final EffectLayoutDelegate mDelegate;

    public EffectLayoutTemplate(@NonNull Context context) {
        this(context, null);
    }

    public EffectLayoutTemplate(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.effectDrawableStyle);
    }

    public EffectLayoutTemplate(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        //这两句代码不能合并执行，否则在setup中添加了view，会导致onViewAdd的回调，此时mDelegate还未创建成功
        mDelegate = new EffectLayoutDelegate(this, this);
        mDelegate.setup(context, attrs, defStyleAttr);
        //修改绘制顺序
        setChildrenDrawingOrderEnabled(true);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int drawingPosition) {
        int position = mDelegate.getChildDrawingOrder(childCount, drawingPosition);
        Log.i("okEffect", this.hashCode() + "@getChildDrawingOrderTop: drawingPosition=" + drawingPosition + " position=" + position);
        return position;
    }

    @Override
    public int superGetChildDrawingOrder(int childCount, int drawingPosition) {
        return super.getChildDrawingOrder(childCount, drawingPosition);
    }

    @SuppressLint("WrongCall")
    @Override
    public void superOnMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        mDelegate.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
        mDelegate.onViewAdded(child);
    }

    @Override
    public void onViewRemoved(View child) {
        super.onViewRemoved(child);
        mDelegate.onViewRemoved(child);
    }

    @Override
    protected void onFocusChanged(boolean gainFocus, int direction, @Nullable Rect previouslyFocusedRect) {
        super.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
        mDelegate.onFocusChanged(gainFocus, direction, previouslyFocusedRect);
    }

}
