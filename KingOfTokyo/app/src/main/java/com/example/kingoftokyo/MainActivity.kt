package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        playButton.setOnClickListener {
            startActivity(Intent(this, CharacterSelectionActivity::class.java))
        }

        val quitButton = findViewById<Button>(R.id.quitButton)
        quitButton.setOnClickListener {
            finish()
        }
    }
}
