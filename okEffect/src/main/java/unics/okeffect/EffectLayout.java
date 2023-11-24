package unics.okeffect;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/23
 */
public class EffectLayout extends FrameLayout {

    public EffectLayout(@NonNull Context context) {
        this(context, null);
    }

    public EffectLayout(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.effectDrawableStyle);
    }

    public EffectLayout(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

    }

    @Override
    public void onViewAdded(View child) {
        super.onViewAdded(child);
    }
}
