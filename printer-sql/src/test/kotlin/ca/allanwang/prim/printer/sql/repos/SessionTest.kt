package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.printer.repos.SessionTestBase
import ca.allanwang.prim.printer.sql.TableExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.koin.standalone.StandAloneContext.startKoin

class SessionExtension : TableExtension(
        tables = listOf(SessionTable),
        modules = listOf(sqlRepositoryModule))

@ExtendWith(SessionExtension::class)
class SessionTest : SessionTestBase()