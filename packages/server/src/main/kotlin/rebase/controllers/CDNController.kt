package rebase.controllers

import io.javalin.http.Context
import rebase.cache.UserCache
import rebase.FileController
import java.io.ByteArrayOutputStream
import java.io.File

class CDNController(val userCache: UserCache, val fileController: FileController) {
    fun getRelease(ctx: Context) {
        val release = ctx.pathParam("release").replace(".", "").toInt()
        val latest = userCache.latestRelease
        val latestVer = latest?.tag?.replace(".", "")?.toInt()
        if (latest == null || release >= latestVer!!) {
            ctx.status(204)
            return
        } else {
            ctx.status(200).json(latest)
        }
    }
    fun getReleases(ctx: Context) {
        if (userCache.releases.size < 1) {
            ctx.status(204)
            return
        } else {
            ctx.status(200).json(userCache.releases.values)
            return
        }
    }

    fun getApp(ctx: Context) {
        val name = ctx.pathParam("file")
        val file = "./storage/$name"
        val fileOut = File(file).readBytes()
        ctx.contentType("application/x-msdownload")
        ctx.status(200).result(fileOut)
    }
    fun getUserAvatar(ctx: Context) {
        val size = ctx.queryParam("size")?.toInt() ?: 512
        val userid = ctx.pathParam("user").toLong()
        val avatarID = ctx.pathParam("avatar").toLong()
        val user = userCache.users[userid]!!
        val file = "/user/${user.identifier}/avatars/avatar_$avatarID.webp"
        ctx.contentType("image/webp")
        var avatar: ByteArrayOutputStream? = null

        if (userCache.avatarCache[user.identifier] == null || userCache.avatarCache[user.identifier] != null && size != 512) {
            val resizedImage = fileController.dynamicResize("./storage$file", size)
            avatar = resizedImage
            if (size == 512) {
                userCache.avatarCache[user.identifier] = resizedImage
            }
        } else {
            avatar = userCache.avatarCache[user.identifier]
        }
        when (size) {
            512 -> {
                ctx.status(200).result(avatar!!.toByteArray())
            }
            128 -> {
                ctx.status(200).result(avatar!!.toByteArray())
            }
            64 -> {
                ctx.status(200).result(avatar!!.toByteArray())
            }
            32 -> {
                ctx.status(200).result(avatar!!.toByteArray())
            }
            16 -> {
                ctx.status(200).result(avatar!!.toByteArray())
            }
            else -> {
                ctx.contentType("image/json")
                ctx.status(403).json(object {
                    val message = "Only supported sizes are 512, 128, 64, 32, 16"
                })
            }
        }
    }
}
