package rebase.schema

import org.bson.codecs.pojo.annotations.BsonCreator
import org.bson.codecs.pojo.annotations.BsonProperty
import java.time.Instant

data class Device constructor(
    @BsonProperty("lastAccess") var lastAccess: Instant = Instant.now(),
    @BsonProperty("ip") var ip: String = "NA",
    @BsonProperty("browser") var browser: String = "NA",
    @BsonProperty("build") var build: String = "NA",
    @BsonProperty("os") var os: String = "NA",
    @field:BsonProperty(useDiscriminator = true) var geo: GeoLocation = GeoLocation()
) {
    override fun equals(other: Any?): Boolean {
        if (other == null) return false
        if (this === other) return true
        if (other !is Device) return false
        if (other.ip != this.ip) return false
        if (other.browser != this.browser) return false
        if (other.build != this.build) return false
        if (other.os != this.os) return false
        return true
    }

    override fun hashCode(): Int {
        var result = lastAccess.hashCode()
        result = 31 * result + ip.hashCode()
        result = 31 * result + browser.hashCode()
        result = 31 * result + build.hashCode()
        result = 31 * result + os.hashCode()
        result = 31 * result + geo.hashCode()
        return result
    }
}

data class GeoLocation constructor(
    @BsonProperty("country") var country: String = "NA",
    @BsonProperty("state") var state: String = "NA",
    @BsonProperty("stateName") var stateName: String = "NA",
    @BsonProperty("zipcode") var zipcode: String = "NA",
    @BsonProperty("timezone") var timezone: String = "NA",
    @BsonProperty("latitude") var latitude: String = "NA",
    @BsonProperty("longitude") var longitude: String = "NA",
    @BsonProperty("city") var city: String = "NA",
    @BsonProperty("continent") var continent: String = "NA"
)