package org.example.component.column

class IntegerColumn(name: String) : Column(name) {
    override val type = ColumnType.INT.name
    override fun validate(data: String) = data.toIntOrNull() != null
}

