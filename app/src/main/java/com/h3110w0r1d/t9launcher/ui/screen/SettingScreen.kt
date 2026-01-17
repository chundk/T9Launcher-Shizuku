package com.h3110w0r1d.t9launcher.ui.screen

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.FileProvider
import android.widget.Toast
import com.h3110w0r1d.t9launcher.ui.dialog.IconPositionActivity
import com.h3110w0r1d.t9launcher.activity.StartCountStatisticalActivity
import com.h3110w0r1d.t9launcher.activity.BatchOperationsConfigureActivity
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.outlined.Book
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.InvertColors
import androidx.compose.material.icons.outlined.Keyboard
import androidx.compose.material.icons.outlined.Cached
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material.icons.outlined.SettingsBackupRestore
import androidx.compose.material.icons.outlined.LayersClear
import androidx.compose.material.icons.outlined.Widgets
import androidx.compose.material.icons.outlined.Merge
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Button
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.net.toUri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException

import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.BuildConfig
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.data.config.LocalAppConfig
import com.h3110w0r1d.t9launcher.model.LocalGlobalViewModel
import com.h3110w0r1d.t9launcher.ui.LocalNavController
import com.h3110w0r1d.t9launcher.ui.theme.getPrimaryColorMap
import com.h3110w0r1d.t9launcher.utils.DBHelper
import com.h3110w0r1d.t9launcher.utils.ShizukuManager

