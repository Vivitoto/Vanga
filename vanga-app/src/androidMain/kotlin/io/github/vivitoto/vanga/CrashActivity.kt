package io.github.vivitoto.vanga

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.github.vivitoto.vanga.ui.error.ErrorView

class CrashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val exceptionData = GlobalExceptionHandler.getExceptionDataFromIntent(intent)
        val exceptionMessage = if (exceptionData == null) "未知错误"
        else "${exceptionData.exceptionName}: ${exceptionData.message}"

        setContent {
            ErrorView(
                exceptionMessage = exceptionMessage,
                stacktrace = exceptionData?.stacktrace,
                isRestartable = true,
                onRestart = {
                    finishAffinity()
                    startActivity(Intent(this@CrashActivity, MainActivity::class.java))
                },
                onExit = { this.finishAndRemoveTask() }
            )
        }
    }
}