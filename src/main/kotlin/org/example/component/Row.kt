package org.example.component

import java.io.Serializable

class Row : Serializable {
    var values = mutableListOf<String>()

    fun getAt(index: Int): String = values[index]
    fun setAt(index: Int, content: String) {
        values[index] = content
    }
}
