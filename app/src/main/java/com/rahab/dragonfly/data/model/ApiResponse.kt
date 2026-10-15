package com.rahab.dragonfly.data.model

import kotlinx.serialization.Serializable

/**
 * 通用接口响应信封。
 *
 * 服务端统一返回 { code, msg }，code == 0 表示成功。
 *
 * @author Rahab
 */
@Serializable
data class ApiResponse(
    val code: Int = -1,
    val msg: String = "",
) {
    val isSuccess: Boolean get() = code == 0
}