@SuppressLint("ShowToast")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingScreen() {
    val navController = LocalNavController.current!!
    val viewModel = LocalGlobalViewModel.current
    val appConfig = LocalAppConfig.current
    val context = LocalContext.current

    val isDarkMode =
        if (appConfig.theme.nightModeFollowSystem) {
            isSystemInDarkTheme()
        } else {
            appConfig.theme.nightModeEnabled
        }
    var selectColorDialogOpened by remember { mutableStateOf(false) }
 
    val themeColorNamesMap =
        hashMapOf(
            "amber" to stringResource(R.string.amber_theme),
            "blue_grey" to stringResource(R.string.blue_grey_theme),
            "blue" to stringResource(R.string.blue_theme),
            "brown" to stringResource(R.string.brown_theme),
            "cyan" to stringResource(R.string.cyan_theme),
            "deep_orange" to stringResource(R.string.deep_orange_theme),
            "deep_purple" to stringResource(R.string.deep_purple_theme),
            "green" to stringResource(R.string.green_theme),
            "indigo" to stringResource(R.string.indigo_theme),
            "light_blue" to stringResource(R.string.light_blue_theme),
            "light_green" to stringResource(R.string.light_green_theme),
            "lime" to stringResource(R.string.lime_theme),
            "orange" to stringResource(R.string.orange_theme),
            "pink" to stringResource(R.string.pink_theme),
            "purple" to stringResource(R.string.purple_theme),
            "red" to stringResource(R.string.red_theme),
            "teal" to stringResource(R.string.teal_theme),
            "yellow" to stringResource(R.string.yellow_theme),
            "sakura" to stringResource(R.string.sakura_theme),
        )
    val themeColorKeys = themeColorNamesMap.keys.toList()
    val scrollState = rememberScrollState()
    
    // Shizuku权限控制
    val shizukuManager = remember { ShizukuManager.getInstance() }
    
    // 检查Shizuku包是否存在
    var isShizukuInstalled by remember {
        mutableStateOf(
            try {
                context.packageManager.getPackageInfo(com.h3110w0r1d.t9launcher.utils.SHIZUKU_PACKAGE_NAME, 0)
                true
            } catch (e: android.content.pm.PackageManager.NameNotFoundException) {
                false
            }
        )
    }
    
    var hasShizukuPermission by remember { mutableStateOf(
        if (isShizukuInstalled) shizukuManager.hasPermission else false
    ) }
    var isShizukuAvailable by remember { mutableStateOf(
        if (isShizukuInstalled) shizukuManager.isShizukuAvailable() else false
    ) }
    
    // 文件选择器
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri -> uri?.let { restorePreferences(context, it) }
    }
    
    // 数据库文件选择器
    val databaseFilePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) {
        uri -> uri?.let { restoreDatabase(context, it) }
    }
    
    // 存储权限请求
    val storagePermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            backupDatabase(context)
        } else {
            Toast.makeText(context, "需要存储权限才能备份数据库", Toast.LENGTH_SHORT).show()
        }
    }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.setting)) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier =
                Modifier
                    .fillMaxHeight()
                    .padding(innerPadding)
                    .verticalScroll(scrollState),
        ) {
            SettingItemGroup(stringResource(R.string.hide))
            SettingItem(
                imageVector = Icons.Outlined.LayersClear,
                title = stringResource(R.string.hide_system_app),
                trailingContent = {
                    Switch(
                        checked = appConfig.search.hideSystemAppEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateSearchConfig(
                        appConfig.search.copy(
                            hideSystemAppEnabled = !appConfig.search.hideSystemAppEnabled,
                        ),
                    )
                },
            )
            
            // PackageReceiver控制开关
            val context = LocalContext.current
            val app = context.applicationContext as App
            var isPackageReceiverEnabled by remember {
                mutableStateOf(app.isPackageReceiverEnabled())
            }
            
            SettingItem(
                imageVector = Icons.Outlined.Cached,
                title = stringResource(R.string.enable_package_receiver),
                description = stringResource(R.string.enable_package_receiver_description),
                trailingContent = {
                    Switch(
                        checked = isPackageReceiverEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    val newValue = !isPackageReceiverEnabled
                    app.setPackageReceiverEnabled(newValue)
                    isPackageReceiverEnabled = newValue
                    
                    // 显示操作结果提示
                    Toast.makeText(
                        context,
                        if (newValue) R.string.package_receiver_enabled else R.string.package_receiver_disabled,
                        Toast.LENGTH_SHORT
                    ).show()
                },
            )
            SettingItem(
                imageVector = Icons.Outlined.VisibilityOff,
                title = stringResource(R.string.hide_app_list),
                onClick = {
                    navController.navigate("hide_app")
                },
            )
            SettingItem(
                imageVector = Icons.Filled.Logout,
                title = stringResource(id = R.string.finish_after_launch_title),
                description = stringResource(id = R.string.finish_after_launch_description),
                trailingContent = {
                    Switch(
                        checked = appConfig.finishAfterLaunched,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateFinishAfterLaunched(!appConfig.finishAfterLaunched)
                },
            )
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.flash_on_24px),
                title = stringResource(R.string.shortcut_setting),
                onClick = {
                    navController.navigate("shortcut")
                },
            )
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.match_word_24px),
                title = stringResource(R.string.english_fuzzy_match),
                description = "udio -> Audio",
                trailingContent = {
                    Switch(
                        checked = appConfig.search.englishFuzzyMatchEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateSearchConfig(
                        appConfig.search.copy(
                            englishFuzzyMatchEnabled = !appConfig.search.englishFuzzyMatchEnabled,
                        ),
                    )
                },
            )

            // 重构数据库选项
            var isRefactorDatabaseDialogVisible by remember { mutableStateOf(false) }
            
            SettingItem(
                imageVector = Icons.Outlined.Storage,
                title = stringResource(R.string.refactor_database),
                description = stringResource(R.string.refactor_database_description),
                onClick = {
                    isRefactorDatabaseDialogVisible = true
                }
            )
            
            // 数据库重构（清除数据库文件）确认对话框
            if (isRefactorDatabaseDialogVisible) {
                Dialog(onDismissRequest = {
                    isRefactorDatabaseDialogVisible = false
                }) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                        modifier = Modifier
                            .padding(16.dp)
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(text = stringResource(R.string.refactor_database_confirm_title))
                            Text(
                                text = stringResource(R.string.refactor_database_confirm_message),
                                modifier = Modifier.padding(vertical = 8.dp)
                            )
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                Button(
                                    onClick = {
                                        isRefactorDatabaseDialogVisible = false
                                        },
                                    modifier = Modifier.padding(end = 8.dp)
                                ) {
                                    Text(stringResource(R.string.database_clear_cancel))
                                }
                                Button(onClick = {
                                    // 执行数据库清除操作
                                    clearDatabase(context)
                                    isRefactorDatabaseDialogVisible = false
                                }) {
                                    Text(stringResource(R.string.database_clear_confirm))
                                }
                            }
                        }
                    }
                }
            }

            var isBackupRestoreDialogVisible by remember { mutableStateOf(false) }
            var isDatabaseBackupDialogVisible by remember { mutableStateOf(false) }
            SettingItem(
                imageVector = Icons.Outlined.Storage,
                title = stringResource(R.string.backup_database),
                description = stringResource(R.string.backup_database_description),
                onClick = {
                    isDatabaseBackupDialogVisible = true
                },
            )
            SettingItem(
                imageVector = Icons.Outlined.SettingsBackupRestore,
                title = stringResource(R.string.backup_restore),
                description = stringResource(R.string.backup_restore_description),
                onClick = {
                    isBackupRestoreDialogVisible = true
                }
            )

            // 备份与恢复 preference 对话框
            if (isBackupRestoreDialogVisible) {
                Dialog(onDismissRequest = { isBackupRestoreDialogVisible = false }) {
                    Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.backup_restore),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = stringResource(R.string.backup_restore_dialog_message),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        // 启动文件选择器
                                        filePickerLauncher.launch("*/*")
                                        isBackupRestoreDialogVisible = false
                                    },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(stringResource(R.string.import_backup))
                                }
                                Button(
                                    onClick = {
                                        // 分享功能
                                        shareBackupFile(context)
                                        isBackupRestoreDialogVisible = false
                                    },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(stringResource(R.string.share_backup))
                                }
                                Button(
                                    onClick = {
                                        // 备份功能
                                        backupPreferences(context)
                                        isBackupRestoreDialogVisible = false
                                    }
                                ) {
                                    Text(stringResource(R.string.backup))
                                }
                            }
                        }
                    }
                }
            }

            // 数据库备份与恢复对话框
            if (isDatabaseBackupDialogVisible) {
                Dialog(onDismissRequest = { isDatabaseBackupDialogVisible = false }) {
                    Card(Modifier.padding(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
                        Column(Modifier.padding(16.dp)) {
                            Text(
                                text = stringResource(R.string.backup_database),
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.padding(bottom = 16.dp)
                            )
                            Text(
                                text = stringResource(R.string.backup_database_dialog_message),
                                style = MaterialTheme.typography.bodyMedium,
                                modifier = Modifier.padding(bottom = 24.dp)
                            )
                            Column(modifier = Modifier.fillMaxWidth()) {
                                Button(
                                    onClick = {
                                        // 启动文件选择器
                                        databaseFilePickerLauncher.launch("*/*")
                                        isDatabaseBackupDialogVisible = false
                                    },
                                    modifier = Modifier.padding(bottom = 8.dp)
                                ) {
                                    Text(stringResource(R.string.restore_database_file))
                                }
                                Button(
                                    onClick = {
                                        // 备份功能 - 先请求权限
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                                            if (android.os.Environment.isExternalStorageManager()) {
                                                backupDatabase(context)
                                            } else {
                                                val intent = android.content.Intent(android.provider.Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION).apply {
                                                    data = android.net.Uri.parse("package:${context.packageName}")
                                                }
                                                context.startActivity(intent)
                                            }
                                        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                            storagePermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                        } else {
                                            backupDatabase(context)
                                        }
                                        isDatabaseBackupDialogVisible = false
                                    }
                                ) {
                                    Text(stringResource(R.string.backup_database_file))
                                }
                            }
                        }
                    }
                }
            }

            SettingItemGroup(stringResource(R.string.appearance))

            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.app_registration_24px),
                title = stringResource(R.string.app_list_style),
                onClick = {
                    navController.navigate("app_list_style")
                },
            )

            SettingItem(
                imageVector = Icons.Outlined.Keyboard,
                title = stringResource(R.string.keyboard_style),
                onClick = {
                    navController.navigate("keyboard_style")
                },
            )
              
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.ink_highlighter_24px),
                title = stringResource(R.string.is_highlight_search_result),
                trailingContent = {
                    Switch(
                        checked = appConfig.search.highlightSearchResultEnabled,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateSearchConfig(
                        appConfig.search.copy(
                            highlightSearchResultEnabled = !appConfig.search.highlightSearchResultEnabled,
                        ),
                    )
                },
            )
            
            SettingItemGroup(stringResource(R.string.color_category))
            SettingItem(
                imageVector = ImageVector.vectorResource(R.drawable.invert_colors_24px),
                title = stringResource(R.string.night_mode_follow_system),
                trailingContent = {
                    Switch(
                        checked = appConfig.theme.nightModeFollowSystem,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateThemeConfig(
                        appConfig.theme.copy(
                            nightModeFollowSystem = !appConfig.theme.nightModeFollowSystem,
                        ),
                    )
                },
            )
            if (!appConfig.theme.nightModeFollowSystem) {
                SettingItem(
                    imageVector = ImageVector.vectorResource(R.drawable.dark_mode_24px),
                    title = stringResource(R.string.night_mode_enabled),
                    trailingContent = {
                        Switch(
                            checked = appConfig.theme.nightModeEnabled,
                            onCheckedChange = null,
                        )
                    },
                    onClick = {
                        viewModel.updateThemeConfig(
                            appConfig.theme.copy(
                                nightModeEnabled = !appConfig.theme.nightModeEnabled,
                            ),
                        )
                    },
                )
            }

            SettingItem(
                imageVector = Icons.Outlined.Palette,
                title = stringResource(R.string.use_system_color),
                trailingContent = {
                    Switch(
                        checked = appConfig.theme.isUseSystemColor,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateThemeConfig(
                        appConfig.theme.copy(
                            isUseSystemColor = !appConfig.theme.isUseSystemColor,
                        ),
                    )
                },
            )
            if (!appConfig.theme.isUseSystemColor) {
                SettingItem(
                    imageVector = ImageVector.vectorResource(R.drawable.colors_24px),
                    title = stringResource(R.string.theme_color),
                    description = themeColorNamesMap.get(appConfig.theme.themeColor),
                    onClick = {
                        selectColorDialogOpened = true
                    },
                )
            }
            SettingItem(
                imageVector = Icons.Outlined.InvertColors,
                title = stringResource(R.string.pure_black_dark_theme),
                description = stringResource(R.string.pure_black_dark_theme_summary),
                trailingContent = {
                    Switch(
                        checked = appConfig.theme.pureBlackDarkTheme,
                        onCheckedChange = null,
                    )
                },
                onClick = {
                    viewModel.updateThemeConfig(
                        appConfig.theme.copy(
                            pureBlackDarkTheme = !appConfig.theme.pureBlackDarkTheme,
                        ),
                    )
                },
            )
            SettingItemGroup(stringResource(R.string.apps_category))
            SettingItem(
                imageVector = Icons.Outlined.Info,
                title = stringResource(R.string.start_statistics),
                onClick = {
                    context.startActivity(Intent(context, StartCountStatisticalActivity::class.java))
                },
            )
            SettingItem(
                imageVector = Icons.Outlined.Dashboard,
                title = stringResource(R.string.app_list),
                description = stringResource(R.string.app_list_description),
                onClick = {
                    context.startActivity(Intent(context, BatchOperationsConfigureActivity::class.java))
                },
            )
             
            SettingItemGroup(stringResource(R.string.shizuku))
            SettingItem(
                imageVector = Icons.Outlined.Lock,
                title = stringResource(R.string.shizuku_permission),
                description = if (!isShizukuInstalled) {
                    stringResource(R.string.shizuku_not_installed)
                } else if (isShizukuAvailable) {
                    if (hasShizukuPermission) stringResource(R.string.shizuku_permission_granted)
                    else stringResource(R.string.shizuku_permission_description)
                } else {
                    stringResource(R.string.shizuku_not_available)
                },
                trailingContent = {
                    Switch(
                        checked = hasShizukuPermission,
                        onCheckedChange = null,
                        enabled = isShizukuInstalled && !hasShizukuPermission && isShizukuAvailable
                    )
                },
                onClick = {
                    if (isShizukuInstalled && isShizukuAvailable && !hasShizukuPermission) {
                        // 请求Shizuku权限
                        shizukuManager.addPermissionResultListener { result ->
                            hasShizukuPermission = result
                        }
                        shizukuManager.checkAndRequestPermission()
                        // 更新Shizuku可用性状态
                        isShizukuAvailable = shizukuManager.isShizukuAvailable()
                    }
                },
            )
            
            SettingItemGroup(stringResource(R.string.widget_category))
            SettingItem(
                imageVector = Icons.Outlined.Widgets,
                title = stringResource(R.string.icon_position_setting),
                description = stringResource(R.string.icon_position_setting_description),
                onClick = {
                    context.startActivity(Intent(context, IconPositionActivity::class.java))
                },
            )
            
            SettingItem(
                imageVector = Icons.Outlined.Widgets,
                title = stringResource(R.string.icon_position_setting),
                description = stringResource(R.string.icon_position_dark_setting_description),
                onClick = {
                    val intent = Intent(context, IconPositionActivity::class.java)
                    intent.putExtra("dark", true)
                    context.startActivity(intent)
                },
            )
            
            SettingItemGroup(stringResource(R.string.about))

            SettingItem(
                imageVector = Icons.Outlined.Book,
                title = stringResource(R.string.user_guide),
                onClick = {
                    navController.navigate("onboarding")
                },
            )

            SettingItem(
                imageVector = Icons.Outlined.Person,
                title = stringResource(R.string.author),
                description = "@h3110w0r1d-y",
                onClick = {
                    val uri = "https://github.com/h3110w0r1d-y".toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
            )

            SettingItem(
                imageVector = Icons.Outlined.Person,
                title = stringResource(R.string.author2),
                description = "Shizuku & @xiaokkangg",
                onClick = {
                    val uri = "https://github.com/chundk".toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
            )

            SettingItem(
                imageVector = Icons.Outlined.Merge,
                title = stringResource(R.string.repository),
                description = "https://github.com/h3110w0r1d-y/T9Launcher",
                onClick = {
                    val uri = "https://github.com/h3110w0r1d-y/T9Launcher".toUri()
                    context.startActivity(Intent(Intent.ACTION_VIEW, uri))
                },
            )
            var clickCount by remember { mutableIntStateOf(0) }
            var mToast by remember { mutableStateOf<Toast?>(null) }
            var lastTimeStamp by remember { mutableLongStateOf(0L) }
            val maxClickCount = 7
            SettingItem(
                imageVector = Icons.Outlined.Info,
                title = stringResource(R.string.version),
                description = "${BuildConfig.VERSION_NAME} (${BuildConfig.VERSION_CODE})",
                onClick = {
                    if (mToast != null) {
                        mToast?.cancel()
                    }
                    if (System.currentTimeMillis() - lastTimeStamp < 500) {
                        clickCount++
                    } else {
                        clickCount = 1
                    }
                    lastTimeStamp = System.currentTimeMillis()
                    if (clickCount >= maxClickCount) {
                        mToast = Toast.makeText(context, "啥都木有", Toast.LENGTH_SHORT)
                        mToast?.show()
                    } else {
                        mToast =
                            Toast.makeText(
                                context,
                                "Click $clickCount times",
                                Toast.LENGTH_SHORT,
                            )
                        mToast?.show()
                    }
                },
            )
        }
    }
    if (selectColorDialogOpened) {
        Dialog(onDismissRequest = {
            selectColorDialogOpened = false
        }) {
            Card(
                colors =
                    CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background),
                modifier =
                    Modifier
                        .fillMaxHeight(.7f),
            ) {
                LazyColumn(
                    modifier =
                        Modifier
                            .padding(8.dp, 16.dp),
                ) {
                    items(themeColorKeys) { it ->
                        ListItem(
                            leadingContent = {
                                Icon(
                                    imageVector =
                                        if (appConfig.theme.themeColor == it) {
                                            Icons.Filled.Palette
                                        } else {
                                            Icons.Outlined.Palette
                                        },
                                    contentDescription = null,
                                    tint = getPrimaryColorMap(isDarkMode, it),
                                )
                            },
                            headlineContent = {
                                Text(text = themeColorNamesMap.get(it) ?: "")
                            },
                            modifier =
                                Modifier.clickable(
                                    enabled = true,
                                    onClick = {
                                        viewModel.updateThemeConfig(
                                            appConfig.theme.copy(
                                                themeColor = it,
                                            ),
                                        )
                                        selectColorDialogOpened = false
                                    },
                                ),
                        )
                    }
                }
            }
        }
    }
    
}

