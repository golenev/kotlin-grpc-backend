package com.example.grpce2e.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.transactions.transaction

enum class DbType {
    ANALYTICS
}

object DbFactory {

    private val databases by lazy { mutableMapOf<DbType, Database>() }

    fun <T> transaction(dbType: DbType, statement: Transaction.() -> T): T {
        val database = databases.getOrPut(dbType) {
            Database.connect(
                url = Environment.DB_URL,
                driver = Environment.DB_DRIVER,
                user = Environment.DB_USER,
                password = Environment.DB_PASSWORD,
            )
        }
        return transaction(db = database) {
            addLogger(StdOutSqlLogger)
            statement()
        }
    }
}

fun <T> dbAnalyticsExec(block: Transaction.() -> T): T = DbFactory.transaction(DbType.ANALYTICS, block)
