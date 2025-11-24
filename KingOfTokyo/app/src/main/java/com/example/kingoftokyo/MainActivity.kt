package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Correction : On charge le fichier de layout complet
        setContentView(R.layout.activity_main)

        val playButton = findViewById<Button>(R.id.playButton)
        val quitButton = findViewById<Button>(R.id.quitButton)

        playButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            startActivity(intent)
        }


        quitButton.setOnClickListener {
            finish() // Ferme l'application
        }
    }
}
