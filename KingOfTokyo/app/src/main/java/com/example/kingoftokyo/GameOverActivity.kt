package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        val titleTextView = findViewById<TextView>(R.id.gameOverTitleTextView)
        val playAgainButton = findViewById<Button>(R.id.playAgainButton)
        val quitButton = findViewById<Button>(R.id.quitButton)

        val playerWon = intent.getBooleanExtra("playerWon", false)
        titleTextView.text = if (playerWon) "VICTOIRE !" else "PARTIE PERDUE"

        playAgainButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        quitButton.setOnClickListener {
            finishAffinity()
        }
    }
}
