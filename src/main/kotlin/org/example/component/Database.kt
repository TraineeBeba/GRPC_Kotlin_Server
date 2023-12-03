package org.example.component

import java.io.Serializable

class Database(val name: String) : Serializable {
    val tables = mutableListOf<Table>()

    fun addTable(table: Table) {
        tables.add(table)
    }

    fun deleteTable(index: Int) {
        tables.removeAt(index)
    }
}
