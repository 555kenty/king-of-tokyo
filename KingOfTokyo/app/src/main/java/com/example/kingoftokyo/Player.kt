package com.example.kingoftokyo

data class Player(
    val monster: Monster,
    var isHuman: Boolean = false,
    var isInTokyo: Boolean = false,
    var health: Int = monster.healthPoints,
    var victoryPoints: Int = monster.victoryPoints,
    var energy: Int = 0
)
