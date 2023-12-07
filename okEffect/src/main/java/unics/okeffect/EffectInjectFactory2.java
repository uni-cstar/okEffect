package unics.okeffect;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * Create by luochao
 * on 2023/11/17
 */
public class EffectInjectFactory2 implements LayoutInflater.Factory2 {

    private final Activity mActivity;
    private final LayoutInflater.Factory2 mCustom;

    private static boolean sDependAppCompat = false;

    private static final String[] sClassPrefixList = {
            "android.widget.",
            "android.view.",
            "android.webkit."
    };

    public EffectInjectFactory2(@NonNull Activity activity) {
        this(activity, null);
    }

    public EffectInjectFactory2(@NonNull Activity activity, @Nullable LayoutInflater.Factory2 custom) {
        mActivity = activity;
        mCustom = custom;
        if (!sDependAppCompat) {
            try {
                AppCompatActivity.class.getName();
                sDependAppCompat = true;
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        View view = null;
        if (mCustom != null) {
            view = mCustom.onCreateView(parent, name, context, attrs);
        }
        if (view == null) {
            if (sDependAppCompat && mActivity instanceof AppCompatActivity) {
                view = ((AppCompatActivity) mActivity).getDelegate().createView(parent, name, context, attrs);
                if (view == null && name.startsWith("androidx"))
                    return null;
            }
            if (view == null) {
                view = mActivity.onCreateView(parent, name, context, attrs);
            }

            if (view == null) {
                LayoutInflater layoutInflater = mActivity.getLayoutInflater();
                if (-1 == name.indexOf('.')) {
                    for (String prefix : sClassPrefixList) {
                        try {
                            view = layoutInflater.createView(name, prefix, attrs);
                            if (view != null) {
                                break;
                            }
                        } catch (Throwable ignored) {
                        }
                    }
                } else {
                    try {
                        view = layoutInflater.createView(name, null, attrs);
                    } catch (ClassNotFoundException ignored) {
                    }
                }
            }
        }

        if (view != null && !(view instanceof EffectLayoutTemplate)) {
            //EffectLayout内部会自己处理effect
            tryInjectEffect(view, context, attrs);
        }
        return view;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
        return onCreateView(null, name, context, attrs);
    }

    private void tryInjectEffect(@NonNull View view, @NonNull Context context, @NonNull AttributeSet attrs) {
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.EffectDrawable, R.attr.effectDrawableStyle, Effects.defaultEffectDrawableStyleRes);
        if (ta.getBoolean(R.styleable.EffectDrawable_ed_inject, false)) {
            Effects.Builder<?, ?> builder = Effects.withAttrs(ta);
            int pl = view.getPaddingLeft();
            int pt = view.getPaddingTop();
            int pr = view.getPaddingRight();
            int pb = view.getPaddingBottom();
            if (view.isFocusable()) {
                view.setBackground(builder.buildFocusStateListDrawable());
            } else {
                view.setBackground(builder.buildDrawable());
            }
            view.setPadding(pl, pt, pr, pb);
        }
        ta.recycle();
    }
}
