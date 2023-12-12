package unics.okeffect.annotation;

import android.view.ViewGroup;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.CLASS)
@Target({ElementType.TYPE})
public @interface EffectLayout {
    Class<? extends ViewGroup>[] value();

    /**
     * 生成的类前缀
     *
     * @return
     */
    String classPrefix() default "Effect";

    /**
     * 生成的类后缀
     *
     * @return
     */
    String classSuffix() default "";

}