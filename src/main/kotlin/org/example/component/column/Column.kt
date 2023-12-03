package org.example.component.column

import java.io.Serializable

abstract class Column(val name: String) : Serializable {
    abstract val type: String
    abstract fun validate(data: String): Boolean
}
