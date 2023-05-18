package com.cotrin.todolist.realm

import com.cotrin.todolist.Task
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmManager {
    private lateinit var realm: Realm

    fun getInstance(): Realm {
        if (!::realm.isInitialized) {
            val config = RealmConfiguration.create(schema = setOf(RealmTask::class))
            realm = Realm.open(config)
        }
        return realm
    }
}