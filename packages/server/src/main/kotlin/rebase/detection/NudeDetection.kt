package rebase.detection

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import rebase.interfaces.IImage
import java.awt.image.BufferedImage
import java.io.ByteArrayOutputStream
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.*
import javax.imageio.ImageIO

class NudeDetection(val endpoint: String) {
    val api = NudeDetectionAPI()
    fun detect(imageMap: MutableMap<String, BufferedImage>): SyncRequest? {
        val nameAndBase64 = mutableMapOf<String, String>()
        val encoder = Base64.getEncoder()
        imageMap.forEach { img ->
            val imageByteArrayOutputStream = ByteArrayOutputStream()
            ImageIO.write(img.value, "webp", imageByteArrayOutputStream)
            nameAndBase64[img.key] = encoder.encodeToString(imageByteArrayOutputStream.toByteArray())
        }
        return api.requestSync(PostObject(nameAndBase64))
    }
    inner class NudeDetectionAPI {
        val JSON: MediaType = "application/json; charset=utf-8".toMediaType()
        val client = OkHttpClient()
        val json = jacksonObjectMapper()
        val jobj = ObjectMapper()
        fun requestSync(obj: PostObject): SyncRequest? {
            val body = RequestBody.create(JSON, json.writeValueAsString(obj))
            val request = Request.Builder()
                .url("${endpoint}/sync")
                .post(body)
                .build()
            client.newCall(request).execute().use { response ->
                println("Time took ${(response.receivedResponseAtMillis - response.sentRequestAtMillis).toFloat()/1000.toFloat()}s")
                val string = response.body?.string()!!
                return jobj
                    .readValue(
                        string,
                        SyncRequest::class.java
                    )
            }
        }

    }

    data class PostObject(val data: MutableMap<String, String>)
    data class SyncRequest(
        @JsonProperty("prediction") val prediction: MutableMap<String, MutableList<ImageAttribute?>?>,
        @JsonProperty("success") val success: Boolean
    )

    data class ImageAttribute(
        @JsonProperty("box") val box: MutableList<Int>,
        @JsonProperty("score") val score: Float,
        @JsonProperty("label") val label: String
    ) {
        @JsonIgnore
        val exceeded = score < 0.65
    }

    class ImageWithNSFW(
        override var height: Int,
        override var width: Int,
        override var path: String,
        override var isNSFW: IImage.NSFW
    ) : IImage {

    }
}

fun main() {
    val nudeDetection = NudeDetection("http://192.168.50.111:8089")
}
