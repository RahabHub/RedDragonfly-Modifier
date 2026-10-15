package com.rahab.dragonfly.data.model

import kotlinx.serialization.Serializable

/**
 * 跑步规则（来自 getRunParam.php）。
 *
 * @author Rahab
 */
@Serializable
data class RunRule(
    val ruleid: String = "",
    val eventno: String = "",
    val atttype: String = "",
    val eventname: String = "",
    val malespeed: String = "",
    val femalespeed: String = "",
    val maleminlimit: String = "",
    val femaleminlimit: String = "",
    val maletotallimit: String = "",
    val femaletotallimit: String = "",
) {
    /** 单次最低里程（米），按性别取最小限制，解析失败返回 null。 */
    val minDistanceM: Int?
        get() = maleminlimit.toIntOrNull() ?: femaleminlimit.toIntOrNull()
}

/**
 * 跑步规则接口响应。
 */
@Serializable
data class RunRuleResponse(
    val code: Int = -1,
    val msg: String = "",
    val data: List<RunRule> = emptyList(),
) {
    val isSuccess: Boolean get() = code == 0

    /** 第一条规则。 */
    val firstRule: RunRule? get() = data.firstOrNull()
}
