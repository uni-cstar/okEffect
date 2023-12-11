package unics.okeffect.sample

import android.content.Intent
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


//        val layout1 = findViewById<View>(R.id.layout1)
//        layout1.setOnClickListener {
//            startActivity(Intent(this, EffectLayoutSampleActivity::class.java))
//        }
//        val layout12 = findViewById<View>(R.id.layout12)
//
//        //阴影 + viewPadding
//        Effects.withDraw().setShadow(20f, Color.RED).into(layout1)
//        //阴影
//        Effects.withNinePath(this, R.drawable.bg_shadow).into(layout12)
    }
}