package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.sql.repos.PrinterRepositorySql
import ca.allanwang.prim.printer.sql.repos.SessionRepositorySql
import org.koin.dsl.module.module
import java.math.BigInteger
import java.security.SecureRandom

/**
 * Exposed koin injection for printer repositories
 */
val sqlRepositoryModule = module {
    single<SessionRepository> { SessionRepositorySql() }
    single<PrinterRepository> { PrinterRepositorySql() }
}

private val random = SecureRandom()
internal fun newId() = BigInteger(130, random).toString(32)