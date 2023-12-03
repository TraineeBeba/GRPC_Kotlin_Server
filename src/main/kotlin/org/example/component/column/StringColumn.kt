package org.example.component.column

class StringColumn(name: String) : Column(name) {
    override val type = ColumnType.STRING.name
    override fun validate(data: String) = true
}