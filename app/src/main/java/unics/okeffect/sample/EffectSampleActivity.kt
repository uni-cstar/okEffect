package unics.okeffect.sample

import android.graphics.Color
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
        Effects.applyInjectFactory2(this, null)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_effect_drawable_layout)

        window.decorView.viewTreeObserver.addOnGlobalFocusChangeListener { oldFocus, newFocus ->
            if (oldFocus != null) {
                oldFocus.animate().cancel()
                oldFocus.animate().scaleX(1f).scaleY(1f).setDuration(300).start()
            }

            if (newFocus != null) {
                println("width=${newFocus.width} height=${newFocus.height}")
                newFocus.animate().cancel()
                newFocus.animate().scaleX(1.2f).scaleY(1.2f).setDuration(300).start()
            }
        }

        Effects.withAttrs(this, null).into(findViewById<View>(R.id.clickLayout).also {
            it.isClickable = true
        })

        val layout1 = findViewById<View>(R.id.layout1)
        val layout12 = findViewById<View>(R.id.layout12)

        //阴影 + viewPadding
        Effects.withDraw().setShadow(20f, Color.RED).setContentCap(3f).into(layout1)
        //阴影
        Effects.withNinePatch(this, R.drawable.bg_shadow).into(layout12)
    }
}