package com.rahab.dragonfly.data

import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.data.model.School
import com.rahab.dragonfly.data.model.SchoolListResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * 学校列表仓库。
 *
 * 从中央接口拉取全部学校，供用户选择。
 *
 * @author Rahab
 */
class SchoolRepository {

    private val client = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .proxy(Proxy.NO_PROXY)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /** 拉取学校列表，失败返回空列表。 */
    suspend fun fetchSchools(): List<School> = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(AppConfig.SCHOOL_LIST_URL)
            .header("User-Agent", "Dart/3.11 (dart:io)")
            .get()
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val body = response.body?.string() ?: return@use emptyList()
                if (!response.isSuccessful) return@use emptyList()
                val parsed = json.decodeFromString<SchoolListResponse>(body)
                if (parsed.isSuccess) parsed.list else emptyList()
            }
        } catch (_: Exception) {
            emptyList()
        }
    }
}
