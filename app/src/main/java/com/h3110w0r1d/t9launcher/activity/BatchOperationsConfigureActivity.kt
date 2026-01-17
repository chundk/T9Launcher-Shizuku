package com.h3110w0r1d.t9launcher.activity
import android.widget.Toast
import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.h3110w0r1d.t9launcher.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

val Context.batchOperationsDataStore: DataStore<Preferences> by preferencesDataStore(name = "batch_operations")

data class BatchAppInfo(
    val packageName: String,
    val appName: String,
    val appIcon: Drawable,
    val isSystemApp: Boolean
)

class BatchOperationsConfigureActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BatchOperationsScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchOperationsScreen() {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val userApps = remember { mutableStateListOf<BatchAppInfo>() }
    val systemApps = remember { mutableStateListOf<BatchAppInfo>() }
    val userAppsChecked = remember { mutableStateMapOf<String, Boolean>() }
    val systemAppsChecked = remember { mutableStateMapOf<String, Boolean>() }
    var isAllSelected by remember { mutableStateOf(false) }
    val listState = rememberLazyListState()
    var searchText by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            loadApps(context, userApps, systemApps)
            loadCheckedStates(context, userAppsChecked, systemAppsChecked)
        }
        isLoading = false
    }

    val filteredUserApps = remember(searchText) {
        if (searchText.isEmpty()) {
            userApps
        } else {
            userApps.filter { it.appName.contains(searchText, ignoreCase = true) }
        }
    }

    val filteredSystemApps = remember(searchText) {
        if (searchText.isEmpty()) {
            systemApps
        } else {
            systemApps.filter { it.appName.contains(searchText, ignoreCase = true) }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_list)) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val currentMap = if (selectedTab == 0) userAppsChecked else systemAppsChecked
                        val currentList = if (selectedTab == 0) {
                            if (searchText.isNotEmpty()) filteredUserApps else userApps
                        } else {
                            if (searchText.isNotEmpty()) filteredSystemApps else systemApps
                        }
                        isAllSelected = !isAllSelected
                        if (isAllSelected) {
                            currentList.forEach { currentMap[it.packageName] = true }
                        } else {
                            currentList.forEach { currentMap[it.packageName] = false }
                        }
                    }) {
                        Icon(Icons.Default.SelectAll, contentDescription = null)
                    }
                    IconButton(onClick = {
                        val currentMap = if (selectedTab == 0) userAppsChecked else systemAppsChecked
                        val prefName = if (selectedTab == 0) "batch_operations_4_user_apps" else "batch_operations_4_system_apps"
                        kotlinx.coroutines.GlobalScope.launch(Dispatchers.IO) {
                            saveCheckedStates(context, prefName, currentMap)
                        }
                        Toast.makeText(context, R.string.batch_operations_saved, Toast.LENGTH_SHORT).show()
                        (context as ComponentActivity).finish()
                    }) {
                        Icon(Icons.Default.Save, contentDescription = null)
                    }
                },
            )
        },
    ) { innerPadding ->
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                TabRow(selectedTabIndex = selectedTab) {
                    Tab(
                        selected = selectedTab == 0,
                        onClick = { selectedTab = 0 },
                        text = { Text(stringResource(R.string.user_apps)) }
                    )
                    Tab(
                        selected = selectedTab == 1,
                        onClick = { selectedTab = 1 },
                        text = { Text(stringResource(R.string.system_apps)) }
                    )
                }
                
                androidx.compose.material3.OutlinedTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    placeholder = { Text(stringResource(R.string.search_app)) },
                    leadingIcon = {
                        Icon(Icons.Default.Search, contentDescription = null)
                    },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = androidx.compose.ui.text.input.ImeAction.Search),
                    keyboardActions = KeyboardActions(
                        onSearch = { }
                    )
                )
                
                if (selectedTab == 0) {
                    UserAppsPage(filteredUserApps, userAppsChecked, listState)
                } else {
                    SystemAppsPage(filteredSystemApps, systemAppsChecked, listState)
                }
            }
        }
    }
}

@Composable
fun UserAppsPage(
    apps: List<BatchAppInfo>,
    checkedMap: Map<String, Boolean>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(state = listState) {
        items(apps) { app ->
            AppListItem(app, checkedMap[app.packageName] ?: false) { isChecked ->
                (checkedMap as MutableMap)[app.packageName] = isChecked
            }
        }
    }
}

@Composable
fun SystemAppsPage(
    apps: List<BatchAppInfo>,
    checkedMap: Map<String, Boolean>,
    listState: androidx.compose.foundation.lazy.LazyListState
) {
    LazyColumn(state = listState) {
        items(apps) { app ->
            AppListItem(app, checkedMap[app.packageName] ?: false) { isChecked ->
                (checkedMap as MutableMap)[app.packageName] = isChecked
            }
        }
    }
}

@Composable
fun AppListItem(
    app: BatchAppInfo,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    ListItem(
        leadingContent = {
            Image(
                bitmap = app.appIcon.toBitmap().asImageBitmap(),
                contentDescription = null,
                modifier = Modifier.size(48.dp)
            )
        },
        headlineContent = {
            Text(
                text = app.appName
            )
        },
        trailingContent = {
            androidx.compose.material3.Checkbox(
                checked = isChecked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.padding(end = 3.dp)
            )
        },
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
    )
}

private suspend fun loadApps(
    context: Context,
    userApps: MutableList<BatchAppInfo>,
    systemApps: MutableList<BatchAppInfo>
) {
    val packageManager = context.packageManager
    val installedPackages = packageManager.getInstalledApplications(PackageManager.GET_UNINSTALLED_PACKAGES)
    
    installedPackages.forEach { appInfo ->
        try {
            val appName = appInfo.loadLabel(packageManager).toString()
            val appIcon = packageManager.getApplicationIcon(appInfo)
            val isSystemApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            val batchAppInfo = BatchAppInfo(appInfo.packageName, appName, appIcon, isSystemApp)
            
            if (isSystemApp) {
                systemApps.add(batchAppInfo)
            } else {
                userApps.add(batchAppInfo)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    // 过滤掉自己的包名
    val ownPackageName = context.packageName
    userApps.removeIf { it.packageName == ownPackageName }
    systemApps.removeIf { it.packageName == ownPackageName }
    
    userApps.sortBy { it.appName }
    systemApps.sortBy { it.appName }
}

private suspend fun loadCheckedStates(
    context: Context,
    userAppsChecked: MutableMap<String, Boolean>,
    systemAppsChecked: MutableMap<String, Boolean>
) {
    val userAppsKey = stringPreferencesKey("batch_operations_4_user_apps")
    val systemAppsKey = stringPreferencesKey("batch_operations_4_system_apps")
    
    context.batchOperationsDataStore.data.map { preferences ->
        val userAppsData = preferences[userAppsKey] ?: ""
        val systemAppsData = preferences[systemAppsKey] ?: ""
        
        userAppsData.split(",").filter { it.isNotEmpty() }.forEach { packageName ->
            userAppsChecked[packageName] = true
        }
        
        systemAppsData.split(",").filter { it.isNotEmpty() }.forEach { packageName ->
            systemAppsChecked[packageName] = true
        }
    }.first()
}

private suspend fun saveCheckedStates(
    context: Context,
    prefName: String,
    checkedMap: Map<String, Boolean>
) {
    val key = stringPreferencesKey(prefName)
    val checkedPackages = checkedMap.filter { it.value }.keys.joinToString(",")
    
    context.batchOperationsDataStore.edit { preferences ->
        preferences[key] = checkedPackages
    }
}

private fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}
