package com.h3110w0r1d.t9launcher.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
 
class BatchOperations4SystemOnlyActivity : BatchOperationsActivity() {
 
    override fun onCreate(savedInstanceState: Bundle?) {
               // 修改Intent，将operation4SystemApps参数强制设置为true
        val modifiedIntent = Intent(intent).apply {
            putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, true)
        }
        setIntent(modifiedIntent)
          
        super.onCreate(savedInstanceState)
    }
 
 
}