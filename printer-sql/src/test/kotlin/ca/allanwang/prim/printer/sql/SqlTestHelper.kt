package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.models.IdModel
import ca.allanwang.prim.printer.Repository
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
import kotlin.test.assertEquals
import kotlin.test.assertTrue

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

/**
 * Checks that list retrieval from db is properly sorted.
 * [comparator] is used to define the expected list order.
 * [getter] is used to retrieve the list from the db.
 * [addData] is used to add the items to the repo. It also returns a list,
 * which is our expected content. This list does not have to be sorted.
 * For default id check, use [assertListSortedById].
 */
fun <ID : Comparable<ID>,
        M : IdModel<ID>,
        R : Repository<ID, M>> assertListSorted(repo: R,
                                                comparator: Comparator<in M>,
                                                getter: R.() -> List<M>,
                                                addData: R.() -> List<M>) {
    val expectedContent = repo.addData()
    val list = repo.getter()
    assertTrue(list.size > 1, "Sort for ${repo::class.simpleName} checker has less than two items")
    assertEquals(expectedContent.sortedWith(comparator), list,
            "List of ${repo::class.simpleName} not sorted properly")
}

fun <ID : Comparable<ID>,
        M : IdModel<ID>,
        R : Repository<ID, M>> assertListSortedById(repo: R, creator: (Int) -> M) {
    assertListSorted(repo, compareBy { it.id }, { getList() }) {
        listOf(6, 3, 7, 4, 78, 1).map(creator)
    }
}