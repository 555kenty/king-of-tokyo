package com.example.kingoftokyo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import android.widget.Toast
import kotlin.math.sign

class GameActivity : AppCompatActivity() {

    private lateinit var gameManager: GameManager
    private lateinit var dicePopupManager: DicePopupManager
    private lateinit var tokyoChoicePopupManager: TokyoChoicePopupManager
    private lateinit var shopPopupManager: ShopPopupManager
    private lateinit var inventoryPopupManager: InventoryPopupManager

    private val playerHudViews = mutableListOf<View>()
    private val playerMonsterImageViews = mutableListOf<ImageView>()
    private lateinit var activeEffectsContainer: LinearLayout
    private lateinit var effectOverlay: View
    private var tokyoCityImageView: ImageView? = null
    private lateinit var rollDiceButton: Button
    private var viewCardsButton: Button? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game)

        initViews()

        dicePopupManager = DicePopupManager(this, findViewById(android.R.id.content))
        tokyoChoicePopupManager = TokyoChoicePopupManager(this, findViewById(android.R.id.content))
        shopPopupManager = ShopPopupManager(this, findViewById(android.R.id.content))
        
        gameManager = GameManager(
            onUpdate = { runOnUiThread { updateUI() } },
            onBotTurn = { diceResult -> runOnUiThread { dicePopupManager.showDiceForBot(diceResult) } },
            onGameOver = { playerWon -> runOnUiThread { launchGameOverScreen(playerWon) } },
            onTokyoChoice = { defender, attacker ->
                runOnUiThread { promptTokyoChoice(defender, attacker) }
            },
            onShopPhase = { _, _ ->
                runOnUiThread { shopPopupManager.showShop(gameManager) }
            },
            onDamageVisual = { targets -> runOnUiThread { flashTargets(targets, isHeal = false) } },
            onHealVisual = { targets -> runOnUiThread { flashTargets(targets, isHeal = true) } },
            onEnergyVisual = { pairs -> runOnUiThread { showEnergyDeltas(pairs) } },
            onCardUsed = { player, card -> runOnUiThread { showBotCardBanner(player, card) } }
        )

        inventoryPopupManager = InventoryPopupManager(this, findViewById(android.R.id.content), gameManager)

        val selectedMonsterName = intent.getStringExtra("selectedMonsterName") ?: GameData.monsters.first().name
        gameManager.setupGame(selectedMonsterName)

        rollDiceButton.setOnClickListener { 
            if (gameManager.currentPlayer.isHuman && gameManager.gameState == GameState.RUNNING) {
                dicePopupManager.showDiceForHuman { gameManager.resolveDice(it) }
            }
        }
        
        viewCardsButton?.setOnClickListener { 
            if (gameManager.currentPlayer.isHuman) {
                inventoryPopupManager.show()
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
        activeEffectsContainer = findViewById(R.id.activeEffectsContainer)
        effectOverlay = findViewById(R.id.effectOverlay)
        tokyoCityImageView = findViewById(R.id.tokyo_city)
        rollDiceButton = findViewById(R.id.rollDiceButton)
        viewCardsButton = findViewById(R.id.view_cards_button)
    }

    private fun updateUI() {
        if (gameManager.gameState == GameState.GAME_OVER) return

        gameManager.players.forEachIndexed { index, player ->
            val hud = playerHudViews[index]
            val monsterImage = playerMonsterImageViews[index]

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

            if (player == gameManager.currentPlayer) {
                hud.setBackgroundResource(R.drawable.player_hud_active_background)
            } else {
                hud.background = null
            }
        }

        val playerInTokyo = gameManager.getPlayerInTokyo()
        if (playerInTokyo != null) {
            tokyoCityImageView?.setImageResource(playerInTokyo.monster.image)
            tokyoCityImageView?.visibility = View.VISIBLE
        } else {
            tokyoCityImageView?.visibility = View.INVISIBLE
        }

        rollDiceButton.isEnabled = gameManager.currentPlayer.isHuman && gameManager.gameState == GameState.RUNNING
        viewCardsButton?.isEnabled = gameManager.currentPlayer.isHuman

        if (gameManager.gameState != GameState.SHOPPING) {
            shopPopupManager.hideShop()
        }

        renderActiveEffects()
    }

    private fun renderActiveEffects() {
        val container = activeEffectsContainer
        container.removeAllViews()
        val inflater = layoutInflater
        val cards = gameManager.currentPlayer.activeCards
        if (cards.isEmpty()) {
            val empty = TextView(this).apply {
                text = "Aucun effet actif"
                setTextColor(getColor(android.R.color.white))
                textSize = 12f
            }
            container.addView(empty)
            return
        }
        cards.forEach { card ->
            val chip = inflater.inflate(R.layout.active_effect_chip, container, false) as TextView
            chip.text = "${card.name} • Actif"
            chip.setOnClickListener { showEffectDetail(card) }
            container.addView(chip)
        }
    }

    private fun flashTargets(targets: List<Player>, isHeal: Boolean = false) {
        targets.forEach { player ->
            val idx = gameManager.players.indexOf(player)
            if (idx in playerHudViews.indices) {
                val view = playerHudViews[idx]
                view.animate().cancel()
                val originalAlpha = view.alpha
                val color = if (isHeal) 0x66A5D6A7 else 0x66F44336
                view.setBackgroundColor(color.toInt())
                view.animate()
                    .alpha(0.5f)
                    .setDuration(120)
                    .withEndAction {
                        view.animate().alpha(originalAlpha).setDuration(120).withEndAction {
                            view.setBackgroundColor(0x00000000)
                        }.start()
                    }
                    .start()
            }
            if (idx in playerMonsterImageViews.indices) {
                val monsterView = playerMonsterImageViews[idx]
                monsterView.animate().cancel()
                val originalAlpha = monsterView.alpha
                monsterView.colorFilter = android.graphics.PorterDuffColorFilter(
                    if (isHeal) 0x99A5D6A7.toInt() else 0x99F44336.toInt(),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
                )
                monsterView.animate()
                    .alpha(0.6f)
                    .setDuration(120)
                    .withEndAction {
                        monsterView.animate().alpha(originalAlpha).setDuration(120).withEndAction {
                            monsterView.colorFilter = null
                        }.start()
                    }
                    .start()
            }
        }
    }

    private fun showEffectDetail(card: Card) {
        val overlayView = effectOverlay as ViewGroup
        overlayView.removeAllViews()
        overlayView.setBackgroundColor(0x99000000.toInt())
        val panel = layoutInflater.inflate(R.layout.effect_overlay_panel, overlayView, false)
        panel.findViewById<TextView>(R.id.effectTitle).text = card.name
        panel.findViewById<TextView>(R.id.effectSubtitle).text = "Actif pour ${gameManager.currentPlayer.monster.name}"
        panel.findViewById<TextView>(R.id.effectDescription).text = card.description
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            marginStart = 32
            topMargin = 32
        }
        overlayView.addView(panel, params)
        overlayView.visibility = View.VISIBLE
        overlayView.setOnClickListener { hideEffectDetail() }
    }

    private fun hideEffectDetail() {
        val overlayView = effectOverlay as ViewGroup
        overlayView.removeAllViews()
        overlayView.visibility = View.GONE
    }

    private fun showEnergyDeltas(pairs: List<Pair<Player, Int>>) {
        pairs.forEach { (player, delta) ->
            val idx = gameManager.players.indexOf(player)
            if (idx in playerHudViews.indices) {
                val view = playerHudViews[idx]
                val label = TextView(this).apply {
                    text = if (delta > 0) "+$delta ⚡" else "$delta ⚡"
                    setTextColor(if (delta > 0) ContextCompat.getColor(this@GameActivity, android.R.color.holo_green_light) else ContextCompat.getColor(this@GameActivity, android.R.color.holo_red_light))
                    textSize = 14f
                }
                val overlayView = effectOverlay as ViewGroup
                overlayView.removeAllViews()
                overlayView.setBackgroundColor(0x00000000)
                val params = ViewGroup.MarginLayoutParams(
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    marginStart = (view.x + view.width / 2f).toInt()
                    topMargin = (view.y - 10f).toInt()
                }
                overlayView.addView(label, params)
                overlayView.visibility = View.VISIBLE
                label.animate()
                    .translationY(-40f)
                    .alpha(0f)
                    .setDuration(1200)
                    .withEndAction {
                        overlayView.removeAllViews()
                        overlayView.visibility = View.GONE
                    }
                    .start()
            }
        }
    }

    private fun showBotCardBanner(player: Player, card: Card) {
        val rootContainer = findViewById<ViewGroup>(android.R.id.content)
        val banner = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            background = ContextCompat.getDrawable(this@GameActivity, R.drawable.button_main_menu)
            val text = TextView(this@GameActivity).apply {
                this.text = "${player.monster.name} utilise ${card.name}"
                setTextColor(ContextCompat.getColor(this@GameActivity, android.R.color.white))
                textSize = 16f
            }
            addView(text)
            alpha = 0f
        }
        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 32
            marginStart = 24
            marginEnd = 24
        }
        rootContainer.addView(banner, params)
        banner.animate()
            .alpha(1f)
            .setDuration(200)
            .withEndAction {
                banner.animate()
                    .alpha(0f)
                    .setStartDelay(2000)
                    .setDuration(300)
                    .withEndAction { rootContainer.removeView(banner) }
                    .start()
            }
            .start()
    }
}
