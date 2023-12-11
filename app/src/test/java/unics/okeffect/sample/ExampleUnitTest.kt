package unics.okeffect.sample

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.concurrent.CopyOnWriteArraySet
import kotlin.concurrent.thread

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun test() {
        try {
            val list = (0..100).map {
                it.toString()
            }.toMutableSet()

            val list2 = CopyOnWriteArraySet<String>(list)
            for (item in list2) {
//                val item22 = item
                thread {
                    list2.remove(item)
                }
//                val it = list2.iterator()
//                while (it.hasNext()) {
//                    val next = it.next()
//                    if (next.contains("5")) {
//                        list2.remove(next)
//                    }
//                }
            }
            Thread.sleep(2000)
            println("$list2")

        } catch (e: Exception) {
            e.printStackTrace()
            println("error $e")
        }

    }

    @Test
    fun test2() {
        try {
            val list = (0..100).map {
                it.toString()
            }.toMutableList()
            val ite = list.iterator()
            while (ite.hasNext()) {
                val next = ite.next()
                val ite2 = list.iterator()
                while (ite2.hasNext()){
                    val next2 = ite2.next()
                    if(next2 == next && next2.contains("5")){
                        ite2.remove()
                    }
                }
            }

            println("$list")

        } catch (e: Exception) {
            e.printStackTrace()
            println("error $e")
        }

    }
}