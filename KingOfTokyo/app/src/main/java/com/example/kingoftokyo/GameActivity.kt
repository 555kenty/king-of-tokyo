package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private lateinit var dicePopupManager: DicePopupManager
    private lateinit var tokyoChoicePopupManager: TokyoChoicePopupManager

    private val playerHudViews = mutableListOf<View>()
    private val playerMonsterImageViews = mutableListOf<ImageView>()
    private lateinit var tokyoCityImageView: ImageView
    private lateinit var rollDiceButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()

        dicePopupManager = DicePopupManager(this, findViewById(android.R.id.content))
        tokyoChoicePopupManager = TokyoChoicePopupManager(this, findViewById(android.R.id.content))

        gameManager = GameManager(
            onUpdate = { runOnUiThread { updateUI() } },
            onBotTurn = { diceResult -> runOnUiThread { dicePopupManager.showDiceForBot(diceResult) } },
            onGameOver = { playerWon -> runOnUiThread { launchGameOverScreen(playerWon) } },
            onTokyoChoice = { defender, attacker ->
                runOnUiThread { promptTokyoChoice(defender, attacker) }
            }
        )

        val selectedMonsterName = intent.getStringExtra("selectedMonsterName") ?: GameData.monsters.first().name
        gameManager.setupGame(selectedMonsterName)

        rollDiceButton.setOnClickListener {
            if (gameManager.getCurrentPlayer().isHuman && gameManager.gameState == GameState.RUNNING) {
                dicePopupManager.showDiceForHuman {
                    gameManager.resolveDice(it)
                }
            }
        }
    }

    private fun promptTokyoChoice(defender: Player, attacker: Player) {
        tokyoChoicePopupManager.showChoicePopup { wantsToStay ->
            gameManager.playerDecidedTokyo(wantsToStay, defender, attacker)
        }
    }

    private fun launchGameOverScreen(playerWon: Boolean) {
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("playerWon", playerWon)
        startActivity(intent)
        finish()
    }

    private fun initViews() {
        playerHudViews.addAll(listOf(
            findViewById(R.id.playerHud1),
            findViewById(R.id.playerHud2),
            findViewById(R.id.playerHud3),
            findViewById(R.id.playerHud4)
        ))
        playerMonsterImageViews.addAll(listOf(
            findViewById(R.id.monsterImage1),
            findViewById(R.id.monsterImage2),
            findViewById(R.id.monsterImage3),
            findViewById(R.id.monsterImage4)
        ))
        tokyoCityImageView = findViewById(R.id.tokyo_city)
        rollDiceButton = findViewById(R.id.rollDiceButton)
    }

    private fun updateUI() {
        if (gameManager.gameState == GameState.GAME_OVER) return

        gameManager.players.forEachIndexed { index, player ->
            val hud = playerHudViews[index]
            val monsterImage = playerMonsterImageViews[index]

            // CORRECTION : Ligne manquante pour afficher l'image du monstre
            monsterImage.setImageResource(player.monster.image)

            if (player.health <= 0) {
                hud.alpha = 0.5f
            } else {
                hud.alpha = 1.0f
            }
            hud.findViewById<TextView>(R.id.monsterName).text = player.monster.name
            hud.findViewById<TextView>(R.id.monsterHealth).text = getString(R.string.health_format, player.health)
            hud.findViewById<TextView>(R.id.monsterEnergy).text = getString(R.string.energy_format, player.energy)
            hud.findViewById<TextView>(R.id.monsterVictoryPoints).text = getString(R.string.victory_points_format, player.victoryPoints)

            if (player == gameManager.getCurrentPlayer()) {
                hud.setBackgroundResource(R.drawable.player_hud_active_background)
            } else {
                hud.background = null
            }
        }

        val playerInTokyo = gameManager.getPlayerInTokyo()
        if (playerInTokyo != null) {
            tokyoCityImageView.setImageResource(playerInTokyo.monster.image)
            tokyoCityImageView.visibility = View.VISIBLE
        } else {
            tokyoCityImageView.visibility = View.INVISIBLE
        }

        rollDiceButton.isEnabled = gameManager.getCurrentPlayer().isHuman && gameManager.gameState == GameState.RUNNING
    }
}
