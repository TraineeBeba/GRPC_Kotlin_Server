package org.example

import org.example.component.*
import org.example.component.Row
import org.example.component.column.*
import org.example.component.column.ColumnType
import org.example.component.column.Column


object DatabaseManager {
    var database: Database? = null

    fun getInstance(): DatabaseManager = this

    fun populateTable() {
        val table = Table("testTable")
        table.apply {
            addColumn(IntegerColumn("column1"))
            addColumn(RealColumn("column2"))
            addColumn(StringColumn("column3"))
            addColumn(CharColumn("column4"))
            addColumn(MoneyColumn("column5"))
            addColumn(MoneyInvlColumn("column6", "0", "1000"))
        }

        val row1 = Row().apply {
            values.addAll(listOf("10", "10.0", "10", "1", "10.00", "10.00"))
        }
        table.addRow(row1)

        val row2 = Row().apply {
            values.addAll(listOf("15", "15.0", "15", "3", "15.00", "15.00"))
        }
        table.addRow(row2)

        database?.addTable(table)
    }

    fun createDB(name: String) {
        database = Database(name)
    }

    fun addTable(name: String?): Boolean {
        return if (!name.isNullOrEmpty()) {
            val table = Table(name)
            database?.addTable(table)
            true
        } else false
    }

    fun deleteTable(tableIndex: Int): Boolean {
        return if (tableIndex != -1) {
            database?.deleteTable(tableIndex)
            true
        } else false
    }

    fun addColumn(tableIndex: Int, columnName: String?, columnType: ColumnType, min: String = "", max: String = ""): Boolean {
        if (columnName.isNullOrEmpty() || tableIndex == -1) return false

        val column: Column = when (columnType) {
            ColumnType.INT -> IntegerColumn(columnName)
            ColumnType.REAL -> RealColumn(columnName)
            ColumnType.STRING -> StringColumn(columnName)
            ColumnType.CHAR -> CharColumn(columnName)
            ColumnType.MONEY -> MoneyColumn(columnName)
            ColumnType.MONEY_INVL -> MoneyInvlColumn(columnName, min, max)
        }

        database?.tables?.get(tableIndex)?.addColumn(column)
        database?.tables?.get(tableIndex)?.rows?.forEach { row ->
            row.values.add("")
        }

        return true
    }


    fun changeColumnType(tableIndex: Int, columnIndex: Int, columnType: ColumnType, min: String = "", max: String = ""): Boolean {
        if (tableIndex == -1 || columnIndex == -1) return false

        val newColumn: Column = when (columnType) {
            ColumnType.INT -> IntegerColumn(database!!.tables[tableIndex].columns[columnIndex].name)
            ColumnType.REAL -> RealColumn(database!!.tables[tableIndex].columns[columnIndex].name)
            ColumnType.STRING -> StringColumn(database!!.tables[tableIndex].columns[columnIndex].name)
            ColumnType.CHAR -> CharColumn(database!!.tables[tableIndex].columns[columnIndex].name)
            ColumnType.MONEY -> MoneyColumn(database!!.tables[tableIndex].columns[columnIndex].name)
            ColumnType.MONEY_INVL -> MoneyInvlColumn(database!!.tables[tableIndex].columns[columnIndex].name, min, max)
        }

        database!!.tables[tableIndex].columns[columnIndex] = newColumn
        return true
    }

    fun deleteColumn(tableIndex: Int, columnIndex: Int): Boolean {
        return if (columnIndex != -1) {
            database?.tables?.get(tableIndex)?.deleteColumn(columnIndex)
            true
        } else false
    }

    fun addRow(tableIndex: Int, row: Row): Boolean {
        if (tableIndex != -1) {
            val table = database?.tables?.get(tableIndex)
            table?.columns?.forEach { _ ->
                row.values.add("")
            }
            table?.addRow(row)
            return true
        } else {
            return false
        }
    }

