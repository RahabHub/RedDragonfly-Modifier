package com.rahab.dragonfly.data.api

import com.rahab.dragonfly.data.model.ApiResponse
import com.rahab.dragonfly.data.model.LoginResponse
import com.rahab.dragonfly.data.model.RunRuleResponse
import kotlinx.serialization.json.Json
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.net.Proxy
import java.util.concurrent.TimeUnit

/**
 * 服务端 API 客户端。
 *
 * 封装登录与跑步数据上传两个接口，使用 OkHttp + kotlinx.serialization。
 *
 * @author Rahab
 */
class ApiClient(
    private val baseUrl: String,
) {
    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(10, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(20, TimeUnit.SECONDS)
        // 直连服务端，忽略系统代理（避免设备上残留的抓包代理干扰请求）。
        .proxy(Proxy.NO_PROXY)
        .build()

    private val json = Json { ignoreUnknownKeys = true }

    /**
     * 登录。
     */
    fun login(params: Map<String, String>): LoginResponse {
        val body = postForm("login.php", params)
        return json.decodeFromString(LoginResponse.serializer(), body)
    }

    /**
     * 上传跑步数据。
     */
    fun uploadRunData(params: Map<String, String>): ApiResponse {
        val body = postForm("uploadRunData.php", params)
        return json.decodeFromString(ApiResponse.serializer(), body)
    }

    /**
     * 拉取跑步规则。
     */
    fun getRunParam(params: Map<String, String>): RunRuleResponse {
        val body = postForm("getRunParam.php", params)
        return json.decodeFromString(RunRuleResponse.serializer(), body)
    }

    /**
     * 以表单编码 POST 指定接口，返回响应体文本。
     */
    private fun postForm(path: String, params: Map<String, String>): String {
        val formBody = FormBody.Builder().apply {
            params.forEach { (k, v) -> add(k, v) }
        }.build()

        val request = Request.Builder()
            .url(baseUrl + path)
            .post(formBody)
            .header("Content-Type", "application/x-www-form-urlencoded")
            .build()

        client.newCall(request).execute().use { response ->
            val body = response.body?.string() ?: throw IOException("响应为空")
            if (!response.isSuccessful) {
                throw IOException("请求失败：HTTP ${response.code}")
            }
            return body
        }
    }
}
