package com.rahab.dragonfly.util

import java.io.ByteArrayOutputStream
import java.security.MessageDigest
import java.util.Base64
import java.util.zip.GZIPOutputStream
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * 签名与编码工具。
 *
 * 上传接口需要 SHA256、HMAC-SHA256 与 gzip+base64url 编码。
 *
 * @author Rahab
 */
object Crypto {

    /** SHA-256，返回小写十六进制。 */
    fun sha256(input: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
            .digest(input.toByteArray(Charsets.UTF_8))
        return digest.toHex()
    }

    /** HMAC-SHA-256，key 为 UTF-8 字符串，返回小写十六进制。 */
    fun hmacSha256(key: String, message: String): String {
        val mac = Mac.getInstance("HmacSHA256")
        mac.init(SecretKeySpec(key.toByteArray(Charsets.UTF_8), "HmacSHA256"))
        return mac.doFinal(message.toByteArray(Charsets.UTF_8)).toHex()
    }

    /** gzip 压缩后做 URL-safe Base64（无填充）。 */
    fun gzipBase64Url(input: String): String {
        val bytes = ByteArrayOutputStream().use { bos ->
            GZIPOutputStream(bos).use { it.write(input.toByteArray(Charsets.UTF_8)) }
            bos.toByteArray()
        }
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    private fun ByteArray.toHex(): String = joinToString("") { "%02x".format(it) }
}
