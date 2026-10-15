package com.rahab.dragonfly.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * 登录接口响应。
 *
 * 服务端统一信封格式：{ code, msg, data }。code == 0 表示成功。
 *
 * @author Rahab
 */
@Serializable
data class LoginResponse(
    val code: Int = -1,
    val msg: String = "",
    val data: LoginData? = null,
) {
    /** 是否登录成功。 */
    val isSuccess: Boolean get() = code == 0
}

/**
 * 登录成功后的用户数据（信封中的 data 字段）。
 */
@Serializable
data class LoginData(
    val userid: String = "",
    val studentno: String = "",
    val studentname: String = "",
    val studentsex: String = "",
    val subtype: String = "",

    @SerialName("login_token")
    val loginToken: String = "",

    @SerialName("login_token_expire")
    val loginTokenExpire: Long = 0,

    @SerialName("upload_token")
    val uploadToken: String = "",

    @SerialName("upload_token_expire")
    val uploadTokenExpire: Long = 0,

    @SerialName("upload_proto")
    val uploadProto: String = "",
)
