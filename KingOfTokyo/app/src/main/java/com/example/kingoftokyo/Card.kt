package com.example.kingoftokyo

import androidx.annotation.DrawableRes
import java.util.UUID

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
    private val id: String = UUID.randomUUID().toString(), // ID unique pour chaque instance de carte
    // CORRECTION : L'effet est maintenant le dernier paramètre, ce qui corrige l'erreur de compilation
    val effect: (Player, GameManager) -> Unit = { _, _ -> }
) {
    // La présence de la lambda `effect` casse l'égalité structurelle des data class.
    // Pour garantir que `remove` fonctionne sur la bonne instance de carte dans une liste,
    // nous basons l'égalité uniquement sur l'ID unique.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false
        other as Card
        return id == other.id
    }

    override fun hashCode(): Int {
        return id.hashCode()
    }
}
