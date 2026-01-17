package com.h3110w0r1d.t9launcher.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SearchBar
import androidx.compose.material3.SearchBarDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import android.content.Context
import android.content.Intent
import android.app.NotificationManager
import android.os.Build
import android.widget.Toast
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.BatchOperationsSystemConfirmActivity
import com.h3110w0r1d.t9launcher.activity.checkDangerousConfirmStatus
import com.h3110w0r1d.t9launcher.data.app.AppInfo
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController
import com.h3110w0r1d.t9launcher.utils.LaunchAppUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectShortcutScreen(index: Int) {
    val navController = LocalNavController.current!!
    val viewModel = LocalGlobalViewModel.current
    val context = LocalContext.current
    val hideAppList by viewModel.hideAppList.collectAsState()
    
    // 创建批量操作列表的数据类
    data class BatchOperationItem(
        val className: String,
        val packageName: String,
        val appName: Int, // 使用资源ID
        val iconRes: Int // 使用资源ID
    )
    
    // 创建批量操作列表
    val batchOperationItems = remember {
        listOf(
            // BatchOperationsConfigureActivity
            BatchOperationItem(
                className = "com.h3110w0r1d.t9launcher.activity.BatchOperationsActivity",
                packageName = "com.h3110w0r1d.t9launcher",
                appName = R.string.batch_operations_user_apps,
                iconRes = R.drawable.icon_user_apps
            ),
            // BatchOperations4SystemOnlyActivity
            BatchOperationItem(
                className = "com.h3110w0r1d.t9launcher.activity.BatchOperations4SystemOnlyActivity",
                packageName = "com.h3110w0r1d.t9launcher",
                appName = R.string.batch_operations_system_apps,
                iconRes = R.drawable.icon_system_apps
            )
        )
    }
    var searchText by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        viewModel.searchHideApp("")
    }

    LaunchedEffect(isSearching) {
        if (isSearching) {
            focusRequester.requestFocus()
        }
    }

    BackHandler(enabled = isSearching) {
        isSearching = false
        searchText = ""
        viewModel.searchHideApp("")
    }
    Scaffold(
        topBar = {
            if (isSearching) {
                SearchBar(
                    expanded = false,
                    onExpandedChange = {},
                    inputField = {
                        SearchBarDefaults.InputField(
                            expanded = false,
                            query = searchText,
                            onQueryChange = {
                                searchText = it
                                viewModel.searchHideApp(searchText)
                            },
                            onSearch = {},
                            onExpandedChange = {},
                            modifier = Modifier.focusRequester(focusRequester),
                            leadingIcon = {
                                IconButton(onClick = { isSearching = false }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = null,
                                    )
                                }
                            },
                            trailingIcon = {
                                if (searchText.isNotEmpty()) {
                                    IconButton(onClick = {
                                        searchText = ""
                                        viewModel.searchHideApp("")
                                    }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = null,
                                        )
                                    }
                                }
                            },
                        )
                    },
                    modifier = Modifier.padding(horizontal = 16.dp),
                ) { }
            } else {
                TopAppBar(
                    title = { Text(stringResource(R.string.select_shortcut_app)) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = null,
                            )
                        }
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                isSearching = !isSearching
                                searchText = ""
                            },
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = null,
                            )
                        }
                    },
                )
            }
        },
    ) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
            // 添加批量操作列表
            items(batchOperationItems.size) { i ->
                val item = batchOperationItems[i]
                ListItem(
                    leadingContent = {
                        Image(
                            painter = painterResource(id = item.iconRes),
                            contentDescription = stringResource(id = item.appName),
                            modifier =
                                Modifier
                                    .width(44.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(percent = 26)),
                        )
                    },
                    headlineContent = { Text(stringResource(id = item.appName)) },
                    supportingContent = { Text(item.packageName) },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = true,
                                onClick = {
                                        // 创建componentId
                                        val componentId = "${item.packageName}/${item.className}"
                                        
                                        // 检查是否是BatchOperations4SystemOnlyActivity
                                        if (componentId.contains("BatchOperations4SystemOnlyActivity")) {
                                            // 检查DANGEROUS_CONFIRM_KEY的状态
                                            checkDangerousConfirmStatus(context) {
                                                if (it) {
                                                    // 检查是否有通知权限
                                                    val hasNotificationPermission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                                        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                                                        notificationManager.areNotificationsEnabled()
                                                    } else {
                                                        true // Android 12及以下默认有通知权限
                                                    }
                                                    
                                                    if (hasNotificationPermission) {
                                                        // 用户已经确认过且有权限，直接设置快捷方式
                                                        viewModel.setQuickStartApp(index, componentId)
                                                        navController.popBackStack()
                                                    } else {
                                                        // 权限不足，发出震动与Toast
                                                        LaunchAppUtil.vibrate(context, 300)
                                                        Toast.makeText(context, context.getString(R.string.need_notification_permission), Toast.LENGTH_LONG).show()
                                                    }
                                                    
                                                } else {
                                                    // 用户未确认过，启动确认界面
                                                    val intent = Intent(context, BatchOperationsSystemConfirmActivity::class.java)
                                                    intent.putExtra(BatchOperationsSystemConfirmActivity.EXTRA_SHOW_CONFIRMATION_ONLY, true)
                                                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                                    context.startActivity(intent)
                                                }
                                            }
                                        } else {
                                            // 普通组件，直接设置快捷方式
                                            viewModel.setQuickStartApp(index, componentId)
                                            navController.popBackStack()
                                        }
                                    },
                            ),
                )
            }
            
            // 添加普通应用列表
            items(hideAppList.size) { i ->
                ListItem(
                    leadingContent = {
                        Image(
                            bitmap = hideAppList[i].appIcon,
                            contentDescription = hideAppList[i].appName,
                            modifier =
                                Modifier
                                    .width(44.dp)
                                    .aspectRatio(1f)
                                    .clip(RoundedCornerShape(percent = 26)),
                        )
                    },
                    headlineContent = { Text(hideAppList[i].appName) },
                    supportingContent = { Text(hideAppList[i].packageName) },
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .clickable(
                                enabled = true,
                                onClick = {
                                    viewModel.setQuickStartApp(index, hideAppList[i].componentId())
                                    navController.popBackStack()
                                },
                            ),
                )
            }
        }
    }
}