@Composable
fun SettingItemGroup(title: String) {
    Text(
                text = title,
                color = MaterialTheme.colorScheme.primary,
                fontSize = 14.sp,
                modifier =
                    Modifier
                        .padding(start = 16.dp, top = 16.dp)
                        .padding(vertical = 4.dp),
            )
}

// 清除数据库并重建表，然后退出应用
private fun clearDatabase(context: Context) {
    try {
        // 获取数据库实例并删除
        val dbHelper = DBHelper(context)
        val database = dbHelper.writableDatabase
        database.execSQL("DROP TABLE IF EXISTS T_AppInfo")
        // 重新创建表
        dbHelper.onCreate(database)
        dbHelper.close()
        
        // 显示Toast提示
        Toast.makeText(
            context,
            context.getString(R.string.database_cleared_message),
            Toast.LENGTH_SHORT
        ).show()
        
        // 退出所有Activity
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        context.startActivity(intent)
        android.os.Process.killProcess(android.os.Process.myPid())
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(
            context,
            context.getString(R.string.database_clear_failed),
            Toast.LENGTH_SHORT
        ).show()
    }
}

private fun backupPreferences(context: Context) {
    try {
        // 获取DataStore文件路径
        val dataStoreFile = context.filesDir.resolve("datastore/settings.preferences_pb")
        // 创建备份文件路径
        val backupDir = context.getExternalFilesDir(null)
        val backupFile = File(backupDir, "preference_bak.dat")
        
        // 确保备份目录存在
        backupDir?.mkdirs()
        
        // 复制文件
        FileInputStream(dataStoreFile).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }
        
        Toast.makeText(context, R.string.backup_successful, Toast.LENGTH_SHORT).show()
    } catch (e: IOException) {
        Toast.makeText(context, R.string.backup_failed, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

private fun restorePreferences(context: Context, uri: Uri) {
    try {
        // 获取DataStore文件路径
        val dataStoreFile = context.filesDir.resolve("datastore/settings.preferences_pb")
        
        // 确保DataStore目录存在
        dataStoreFile.parentFile?.mkdirs()
        
        // 从URI读取文件并写入DataStore
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dataStoreFile).use { output ->
                input.copyTo(output)
            }
        }
        
        Toast.makeText(context, R.string.restore_successful, Toast.LENGTH_SHORT).show()
        // 延迟结束进程，让用户看到成功提示
        Thread { 
            try {
                Thread.sleep(1000) // 等待1秒
                System.exit(0) // 结束进程
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    } catch (e: IOException) {
        Toast.makeText(context, R.string.restore_failed, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

private fun shareBackupFile(context: Context) {
    try {
        // 获取备份文件路径
        val backupDir = context.getExternalFilesDir(null)
        val backupFile = File(backupDir, "preference_bak.dat")
        
        // 检查备份文件是否存在
        if (!backupFile.exists()) {
            Toast.makeText(context, R.string.backup_file_not_found, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 确保文件可读
        if (!backupFile.canRead()) {
            Toast.makeText(context, R.string.share_failed, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 创建分享Intent
        val shareIntent = Intent(Intent.ACTION_SEND)
        // 使用通用的文件类型
        shareIntent.type = "*/*"
        // 使用FileProvider安全地获取文件URI
        val fileUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            FileProvider.getUriForFile(context, "com.h3110w0r1d.t9launcher.fileprovider", backupFile)
        } else {
            backupFile.toUri()
        }
        shareIntent.putExtra(Intent.EXTRA_STREAM, fileUri)
        
        // 添加权限标志
        shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        
        // 启动分享活动
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share_backup)))
    } catch (e: Exception) {
        // 显示更详细的错误信息
        Toast.makeText(context, "${context.getString(R.string.share_failed)}: ${e.message}", Toast.LENGTH_LONG).show()
        e.printStackTrace()
    }
}

private fun backupDatabase(context: Context) {
    try {
        // 获取数据库文件路径
        val dbPath = context.getDatabasePath("AppList4.db")
        
        // 创建备份文件路径到 Downloads 目录
        val downloadsDir = android.os.Environment.getExternalStoragePublicDirectory(android.os.Environment.DIRECTORY_DOWNLOADS)
        val backupFile = File(downloadsDir, "AppList4_backup_${System.currentTimeMillis()}.db")
        
        // 确保目录存在
        downloadsDir.mkdirs()
        
        // 复制文件
        FileInputStream(dbPath).use { input ->
            FileOutputStream(backupFile).use { output ->
                input.copyTo(output)
            }
        }
        
        // 显示成功提示和路径
        Toast.makeText(context, context.getString(R.string.database_backup_successful), Toast.LENGTH_SHORT).show()
        
        // 显示路径对话框
        android.app.AlertDialog.Builder(context)
            .setTitle(R.string.database_backup_successful)
            .setMessage(context.getString(R.string.database_backup_path, backupFile.absolutePath))
            .setPositiveButton(android.R.string.ok, null)
            .show()
    } catch (e: IOException) {
        Toast.makeText(context, R.string.database_backup_failed, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

private fun restoreDatabase(context: Context, uri: Uri) {
    try {
        // 检查文件后缀
        val fileName = context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                cursor.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME).let { index ->
                    if (index >= 0) cursor.getString(index) else null
                }
            } else null
        }
        
        if (fileName != null && !fileName.endsWith(".db")) {
            Toast.makeText(context, R.string.invalid_database_file, Toast.LENGTH_SHORT).show()
            return
        }
        
        // 获取数据库文件路径
        val dbPath = context.getDatabasePath("AppList4.db")
        
        // 确保数据库目录存在
        dbPath.parentFile?.mkdirs()
        
        // 从URI读取文件并写入数据库目录
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(dbPath).use { output ->
                input.copyTo(output)
            }
        }
        
        Toast.makeText(context, R.string.database_restore_successful, Toast.LENGTH_SHORT).show()
        // 延迟结束进程，让用户看到成功提示
        Thread { 
            try {
                Thread.sleep(1000) // 等待1秒
                System.exit(0) // 结束进程
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
        }.start()
    } catch (e: IOException) {
        Toast.makeText(context, R.string.database_restore_failed, Toast.LENGTH_SHORT).show()
        e.printStackTrace()
    }
}

@Composable
fun SettingItem(
    imageVector: ImageVector,
    title: String,
    description: String? = null,
    onClick: () -> Unit,
    trailingContent: @Composable (() -> Unit)? = null
) {
    ListItem(
        leadingContent = {
            Icon(
                imageVector = imageVector,
                contentDescription = null,
                modifier = if (description != null)Modifier.height(42.dp) else Modifier,
            )
        },
        headlineContent = { Text(title) },
        supportingContent = { if (description != null) Text(description) },
        trailingContent = trailingContent,
        modifier =
            Modifier.clickable(
                onClick = onClick,
            ),
    )
}
