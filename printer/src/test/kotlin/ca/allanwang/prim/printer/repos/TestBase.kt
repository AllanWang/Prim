package ca.allanwang.prim.printer.repos

import ca.allanwang.prim.models.IdModel
import ca.allanwang.prim.printer.Repository
import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.test.KoinTest
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@ExtendWith(KoinTestExtension::class)
abstract class TestBase : KoinTest

class KoinTestExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        println("Stop koin")
        stopKoin()
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