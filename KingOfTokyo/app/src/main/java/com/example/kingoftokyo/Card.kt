package com.example.kingoftokyo

import androidx.annotation.DrawableRes

// Le comportement de la carte (effet permanent ou à usage unique)
enum class CardType {
    POWER,  // Effet permanent
    ACTION  // Effet immédiat puis défaussée
}

// La catégorie de la carte (pour le style, le tri, etc.)
enum class CardCategory {
    POUVOIR,
    ACTION,
    MUTATION,
    ULTIME
}

data class Card(
    val name: String,
    val cost: Int,
    val type: CardType,
    val category: CardCategory,
    val description: String,
    @DrawableRes val image: Int? = null, // Image de la carte
    val effect: (Player, GameManager) -> Unit
)
