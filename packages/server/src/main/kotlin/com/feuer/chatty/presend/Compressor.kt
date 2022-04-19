package com.feuer.chatty.presend

import java.io.UnsupportedEncodingException
import java.util.zip.DataFormatException
import java.util.zip.Deflater
import java.util.zip.Inflater


internal class Compressor {
    fun compress(bytesToCompress: ByteArray?): ByteArray {
        val deflater = Deflater()
        deflater.setInput(bytesToCompress)
        deflater.finish()
        val bytesCompressed = ByteArray(Short.MAX_VALUE.toInt())
        val numberOfBytesAfterCompression = deflater.deflate(bytesCompressed)
        val returnValues = ByteArray(numberOfBytesAfterCompression)
        System.arraycopy(
            bytesCompressed,
            0,
            returnValues,
            0,
            numberOfBytesAfterCompression
        )
        return returnValues
    }

    fun compress(stringToCompress: String): ByteArray? {
        var returnValues: ByteArray? = null
        try {
            returnValues = this.compress(
                stringToCompress.toByteArray(charset("UTF-8"))
            )
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
        }
        return returnValues
    }

    fun decompress(bytesToDecompress: ByteArray): ByteArray? {
        var returnValues: ByteArray? = null
        val inflater = Inflater()
        val numberOfBytesToDecompress = bytesToDecompress.size
        inflater.setInput(
            bytesToDecompress,
            0,
            numberOfBytesToDecompress
        )
        var numberOfBytesDecompressedSoFar = 0
        val bytesDecompressedSoFar: MutableList<Byte> = ArrayList()
        try {
            while (inflater.needsInput() == false) {
                val bytesDecompressedBuffer = ByteArray(numberOfBytesToDecompress)
                val numberOfBytesDecompressedThisTime = inflater.inflate(
                    bytesDecompressedBuffer
                )
                numberOfBytesDecompressedSoFar += numberOfBytesDecompressedThisTime
                for (b in 0 until numberOfBytesDecompressedThisTime) {
                    bytesDecompressedSoFar.add(bytesDecompressedBuffer[b])
                }
            }
            returnValues = ByteArray(bytesDecompressedSoFar.size)
            for (b in returnValues.indices) {
                returnValues[b] = bytesDecompressedSoFar[b]
            }
        } catch (dfe: DataFormatException) {
            dfe.printStackTrace()
        }
        inflater.end()
        return returnValues
    }

    fun decompressToString(bytesToDecompress: ByteArray): String? {
        val bytesDecompressed = decompress(
            bytesToDecompress
        )
        var returnValue: String? = null
        try {
            returnValue = String(
                bytesDecompressed!!,
                0,
                bytesDecompressed.size,
                Charsets.UTF_8
            )
        } catch (uee: UnsupportedEncodingException) {
            uee.printStackTrace()
        }
        return returnValue
    }
}
