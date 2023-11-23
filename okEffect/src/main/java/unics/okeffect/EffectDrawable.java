package unics.okeffect;

import android.graphics.drawable.Drawable;
import android.graphics.drawable.InsetDrawable;

import androidx.annotation.Nullable;

/**
 * Create by luochao
 * on 2023/11/14
 */
class EffectDrawable extends InsetDrawable {

    public EffectDrawable(@Nullable Drawable drawable, int inset) {
        super(drawable, inset);
    }

    public EffectDrawable(@Nullable Drawable drawable, int insetLeft, int insetTop, int insetRight, int insetBottom) {
        super(drawable, insetLeft, insetTop, insetRight, insetBottom);
    }

}
