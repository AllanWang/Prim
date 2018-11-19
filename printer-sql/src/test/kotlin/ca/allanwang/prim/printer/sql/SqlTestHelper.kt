package ca.allanwang.prim.printer.sql

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.transactions.DEFAULT_ISOLATION_LEVEL
import org.jetbrains.exposed.sql.transactions.ThreadLocalTransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.extension.AfterTestExecutionCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.dsl.module.Module
import org.koin.standalone.StandAloneContext.startKoin

/**
 * Extension that binds additional koin modules and sql tables for each test.
 * All sql tables are dropped with each test.
 */
open class SqlExtension(tables: List<Table>,
                        private val modules: List<Module>,
                        private val debug: Boolean = false)
    : BeforeEachCallback, BeforeTestExecutionCallback, AfterTestExecutionCallback {

    private val tables = tables.toTypedArray()

    override fun beforeEach(context: ExtensionContext) {
        startKoin(modules)
    }

    override fun beforeTestExecution(context: ExtensionContext) {
        Database.connect("jdbc:h2:mem:test;MODE=MySQL;DB_CLOSE_DELAY=-1", "org.h2.Driver", "test", "test") {
            ThreadLocalTransactionManager(it, DEFAULT_ISOLATION_LEVEL, 1)
        }
        transaction {
            debug = this@SqlExtension.debug
            SchemaUtils.create(*tables)
        }
    }

    override fun afterTestExecution(context: ExtensionContext) {
        transaction {
            SchemaUtils.drop(*tables)
        }
    }

}