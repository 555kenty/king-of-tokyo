package com.example.kingoftokyo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var menuMusicPlayer: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        try {
            menuMusicPlayer = MediaPlayer.create(this, R.raw.menu_bgm)
            menuMusicPlayer?.isLooping = true
            menuMusicPlayer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val playButton = findViewById<Button>(R.id.playButton)
        val quitButton = findViewById<Button>(R.id.quitButton)

        playButton.setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            startActivity(intent)
        }

        quitButton.setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        menuMusicPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        menuMusicPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuMusicPlayer?.release()
    }
}