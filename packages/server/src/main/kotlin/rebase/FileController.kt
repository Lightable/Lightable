package rebase

import com.luciad.imageio.webp.WebPWriteParam
import io.javalin.core.util.FileUtil
import io.javalin.http.UploadedFile
import java.awt.image.BufferedImage
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import javax.imageio.metadata.IIOMetadata
import kotlin.system.measureTimeMillis

class FileController() {
    private val imageEditor = ImageEditor
    fun createUserDir(user: Long) {
        val dir = File("./storage/user/${user}/avatars")
        if (dir.exists()) {
            return
        } else {
            dir.mkdirs()
        }
    }
    fun dynamicResize(path: String, size: Int): ByteArrayOutputStream {
        val image = ImageIO.read(File(path))
        val resizedImage =  imageEditor.createResizedImageMaintainingAspect(image, size, size)
        val out = ByteArrayOutputStream()
        val writer = ImageIO.getImageWritersByMIMEType("image/webp").next()
        val ios = ImageIO.createImageOutputStream(out)
        writer.output = ios
        writer.write(null, IIOImage(resizedImage, null, null), null)
        ios.close()
        out.close()
        return out
    }
    fun addAvatar(user: Long, imageID: Long, avatar: UploadedFile, type: String) {
        val path = "./storage/user/${user}/avatars/avatar_${imageID}.webp"
        val file = File(path)
        val existingFiles = File("./storage/user/${user}/avatars/").listFiles()
        if (existingFiles.isNotEmpty()) {
            for (existingFile in existingFiles) {
                existingFile.delete()
            }
        }
        if (type == "png" || type == "jpg" || type == "webp") {
            val image = ImageIO.read(ByteArrayInputStream(avatar.content.readBytes()))
            val compressWriteTiming = compressAndWriteImage(file, image)
            println("Compression took ${compressWriteTiming}ms")
        }
        if (type == "gif") {

        }
    }

    private fun compressAndWriteImage(file: File, image: BufferedImage): Long {
        return measureTimeMillis {
            val scaled = imageEditor.createResizedImageMaintainingAspect(image, 512, 512)
            val out = FileOutputStream(file)
            val writer = ImageIO.getImageWritersByMIMEType("image/webp").next()
            val ios = ImageIO.createImageOutputStream(out)
            writer.output = ios
            val writeParams = WebPWriteParam(writer.locale)
            if (writeParams.canWriteCompressed()) {
                writeParams.compressionMode = ImageWriteParam.MODE_EXPLICIT
                writeParams.compressionType = writeParams.compressionTypes[0]
                writeParams.compressionQuality = 0.8f;
            }

            writer.write(null, IIOImage(scaled, null, null), writeParams)
            ios.close()
            out.close()
            writer.dispose()
        }
    }

}