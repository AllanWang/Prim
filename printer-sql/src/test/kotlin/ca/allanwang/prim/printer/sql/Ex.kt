package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.printer.Test2
import org.junit.After
import org.junit.Before


class Ex : Test2() {

    @Before
    fun t() {
        println("Before")
    }

    @After
    fun t2() {
        println("after")
    }

}