package rebase.schema

import org.bson.codecs.pojo.annotations.BsonProperty
import rebase.Utils

data class InviteCode constructor(
    @BsonProperty("code") var code: String? = null,
    @BsonProperty("email") var email: String = "testuser@email.com"
)