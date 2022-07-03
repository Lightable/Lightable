package rebase.generator

import com.github.ajalt.colormath.parse
import java.awt.Color
import java.awt.Font
import java.awt.RenderingHints
import java.awt.font.TextLayout
import java.awt.image.BufferedImage
import java.awt.image.DataBufferByte
import java.time.Instant
import javax.imageio.ImageIO
import kotlin.system.measureTimeMillis

class EmbedImageGenerator {
    // 1024, 585
    private val staticBase = ImageIO.read(this.javaClass.getResource("/assets/base.png"))
    // 512, 512
    private val logo = ImageIO.read(this.javaClass.getResource("/assets/logo.png"))
    private val titilliumFont = Font.createFont(Font.TRUETYPE_FONT, this.javaClass.getResource("/assets/TitilliumWeb-Bold.ttf").openStream())
    fun generateEmbed(text: String, color: String = "#ffffff", showCreated: Boolean = false): BufferRes {
        val base = copyImage(staticBase)
        val imageGenTime = measureTimeMillis {
            val g = base.createGraphics()
            val chars = text.toCharArray()
            g.font = titilliumFont.deriveFont(Font.PLAIN, 55f)
            val textLayout = TextLayout(text, g.font, g.fontRenderContext, )
            val textWidth = textLayout.bounds.width
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON)
            g.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY)
            g.drawImage(logo, (base.width / 2  - 215 / 2), 150, 215, 215, null)
            g.color = Color.decode(color)
            g.drawString(text, (base.width / 2 - textWidth.toInt() / 2) , 400)
            g.font = titilliumFont.deriveFont(Font.PLAIN, 22F)
            if (showCreated) {
                g.drawString("Created At: ${Instant.now()}", 30, 540)
            }
            g.dispose()
        }
        return BufferRes(base, imageGenTime)
    }
    private fun copyImage(source: BufferedImage): BufferedImage {
        val bi = BufferedImage(source.width, source.height, source.type)
        val sourceData = (source.raster.dataBuffer as DataBufferByte).data
        val biData = (bi.raster.dataBuffer as DataBufferByte).data
        System.arraycopy(sourceData, 0, biData, 0, sourceData.size)
        return bi
    }
    data class BufferRes(val image: BufferedImage, val timing: Long)
}