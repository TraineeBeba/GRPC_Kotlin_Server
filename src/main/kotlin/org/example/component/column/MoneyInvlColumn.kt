package org.example.component.column

class MoneyInvlColumn(name: String, val min: String, val max: String) : Column(name) {
    override val type = ColumnType.MONEY_INVL.name
    override fun validate(data: String): Boolean {
        val amount = data.replace(",", "").toDoubleOrNull() ?: return false
        val minAmount = min.replace(",", "").toDoubleOrNull() ?: return false
        val maxAmount = max.replace(",", "").toDoubleOrNull() ?: return false
        return amount in minAmount..maxAmount && data.split("\\.").let { it.size == 2 && it[1].length == 2 }
    }
}

