package com.rahab.dragonfly.config

/**
 * 应用级配置常量。
 *
 * 学校相关的地址与编号不再硬编码，改为运行时从中央学校列表拉取并由用户选择。
 *
 * @author Rahab
 */
object AppConfig {

    /** 中央学校列表接口（固定地址）。注意：证书仅覆盖 www.sknow.com.cn，必须带 www。 */
    const val SCHOOL_LIST_URL = "https://www.sknow.com.cn/appapi/getschoollist.php"

    /** App 版本号，登录时上报，与原版保持一致。 */
    const val APP_VERSION = "1.2.22"

    /** 设备系统类型。 */
    const val SYS_TYPE = "android"

    /** 用户类型：学生。 */
    const val SUBTYPE = "student"

    // 跑步规则常量（兜底值；正常情况下从 getRunParam.php 动态获取 eventno/ruleid）。
    /** 事件编号（兜底）。 */
    const val EVENT_NO = "802"

    /** 规则编号（兜底）。 */
    const val RULE_ID = "5"

    /** 打卡点顺序。 */
    const val RUN_PATH = "1,12,2,10"

    /** 点位状态。 */
    const val POINT_STATUS = "3"

    /** 地图样式地址：OpenFreeMap 免费矢量瓦片，无需 key。 */
    const val MAP_STYLE_URL = "https://tiles.openfreemap.org/styles/liberty"

    /** 地图初始中心（北京语言大学附近）。 */
    const val MAP_INIT_LATITUDE = 39.9985
    const val MAP_INIT_LONGITUDE = 116.3506
}