    fun deleteRow(tableIndex: Int, rowIndex: Int): Boolean {
        return if (rowIndex != -1) {
            database?.tables?.get(tableIndex)?.deleteRow(rowIndex)
            true
        } else false
    }

    fun updateCellValue(value: String, tableIndex: Int, columnIndex: Int, rowIndex: Int): Boolean {
        val table = database?.tables?.get(tableIndex)
        val column = table?.columns?.get(columnIndex)
        return if (column?.validate(value) == true) {
            val row = table.rows[rowIndex]
            row.setAt(columnIndex, value.trim())
            true
        } else false
    }

    private fun evaluateCondition(columnValue: String?, operator: String, inputValue: String, column: Column): Boolean {
        if (columnValue.isNullOrEmpty()) return false

        return when (ColumnType.valueOf(column.type)) {
            ColumnType.INT -> {
                val columnIntValue = columnValue.toInt()
                val inputIntValue = inputValue.toInt()
                compareIntegers(columnIntValue, inputIntValue, operator)
            }
            ColumnType.REAL -> {
                val columnDoubleValue = columnValue.toDouble()
                val inputDoubleValue = inputValue.toDouble()
                compareDoubles(columnDoubleValue, inputDoubleValue, operator)
            }
            ColumnType.STRING -> compareStrings(columnValue, inputValue, operator)
            ColumnType.CHAR -> compareChars(columnValue, inputValue, operator)
            ColumnType.MONEY, ColumnType.MONEY_INVL -> {
                val columnMoneyValue = MoneyColumn.toDouble(columnValue)
                val inputMoneyValue = MoneyColumn.toDouble(inputValue)
                compareDoubles(columnMoneyValue, inputMoneyValue, operator)
            }
        }
    }

    private fun compareIntegers(columnValue: Int, inputValue: Int, operator: String): Boolean {
        return when (operator) {
            ">" -> columnValue > inputValue
            "<" -> columnValue < inputValue
            ">=" -> columnValue >= inputValue
            "<=" -> columnValue <= inputValue
            "==" -> columnValue == inputValue
            else -> false
        }
    }

    private fun compareDoubles(columnValue: Double, inputValue: Double, operator: String): Boolean {
        return when (operator) {
            ">" -> columnValue > inputValue
            "<" -> columnValue < inputValue
            ">=" -> columnValue >= inputValue
            "<=" -> columnValue <= inputValue
            "==" -> columnValue == inputValue
            else -> false
        }
    }

    private fun compareStrings(columnValue: String, inputValue: String, operator: String): Boolean {
        return when (operator) {
            "==" -> columnValue == inputValue
            "!=" -> columnValue != inputValue
            ">" -> columnValue > inputValue
            "<" -> columnValue < inputValue
            ">=" -> columnValue >= inputValue
            "<=" -> columnValue <= inputValue
            else -> false
        }
    }

    private fun compareChars(columnValue: String, inputValue: String, operator: String): Boolean {
        if (columnValue.length != 1 || inputValue.length != 1) return false

        val columnChar = columnValue[0]
        val inputChar = inputValue[0]

        return when (operator) {
            "==" -> columnChar == inputChar
            "!=" -> columnChar != inputChar
            ">" -> columnChar > inputChar
            "<" -> columnChar < inputChar
            ">=" -> columnChar >= inputChar
            "<=" -> columnChar <= inputChar
            else -> false
        }
    }

    fun deleteDuplicateRows(tableIndex: Int): Boolean {
        var i = 0
        while (i < database?.tables?.get(tableIndex)?.rows?.size ?: 0) {
            var flag = true
            for (j in (i + 1) until (database!!.tables[tableIndex].rows.size)) {
                if (database!!.tables[tableIndex].rows[i].values == database!!.tables[tableIndex].rows[j].values) {
                    deleteRow(tableIndex, i)
                    flag = false
                    break
                }
            }
            if (flag) i++
        }
        return true
    }

}
