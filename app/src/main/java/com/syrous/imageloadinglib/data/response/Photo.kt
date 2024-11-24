package com.syrous.imageloadinglib.data.response

import com.squareup.moshi.Json

data class Photo(
    val id: String,
    @Json(name = "created_at") val createdAt: String,
    val width: Int,
    val height: Int,
    val color: String,
    val description: String?,
    val likes: Long,
    @Json(name = "urls") val url: URL
)