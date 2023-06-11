package com.cotrin.todolist.realm

import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

object RealmUtil {
    private lateinit var realm: Realm

    fun getRealm(): Realm {
        return if (this::realm.isInitialized) realm
        else {
            val config = RealmConfiguration.Builder(schema = setOf(Task::class, SubTask::class)).apply {
                this.deleteRealmIfMigrationNeeded()
                this.schemaVersion(1)
            }.build()
            realm = Realm.open(config)
            realm
        }
    }
}