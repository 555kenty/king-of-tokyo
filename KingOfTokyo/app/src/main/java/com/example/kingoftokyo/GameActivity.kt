package com.example.kingoftokyo

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var players: List<Player>
    private lateinit var dicePopupManager: DicePopupManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        val selectedMonsterName = intent.getStringExtra("selectedMonsterName")
        val allMonsters = GameData.monsters.toMutableList()

        val humanMonster = allMonsters.find { it.name == selectedMonsterName }!!
        allMonsters.remove(humanMonster)

        val humanPlayer = Player(humanMonster, isHuman = true)

        val bots = allMonsters.shuffled().take(3).map { Player(it) }

        players = listOf(humanPlayer) + bots

        setupUI()

        val rootView = findViewById<ViewGroup>(android.R.id.content)
        dicePopupManager = DicePopupManager(this, rootView)

        val rollDiceButton = findViewById<Button>(R.id.rollDiceButton)
        rollDiceButton.setOnClickListener {
            dicePopupManager.showDicePopup()
        }
    }

    private fun setupUI() {
        val playerViews = listOf(
            findViewById<View>(R.id.playerHud1) to findViewById<ImageView>(R.id.monsterImage1),
            findViewById<View>(R.id.playerHud2) to findViewById<ImageView>(R.id.monsterImage2),
            findViewById<View>(R.id.playerHud3) to findViewById<ImageView>(R.id.monsterImage3),
            findViewById<View>(R.id.playerHud4) to findViewById<ImageView>(R.id.monsterImage4)
        )

        for (i in players.indices) {
            val player = players[i]
            val (hud, monsterImage) = playerViews[i]

            monsterImage.setImageResource(player.monster.image)

            hud.findViewById<TextView>(R.id.monsterName).text = player.monster.name
            hud.findViewById<TextView>(R.id.monsterHealth).text = getString(R.string.health_format, player.health)
            hud.findViewById<TextView>(R.id.monsterEnergy).text = getString(R.string.energy_format, player.energy)
            hud.findViewById<TextView>(R.id.monsterVictoryPoints).text = getString(R.string.victory_points_format, player.victoryPoints)
        }
    }
}
