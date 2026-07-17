package com.example.data.database.converter

import androidx.room.TypeConverter
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.serializer

class DatabaseConverters {
    private val json = Json { ignoreUnknownKeys = true }

    @TypeConverter
    fun fromInstant(instant: Instant?): Long? {
        return instant?.toEpochMilliseconds()
    }

    @TypeConverter
    fun toInstant(epochMs: Long?): Instant? {
        return epochMs?.let { Instant.fromEpochMilliseconds(it) }
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        if (list == null) return null
        return json.encodeToString(ListSerializer(serializer<String>()), list)
    }

    @TypeConverter
    fun toStringList(jsonStr: String?): List<String>? {
        if (jsonStr == null) return null
        return json.decodeFromString(ListSerializer(serializer<String>()), jsonStr)
    }
}
