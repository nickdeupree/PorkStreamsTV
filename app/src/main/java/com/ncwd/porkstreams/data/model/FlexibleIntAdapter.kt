package com.ncwd.porkstreams.data.model

import com.squareup.moshi.FromJson
import com.squareup.moshi.JsonQualifier
import com.squareup.moshi.JsonReader
import com.squareup.moshi.JsonWriter
import com.squareup.moshi.ToJson

/**
 * Qualifier for fields where the API may return either a boolean (true/false)
 * or an integer (0/1). Both are normalized to Int (0 or 1).
 */
@Retention(AnnotationRetention.RUNTIME)
@JsonQualifier
annotation class FlexibleInt

class FlexibleIntAdapter {
    @FromJson
    @FlexibleInt
    fun fromJson(reader: JsonReader): Int = when (reader.peek()) {
        JsonReader.Token.BOOLEAN -> if (reader.nextBoolean()) 1 else 0
        JsonReader.Token.NUMBER -> reader.nextInt()
        else -> { reader.skipValue(); 0 }
    }

    @ToJson
    fun toJson(writer: JsonWriter, @FlexibleInt value: Int) {
        writer.value(value)
    }
}
