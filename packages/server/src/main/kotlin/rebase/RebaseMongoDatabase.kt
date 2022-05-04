package rebase

import com.mongodb.*
import com.mongodb.client.MongoCollection
import com.mongodb.client.MongoDatabase
import org.bson.codecs.configuration.CodecRegistries
import org.bson.codecs.configuration.CodecRegistry
import org.bson.codecs.pojo.PojoCodecProvider
import org.litote.kmongo.getCollection

class RebaseMongoDatabase(val username: String = "root", val password: String = "rootpass", val host: String = "192.168.50.111", val port: Int = 27017) {
    private val creds = MongoCredential.createCredential(username, "admin", password.toCharArray())

    private val client = MongoClient(ServerAddress(host, port), creds, MongoClientOptions.builder().build())
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
