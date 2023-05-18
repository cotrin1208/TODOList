package com.cotrin.todolist.realm

import com.cotrin.todolist.SubTask
import com.cotrin.todolist.Task
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.RealmUUID
import io.realm.kotlin.types.annotations.Ignore
import io.realm.kotlin.types.annotations.PrimaryKey
import org.jetbrains.annotations.PropertyKey

class RealmTask(): RealmObject {
    var name: String = ""

    constructor(name: String) : this() {
        this.name = name
    }
    init {

    }

    companion object {
        //タスク追加
        suspend fun addTask(task: RealmTask) {
            val config = RealmConfiguration.Builder(schema = setOf(RealmTask::class)).build()
            val realm = Realm.open(config)
            realm.write {
                copyToRealm(task)
            }
        }

        //タスク取得
        fun getTasks(): List<RealmTask> {
            val config = RealmConfiguration.Builder(schema = setOf(RealmTask::class)).build()
            val realm = Realm.open(config)
            val tasks = realm.query<RealmTask>().find()
            return tasks.toList()
        }
    }
}