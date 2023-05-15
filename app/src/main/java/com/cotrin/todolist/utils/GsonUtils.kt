package com.cotrin.todolist.utils

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonArray
import com.google.gson.JsonDeserializer
import com.google.gson.JsonObject
import com.google.gson.JsonPrimitive
import com.google.gson.JsonSerializer
import java.time.LocalDate
import java.time.LocalTime

object GsonUtils {
    fun getCustomGson(): Gson {
        return GsonBuilder().apply {
            registerTypeAdapter(LocalTime::class.java, JsonSerializer<LocalTime?> { src, _, _ ->
                if (src == null) {
                    JsonPrimitive("")
                } else JsonPrimitive(src.toString())
            })
            registerTypeAdapter(LocalTime::class.java, JsonDeserializer { json, _, _ ->
                if (json.asString == "") {
                    null
                } else LocalTime.parse(json.asString)
            })
            registerTypeAdapter(LocalDate::class.java, JsonSerializer<LocalDate> { src, _, _ ->
                JsonPrimitive(src.format(Reference.DATE_FORMATTER))
            })
            registerTypeAdapter(LocalDate::class.java, JsonDeserializer { json, _, _ ->
                LocalDate.parse(json.asJsonPrimitive.asString)
            })
        }.create()
    }
}