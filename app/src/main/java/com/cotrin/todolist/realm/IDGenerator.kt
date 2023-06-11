package com.cotrin.todolist.realm

object IDGenerator {
    private var id: Int = -1

    @Synchronized
    fun generateID(): Int {
        id++
        return id
    }

    fun setStartValue(value: Int) {
        id = value
    }
}