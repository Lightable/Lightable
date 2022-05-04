package rebase.compression

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.util.Base64
import java.util.zip.DeflaterOutputStream
import java.util.zip.InflaterOutputStream

object CompressionUtil {
    @Throws(IOException::class)
    fun compressAndReturnB64(text: String): String {
        return String(Base64.getEncoder().encode(compress(text)))
    }

    @Throws(IOException::class)
    fun decompressB64(b64Compressed: String?): String {
        val decompressedBArray = decompress(Base64.getDecoder().decode(b64Compressed))
        return String(decompressedBArray, StandardCharsets.UTF_8)
    }

    @Throws(IOException::class)
    fun compress(text: String): ByteArray {
        return compress(text.toByteArray())
    }

    @Throws(IOException::class)
    fun compress(bArray: ByteArray?): ByteArray {
        val os = ByteArrayOutputStream()
        DeflaterOutputStream(os).use { dos -> dos.write(bArray) }
        return os.toByteArray()
    }

    @Throws(IOException::class)
    fun decompress(compressedTxt: ByteArray?): ByteArray {
        val os = ByteArrayOutputStream()
        InflaterOutputStream(os).use { ios -> ios.write(compressedTxt) }
        return os.toByteArray()
    }
}
