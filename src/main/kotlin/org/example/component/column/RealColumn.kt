package org.example.component.column

class RealColumn(name: String) : Column(name) {
    override val type = ColumnType.REAL.name
    override fun validate(data: String) = data.toDoubleOrNull() != null
}

