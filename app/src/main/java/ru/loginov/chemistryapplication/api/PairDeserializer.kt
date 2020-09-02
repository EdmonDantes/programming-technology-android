package ru.loginov.chemistryapplication.api

import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.core.JsonToken
import com.fasterxml.jackson.databind.BeanProperty
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JavaType
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.deser.ContextualDeserializer

class PairDeserializer() : JsonDeserializer<Pair<*, *>>(), ContextualDeserializer {

    private var type: JavaType? = null

    constructor(javaType: JavaType) : this() {
        this.type = javaType;
    }

    override fun deserialize(p: JsonParser, ctxt: DeserializationContext): Pair<*, *>? {
        if (type == null) {
            throw RuntimeException("Have not type for deserialization");
        }

        if (p.currentToken != JsonToken.START_ARRAY) {
            throw RuntimeException("Can not parse pair value. Can not find array token")
        }

        p.nextToken()

        val typeParameters = type?.bindings?.typeParameters
        val result = typeParameters?.let { Pair(p.readValueAs(it[0].rawClass), p.readValueAs(it[1].rawClass)); }
        p.nextToken();
        return result;
    }

    override fun createContextual(ctxt: DeserializationContext?, property: BeanProperty?): JsonDeserializer<*> {
        return PairDeserializer((ctxt?.contextualType ?: property?.member?.type)!!)
    }
}