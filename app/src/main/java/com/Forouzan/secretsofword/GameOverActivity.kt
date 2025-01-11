package com.Forouzan.secretsofword

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val restartButton = findViewById<Button>(R.id.restart_button)
        restartButton.setOnClickListener {
            restartGame()
        }
    }

    private fun restartGame() {
        // Reset the player's lives to 5 in SharedPreferences
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("remaining_lives", 5)  // بازنشانی تعداد زندگی‌ها به ۵
            apply()
        }

        // Start the MainActivity or GameActivity again
        val intent = Intent(this, GameActivity::class.java)  // یا MainActivity
        intent.putExtra("level", 1)  // برگرداندن بازی از سطح اول
        startActivity(intent)
        finish()
    }
}