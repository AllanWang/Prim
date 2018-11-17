package ca.allanwang.prim.printer.sql.repos

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import kotlin.test.Test

class Basic {

    @Test
    fun basic() {
        val db = Database.connect("jdbc:h2:mem:test;MODE=MySQL", "org.h2.Driver", "test", "test") {
            ThreadLocalTransactionManager(it, DEFAULT_ISOLATION_LEVEL, 1)
        }
        transaction(db = db) {
            SchemaUtils.create(SessionTable)
            commit()
        }
        transaction(db = db) {
            println("Test")
            println(SessionTable.selectAll().count())
            commit()
        }
        transaction(db = db) {
            SchemaUtils.drop(SessionTable)
            commit()
        }
    }
}