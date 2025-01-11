package com.Forouzan.secretsofword

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.GridLayout
import androidx.appcompat.app.AppCompatActivity

class LevelsMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_levels_menu)

        val levelsGrid = findViewById<GridLayout>(R.id.levels_grid)
        val part = intent.getIntExtra("part", 1) // دریافت شماره بخش
        val lastCompletedLevel = getLastCompletedLevel() // دریافت آخرین سطح تکمیل‌شده
        val firstLevelInPart = (part - 1) * 36 + 1 // محاسبه اولین سطح در بخش
        val lastLevelInPart = part * 36 // محاسبه آخرین سطح در بخش

        Log.d("LevelsMenuActivity", "Part: $part, Last Completed Level: $lastCompletedLevel")

        // اضافه کردن دکمه‌ها برای هر سطح در بخش انتخاب‌شده
        for (level in firstLevelInPart..lastLevelInPart) {
            val button = Button(this)
            button.text = "مرحله $level"
            button.textSize = 16f

            // اگر سطح قابل دسترس باشد (سطح قفل نیست)
            if (level <= lastCompletedLevel + 1) {
                button.background = getDrawable(R.drawable.rounded_button)
                button.setOnClickListener {
                    // ارسال شماره سطح به GameActivity
                    Log.d("LevelsMenuActivity", "Starting level: $level")
                    val intent = Intent(this, GameActivity::class.java)
                    intent.putExtra("level", level)  // ارسال شماره سطح
                    startActivity(intent)
                }
            } else {
                button.background = getDrawable(R.drawable.locked_button) // سطح قفل‌شده
                button.isEnabled = false
            }

            val layoutParams = GridLayout.LayoutParams()
            layoutParams.setMargins(8, 8, 8, 8) // حاشیه برای دکمه‌ها
            button.layoutParams = layoutParams

            levelsGrid.addView(button)
        }

        // دکمه برگشت برای بازگشت به صفحه اصلی
        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()  // برگشت به صفحه قبلی
        }
    }

    // بارگذاری آخرین سطح تکمیل‌شده از SharedPreferences
    private fun getLastCompletedLevel(): Int {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        return sharedPref.getInt("last_completed_level", 0) // پیش‌فرض سطح 0
    }
}