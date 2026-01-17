package com.h3110w0r1d.t9launcher.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.CheckBox
import android.widget.CompoundButton
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import com.h3110w0r1d.t9launcher.App
import com.h3110w0r1d.t9launcher.R
import com.h3110w0r1d.t9launcher.activity.EXTRA_OPERATION_4_SYSTEM_APPS
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first

/**
 * 检查用户是否已经确认过危险操作
 */
const val DANGEROUS_CONFIRM_KEY = "batch_operations_dangerous_confirmed"
fun checkDangerousConfirmStatus(context: Context, callback: (Boolean) -> Unit) {
    GlobalScope.launch(Dispatchers.IO) {
        
        // 读取preference
        val preferences = context.batchOperationsDataStore.data.first()
        val isConfirmed = preferences[booleanPreferencesKey(DANGEROUS_CONFIRM_KEY)] ?: false
        
        withContext(Dispatchers.Main) {
            callback(isConfirmed)
        }
    }
}

class BatchOperationsSystemConfirmActivity : Activity() {

    companion object {
         
        const val EXTRA_ACTION = "EXTRA_ACTION"
        const val EXTRA_LAUNCH_FROM_WIDGET = "EXTRA_LAUNCH_FROM_WIDGET"
        const val EXTRA_SHOW_CONFIRMATION_ONLY = "EXTRA_SHOW_CONFIRMATION_ONLY"
    }

    private lateinit var confirmButton: Button
    private lateinit var confirmCheckBox: CheckBox
    private var action: String? = null
    private var isFromWidget = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 如果是只显示确认界面模式，直接显示确认界面
        if (intent.getBooleanExtra(EXTRA_SHOW_CONFIRMATION_ONLY, false)) {
            showConfirmationView()
            return
        }

        // 获取传入的参数
        action = intent.getStringExtra(EXTRA_ACTION)
        isFromWidget = intent.getBooleanExtra(EXTRA_LAUNCH_FROM_WIDGET, false)
      
        // 检查用户是否已经确认过
        checkDangerousConfirmStatus(this) { isConfirmed ->
            if (isConfirmed) {
                // 用户已经确认过，直接启动目标Activity
                val intent = Intent(this@BatchOperationsSystemConfirmActivity, BatchOperations4SystemOnlyActivity::class.java)
                action?.let { actionValue -> intent.action = actionValue }
                intent.putExtra(EXTRA_LAUNCH_FROM_WIDGET, isFromWidget)
                intent.putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, true)
                startActivity(intent)
                finish()
            } else {
                // 用户未确认过，显示确认界面
                showConfirmationView()
            }
        }
    }

    private fun showConfirmationView() {
        // 创建相对布局作为最上层容器
        val relativeLayout = RelativeLayout(this)
        relativeLayout.setBackgroundColor(resources.getColor(android.R.color.white, theme))

        // 创建线性布局
        val layout = LinearLayout(this)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(40, 40, 40, 40)

        // 设置线性布局在相对布局中居中
        val layoutParams = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.layoutParams = layoutParams

        // 创建警告文本
        val warningText = TextView(this)
        warningText.text = getString(R.string.batch_operations_dangerous_warning)
        warningText.textSize = 16f
        warningText.setTextColor(resources.getColor(android.R.color.darker_gray, theme))
        warningText.textAlignment = TextView.TEXT_ALIGNMENT_CENTER
        warningText.setPadding(0, 0, 0, 30)

        // 创建复选框
        confirmCheckBox = CheckBox(this)
        confirmCheckBox.text = getString(R.string.batch_operations_dangerous_confirm)
        confirmCheckBox.textSize = 14f
        confirmCheckBox.setPadding(0, 0, 0, 30)
        // 设置复选框与文字水平对齐居中
        confirmCheckBox.gravity = android.view.Gravity.CENTER_HORIZONTAL

        // 创建确认按钮
        confirmButton = Button(this)
        confirmButton.text = getString(R.string.batch_operations_dangerous_confirm_button)
        confirmButton.isEnabled = false

        // 设置复选框监听器
        confirmCheckBox.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
            confirmButton.isEnabled = isChecked
        }

        // 设置确认按钮监听器
        confirmButton.setOnClickListener {
            // 保存确认状态
            saveConfirmation()
        }

        // 添加布局组件
        layout.addView(warningText)
        layout.addView(confirmCheckBox)
        layout.addView(confirmButton)

        // 将线性布局添加到相对布局中
        relativeLayout.addView(layout)

        // 设置内容视图
        setContentView(relativeLayout)
    }

    private fun saveConfirmation() {
        // 保存确认状态到数据存储
        GlobalScope.launch {
            val dataStore = (applicationContext as App).batchOperationsDataStore
            dataStore.edit { preferencesEditor ->
                preferencesEditor[booleanPreferencesKey(DANGEROUS_CONFIRM_KEY)] = true
            }

            // 启动目标Activity
            val intent = Intent(this@BatchOperationsSystemConfirmActivity, BatchOperations4SystemOnlyActivity::class.java)
            action?.let { actionValue -> intent.action = actionValue }
            intent.putExtra(EXTRA_LAUNCH_FROM_WIDGET, isFromWidget)
            intent.putExtra(EXTRA_OPERATION_4_SYSTEM_APPS, true)
            startActivity(intent)
            finish()
        }
    }
}
