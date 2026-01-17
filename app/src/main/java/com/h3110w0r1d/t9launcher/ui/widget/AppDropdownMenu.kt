package com.h3110w0r1d.t9launcher.ui.widget

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.DeleteForever
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Block
import androidx.compose.material.icons.outlined.Android
import com.h3110w0r1d.t9launcher.utils.ShizukuManager
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import android.widget.Toast
import com.h3110w0r1d.t9launcher.utils.LaunchAppUtil

@Composable
fun AppDropdownMenu(
    app: AppInfo,
    expanded: Boolean,
    onExpandedChange: (Boolean) -> Unit,
    onAppStateChanged: (() -> Unit)? = null
) {
    val context = LocalContext.current

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        shape = RoundedCornerShape(16.dp),
        containerColor = colorScheme.surfaceContainer,
    ) {
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Info,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.app_info)) },
            onClick = {
                app.detail(context)
                onExpandedChange(false)
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.ContentCopy,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.copy_package_name)) },
            onClick = {
                app.copyPackageName(context)
                onExpandedChange(false)
            },
        )
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.DeleteForever,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.uninstall_app)) },
            onClick = {
                app.uninstall(context)
                onExpandedChange(false)
            },
        )
        
        // 新添加的设置应用状态菜单项
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Android,
                    contentDescription = null,
                )
            },
             text = { Text(stringResource(id = R.string.set_app_state)) },
            onClick = {
                // 初始化ShizukuManager并切换应用状态
                val shizukuManager = ShizukuManager.getInstance()
                shizukuManager.initialize(context)
                var enabled_ = app.isEnabled
                
                // 检查是否有Shizuku权限，如果没有则请求
                if (!shizukuManager.hasPermission) {
                   Toast.makeText(context, context.getString(R.string.need_shizuku_permission), Toast.LENGTH_SHORT).show()
                } else {
                    var success = shizukuManager.setAppState(app.packageName, if(enabled_) false else true)
                         if(success) {
                             var newEnabled = ShizukuManager.checkAppEnabled(context, app.packageName);
                             if(enabled_ != newEnabled){
                                LaunchAppUtil.vibrateWeak(context)
                                app.isEnabled = newEnabled
                                Toast.makeText(context, context.getString(
                                    if(newEnabled) R.string.app_enabled else R.string.app_disabled), Toast.LENGTH_SHORT).show()
                                 
                                // 通知父组件刷新UI，确保UI能够及时更新
                                onAppStateChanged?.invoke()
                            }
                         } else {
                             Toast.makeText(context, context.getString(R.string.operation_failed), Toast.LENGTH_SHORT).show()
                         }
                }
                
                onExpandedChange(false)
            },
        )
        
        // 强制停止应用菜单项
        DropdownMenuItem(
            leadingIcon = {
                Icon(
                    Icons.Outlined.Block,
                    contentDescription = null,
                )
            },
            text = { Text(stringResource(id = R.string.force_stop_app)) },
            onClick = {
                // 初始化ShizukuManager并强制停止应用
                val shizukuManager = ShizukuManager.getInstance()
                shizukuManager.initialize(context)
                
                // 检查是否有Shizuku权限，如果没有则请求
                if (!shizukuManager.hasPermission) {
                    Toast.makeText(context, context.getString(R.string.need_shizuku_permission), Toast.LENGTH_SHORT).show()
                } else {
                    val success = shizukuManager.forceStopApp(app.packageName)
                    if (success) {
                        Toast.makeText(context, context.getString(R.string.force_stop_success), Toast.LENGTH_SHORT).show()
                        LaunchAppUtil.vibrate(context, 25)
                    } else {
                        Toast.makeText(context, context.getString(R.string.operation_failed), Toast.LENGTH_SHORT).show()
                    }
                }
                
                onExpandedChange(false)
            },
        )
    }
}
