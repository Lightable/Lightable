package rebase

import com.feuer.chatty.schemas.MongoSchemas
import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.getCollection


class MongoDatabase {
    private val client = MongoClient("localhost", 27017)
    private val database: MongoDatabase = client.getDatabase("Chatty")
    private var codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build(), PojoCodecProvider.builder().register(User::class.java).build())
    )

    fun getDMCollection(): MongoCollection<MongoSchemas.DM> {
        return database.getCollection<MongoSchemas.DM>().withCodecRegistry(codecRegistry)
    }

    fun getUserCollection(): MongoCollection<MongoSchemas.MongoUser> {
        return database.getCollection<MongoSchemas.MongoUser>().withCodecRegistry(codecRegistry)
    }

    fun getReleaseCollection(): MongoCollection<MongoSchemas.ChattyRelease> {
        return database.getCollection<MongoSchemas.ChattyRelease>().withCodecRegistry(codecRegistry)
    }
}