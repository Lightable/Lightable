package com.feuer.chatty.Message

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.awt.Color

/**
 * Message embeds
 */
class Embed @BsonCreator constructor(
    @BsonProperty("author")
    val author: String?,
    @BsonProperty("title")
    val title: String?,
    @BsonProperty("content")
    val content: String?,
    @BsonProperty("color")
    val color: Color?,
    @BsonProperty("image")
    val image: String?,
    @BsonProperty("thumbnail")
    val thumbnail: String?,
    @BsonProperty("footer")
    val footer: String?,
    @BsonProperty("fields")
    val fields: ArrayList<Field>?)
{

    companion object {
        /**
         * Message Embed Field
         */
        class Field @BsonCreator constructor(
                    @BsonProperty("name")
                    val name: String,
                    @BsonProperty("value")
                    val value: String,
                    @BsonProperty("inline")
                    val inline: Boolean)
    }
}