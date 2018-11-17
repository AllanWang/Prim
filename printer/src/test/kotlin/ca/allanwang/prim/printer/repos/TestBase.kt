package ca.allanwang.prim.printer.repos

import org.junit.jupiter.api.extension.AfterEachCallback
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.api.extension.ExtensionContext
import org.koin.standalone.StandAloneContext.stopKoin
import org.koin.test.KoinTest

@ExtendWith(KoinTestExtension::class)
abstract class TestBase : KoinTest

class KoinTestExtension : AfterEachCallback {
    override fun afterEach(context: ExtensionContext) {
        println("Stop koin")
        stopKoin()
    }
}