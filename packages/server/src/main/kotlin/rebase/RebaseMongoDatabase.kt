package rebase

import com.mongodb.MongoClient
import com.mongodb.MongoClientSettings
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.getCollection

class RebaseMongoDatabase {
    private val client = MongoClient("localhost", 27017)
    private val database: MongoDatabase = client.getDatabase("Rebase")
    private var codecRegistry: CodecRegistry = CodecRegistries.fromRegistries(
        MongoClientSettings.getDefaultCodecRegistry(),
        CodecRegistries.fromProviders(PojoCodecProvider.builder().automatic(true).build()),
    )

    fun getUserCollection(): MongoCollection<User> {
        return database.getCollection<User>().withCodecRegistry(codecRegistry)
    }

    fun getReleaseCollection(): MongoCollection<rebase.ChattyRelease> {
        return database.getCollection<rebase.ChattyRelease>().withCodecRegistry(codecRegistry)
    }
}
