package org.example.component.column

class MoneyColumn(name: String) : Column(name) {
    override val type = ColumnType.MONEY.name
    override fun validate(data: String): Boolean {
        val amount = data.replace(",", "").toDoubleOrNull() ?: return false
        return amount in 0.0..10_000_000_000_000.00 && data.split("\\.").let { it.size == 2 && it[1].length == 2 }
    }

    companion object {
        fun toDouble(value: String): Double {
            return value.replace(",", "").toDoubleOrNull() ?: 0.0
        }
    }
}

