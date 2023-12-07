package unics.okeffect.sample

import android.graphics.RectF
import android.os.Bundle
import android.text.style.ForegroundColorSpan
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.imageview.ShapeableImageView
import unics.okeffect.Effects

/**
 * Create by luochao
 * on 2023/12/5
 */
class EffectLayoutSampleActivity : AppCompatActivity() {

//    val spanCache = mutableListOf<ForegroundColorSpan>()
    override fun onCreate(savedInstanceState: Bundle?) {
//        Effects.applyInjectFactory2(this, null)
        super.onCreate(savedInstanceState)
        Effects.setAutoOptStrokeCorner(false)
        setContentView(R.layout.activity_effectlayout_sample)
        val rectF = RectF()
        findViewById<ShapeableImageView>(R.id.effectImage2).shapeAppearanceModel.apply {
            Log.i(
                "EffectCorner", "topLeftCornerSize= ${
                    this.topLeftCornerSize.getCornerSize(
                        rectF
                    )
                }  topRightCornerSize= ${this.topRightCornerSize.getCornerSize(rectF)} bottomRightCornerSize=${
                    this.bottomRightCornerSize.getCornerSize(
                        rectF
                    )
                } bottomLeftCornerSize=${this.bottomLeftCornerSize.getCornerSize(rectF)}"
            )
        }
//    findViewById<View>(R.id.root).setLayerType(View.LAYER_TYPE_SOFTWARE,null)
//    findViewById<View>(R.id.effectGroup2).setLayerType(View.LAYER_TYPE_SOFTWARE,null)
//    findViewById<View>(R.id.effectLayout2).setLayerType(View.LAYER_TYPE_SOFTWARE,null)

//        val texts = listOf<String>(
//            "当阳光洒在小村庄的土地上，勾勒出金黄的麦浪，仿佛是大地的诗篇",
//            "小溪潺潺流淌，伴随着鸟儿的歌唱，构成了这个宁静而美好的角落。",
//            "在这个小村庄里，生活的节奏仿佛放慢了时间的脚步。清晨，农夫们挎着锄头，走进田野，迎着朝霞劳作。他们辛勤耕耘，播种希望，期待着金黄的麦穗在夏日的阳光中摇曳生姿。",
//            "小学的教室里，孩子们认真聆听老师的讲解",
//            "他们眼中闪烁着未来的憧憬，每一次的知识积累都是心灵成长的阶梯。在这个村庄的学堂里，知识的种子在老师的耕耘下生根发芽，茁壮成长。"
//        )
//        val textView = findViewById<TextView>(R.id.text)
//        thread {
//            while (!this.isFinishing) {
//                val prv = textView.text
//                if (prv != null && prv is Spanned) {
//                    val cacheSpans =
//                        prv.getSpans(0, prv.length - 1, ForegroundColorSpan::class.java)
//                    if (!cacheSpans.isNullOrEmpty()) {
//                        spanCache.addAll(cacheSpans)
//                        Log.i(
//                            "SpanCache",
//                            "前一个字符串使用了${cacheSpans.size}个Span，保存到缓存列表，缓存列表当前大小${spanCache.size} "
//                        )
//                    } else {
//                        Log.i(
//                            "SpanCache",
//                            "前一个字符串未使用缓存 ${prv}"
//                        )
//                    }
//                }else{
//                    Log.i(
//                        "SpanCache",
//                        "前一个字符串为空或未使用缓存 ${prv}"
//                    )
//                }
//                val textIndex = Random.nextInt(0, texts.size)
//                val text = texts[textIndex]
//                val randomCnt = Random.nextInt(0, text.length)
//                val style = SpannableStringBuilder(text)
//                for (i in 0..randomCnt) {
//                    val span = if (spanCache.size > 0) {
//                        val index = spanCache.size - 1
//                        val cacheSpan = spanCache[index]
//                        spanCache.removeAt(index)
//                        Log.i(
//                            "SpanCache",
//                            "从SpanCache中获取1个，缓存列表当前大小${spanCache.size} "
//                        )
//                        cacheSpan
//                    } else {
//                        Log.i("SpanCache", "新建Span")
//                        ForegroundColorSpan(Color.RED)
//                    }
//                    style.setSpan(span, i, i + 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
//                }
//                runOnUiThread {
//
//                    textView.text = style
//                }
//                Thread.sleep(10000)
//            }
//        }

    }

    override fun onStart() {
        super.onStart()
        Log.i("Lifecycle", "onStart: ")
    }

    override fun onResume() {
        super.onResume()
        Log.i("Lifecycle", "onResume: ")
    }

    override fun onPause() {
        super.onPause()
        Log.i("Lifecycle", "onPause: ")
    }

    override fun onStop() {
        super.onStop()
        Log.i("Lifecycle", "onStop: ")
    }
}

class MyF(color: Int) : ForegroundColorSpan(color) {

    override fun hashCode(): Int {
        return System.currentTimeMillis().toInt()
    }

    override fun equals(other: Any?): Boolean {
        return false
    }
}
