package unics.okeffect.sample

import android.graphics.Color
import android.graphics.drawable.NinePatchDrawable
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import unics.okeffect.Effects

/**
 * Create by luochao
 * on 2023/11/2
 */
class EffectSampleActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        Effects.applyInjectFactory2(this,null);
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_effect_drawable_layout)

        window.decorView.viewTreeObserver.addOnGlobalFocusChangeListener { oldFocus, newFocus ->
            if (oldFocus != null) {
                oldFocus.animate().cancel()
                oldFocus.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            }

            if (newFocus != null) {
//                newFocus.bringToFront()
                println("width=${newFocus.width} height=${newFocus.height}")
                newFocus.animate().cancel()
                newFocus.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).start()
            }
        }
        findViewById<View>(R.id.c0).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        findViewById<View>(R.id.c1).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        findViewById<View>(R.id.c2).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        findViewById<View>(R.id.v_62).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        findViewById<View>(R.id.v6_1).setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        val view1 = findViewById<View>(R.id.view1)
        val view3 = findViewById<View>(R.id.view3)
        val view4 = findViewById<View>(R.id.view4)
        val view5 = findViewById<View>(R.id.view5)
        val view6 = findViewById<View>(R.id.view6)
//        view1.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view3.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view4.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view5.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view6.setLayerType(View.LAYER_TYPE_SOFTWARE, null)


        //.9 view设置了padding
        view1.background =
            Effects.withNinePath(this, R.drawable.bg_shadow).buildFocusSelectorDrawable()
        view1.setPadding(20, 20, 20, 20)

        //.9
        view3.background =
            Effects.withNinePath(this, R.drawable.bg_shadow).buildFocusSelectorDrawable()

        //.9 + stroke
        view4.background =
            Effects.withNinePath(resources.getDrawable(R.drawable.bg_shadow) as NinePatchDrawable)
                .setStroke(20f, Color.RED).buildFocusSelectorDrawable()

        //.9 + stroke + contentGap
        view5.background = Effects.withNinePath(this, R.drawable.bg_shadow)
            .setStroke(10f, Color.YELLOW).setContentCap(10f).buildFocusSelectorDrawable()

        //.9 + stroke(圆角) + contentGap
        view6.background = Effects.withNinePath(this, R.drawable.bg_shadow)
            .setStroke(10f, Color.BLUE)
            .setContentCap(10f)
            .setCornerRadii(floatArrayOf(20f, 20f, 0f, 0f, 20f, 20f, 0f, 0f))
            .buildFocusSelectorDrawable()
        val view11 = findViewById<View>(R.id.view11)
        val view33 = findViewById<View>(R.id.view33)
        val view44 = findViewById<View>(R.id.view44)
        val view55 = findViewById<View>(R.id.view55)
        val view66 = findViewById<View>(R.id.view66)
//        view11.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view33.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view44.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view55.setLayerType(View.LAYER_TYPE_SOFTWARE, null)
//        view66.setLayerType(View.LAYER_TYPE_SOFTWARE, null)

        //阴影 + viewPadding
        view11.background = Effects.withDraw().setShadow(20f, Color.RED).buildFocusSelectorDrawable()
        view11.setPadding(20, 20, 20, 20)
        //阴影
        view33.background =
            Effects.withDraw().setStroke(10f, Color.GREEN).buildFocusSelectorDrawable()
        //边框 + contentGap
        view44.background =
            Effects.withDraw().setStroke(10f, Color.CYAN).setCornerRadius(20f).setContentCap(10f)
                .buildFocusSelectorDrawable()
        //阴影 + 边框
        view55.background = Effects.withDraw().setShadow(20f, 20f, 20f, 40f, Color.BLACK)
            .setCornerRadius(10f)
            .setContentCap(30f)
            .setStroke(20f, Color.RED).buildFocusSelectorDrawable()

        //异性圆角 + 阴影 + 边框 + contentGap
        view66.background = Effects.withDraw()
            .setShadow(20f, Color.CYAN)
            .setCornerRadii(floatArrayOf(20f, 20f, 0f, 0f, 20f, 20f, 0f, 0f))
            .setStroke(10f, Color.MAGENTA)
            .setContentCap(10f)
            .buildFocusSelectorDrawable()

    }
}