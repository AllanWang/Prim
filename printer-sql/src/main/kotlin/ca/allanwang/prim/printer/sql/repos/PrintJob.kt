package ca.allanwang.prim.printer.sql.repos

import ca.allanwang.prim.models.PrintJob
import ca.allanwang.prim.printer.sql.FLAG_SIZE
import ca.allanwang.prim.printer.sql.ID_SIZE
import ca.allanwang.prim.printer.sql.USER_SIZE
import ca.allanwang.prim.printer.sql.newId
import org.jetbrains.exposed.sql.Table
import org.joda.time.DateTime

object PrintJobTable : Table("print_job") {
    val id = varchar("id", ID_SIZE).primaryKey().clientDefault(::newId)
    val flag = varchar("flag", FLAG_SIZE).default(PrintJob.CREATED)
    val user = varchar("user", USER_SIZE).uniqueIndex()
    val pageCount = integer("page_count").default(0)
    val colorPageCount = integer("color_page_count").default(0)
    val createdAt = datetime("created_at").clientDefault(DateTime::now)
    val processedAt = datetime("processed_at").nullable()
    val finishedAt = datetime("finished_at").nullable()
    val errorFlag = varchar("error_flag", FLAG_SIZE).nullable()
}