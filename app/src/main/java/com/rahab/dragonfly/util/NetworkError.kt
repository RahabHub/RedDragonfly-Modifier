package com.rahab.dragonfly.util

import java.net.ConnectException
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException

/**
 * 网络异常判断。
 *
 * @author Rahab
 */
object NetworkError {

    /** 是否为「无法连接服务端」类错误（未连接校园网 / 断网 / 超时）。 */
    fun isConnectionError(e: Throwable): Boolean =
        e is UnknownHostException ||
            e is ConnectException ||
            e is SocketTimeoutException ||
            e is SocketException
}
