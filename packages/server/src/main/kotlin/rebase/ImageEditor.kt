package rebase

import com.github.ajalt.mordant.rendering.TextColors
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.Position
import com.sksamuel.scrimage.ScaleMethod
import org.imgscalr.Scalr
import java.awt.image.BufferedImage
import java.util.logging.Logger
import kotlin.system.measureTimeMillis

object ImageEditor {
    private val logger: Logger = Logger.getLogger("ImageEditor")
    fun createResizedImageMaintainingAspect(image: BufferedImage, height: Int, width: Int, isWebp: Boolean = false): BufferedImage {
        var data: BufferedImage
        val resizeTiming = measureTimeMillis {
            data = ImmutableImage.fromAwt(image).cover(512, 512, ScaleMethod.BSpline, Position.Center).awt()
            image.flush()
        }
        logger.info("${TextColors.red("Resize")} Resize on image took ${TextColors.magenta("${resizeTiming}ms")}")
        return data
    }
}