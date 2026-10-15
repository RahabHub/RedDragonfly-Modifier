package com.rahab.dragonfly.ui.agreement

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rahab.dragonfly.ui.theme.ErrorRed
import com.rahab.dragonfly.ui.theme.Ink600
import com.rahab.dragonfly.ui.theme.Ink900
import com.rahab.dragonfly.ui.theme.Mist100
import com.rahab.dragonfly.ui.theme.Teal700

/** 需要用户手动输入的承诺语句。 */
private const val COMMITMENT_PHRASE = "我承诺仅用于学习交流"

/**
 * 首次使用协议与免责声明页。
 *
 * 用户需阅读协议，并手动输入承诺语句（禁止复制粘贴）后才能进入。
 *
 * @author Rahab
 */
@Composable
fun AgreementScreen(onAgreed: () -> Unit) {
    var typed by remember { mutableStateOf("") }
    val matched = typed == COMMITMENT_PHRASE

    // 禁用剪贴板：承诺语句只能手打，不允许粘贴。
    CompositionLocalProvider(LocalClipboardManager provides NoOpClipboardManager) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Mist100)
                .verticalScroll(rememberScrollState())
                .imePadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
        ) {
            Text(
                text = "使用协议与免责声明",
                style = MaterialTheme.typography.headlineMedium,
                color = Ink900,
            )
            Spacer(Modifier.height(20.dp))

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
            ) {
                Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // 第一条：严禁商业行为（措辞从重）。
                    Column {
                        Text(
                            text = "一、严禁任何商业行为",
                            style = MaterialTheme.typography.titleMedium,
                            color = ErrorRed,
                            fontWeight = FontWeight.Bold,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "本软件仅供个人学习、技术研究与交流使用，严禁任何形式的商业行为。\n\n" +
                                "「商业行为」包括但不限于：出售、出租、转让、倒卖本软件；收费代跑、付费代做；" +
                                "将本软件或跑步数据用于任何有偿服务、付费推广、广告植入、捆绑销售；" +
                                "以及任何直接或间接的营利性用途。\n\n" +
                                "一经发现上述任何商业用途，开发者将立即终止授权，并保留追究一切法律责任的权利。" +
                                "使用者因违规使用（包括但不限于商业用途）所产生的一切后果——学校纪律处分、学籍处理、" +
                                "行政或法律责任、经济损失等——均由使用者本人自行承担，开发者概不负责。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink900,
                            lineHeight = 22.sp,
                        )
                    }

                    // 第二条：软件目的。
                    Column {
                        Text(
                            text = "二、软件目的",
                            style = MaterialTheme.typography.titleMedium,
                            color = Teal700,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "本软件的目的，是希望大家能把时间花在自己真正想做的事情上，减少不必要的重复劳动。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink900,
                            lineHeight = 22.sp,
                        )
                    }

                    // 第三条：地图瓦片。
                    Column {
                        Text(
                            text = "三、地图数据说明",
                            style = MaterialTheme.typography.titleMedium,
                            color = Teal700,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "本软件使用的地图瓦片数据来自境外服务，国内访问可能较慢甚至加载失败；" +
                                "如有需要，可自行使用代理（梯子）加速。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink900,
                            lineHeight = 22.sp,
                        )
                    }

                    // 第四条：距离计算说明。
                    Column {
                        Text(
                            text = "四、距离计算说明",
                            style = MaterialTheme.typography.titleMedium,
                            color = Teal700,
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            text = "本软件在地图上绘制的路线距离为估算值，实际跑步距离可能与显示值存在偏差，" +
                                "且显示距离可能小于实际距离，请以学校系统记录为准。",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Ink900,
                            lineHeight = 22.sp,
                        )
                    }
                }
            }

            Spacer(Modifier.height(24.dp))

            // 承诺输入区。
            Text(
                text = "请在下方手动输入以下承诺（禁止复制粘贴）：",
                style = MaterialTheme.typography.bodyMedium,
                color = Ink600,
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "「$COMMITMENT_PHRASE」",
                style = MaterialTheme.typography.titleLarge,
                color = Ink900,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = typed,
                onValueChange = { typed = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("手动输入承诺语句") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = if (matched) Teal700 else ErrorRed,
                    cursorColor = Teal700,
                ),
            )
            Spacer(Modifier.height(20.dp))
            Button(
                onClick = onAgreed,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                enabled = matched,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Teal700,
                    contentColor = Color.White,
                    disabledContainerColor = Mist100,
                    disabledContentColor = Ink600,
                ),
            ) {
                Text("同意并继续", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

/** 空剪贴板：禁止粘贴。 */
private object NoOpClipboardManager : ClipboardManager {
    override fun getText(): AnnotatedString? = null
    override fun setText(annotatedString: AnnotatedString) = Unit
}
