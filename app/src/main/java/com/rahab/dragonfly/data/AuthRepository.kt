package com.rahab.dragonfly.data

import android.content.Context
import com.rahab.dragonfly.config.AppConfig
import com.rahab.dragonfly.data.api.ApiClient
import com.rahab.dragonfly.data.model.LoginData
import com.rahab.dragonfly.data.model.School
import com.rahab.dragonfly.util.DeviceInfo
import com.rahab.dragonfly.util.Md5
import com.rahab.dragonfly.util.NetworkError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 登录业务仓库。
 *
 * 负责组装登录参数、调用指定学校的接口，并把网络层异常转换为可展示的结果。
 *
 * @author Rahab
 */
class AuthRepository(private val context: Context) {

    /** 登录结果：成功携带用户数据，失败携带可读错误信息。 */
    sealed interface LoginResult {
        data class Success(val data: LoginData) : LoginResult
        data class Failure(val message: String) : LoginResult
    }

    /**
     * 登录。
     *
     * @param school     用户选择的学校
     * @param studentno  学号
     * @param password   明文密码（内部会转为 MD5 后上报）
     */
    suspend fun login(school: School, studentno: String, password: String): LoginResult =
        withContext(Dispatchers.IO) {
            try {
                val api = ApiClient(school.baseUrl)
                val params = buildLoginParams(school, studentno, password)
                val response = api.login(params)

                val data = response.data
                if (response.isSuccess && data != null) {
                    LoginResult.Success(data)
                } else {
                    LoginResult.Failure(response.msg.ifBlank { "登录失败，请检查账号密码" })
                }
            } catch (e: Exception) {
                LoginResult.Failure(
                    if (NetworkError.isConnectionError(e)) "未连接校园网，请检查网络"
                    else (e.message ?: "网络错误，请稍后重试")
                )
            }
        }

    /**
     * 组装登录表单参数（字段与新版红蜻蜓一致）。
     */
    private fun buildLoginParams(school: School, studentno: String, password: String): Map<String, String> {
        return mapOf(
            "schoolno" to school.schoolno,
            "studentno" to studentno,
            "pwd" to Md5.hex(password),
            "subtype" to AppConfig.SUBTYPE,
            "machineno" to DeviceInfo.machineNo(context),
            "phonebrand" to DeviceInfo.phoneBrand(),
            "phonemodel" to DeviceInfo.phoneModel(),
            "sysrelease" to DeviceInfo.sysRelease(),
            "systype" to AppConfig.SYS_TYPE,
            "appversion" to AppConfig.APP_VERSION,
        )
    }
}
