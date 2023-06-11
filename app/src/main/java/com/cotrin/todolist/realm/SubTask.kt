package com.cotrin.todolist.realm

import io.realm.kotlin.types.EmbeddedRealmObject
import io.realm.kotlin.types.RealmUUID

class SubTask(): EmbeddedRealmObject {
    var name: String = ""
    var isFinished: Boolean = false
    var uuid: RealmUUID = RealmUUID.random()

    constructor(
        name: String = "",
        isFinished: Boolean = false,
        uuid: RealmUUID = RealmUUID.random()
    ): this() {
        this.name = name
        this.isFinished = isFinished
        this.uuid = uuid
    }

    fun copy(
        name: String = this.name,
        isFinished: Boolean = this.isFinished,
        uuid: RealmUUID = this.uuid
    ): SubTask {
        return SubTask(name, isFinished, uuid)
    }
}