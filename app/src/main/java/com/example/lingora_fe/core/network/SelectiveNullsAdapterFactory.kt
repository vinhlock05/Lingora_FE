package com.example.lingora_fe.core.network

import android.util.Log
import com.google.gson.*
import com.google.gson.annotations.SerializedName
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.lang.reflect.Modifier

@Target(AnnotationTarget.FIELD)
@Retention(AnnotationRetention.RUNTIME)
annotation class SerializeNull

class SelectiveNullsAdapterFactory : TypeAdapterFactory {
    override fun <T> create(gson: Gson, type: TypeToken<T>): TypeAdapter<T>? {
        val delegate = gson.getDelegateAdapter(this, type)
        val rawType = type.rawType

        // ✅ Only apply to classes we want to customize (user-defined data classes)
        // Skip: primitives, java.lang.*, kotlin.collections.*, arrays
        if (rawType.isPrimitive ||
            rawType.isArray ||
            rawType.name.startsWith("java.lang.") ||
            rawType.name.startsWith("java.util.") ||
            rawType.name.startsWith("kotlin.collections.") ||
            Collection::class.java.isAssignableFrom(rawType) ||
            Map::class.java.isAssignableFrom(rawType)
        ) {
            return delegate
        }

        Log.d("SelectiveNulls", "Factory applied to ${rawType.simpleName}")

        return object : TypeAdapter<T>() {
            override fun write(out: JsonWriter, value: T?) {
                if (value == null) {
                    out.nullValue()
                    return
                }

                val originalSerializeNulls = out.serializeNulls

                // ✅ Check if this object has ANY @SerializeNull fields
                val hasAnySerializeNull = rawType.declaredFields.any {
                    it.isAnnotationPresent(SerializeNull::class.java)
                }

                if (hasAnySerializeNull) {
                    out.serializeNulls = true // Enable for entire object
                }

                out.beginObject()

                for (field in rawType.declaredFields) {
                    field.isAccessible = true

                    if (field.isSynthetic || field.name.startsWith("$") ||
                        Modifier.isTransient(field.modifiers) || Modifier.isStatic(field.modifiers)
                    ) continue

                    val hasSerializeNull = field.isAnnotationPresent(SerializeNull::class.java)
                    val serializedNameAnnotation = field.getAnnotation(SerializedName::class.java)
                    val jsonKeyName = serializedNameAnnotation?.value ?: field.name
                    val fieldValue = field.get(value)

                    if (fieldValue != null || hasSerializeNull) {
                        out.name(jsonKeyName)
                        if (fieldValue == null) {
                            out.nullValue()
                        } else {
                            @Suppress("UNCHECKED_CAST")
                            val adapter = gson.getAdapter(TypeToken.get(field.genericType)) as TypeAdapter<Any>
                            adapter.write(out, fieldValue)
                        }
                    }
                }

                out.endObject()
                out.serializeNulls = originalSerializeNulls // ✅ Restore original setting
            }

            override fun read(reader: JsonReader): T? = delegate.read(reader)
        }
    }
}
