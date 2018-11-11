package ca.allanwang.prim.printer.sql

import ca.allanwang.prim.printer.PrinterGroupRepository
import ca.allanwang.prim.printer.PrinterRepository
import ca.allanwang.prim.printer.SessionRepository
import ca.allanwang.prim.printer.sql.repos.PrinterGroupRepositorySql
import ca.allanwang.prim.printer.sql.repos.PrinterRepositorySql
import ca.allanwang.prim.printer.sql.repos.SessionRepositorySql
import org.koin.dsl.module.module
import java.math.BigInteger
import java.security.SecureRandom

/**
 * Exposed koin injection for printer repositories
 */
val sqlRepositoryModule = module {
    single<SessionRepository> { SessionRepositorySql }
    single<PrinterRepository> { PrinterRepositorySql }
    single<PrinterGroupRepository> { PrinterGroupRepositorySql }
}

private val random = SecureRandom()
internal fun newId() = BigInteger(130, random).toString(32)

internal const val ID_SIZE = 255
internal const val FLAG_SIZE = 64
internal const val NAME_SIZE = 64
internal const val USER_SIZE = 64
internal const val MESSAGE_SIZE = 255