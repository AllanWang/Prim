package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.ktor.models.PrintJob
import ca.allanwang.prim.printer.sql.newId
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime


object PrintJobTable : Table() {
    val id = varchar("id", 255).primaryKey().clientDefault(::newId)
    val flag = varchar("flag", 16).default(PrintJob.CREATED)
    val user = varchar("user", 64).uniqueIndex()
    val pageCount = integer("page_count").default(0)
    val colorPageCount = integer("color_page_count").default(0)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val processedAt = datetime("processed_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val errorFlag = varchar("error_flag", 16).nullable()
}