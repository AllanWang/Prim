package ca.allanwang.prim.printer

import org.junit.Test
import kotlin.test.fail

abstract class Test2 {

    @Test
    fun test() {
        println("a")
        fail("NO")
    }
}