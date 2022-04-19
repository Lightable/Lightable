package com.feuer.chatty

import com.feuer.chatty.decoders.BASE64Decoder
import com.feuer.chatty.schemas.MongoSchemas
import com.feuer.chatty.utils.GIFResizer
import io.prometheus.client.Gauge
import org.imgscalr.Scalr
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import rebase.MongoDatabase
import java.awt.Graphics
import java.awt.Rectangle
import java.awt.image.BufferedImage
import java.io.*
import java.nio.file.Files
import java.util.*
import javax.imageio.IIOImage
import javax.imageio.ImageIO
import javax.imageio.ImageWriteParam
import kotlin.system.measureTimeMillis


class Storage(val db: MongoDatabase) {
    private var logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val utils = StorageUtils(this)
    private val totalFolders: Gauge =
        Gauge.build("chatty_storage_folder_total", "Get the total folders for storage").register()
    val totalFiles: Gauge = Gauge.build("chatty_storage_files_total", "Get the total files for storage").register()
    private fun createUserStorage(id: String) {
        val userDir = File("./storage/${id}")
        if (!userDir.exists()) {
            totalFolders.inc()
            userDir.mkdir()
        }
    }

    private fun getTotalFolders() {
        totalFolders.inc(File("./storage").listFiles().size.toDouble())
        val fileList = mutableListOf<File?>()
        utils.listf("./storage", fileList)
        totalFiles.inc(fileList.size.toDouble())
    }

    private fun createUserGlobalStorage(path: String) {
        val dir = File(path)
        if (!dir.exists()) {
            dir.mkdir()
        }
    }

    fun addItemToGlobalStorage(fileEx: String, item: String, id: String): String {
        val fileID = Utils.generateID()
        val userPath = File("./storage/global/${id}")
        utils.purgeUserAvatars(userPath, 1)
        createUserGlobalStorage(userPath.path)
        if (item.length >= 2000000) throw IOException("Item must be under 2mb!")
        val file = File("./storage/global/${id}/${fileID}.${fileEx}")
        val data: ByteArray = Base64.getDecoder().decode(item)
        logger.info("Writing data to file (${Utils.formatSize(item.length.toLong())})")
        Files.write(file.toPath(), data)
        totalFiles.inc()
        logger.info("File was written")
        return "/global/${id}/${fileID}.${fileEx}"
    }

    fun addItemToUserStorage(fileEx: String, item: String, id: String, fileID: String = Utils.generateID()): String {
        val userPath = File("./storage/${id}")
        utils.purgeUserAvatars(userPath, 2)
        createUserStorage(id)
        if (item.length >= 12000000) throw IOException("Item must be under 12mb!")
        val file = File("./storage/${id}/${fileID}.${fileEx}")
        if (fileEx == "gif") {
            val bArrGif = Base64.getDecoder().decode(item)
            val globalResizeFrameTime = measureTimeMillis {
                val resized = GIFResizer(bArrGif, id, fileID)
            }
            logger.info("Frame resize took ${globalResizeFrameTime}ms")
            logger.info("Writing data to file (${Utils.formatSize(item.length.toLong())})")
            logger.info("File was written")
            db.getUserCollection().updateOne(
                MongoSchemas.MongoUser::identifier eq id,
                setValue(MongoSchemas.MongoUser::avatar, MongoSchemas.Avatar(id, fileID, true))
            )
            return "https://chatty-api.feuer.tech/${id}/${fileID}.${fileEx}"
        }
        val decoder = BASE64Decoder()
        val data: ByteArray = decoder.decodeBuffer(item)

        val image = ImageIO.read(ByteArrayInputStream(data))
        val cropped = getResizedMaintainingAspect(image, 512, 512)
        if (cropped != null) {
            totalFiles.inc()
            writeCompressedImage(file, cropped, fileEx)
        }
        logger.info("Writing data to file (${Utils.formatSize(item.length.toLong())})")
        logger.info("File was written")
        if (fileEx.matches(Regex("/gif|jpg|png/"))) {
            db.getUserCollection().updateOne(
                MongoSchemas.MongoUser::identifier eq id,
                setValue(MongoSchemas.MongoUser::avatar, MongoSchemas.Avatar(id, fileID, false))
            )
            logger.info("Avatar saved to Database for indexing")
        }
        return "https://chatty-api.feuer.tech/${id}/${fileID}.${fileEx}"
    }

