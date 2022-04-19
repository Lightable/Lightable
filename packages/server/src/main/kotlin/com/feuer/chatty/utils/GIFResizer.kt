package com.feuer.chatty.utils

import com.feuer.chatty.getResizedMaintainingAspect
import com.feuer.chatty.writeCompressedImage
import com.sksamuel.scrimage.ImmutableImage
import com.sksamuel.scrimage.Position
import com.sksamuel.scrimage.ScaleMethod
import com.sksamuel.scrimage.nio.AnimatedGifReader
import com.sksamuel.scrimage.nio.ImageSource
import com.sksamuel.scrimage.nio.StreamingGifWriter
import org.slf4j.LoggerFactory
import java.awt.image.BufferedImage
import java.io.File
import kotlin.system.measureTimeMillis

fun GIFResizer(image: ByteArray, userID: String, fileID: String): StreamingGifWriter.GifStream? {
    val logger = LoggerFactory.getLogger("GIF RESIZE")
    val gif = AnimatedGifReader.read(ImageSource.of(image))
    val fileStatic = File("./storage/${userID}/${fileID}.webp")
    val file = File("./storage/${userID}/a_${fileID}.gif")
        logger.info("Resizing GIF with ${gif.frameCount} frames")
    val newFrames = ArrayList<ImmutableImage>()
    for (frameNum in 0 until gif.frameCount) {
        val frame = gif.frames[frameNum]
        if (frameNum == 0) {
            logger.info("Writing static image")
            val cropped = getResizedMaintainingAspect(frame.awt(), 512, 512)!!
            writeCompressedImage(file, cropped, "webp")
            logger.info("Finished writing static frame")
        }
        logger.info("Starting frame $frameNum out of ${gif.frameCount} (${frameNum}/${gif.frameCount})")
        val frameTiming = measureTimeMillis {
            newFrames.add(frame.cover(512, 512, ScaleMethod.FastScale, Position.Center))
        }
        logger.info("Finished frame $frameNum out of ${gif.frameCount} (${frameNum}/${gif.frameCount}) in ${frameTiming}ms")
    }
    val newGIFWriter = StreamingGifWriter(gif.getDelay(0), true)
    val newGIF = newGIFWriter.prepareStream(file, BufferedImage.TYPE_INT_ARGB)
    val totalFrames = measureTimeMillis {
        for (frame in newFrames) {
            val newGIFFrames = measureTimeMillis {
                newGIF.writeFrame(frame)
            }
            logger.info("Finished writing new frame for new gif in ${newGIFFrames}ms")
        }
    }
    logger.info("Finished in ${totalFrames}ms")
    newGIF.close()
    return newGIF

}