package com.rahab.dragonfly.util

import java.security.MessageDigest

/**
 * 密码摘要工具。
 *
 * 登录接口的 pwd 字段 = 密码的 MD5 小写十六进制（与新版红蜻蜓逻辑一致，
 * 反编译自 AuthProvider::md5Hash，无加盐）。
 *
 * @author Rahab
 */
object Md5 {

    /** 计算字符串的 MD5 小写十六进制摘要。 */
    fun hex(input: String): String {
        val digest = MessageDigest.getInstance("MD5")
        val bytes = digest.digest(input.toByteArray(Charsets.UTF_8))
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
