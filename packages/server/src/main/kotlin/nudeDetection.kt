import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.filter.ChromeFilter
import com.sksamuel.scrimage.filter.PixelateFilter
import com.sksamuel.scrimage.nio.ImageWriter
import com.sksamuel.scrimage.pixels.Pixel
import java.awt.Color
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.io.File
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis
import kotlin.time.measureTime

class NudeDetection {
    val staticBase = ImageIO.read(this.javaClass.getResource("/assets/yor.jpg"))
    fun copyImage(source: BufferedImage): BufferedImage {
        val bi = BufferedImage(source.width, source.height, source.type)
        val sourceData = (source.raster.dataBuffer as DataBufferByte).data
        val biData = (bi.raster.dataBuffer as DataBufferByte).data
        System.arraycopy(sourceData, 0, biData, 0, sourceData.size)
        return bi
    }
    fun pixelateInvalid() {

        val timing = measureTimeMillis {
            val baseScrim = ImmutableImage.fromAwt(staticBase)
            val pixelatedFBR = baseScrim.subimage(1206, 1430, 548, 507).filter(PixelateFilter(516 / 10))
            val pixelatedFBL = baseScrim.subimage(654, 1479, 516, 536).filter(PixelateFilter(516 / 10))
//            val pixelatedFG = baseScrim.subimage(900, 2836, 281, 234).filter(PixelateFilter(281 / 5))
//            val pixelateBelly = baseScrim.subimage(875, 2009, 562, 660).filter(PixelateFilter(562 / 10))
            val overlayed = baseScrim.overlay(pixelatedFBL, 654, 1479).overlay(pixelatedFBR, 1206, 1430)

//        ImageIO.write(pixelatedFBRRaw.awt(), "jpg", File("./rawfbr.jpg"))
//            ImageIO.write(overlayed.awt(), "PNG", File("pixeltest.png"))
        }
        println("Took ${timing}ms to censor")
    }
    data class BufferRes(val image: BufferedImage, val timing: Long)
}
fun main() {
    val nudeDetection = NudeDetection()
    nudeDetection.pixelateInvalid()
    nudeDetection.pixelateInvalid()
    val base = nudeDetection.copyImage(nudeDetection.staticBase)
    val speed = measureTimeMillis {
        val g = base.createGraphics()
        g.color = Color.BLACK
        val rectFBL = Rectangle(654, 1479, 516, 536)
        val rectFBR = Rectangle(1206, 1430, 548, 507)
        g.fill(rectFBL)
        g.fill(rectFBR)
        g.dispose()
    }
    ImageIO.write(base, "jpg", File("./test_DELETE.jpg"))
    println("Took ${speed}ms to censor with black boxes")
}