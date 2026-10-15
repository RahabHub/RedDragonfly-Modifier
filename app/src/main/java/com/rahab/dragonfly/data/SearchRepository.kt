package com.rahab.dragonfly.data

import com.rahab.dragonfly.data.model.SearchResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.URLEncoder
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * 地点搜索仓库。
 *
 * 使用 OpenStreetMap Nominatim 免费地理编码服务，无需 key。
 *
 * @author Rahab
 */
class SearchRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .proxy(Proxy.NO_PROXY)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** 搜索地点，返回最多 [limit] 条结果。 */
    suspend fun search(query: String, limit: Int = 5): List<SearchResult> =
        withContext(Dispatchers.IO) {
            val encoded = URLEncoder.encode(query, "UTF-8")
            val url = "https://nominatim.openstreetmap.org/search" +
                "?q=$encoded&format=json&limit=$limit&accept-language=zh-CN"
            val request = Request.Builder()
                .url(url)
                .header("User-Agent", "DragonflyRoute/1.0 (learning tool)")
                .get()
                .build()

            try {
                client.newCall(request).execute().use { response ->
                    val body = response.body?.string() ?: return@use emptyList()
                    if (!response.isSuccessful) return@use emptyList()
                    json.decodeFromString<List<SearchResult>>(body)
                }
            } catch (_: Exception) {
                emptyList()
            }
        }
}