    init {
        val dir = File("./storage/")
        if (!dir.exists()) {
            dir.mkdirs()
        }
    }

    init {
        getTotalFolders()
    }
}

class StorageUtils(val storage: Storage) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)!!
    fun purgeUserAvatars(avatarDir: File, limit: Int) {
        val avatarFiles = avatarDir.listFiles()
        logger.info("PURGE - Found ${avatarFiles?.size}")
        var oldestDate = Long.MAX_VALUE
        var oldestFile: File? = null
        if (avatarFiles != null && avatarFiles.size > limit) {
            logger.info("PURGE - File limit exceeded. Purging old files..")
            for (f in avatarFiles) {
                if (f.lastModified() < oldestDate) {
                    oldestDate = f.lastModified()
                    oldestFile = f
                }
            }
            logger.info("PURGE - Purging ${oldestFile?.nameWithoutExtension} (${Utils.formatSize(oldestFile?.length()!!)})")
            storage.totalFiles.dec()
            oldestFile.delete()
        }
    }

    fun listf(directoryName: String, files: MutableList<File?>) {
        val directory = File(directoryName)
        // Get all files from a directory.
        val fList = directory.listFiles()
        if (fList != null) for (file in fList) {
            if (file.isFile) {
                files.add(file)
            } else if (file.isDirectory) {
                listf(file.absolutePath, files)
            }
        }
    }
}

val imageProcessLog = LoggerFactory.getLogger("Image Processor")

@Throws(IOException::class)
fun resizeImage(originalImage: BufferedImage?, targetWidth: Int, targetHeight: Int): BufferedImage? {
    val resizedImage = BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB)
    val graphics2D = resizedImage.createGraphics()
    graphics2D.drawImage(originalImage, 0, 0, targetWidth, targetHeight, null)
    graphics2D.dispose()
    return resizedImage
}

fun getResizedMaintainingAspect(image: BufferedImage, height: Int, width: Int): BufferedImage? {
    var data: BufferedImage? = null
    val timing = measureTimeMillis {
        data = Scalr.resize(image, Scalr.Method.BALANCED, height, width)
    }
    imageProcessLog.info("[-> Resize] - Resize (Orig = ${image.height}px/${image.width}px New = ${data?.height}px/${data?.width}px) on image (Size ${image.data.dataBuffer.size * 4L / (1024L * 1024L)}MB) took ${timing}ms")
    return data
}

fun crop(src: BufferedImage?, rect: Rectangle): BufferedImage? {
    val dest = BufferedImage(rect.getWidth().toInt(), rect.getHeight().toInt(), BufferedImage.TYPE_INT_ARGB)
    val g: Graphics = dest.graphics
    g.drawImage(
        src,
        0,
        0,
        rect.getWidth().toInt(),
        rect.getHeight().toInt(),
        rect.getX().toInt(),
        rect.getY().toInt(),
        (rect.getX() + rect.getWidth()).toInt(),
        (rect.getY() + rect.getHeight()).toInt(),
        null
    )
    g.dispose()
    return dest
}

fun writeCompressedImage(file: File, image: BufferedImage, encode: String) {
    val timing = measureTimeMillis {
        val out: OutputStream = FileOutputStream(file)
        val writer = ImageIO.getImageWritersByFormatName("png").next()
        val ios = ImageIO.createImageOutputStream(out)
        writer.output = ios
        val param = writer.defaultWriteParam
        if (param.canWriteCompressed()) {
            param.compressionMode = ImageWriteParam.MODE_EXPLICIT
            param.compressionQuality = 0.5f
        }
        writer.write(null, IIOImage(image, null, null), param)
        out.close()
        ios.close()
        writer.dispose()
    }
    com.feuer.chatty.imageProcessLog.info("[Compress -> Save] - Save on image (Size ${image.data.dataBuffer.size * 4L / (1024L * 1024L)}MB, Name ${file.name}) took ${timing}ms")
}