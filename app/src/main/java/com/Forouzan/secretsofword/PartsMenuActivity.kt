package com.Forouzan.secretsofword

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import androidx.appcompat.app.AppCompatActivity

class PartsMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu_part)

        val partsList = findViewById<LinearLayout>(R.id.parts_list)

        for (part in 1..3) {
            val button = Button(this)
            button.text = "پارت $part"
            button.textSize = 18f
            button.setPadding(16, 16, 16, 16)
            button.background = getDrawable(R.drawable.rounded_button)

            button.setOnClickListener {
                val intent = Intent(this, LevelsMenuActivity::class.java)
                intent.putExtra("part", part)
                startActivity(intent)
            }

            partsList.addView(button)
        }

        findViewById<Button>(R.id.back_button).setOnClickListener {
            finish()
        }
    }
}