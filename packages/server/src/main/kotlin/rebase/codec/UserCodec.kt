package rebase.codec
//
// import org.bson.BsonReader
// import org.bson.BsonWriter
// import org.bson.Document
// import org.bson.codecs.Codec
// import org.bson.codecs.DecoderContext
// import org.bson.codecs.EncoderContext
// import org.bson.codecs.configuration.CodecRegistry
// import rebase.User
//
//
// class UserCodec(private val registry: CodecRegistry) :
//    Codec<User?> {
//    private val documentCodec: Codec<Document> = registry.get(Document::class.java)
//    override fun encode(writer: BsonWriter?, value: User?, encoderContext: EncoderContext?) {
//        val obj = Document()
//        obj["id"] = value?.identifier
//        obj["name"] = value?.name
//        obj["email"] = value?.email
//        obj["password"] = value?.password
//        obj["token"] = value?.token
//        obj["friends"] = value?.friends
//        obj["state"] = value?.state
//        obj["status"] = value?.status
//        obj["admin"] = value?.admin
//        obj["enabled"] = value?.enabled
//        obj["avatar"] = value?.avatar
//        writer.write
//    }
//
//    override fun getEncoderClass(): Class<User?> {
//        return User::class.java
//    }
//
//    override fun decode(reader: BsonReader?, decoderContext: DecoderContext?): User? {
//        TODO("Not yet implemented")
//    }
//
// }
