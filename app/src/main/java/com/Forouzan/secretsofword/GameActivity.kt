package com.Forouzan.secretsofword

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

data class WordData(val id: Int, val word: String, val letters: List<String>)

class GameActivity : AppCompatActivity() {

    private var playerWord = ""
    private lateinit var correctWord: String
    private lateinit var shuffledLetters: List<String>
    private var currentLevel = 1
    private var remainingLives = 3
    private var playerPoints = 3
    private var correctAnswersInARow = 0  // شمارنده مراحل درست
    private lateinit var retryButton: Button

    private lateinit var victorySound: MediaPlayer
    private lateinit var loseSound: MediaPlayer
    private lateinit var wrongAnswerSound: MediaPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        // بارگذاری صداها
        victorySound = MediaPlayer.create(this, R.raw.victory_sound)
        loseSound = MediaPlayer.create(this, R.raw.lose_sound)
        wrongAnswerSound = MediaPlayer.create(this, R.raw.wrong_answer_sound)

        currentLevel = intent.getIntExtra("level", 1)
        remainingLives = loadPlayerLives()
        playerPoints = loadPlayerPoints()

        val levelText = findViewById<TextView>(R.id.level_text)
        levelText.text = "مرحله: $currentLevel"

        loadWordData(currentLevel)
        setupGameUI()
    }

    private fun loadWordData(level: Int) {
        val json = readJsonFile("words.json")
        val type = object : TypeToken<List<WordData>>() {}.type
        val words: List<WordData> = Gson().fromJson(json, type)

        val wordData = words.find { it.id == level } ?: return
        correctWord = wordData.word

        shuffledLetters = correctWord.toList().shuffled().map { it.toString() }
    }

    private fun readJsonFile(fileName: String): String {
        val inputStream = assets.open(fileName)
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))
        return bufferedReader.use { it.readText() }
    }

    private fun setupGameUI() {
        val wordPlaceholder = findViewById<TextView>(R.id.word_placeholder)
        val lettersGrid = findViewById<GridLayout>(R.id.letters_grid)
        retryButton = findViewById<Button>(R.id.retry_button)

        val buyLifeButton = findViewById<Button>(R.id.buy_life_button)
        val getHintButton = findViewById<Button>(R.id.get_hint_button)
        val pointsText = findViewById<TextView>(R.id.points_text)

        wordPlaceholder.text = "_ ".repeat(correctWord.length)
        updateLivesDisplay()
        updatePointsDisplay(pointsText)

        lettersGrid.removeAllViews()
        for (letter in shuffledLetters) {
            val button = Button(this)
            button.text = letter
            button.setOnClickListener {
                updateWord(letter)
                button.isEnabled = false
            }
            lettersGrid.addView(button)
        }

        retryButton.setOnClickListener {
            restartLevel()
        }
        retryButton.visibility = Button.GONE

        buyLifeButton.setOnClickListener {
            if (remainingLives < 3) {  // بررسی اینکه زندگی‌ها کمتر از 3 باشد
                if (playerPoints >= 5) {  // چک کردن امتیاز
                    playerPoints -= 5  // کسر امتیاز
                    remainingLives++  // افزایش زندگی
                    updateLivesDisplay()
                    updatePointsDisplay(pointsText)
                    savePlayerPoints(playerPoints)
                } else {
                    Toast.makeText(this, "امتیازات کافی نیست!برای اضافه کردن قلب باید امتیازات خود را به 5 برسانید!", Toast.LENGTH_SHORT).show()
                }
            } else {
                // پیغام در صورتی که زندگی‌ها به 3 رسیده باشد
                Toast.makeText(this, "شما به حداکثر تعداد زندگی‌ها رسیده‌اید!", Toast.LENGTH_SHORT).show()
            }
        }

        getHintButton.setOnClickListener {
            if (playerPoints >= 10) {
                playerPoints -= 10
                provideHint()
                updatePointsDisplay(pointsText)
                savePlayerPoints(playerPoints)
            } else {
                Toast.makeText(this, "امتیازات کافی نیست!برای ایجاد سرنخ امتیاز خود را به 10 برسانید!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun provideHint() {
        val wordPlaceholder = findViewById<TextView>(R.id.word_placeholder)
        val revealedIndex = playerWord.length
        if (revealedIndex < correctWord.length) {
            playerWord += correctWord[revealedIndex]
            val displayedWord = playerWord.padEnd(correctWord.length, '_').map { "$it " }.joinToString(" ")
            wordPlaceholder.text = displayedWord
            if (playerWord.length == correctWord.length) {
                checkAnswer()
            }
        }
    }

    private fun updateWord(letter: String) {
        playerWord += letter
        val wordPlaceholder = findViewById<TextView>(R.id.word_placeholder)

        val displayedWord = playerWord.padEnd(correctWord.length, '_').map { "$it " }.joinToString(" ")
        wordPlaceholder.text = displayedWord

        if (playerWord.length == correctWord.length) {
            checkAnswer()
        }
    }

    private fun checkAnswer() {
        if (playerWord.equals(correctWord, true)) {
            Toast.makeText(this, "تبریک! شما درست گفتید.", Toast.LENGTH_SHORT).show()
            victorySound.start()
            savePlayerProgress(currentLevel)
            savePlayerLives(remainingLives)
            playerPoints += 1 // جایزه امتیاز
            savePlayerPoints(playerPoints)
            goToNextLevel()
        } else {
            Toast.makeText(this, "متاسفم، جواب اشتباه بود.", Toast.LENGTH_SHORT).show()
            wrongAnswerSound.start()
            updateLives(false)
            retryButton.visibility = Button.VISIBLE
        }
    }

    private fun updateLives(isCorrect: Boolean) {
        if (!isCorrect) {
            remainingLives--  // کاهش تعداد زندگی‌ها
            updateLivesDisplay()

            if (remainingLives <= 0) {
                Toast.makeText(this, "متاسفم، شما باختید!", Toast.LENGTH_SHORT).show()
                loseSound.start()
                resetGame()
            }
        }
    }

    private fun updateLivesDisplay() {
        val livesContainer = findViewById<LinearLayout>(R.id.lives_container)
        livesContainer.removeAllViews()

        for (i in 1..3) {
            val heartImage = ImageView(this)
            val drawable = if (i <= remainingLives) {
                R.drawable.heart_full
            } else {
                R.drawable.heart_empty
            }
            heartImage.setImageResource(drawable)
            val params = LinearLayout.LayoutParams(100, 100)
            params.marginEnd = 8
            heartImage.layoutParams = params
            livesContainer.addView(heartImage)
        }
    }

    private fun updatePointsDisplay(pointsText: TextView) {
        pointsText.text = "امتیاز: $playerPoints"
    }

    private fun resetGame() {
        currentLevel = 1
        remainingLives = 3
        playerPoints = 0
        correctAnswersInARow = 0  // ریست کردن شمارنده مراحل درست
        savePlayerProgress(0)
        savePlayerLives(remainingLives)
        savePlayerPoints(playerPoints)
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("level", currentLevel)
        startActivity(intent)
        finish()
    }

    private fun restartLevel() {
        playerWord = ""
        retryButton.visibility = Button.GONE
        setupGameUI()
    }

    private fun goToNextLevel() {
        val nextLevel = currentLevel + 1
        val intent = Intent(this, GameActivity::class.java)
        intent.putExtra("level", nextLevel)
        startActivity(intent)
        finish()
    }

    private fun savePlayerProgress(level: Int) {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("last_completed_level", level)
            apply()
        }
    }

    private fun savePlayerLives(lives: Int) {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("remaining_lives", lives)
            apply()
        }
    }

    private fun savePlayerPoints(points: Int) {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        with(sharedPref.edit()) {
            putInt("player_points", points)
            apply()
        }
    }

    private fun loadPlayerLives(): Int {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        return sharedPref.getInt("remaining_lives", 3)
    }

    private fun loadPlayerPoints(): Int {
        val sharedPref = getSharedPreferences("game_progress", MODE_PRIVATE)
        return sharedPref.getInt("player_points", 3)
    }

    override fun onDestroy() {
        super.onDestroy()
        victorySound.release()
        loseSound.release()
        wrongAnswerSound.release()
    }
}
