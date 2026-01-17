package com.h3110w0r1d.t9launcher.activity

import android.content.pm.PackageManager
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.utils.DBHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

data class AppStartInfo(
    val packageName: String,
    val startCount: Int,
    val appName: String,
    val appIcon: Drawable,
    val packageExist: Boolean
)

class StartCountStatisticalActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            StartCountScreen()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StartCountScreen() {
    val context = LocalContext.current
    val appStartInfoList = remember { mutableStateListOf<AppStartInfo>() }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            loadAppStartInfo(context, appStartInfoList)
        }
        isLoading = false
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.start_statistics)) },
                navigationIcon = {
                    IconButton(onClick = { (context as ComponentActivity).finish() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
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
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                items(appStartInfoList) { appInfo ->
                    val alpha = if (appInfo.packageExist) 1f else 0.5f
                    ListItem(
                        leadingContent = {
                            Image(
                                bitmap = appInfo.appIcon.toBitmap().asImageBitmap(),
                                contentDescription = stringResource(R.string.app_icon),
                                modifier = Modifier.size(48.dp),
                                alpha = alpha
                            )
                        },
                        headlineContent = {
                            Text(
                                text = appInfo.appName,
                                color = if (appInfo.packageExist) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                                textDecoration = if (!appInfo.packageExist) 
                                    TextDecoration.LineThrough 
                                else 
                                    null
                            )
                        },
                        trailingContent = {
                            Text(
                                text = stringResource(R.string.start_count_format, appInfo.startCount),
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (appInfo.packageExist) 
                                    MaterialTheme.colorScheme.onSurface 
                                else 
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        },
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

private fun loadAppStartInfo(context: android.content.Context, appStartInfoList: MutableList<AppStartInfo>) {
    val dbHelper = DBHelper(context)
    val db = dbHelper.readableDatabase
    val cursor: Cursor = db.query(
        "T_AppInfo",
        arrayOf("packageName", "startCount", "appName"),
        null,
        null,
        null,
        null,
        "startCount DESC"
    )

    val packageManager = context.packageManager

    while (cursor.moveToNext()) {
        val packageName = cursor.getString(0)
        val startCount = cursor.getInt(1)
        val appName = cursor.getString(2)

        try {
            val applicationInfo = packageManager.getApplicationInfo(packageName, 0)
            val appIcon = packageManager.getApplicationIcon(applicationInfo)

            appStartInfoList.add(AppStartInfo(packageName, startCount, appName, appIcon, true))
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            val systemIcon = android.R.drawable.sym_def_app_icon.let { 
                context.resources.getDrawable(it, null)
            }
            appStartInfoList.add(AppStartInfo(packageName, -startCount, appName, systemIcon, false))
        }
    }
    cursor.close()
    dbHelper.close()
}

private fun Drawable.toBitmap(): Bitmap {
    val bitmap = Bitmap.createBitmap(intrinsicWidth, intrinsicHeight, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)
    setBounds(0, 0, canvas.width, canvas.height)
    draw(canvas)
    return bitmap
}