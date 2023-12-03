package org.example.component

import org.example.component.column.*

class Table(val name: String) {
    val rows = mutableListOf<Row>()
    val columns = mutableListOf<Column>()

    constructor(table: Table) : this(table.name) {
        table.rows.forEach { row ->
            val newRow = Row()
            newRow.values.addAll(row.values)
            rows.add(newRow)
        }

        table.columns.forEach { column ->
            val newColumn = when (ColumnType.valueOf(column.type)) {
                ColumnType.INT -> IntegerColumn(column.name)
                ColumnType.REAL -> RealColumn(column.name)
                ColumnType.STRING -> StringColumn(column.name)
                ColumnType.CHAR -> CharColumn(column.name)
                ColumnType.MONEY -> MoneyColumn(column.name)
                ColumnType.MONEY_INVL -> MoneyInvlColumn(column.name, (column as MoneyInvlColumn).min, column.max)
            }
            columns.add(newColumn)
        }
    }

    fun addRow(row: Row) {
        rows.add(row)
    }

    fun deleteRow(rowIndex: Int) {
        rows.removeAt(rowIndex)
    }

    fun deleteColumn(columnIndex: Int) {
        columns.removeAt(columnIndex)
        rows.forEach { row ->
            row.values.removeAt(columnIndex)
        }
    }

    fun addColumn(column: Column) {
        columns.add(column)
    }
}
