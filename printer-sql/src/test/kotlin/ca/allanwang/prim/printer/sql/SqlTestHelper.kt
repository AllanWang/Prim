package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.printer.sql.repos.SessionTable
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.dsl.module.Module
import org.koin.standalone.StandAloneContext.startKoin

open class TableExtension(tables: List<Table>,
                          private val modules: List<Module>,
                          private val debug: Boolean = false)
    : BeforeEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private val tables = tables.toTypedArray()

    override fun beforeEach(context: ExtensionContext?) {
        println("BeforeAA")
        startKoin(modules)
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        println("BeforeEx")
        Database.connect("jdbc:h2:mem:test;MODE=MySQL", "org.h2.Driver", "test", "test")
        transaction {
            debug = this@TableExtension.debug
            SchemaUtils.create(*tables)
            println("Initialized 1 ${SessionTable.selectAll().count()}")
        }
        transaction {
            println("Test 2")
            println("Initialized 2 ${SessionTable.selectAll().count()}")
        }
    }

    override fun afterTestExecution(context: ExtensionContext) = transaction {
        println("After")
        SchemaUtils.drop(*tables)
    }

}