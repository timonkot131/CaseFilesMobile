package com.example.casefilesmobile.adapters

import com.google.gson.*
import java.lang.reflect.Type
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*


class UTCAdapter : JsonSerializer<Date?>, JsonDeserializer<Date?> {
    private val dateFormat: DateFormat

    override fun serialize(
        date: Date?,
        type: Type?,
        jsonSerializationContext: JsonSerializationContext?
    ): JsonElement {
        return JsonPrimitive(dateFormat.format(date))
    }

    override fun deserialize(
        jsonElement: JsonElement,
        type: Type?,
        jsonDeserializationContext: JsonDeserializationContext?
    ): Date {
        return try {
            dateFormat.parse(jsonElement.asString)
        } catch (e: ParseException) {
            throw JsonParseException(e)
        }
    }

    init {
        dateFormat =
            SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"))
    }
}