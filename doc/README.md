# 逆向工程文档

本目录收录「红蜻蜓轨迹」项目对校园跑步系统（飞翔的红蜻蜓）所做的逆向分析工作，供学习与技术研究参考。

> ⚠️ 全部内容仅供学习交流，严禁任何商业用途。

## 文档索引

| 文档 | 内容 |
|---|---|
| [01-接口与协议.md](01-接口与协议.md) | 服务端地址、登录协议、跑步规则接口、上传协议 |
| [02-签名算法.md](02-签名算法.md) | SHA256 哈希、canonical 字符串、两个 HMAC-SHA256 签名 |
| [03-数据格式.md](03-数据格式.md) | location 轨迹格式、datalog 日志格式、WGS84/GCJ02 坐标系 |
| [04-逆向方法.md](04-逆向方法.md) | 抓包、透明代理、Flutter AOT 反编译（blutter）全过程 |

## 关键结论速览

- 登录：`pwd = MD5(密码)`，无加盐。
- 上传：两个 SHA256（location/datalog 哈希）+ 两个 HMAC-SHA256（payload_sign / sign，key 均为 `upload_token`）。
- 坐标：地图用 WGS84（OSM），上传用 GCJ02（高德/国测局），需偏移转换。
- 新版 App 为 Flutter（Dart 3.11.4 AOT），用 blutter 反编译。
