package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import com.example.ui.AriseApp
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // Install a global diagnostics crash capturer for robust systems validation
    val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
    Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
      android.util.Log.e("ARISE_CRASH", "FATAL CRASH DETECTED IN THREAD [${thread.name}]", throwable)
      throwable?.printStackTrace()
      defaultHandler?.uncaughtException(thread, throwable)
    }

    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        AriseApp()
      }
    }
  }
}
