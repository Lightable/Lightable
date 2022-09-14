package rebase
import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.lang.management.ManagementFactory
import java.math.BigInteger
import java.security.InvalidParameterException
import java.security.MessageDigest
import java.security.SecureRandom
import java.time.Instant
import java.util.*
import javax.management.Attribute
import javax.management.AttributeList
import javax.management.ObjectName
import kotlin.collections.ArrayList
import kotlin.random.Random

object Utils {
    // Banner :P Because why not?
    const val BANNER = " ██████╗  ██╗  ██╗   █████╗   ████████╗  ████████╗ ██╗   ██╗\n" +
        "██╔════╝  ██║  ██║  ██╔══██╗  ╚══██╔══╝  ╚══██╔══╝ ╚██╗ ██╔╝\n" +
        "██║       ███████║  ███████║     ██║        ██║     ╚████╔╝ \n" +
        "██║       ██╔══██║  ██╔══██║     ██║        ██║      ╚██╔╝  \n" +
        "╚██████╗  ██║  ██║  ██║  ██║     ██║        ██║       ██║   \n" +
        " ╚═════╝  ╚═╝  ╚═╝  ╚═╝  ╚═╝     ╚═╝        ╚═╝       ╚═╝     "
    const val VERSION = "1.9.12"
    private val RANDOM = SecureRandom()
    fun generateID(): String {
        val rand = (10000000..99999999).random()
        return "${rand}${Instant.now().toEpochMilli()}" // Parse 8 chars to get timestamp
    }
    fun generateGenericInvite(len: Int = 25): String {
        val alphanumerics = CharArray(26) { it -> (it + 97).toChar() }.toSet()
            .union(CharArray(9) { it -> (it + 48).toChar() }.toSet())
        return (0 until len).map {
            alphanumerics.toList().random()
        }.joinToString("")
    }
    fun formatSize(v: Long): String? {
        if (v < 1024) return "$v B"
        val z = (63 - java.lang.Long.numberOfLeadingZeros(v)) / 10
        return String.format("%.1f %sB", v.toDouble() / (1L shl z * 10), " KMGTPE"[z])
    }
   fun getSHA512(input: String, salt: String):String{
        val md: MessageDigest = MessageDigest.getInstance("SHA-512")
        val messageDigest = md.digest("${salt}${input}".toByteArray())
        // Convert byte array into signum representation
        val no = BigInteger(1, messageDigest)
        // Convert message digest into hex value
        var hashtext: String = no.toString(16)
        // Add preceding 0s to make it 128 chars long
        while (hashtext.length < 128) {
            hashtext = "0$hashtext"
        }
        // return the HashText
        return hashtext
    }
    fun getNextSalt(): String {
        val salt = ByteArray(16)
        RANDOM.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt);
    }
    fun getNextSalt(size: Int): String {
        val salt = ByteArray(size)
        RANDOM.nextBytes(salt)
        return Base64.getEncoder().encodeToString(salt);
    }
    fun versionCompare(local: String, remoteVersion: String?): Int {
        val remote = remoteVersion?.splitToSequence(".")?.toList() ?: return 1
        val local = local.splitToSequence(".").toList()

        if(local.filter { it.toIntOrNull() != null }.size != local.size) throw InvalidParameterException("version invalid: $this")
        if(remote.filter { it.toIntOrNull() != null }.size != remote.size) throw InvalidParameterException("version invalid: $remoteVersion")

        val totalRange = 0 until kotlin.math.max(local.size, remote.size)
        for (i in totalRange) {
            if (i < remote.size && i < local.size) {
                val result = local[i].compareTo(remote[i])
                if (result != 0) return result
            } else (
                    return local.size.compareTo(remote.size)
                    )
        }
        return 0
    }
    @Throws(Exception::class)
    fun getProcessCpuLoad(): Double {
        val mbs = ManagementFactory.getPlatformMBeanServer()
        val name = ObjectName.getInstance("java.lang:type=OperatingSystem")
        val list: AttributeList = mbs.getAttributes(name, arrayOf("ProcessCpuLoad"))
        if (list.isEmpty()) return Double.NaN
        val att: Attribute = list.get(0) as Attribute
        val value = att.value as Double
        return if (value == -1.0) Double.NaN else (value * 1000).toInt() / 10.0
    }

    data class User(var name: String, var userid: String, var friends: ArrayList<String>?, var auth: String?, var status: String?, var about: String?, var online: Boolean?, var statusIcon: String?, var avatar: String?, var created: Instant)
    class Status @BsonCreator constructor(
        @BsonProperty("text") var text: String?,
        @BsonProperty("icon") var icon: String?
    )

    object Constants {
        enum class Badges {
            CHATTY_DEVELOPER,
            CHATTY_SUPPORTER,
            CHATTY_ADMIN
        }
        enum class OnlineStates {
            OFFLINE,
            IDLE,
            ONLINE
        }
    }
}
