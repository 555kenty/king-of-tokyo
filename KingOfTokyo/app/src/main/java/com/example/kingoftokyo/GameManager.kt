package com.example.kingoftokyo

import android.os.Handler
import android.os.Looper
import android.util.Log

enum class GameState {
    RUNNING,
    AWAITING_TOKYO_CHOICE,
    GAME_OVER
}

class GameManager(
    private val onUpdate: () -> Unit,
    private val onBotTurn: (List<Die>) -> Unit,
    private val onGameOver: (Boolean) -> Unit,
    private val onTokyoChoice: (Player, Player) -> Unit
) {

    lateinit var players: List<Player>
    private var currentPlayerIndex = 0
    var gameState = GameState.RUNNING

    fun setupGame(selectedMonsterName: String) {
        val allMonsters = GameData.monsters.toMutableList()
        val humanMonster = allMonsters.find { it.name == selectedMonsterName }!!
        allMonsters.remove(humanMonster)

        val humanPlayer = Player(monster = humanMonster, isHuman = true)
        val bots = allMonsters.shuffled().take(3).map { Player(it) }

        players = (listOf(humanPlayer) + bots).shuffled()
        currentPlayerIndex = 0
        startTurn()
    }

    fun getCurrentPlayer(): Player {
        return players[currentPlayerIndex]
    }

    private fun startTurn() {
        if (gameState == GameState.GAME_OVER) return

        val currentPlayer = getCurrentPlayer()
        if (currentPlayer.isInTokyo) {
            currentPlayer.victoryPoints += 2
        }
        checkGameOver()
        if (gameState == GameState.GAME_OVER) return

        onUpdate()

        if (!currentPlayer.isHuman) {
            Handler(Looper.getMainLooper()).postDelayed({ executeBotTurn() }, 1000)
        }
    }

    private fun executeBotTurn() {
        val diceResult = List(3) { Die(DieFace.values().random()) }
        onBotTurn(diceResult)
        Handler(Looper.getMainLooper()).postDelayed({ resolveDice(diceResult) }, 2000)
    }

    fun resolveDice(diceResult: List<Die>) {
        val currentPlayer = getCurrentPlayer()
        val faceCounts = diceResult.groupingBy { it.face }.eachCount()

        resolveVictoryPoints(currentPlayer, faceCounts)
        resolveEnergy(currentPlayer, faceCounts)
        resolveHeal(currentPlayer, faceCounts)

        val attackCount = faceCounts[DieFace.SMASH] ?: 0
        var attackPhaseOver = true
        if (attackCount > 0) {
            attackPhaseOver = handleAttack(currentPlayer, attackCount)
        }

        if (getPlayerInTokyo() == null) {
            enterTokyo(currentPlayer)
        }

        if (attackPhaseOver) {
            endTurn()
        }
    }

    private fun resolveVictoryPoints(player: Player, faceCounts: Map<DieFace, Int>) {
        faceCounts.forEach { (face, count) ->
            if (count >= 3) {
                when (face) {
                    DieFace.ONE -> player.victoryPoints += 1 + (count - 3)
                    DieFace.TWO -> player.victoryPoints += 2 + (count - 3)
                    DieFace.THREE -> player.victoryPoints += 3 + (count - 3)
                    else -> {}
                }
            }
        }
    }

    private fun resolveEnergy(player: Player, faceCounts: Map<DieFace, Int>) {
        player.energy += faceCounts[DieFace.ENERGY] ?: 0
    }

    private fun resolveHeal(player: Player, faceCounts: Map<DieFace, Int>) {
        if (!player.isInTokyo) {
            val healCount = faceCounts[DieFace.HEART] ?: 0
            player.health = (player.health + healCount).coerceAtMost(10)
        }
    }

    private fun handleAttack(attacker: Player, damage: Int): Boolean {
        if (attacker.isInTokyo) {
            players.filter { !it.isInTokyo && it.health > 0 }.forEach { it.health -= damage }
            onUpdate()
            return true
        } else {
            getPlayerInTokyo()?.let { playerInTokyo ->
                playerInTokyo.health -= damage
                onUpdate()
                if (playerInTokyo.health <= 0) {
                    leaveTokyo(playerInTokyo, attacker)
                    checkGameOver()
                    return true
                }
                if (playerInTokyo.isHuman) { // CORRECTION: it -> playerInTokyo
                    gameState = GameState.AWAITING_TOKYO_CHOICE
                    onTokyoChoice(playerInTokyo, attacker)
                    return false
                } else {
                    if (playerInTokyo.health < 5) { // CORRECTION: it -> playerInTokyo
                        leaveTokyo(playerInTokyo, attacker)
                    }
                    return true
                }
            }
        }
        return true
    }

    fun playerDecidedTokyo(wantsToStay: Boolean, defender: Player, attacker: Player) {
        if (!wantsToStay) {
            leaveTokyo(defender, attacker)
        }
        gameState = GameState.RUNNING
        endTurn()
    }

    private fun enterTokyo(player: Player) {
        getPlayerInTokyo()?.isInTokyo = false
        player.isInTokyo = true
        if (gameState != GameState.GAME_OVER) {
            player.victoryPoints += 1
        }
        onUpdate()
    }

    private fun leaveTokyo(defender: Player, attacker: Player) {
        defender.isInTokyo = false
        enterTokyo(attacker)
    }

    private fun endTurn() {
        checkGameOver()
        if (gameState == GameState.GAME_OVER) return
        onUpdate()

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (players[currentPlayerIndex].health <= 0)

        startTurn()
    }

    private fun checkGameOver() {
        val alivePlayers = players.filter { it.health > 0 }
        if (alivePlayers.size <= 1) {
            val humanPlayer = players.find { it.isHuman }!!
            onGameOver(humanPlayer.health > 0)
            gameState = GameState.GAME_OVER
            return
        }

        val winnerByPoints = players.find { it.victoryPoints >= 20 }
        if (winnerByPoints != null) {
            onGameOver(winnerByPoints.isHuman)
            gameState = GameState.GAME_OVER
        }
    }

    fun getPlayerInTokyo(): Player? {
        return players.find { it.isInTokyo }
    }
}
