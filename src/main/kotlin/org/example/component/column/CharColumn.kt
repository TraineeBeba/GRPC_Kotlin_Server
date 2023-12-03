package org.example.component.column

class CharColumn(name: String) : Column(name) {
    override val type = ColumnType.CHAR.name
    override fun validate(data: String) = data.length <= 1
}


