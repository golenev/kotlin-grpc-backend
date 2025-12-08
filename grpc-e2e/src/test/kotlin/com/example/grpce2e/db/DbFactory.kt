package com.example.grpce2e.db

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Transaction
import org.jetbrains.exposed.sql.transactions.transaction

enum class DbType {
    ANALYTICS,
    ORDERS,
}

object DbFactory {

    private val databases by lazy { mutableMapOf<DbType, Database>() }

    fun <T> transaction(dbType: DbType, statement: Transaction.() -> T): T {
        val database = databases.getOrPut(dbType) {
            val (url, user, password) = when (dbType) {
                DbType.ANALYTICS -> Triple(
                    Environment.ANALYTICS_DB_URL,
                    Environment.ANALYTICS_DB_USER,
                    Environment.ANALYTICS_DB_PASSWORD,
                )
                DbType.ORDERS -> Triple(
                    Environment.ORDER_DB_URL,
                    Environment.ORDER_DB_USER,
                    Environment.ORDER_DB_PASSWORD,
                )
            }
            Database.connect(
                url = url,
                driver = Environment.DB_DRIVER,
                user = user,
                password = password,
            )
        }
        return transaction(db = database) {
            statement()
        }
    }
}

fun <T> dbAnalyticsExec(block: Transaction.() -> T): T = DbFactory.transaction(DbType.ANALYTICS, block)
fun <T> dbOrdersExec(block: Transaction.() -> T): T = DbFactory.transaction(DbType.ORDERS, block)
