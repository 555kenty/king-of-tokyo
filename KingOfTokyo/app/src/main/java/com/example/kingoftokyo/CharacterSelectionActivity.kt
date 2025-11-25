package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
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
        val monsterDescription = findViewById<TextView>(R.id.monsterDescription)

        val columns = resources.getInteger(R.integer.character_grid_columns)
        monstersRecyclerView.layoutManager = GridLayoutManager(this, columns)
        val backButton = findViewById<ImageButton>(R.id.backButton)
        backButton.setOnClickListener {
            finish()  // retourne à MainActivity automatiquement
        }
        val adapter = MonsterAdapter(GameData.monsters) { monster ->
            selectedMonster = monster
            playGameButton.isEnabled = true
            playGameButton.alpha = 1.0f
            monsterDescription.text = monster.description
        }
        monstersRecyclerView.adapter = adapter

        playGameButton.setOnClickListener {
            selectedMonster?.let {
                val intent = Intent(this, GameActivity::class.java)
                intent.putExtra("selectedMonsterName", it.name)
                startActivity(intent)
            }
        }
    }
}
