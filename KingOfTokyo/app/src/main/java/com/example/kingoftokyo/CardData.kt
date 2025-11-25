package com.example.kingoftokyo

object CardData {

    val allCards = listOf(
        // --- POUVOIRS (8 cartes) ---
        Card("Propulsion", 4, CardType.POWER, CardCategory.POUVOIR, "Lorsque vous quittez Tokyo, gagnez 4 ⭐.", R.drawable.carte_propulsion) { player, game ->
            // Implémentation via un listener
        },
        Card("Griffes Chargées", 5, CardType.POWER, CardCategory.POUVOIR, "Vos attaques infligent +1 👊 si vous êtes hors de Tokyo.", R.drawable.carte_griffe_chargees) { player, game ->
            // Implémentation dans handleAttack
        },
        Card("Cœur Atomique", 7, CardType.POWER, CardCategory.POUVOIR, "Lorsque vous attaquez Tokyo, gagnez 2 ⭐.", R.drawable.carte_coeur_atomique) { player, game ->
            // Implémentation dans handleAttack
        },
        Card("Nano-Régénération", 7, CardType.POWER, CardCategory.POUVOIR, "À la fin de chaque tour, gagnez +1 ❤️ (même dans Tokyo).", R.drawable.carte_nano_regeneration) { player, game ->
            // Implémentation dans endTurn
        },
        Card("Carapace Adaptative", 5, CardType.POWER, CardCategory.POUVOIR, "Si vous commencez votre tour dans Tokyo, gagnez 1 ⭐ supplémentaire.", R.drawable.carte_carapace_adaptative) { player, game ->
            // Implémentation dans startTurn
        },
        Card("Hurlement Terrifiant", 4, CardType.POWER, CardCategory.POUVOIR, "Les autres monstres perdent 1 ⭐ au début de votre tour.", R.drawable.carte_hurlement_terrifiant) { player, game ->
            // Implémentation dans startTurn
        },
        Card("Vision Nocturne", 5, CardType.POWER, CardCategory.POUVOIR, "Les autres joueurs ne peuvent pas éviter vos attaques.", R.drawable.carte_vision_nocturne) { player, game ->
            // Implémentation dans handleAttack (pour contrer des cartes futures)
        },
        Card("Batterie Surchargée", 6, CardType.POWER, CardCategory.POUVOIR, "Si vous conservez 3⚡, gagnez immédiatement 3 énergie.", R.drawable.carte_batterie_surchargee) { player, game ->
            // Implémentation dans resolveDice
        },

        // --- ACTIONS (6 cartes) ---
        Card("Frappe Orbitale", 5, CardType.ACTION, CardCategory.ACTION, "Infligez 3 ❤️ à n’importe quel monstre.", R.drawable.carte_frappe_orbitale) { player, game ->
            // Nécessite une sélection de cible
        },
        Card("Onde de Choc", 4, CardType.ACTION, CardCategory.ACTION, "Tous les autres monstres perdent 1 ❤️.", R.drawable.carte_onde_de_choc) { player, game ->
            game.players.filter { it != player }.forEach { it.health -= 1 }
        },
        Card("Absorption d’Énergie", 3, CardType.ACTION, CardCategory.ACTION, "Gagnez 3 énergie immédiatement.", R.drawable.carte_absorption_energie) { player, game ->
            player.energy += 3
        },
        Card("Bond Titanesque", 4, CardType.ACTION, CardCategory.ACTION, "Entrez immédiatement dans Tokyo. Gagnez 1 ⭐.", R.drawable.carte_bond_titanesque) { player, game ->
            game.getPlayerInTokyo()?.isInTokyo = false
            player.isInTokyo = true
            player.victoryPoints += 1
        },
        Card("Mutation Express", 5, CardType.ACTION, CardCategory.ACTION, "Changez un symbole d’un dé de votre choix.", R.drawable.carte_mutation_express) { player, game ->
            // Nécessite une interaction avec les dés
        },
        Card("Téléportation", 6, CardType.ACTION, CardCategory.ACTION, "Placez votre monstre où vous voulez (dans Tokyo ou dehors).", R.drawable.carte_teleportation) { player, game ->
            // Nécessite une sélection de zone
        },

        // --- MUTATIONS (4 cartes) ---
        Card("Rage Primale", 5, CardType.POWER, CardCategory.MUTATION, "+1 👊 à toutes vos attaques. Impossible de quitter Tokyo.", R.drawable.carte_rage_primale) { player, game ->
            // Implémentation dans handleAttack et playerDecidedTokyo
        },
        Card("Sang Corrompu", 4, CardType.POWER, CardCategory.MUTATION, "+2 👊 par attaque, mais perdez 1 ❤️ après chaque attaque.", R.drawable.carte_sang_corrompu) { player, game ->
            // Implémentation dans handleAttack
        },
        Card("Mutation Cristalline", 6, CardType.POWER, CardCategory.MUTATION, "Lorsque vous recevez 3+ dégâts, gagnez 3 énergie.", R.drawable.carte_mutation_cristalline) { player, game ->
            // Implémentation via un listener
        },
        Card("Parasite Kaiju", 5, CardType.POWER, CardCategory.MUTATION, "Lorsque vous blessez un monstre, volez 1 énergie, mais perdez 1 ❤️.", R.drawable.carte_parasite_kaiju) { player, game ->
            // Implémentation dans handleAttack
        },

        // --- ULTIMES (2 cartes) ---
        Card("Mode Apocalypse", 10, CardType.POWER, CardCategory.ULTIME, "Toutes vos attaques infligent +3 👊 jusqu’à la fin de la partie.", R.drawable.carte_mode_apocalypse) { player, game ->
            // Implémentation dans handleAttack
        },
        Card("Résurrection", 10, CardType.POWER, CardCategory.ULTIME, "Si vous mourrez, revenez avec 6 ❤️.", R.drawable.carte_resurrection) { player, game ->
            // Implémentation via un listener
        }
    )
}
