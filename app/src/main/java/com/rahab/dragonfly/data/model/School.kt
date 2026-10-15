package com.rahab.dragonfly.data.model

import kotlinx.serialization.Serializable

/**
 * 学校信息（来自中央学校列表 getschoollist.php）。
 *
 * @author Rahab
 */
@Serializable
data class School(
    val schoolno: String = "",
    val schoolname: String = "",
    val address: String = "",
    val h5url: String = "",
    val androidkey: String = "",
    val ioskey: String = "",
    val parameter: String? = null,
) {
    /**
     * 规范化后的服务端 baseUrl。
     *
     * 部分学校列表里的 address 仍是 http://，但服务器已强制 HTTPS（302 跳转），
     * 若用 http 会因 POST 被重定向为 GET 而登录失败，因此统一转为 https。
     */
    val baseUrl: String
        get() {
            var url = address.trim()
            if (url.startsWith("http://")) {
                url = "https://" + url.removePrefix("http://")
            }
            if (url.isNotEmpty() && !url.endsWith("/")) {
                url += "/"
            }
            return url
        }
}

/**
 * 学校列表接口响应。
 */
@Serializable
data class SchoolListResponse(
    val code: Int = -1,
    val list: List<School> = emptyList(),
) {
    val isSuccess: Boolean get() = code == 0
}
