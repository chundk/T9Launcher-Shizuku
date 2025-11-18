package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
// 已移除错误的import
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import android.widget.Toast
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.utils.ShizukuManager

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun AppItem(
    app: AppInfo,
    onClick: () -> Unit = {},
    onLongPress: (Offset) -> Unit = {},
) {
    val appConfig = LocalAppConfig.current
    val context = LocalContext.current
    var scaleTarget by remember { mutableFloatStateOf(1f) }
    val scaleState by animateFloatAsState(
        targetValue = scaleTarget,
        label = "AppItemScale",
    )
    val annotatedName by app.annotatedName.collectAsState()
    val matchRange by app.matchRange.collectAsState()
    val highlightColor = colorScheme.primary
    var appEnabled by remember { mutableStateOf(app.isEnabled) }
    val shizukuManager = remember { ShizukuManager.getInstance() }
      
    app.updateAnnotatedName(highlightColor)
    LaunchedEffect(matchRange) {
        app.updateAnnotatedName(highlightColor)
    }
    
    // 监听app.isEnabled的变化，确保UI能够同步更新
    LaunchedEffect(app.isEnabled) {
        appEnabled = app.isEnabled
    }
    
    // 覆盖长按处理以添加启用/禁用切换
    val enhancedLongPress: (Offset) -> Unit = {
        // 先调用原始长按处理
        onLongPress(it)
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier =
            Modifier
                .pointerInput(Unit) {
                    detectTapGestures(
                        onLongPress = {
                            scaleTarget = 1.1f
                            enhancedLongPress(it)
                        },
                        onPress = {
                            // 在手指按下的瞬间触发
                            scaleTarget = 0.9f
                            try {
                                awaitRelease()
                            } catch (_: Exception) {
                                // 长按成功，这里不做处理
                            }
                            scaleTarget = 1f
                        },
                        onTap = {
                            onClick()
                        },
                    )
                }.scale(scaleState)
                .animateContentSize(),
    ) {
        Box(
            modifier =
                Modifier.padding(
                    vertical = appConfig.appListStyle.iconVerticalPadding.dp,
                    horizontal = appConfig.appListStyle.iconHorizonPadding.dp,
                ),
        ) {
            Image(
                bitmap = app.appIcon,
                contentDescription = app.appName,
                modifier =
                    Modifier
                        .width(appConfig.appListStyle.iconSize.dp)
                        .aspectRatio(1f)
                        .clip(RoundedCornerShape(percent = appConfig.appListStyle.iconCornerRadius)),
                colorFilter = if (!appEnabled) {
                    // 将禁用的应用图标变灰
                    androidx.compose.ui.graphics.ColorFilter.colorMatrix(androidx.compose.ui.graphics.ColorMatrix().apply {
                        setToSaturation(0f) // 设置饱和度为0，实现灰度效果
                    })
                } else null,
            )
        }
        Text(
            text = if (appConfig.search.highlightSearchResultEnabled) annotatedName else AnnotatedString(app.appName),
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontSize = appConfig.appListStyle.appNameSize.sp,
            lineHeight = (appConfig.appListStyle.appNameSize * 1.2).sp,
            color = if (!appEnabled) Color.Gray else colorScheme.onSurface,
            modifier =
                Modifier
                    .padding(bottom = appConfig.appListStyle.rowSpacing.dp)
                    .padding(horizontal = 4.dp),
        )
    }
}
