package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.printer.repos.SessionTestBase
import ca.allanwang.prim.printer.sql.SqlExtension
import ca.allanwang.prim.printer.sql.sqlRepositoryModule
import org.junit.jupiter.api.extension.ExtendWith

class SessionExtension : SqlExtension(
        tables = listOf(SessionTable),
        modules = listOf(sqlRepositoryModule))

@ExtendWith(SessionExtension::class)
class SessionTest : SessionTestBase()