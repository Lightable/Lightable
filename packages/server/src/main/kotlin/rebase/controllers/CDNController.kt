package rebase.controllers

import io.javalin.http.Context
import rebase.Cache
import rebase.FileController
import java.io.ByteArrayOutputStream

class CDNController(val cache: Cache, val fileController: FileController) {
    fun getRelease(ctx: Context) {
        val release = ctx.pathParam("release").replace(".", "").toInt()
        val latest = cache.latestRelease
        if (latest == null || release > latest.tag.replace(".", "").toInt()) {
            ctx.status(204)
            return
        } else {
            ctx.status(200).json(latest)
        }
    }
    fun getReleases(ctx: Context) {
        if (cache.releases.size < 1) {
            ctx.status(204)
            return
        } else {
            ctx.status(200).json(cache.releases.values)
            return
        }
    }
    fun getUserAvatar(ctx: Context) {
        val size = ctx.queryParam("size")?.toInt() ?: 512
        val userid = ctx.pathParam("user").toLong()
        val avatarID = ctx.pathParam("avatar").toLong()
        val user = cache.users[userid]!!
        val file = "/user/${user.identifier}/avatars/avatar_$avatarID.webp"
        ctx.contentType("image/webp")
        var avatar: ByteArrayOutputStream? = null

        if (cache.userAvatarCache[user.identifier] == null || cache.userAvatarCache[user.identifier] != null && size != 512) {
            val resizedImage = fileController.dynamicResize("./storage$file", size)
            avatar = resizedImage
            if (size == 512) {
                cache.userAvatarCache[user.identifier] = resizedImage
            }
        } else {
            avatar = cache.userAvatarCache[user.identifier]
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
