package ca.allanwang.prim.printer.sql

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun withTables(vararg tables: Table, debug: Boolean = false, statement: Transaction.() -> Unit) {
    val connection = Database.connect("jdbc:h2:mem:test;MODE=MySQL", "org.h2.Driver", "test", "test")
    transaction(connection.connector().metaData.defaultTransactionIsolation, repetitionAttempts = 1) {
        if (debug)
            addLogger(Slf4jSqlDebugLogger)
        SchemaUtils.create(*tables)
        try {
            statement()
            commit()
        } finally {
            SchemaUtils.drop(*tables)
            commit()
        }
    }
}
