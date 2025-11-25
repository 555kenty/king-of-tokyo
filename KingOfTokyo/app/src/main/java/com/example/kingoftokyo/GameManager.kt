package com.example.kingoftokyo

import android.os.Handler
import android.os.Looper
import android.util.Log

enum class GameState {
    RUNNING,
    AWAITING_TOKYO_CHOICE,
    SHOPPING,
    GAME_OVER
}

class GameManager(
    private val onUpdate: () -> Unit,
    private val onBotTurn: (List<Die>) -> Unit,
    private val onGameOver: (Boolean) -> Unit,
    private val onTokyoChoice: (Player, Player) -> Unit,
    private val onShopPhase: (List<Card>, Player) -> Unit
) {

    lateinit var players: List<Player>
    private var currentPlayerIndex = 0
    var gameState = GameState.RUNNING

    val currentPlayer: Player
        get() = players[currentPlayerIndex]

    private val deck = CardData.allCards.toMutableList()
    val shop = mutableListOf<Card>()

    fun setupGame(selectedMonsterName: String) {
        deck.shuffle()
        drawShopCards()

        val allMonsters = GameData.monsters.toMutableList()
        val humanMonster = allMonsters.find { it.name == selectedMonsterName }!!
        allMonsters.remove(humanMonster)

        val humanPlayer = Player(monster = humanMonster, isHuman = true)
        val bots = allMonsters.shuffled().take(3).map { Player(it) }

        players = (listOf(humanPlayer) + bots).shuffled()
        currentPlayerIndex = 0
        startTurn()
    }

    private fun startTurn() {
        if (gameState == GameState.GAME_OVER) return

        Log.d("GameManager", "Début du tour de: ${currentPlayer.monster.name}")

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
        val diceResult = List(DICE_COUNT) { Die(DieFace.values().random()) }
        onBotTurn(diceResult) 
        Handler(Looper.getMainLooper()).postDelayed({ 
            resolveDice(diceResult)
        }, 2000)
    }

    fun resolveDice(diceResult: List<Die>) {
        if (gameState == GameState.GAME_OVER) return

        val faceCounts = diceResult.groupingBy { it.face }.eachCount()
        resolveVictoryPoints(currentPlayer, faceCounts)
        resolveEnergy(currentPlayer, faceCounts)
        resolveHeal(currentPlayer, faceCounts)
        onUpdate()

        val attackCount = faceCounts[DieFace.SMASH] ?: 0
        if (attackCount > 0) {
            if (handleAttack(currentPlayer, attackCount)) {
                 startShoppingPhase()
            }
        } else {
            startShoppingPhase()
        }
    }

    private fun endTurn() {
        if (gameState == GameState.GAME_OVER) return

        if (getPlayerInTokyo() == null) {
            enterTokyo(currentPlayer)
        }

        checkGameOver()
        if (gameState == GameState.GAME_OVER) return
        onUpdate()
        Log.d("GameManager", "Fin du tour de: ${currentPlayer.monster.name}")

        do {
            currentPlayerIndex = (currentPlayerIndex + 1) % players.size
        } while (players.getOrNull(currentPlayerIndex)?.health?.let { it <= 0 } == true)

        startTurn()
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
            player.health = (player.health + healCount).coerceAtMost(player.monster.healthPoints)
        }
    }

    private fun handleAttack(attacker: Player, damage: Int): Boolean {
        if (attacker.isInTokyo) {
            players.filter { !it.isInTokyo && it.health > 0 }.forEach { target ->
                applyDamage(target, damage, attacker)
            }
        } else {
            getPlayerInTokyo()?.let { playerInTokyo ->
                applyDamage(playerInTokyo, damage, attacker)
                
                if (playerInTokyo.health <= 0) {
                    leaveTokyo(playerInTokyo, attacker)
                } else {
                    if (playerInTokyo.isHuman) {
                        gameState = GameState.AWAITING_TOKYO_CHOICE
                        onTokyoChoice(playerInTokyo, attacker)
                        return false
                    } else {
                        if (playerInTokyo.health < 5) {
                            leaveTokyo(playerInTokyo, attacker)
                        }
                    }
                }
            }
        }
        onUpdate()
        checkGameOver()
        return true
    }
    
    fun applyDamage(target: Player, damage: Int, attacker: Player) {
        if (target.health <= 0) return
        target.health -= damage
        onUpdate()
    }

    fun playerDecidedTokyo(wantsToStay: Boolean, defender: Player, attacker: Player) {
        if (!wantsToStay) {
            leaveTokyo(defender, attacker)
        }
        gameState = GameState.RUNNING
        startShoppingPhase()
    }

    fun enterTokyo(player: Player) {
        if (player.health <= 0) return
        
        val oldPlayerInTokyo = getPlayerInTokyo()
        if (oldPlayerInTokyo != null) {
            oldPlayerInTokyo.isInTokyo = false
        }

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

    fun getPlayerInTokyo(): Player? {
        return players.find { it.isInTokyo }
    }

    fun drawShopCards() {
        shop.clear()
        repeat(3) {
            if (deck.isNotEmpty()) {
                shop.add(deck.removeAt(0))
            }
        }
    }

    private fun startShoppingPhase() {
        if (gameState == GameState.GAME_OVER) {
            endTurn()
            return
        }

        gameState = GameState.SHOPPING
        if (!currentPlayer.isHuman) {
            Handler(Looper.getMainLooper()).postDelayed({ executeBotShopping() }, 1000)
        } else {
            onShopPhase(shop, currentPlayer)
        }
    }
    
    private fun executeBotShopping() {
        val affordableCard = shop.filter { currentPlayer.energy >= it.cost }.maxByOrNull { it.cost }
        if (affordableCard != null) {
            Log.d("GameManager", "Bot ${currentPlayer.monster.name} is buying ${affordableCard.name}")
            buyCard(affordableCard)
        }
        finishShopping()
    }

    fun buyCard(card: Card) {
        if (currentPlayer.energy >= card.cost) {
            currentPlayer.energy -= card.cost
            val isAction = card.type == CardType.ACTION
            if (isAction) {
                card.effect(currentPlayer, this)
            } else {
                currentPlayer.cards.add(card)
            }

            val cardIndex = shop.indexOf(card)
            if (cardIndex != -1) {
                if (deck.isNotEmpty()) {
                    shop[cardIndex] = deck.removeAt(0)
                } else {
                    shop.removeAt(cardIndex)
                }
            }
            onUpdate()
        }
    }

    fun refreshShop() {
        if (currentPlayer.energy >= 2) {
            currentPlayer.energy -= 2
            drawShopCards()
            onUpdate()
        }
    }

    fun useCard(card: Card, removeFromInventory: Boolean = false): Boolean {
        card.effect(currentPlayer, this)

        val removed = if (removeFromInventory || card.type == CardType.ACTION) {
            currentPlayer.cards.remove(card)
        } else {
            false
        }
        onUpdate()
        return removed
    }

    fun finishShopping() {
        gameState = GameState.RUNNING
        endTurn()
    }

    private fun checkGameOver() {
        val alivePlayers = players.filter { it.health > 0 }
        if (alivePlayers.size <= 1) {
            val humanIsWinner = players.find { it.isHuman }?.let { it.health > 0 } ?: (alivePlayers.isEmpty())
            onGameOver(humanIsWinner)
            gameState = GameState.GAME_OVER
            return
        }

        val winnerByPoints = players.find { it.victoryPoints >= 20 }
        if (winnerByPoints != null) {
            onGameOver(winnerByPoints.isHuman)
            gameState = GameState.GAME_OVER
        }
    }

    companion object {
        private const val DICE_COUNT = 3
    }
}
