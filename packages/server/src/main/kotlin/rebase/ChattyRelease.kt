package rebase

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import org.bson.codecs.pojo.annotations.BsonProperty

data class ChattyRelease constructor(
    @BsonProperty("version") @JsonIgnore var version: Int = 0,
    @BsonProperty("tag") @JsonProperty("version") var tag: String = "0.0.1",
    @BsonProperty("title") var title: String = "Release Title",
    @BsonProperty("notes") var notes: String = "Release Notes",
    @BsonProperty("signature") var signature: String = "",
    @BsonProperty("url") var url: String = ""
)