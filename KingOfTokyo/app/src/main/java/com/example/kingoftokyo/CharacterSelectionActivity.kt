package com.example.kingoftokyo

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class CharacterSelectionActivity : AppCompatActivity() {

    private var selectedMonster: Monster? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_character_selection)

        val monstersRecyclerView = findViewById<RecyclerView>(R.id.monstersRecyclerView)
        val playGameButton = findViewById<Button>(R.id.playGameButton)

        monstersRecyclerView.layoutManager = GridLayoutManager(this, 2)

        val adapter = MonsterAdapter(GameData.monsters) { monster ->
            selectedMonster = monster
            playGameButton.isEnabled = true
            playGameButton.alpha = 1.0f
        }
        monstersRecyclerView.adapter = adapter
    }
}
