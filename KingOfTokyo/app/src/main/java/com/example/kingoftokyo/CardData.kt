package com.example.kingoftokyo

object CardData {
    private val cardModels = listOf(
        // --- POUVOIRS (PASSIVE) ---
        Card("Propulsion", 4, CardType.POWER, CardCategory.POUVOIR, "Lorsque vous quittez Tokyo, gagnez 4 ⭐.", R.drawable.carte_propulsion) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Griffes Chargées", 5, CardType.POWER, CardCategory.POUVOIR, "Vos attaques infligent +1 👊 si vous êtes hors de Tokyo.", R.drawable.carte_griffe_chargees) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Cœur Atomique", 3, CardType.POWER, CardCategory.POUVOIR, "Lorsque vous attaquez Tokyo, gagnez 2 ⭐.", R.drawable.carte_coeur_atomique) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Nano-Régénération", 7, CardType.POWER, CardCategory.POUVOIR, "À la fin de chaque tour, gagnez +1 ❤️ (même dans Tokyo).", R.drawable.carte_nano_regeneration) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Carapace Adaptative", 5, CardType.POWER, CardCategory.POUVOIR, "Si vous commencez votre tour dans Tokyo, gagnez 1 ⭐ supplémentaire.", R.drawable.carte_carapace_adaptative) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Hurlement Terrifiant", 4, CardType.POWER, CardCategory.POUVOIR, "Les autres monstres perdent 1 ⭐ au début de votre tour.", R.drawable.carte_hurlement_terrifiant) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Vision Nocturne", 5, CardType.POWER, CardCategory.POUVOIR, "Les autres joueurs ne peuvent pas éviter vos attaques.", R.drawable.carte_vision_nocturne) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Batterie Surchargée", 6, CardType.POWER, CardCategory.POUVOIR, "Si vous conservez 3⚡, gagnez immédiatement 3 énergie.", R.drawable.carte_batterie_surchargee) { _, _ -> /* Effet passif géré par le GameManager */ },

        // --- ACTIONS (ACTIVE) ---
        Card("Frappe Orbitale", 5, CardType.ACTION, CardCategory.ACTION, "Infligez 3 ❤️ à n’importe quel monstre.", R.drawable.carte_frappe_orbitale) { player, game ->
            game.selectTarget(player) { target ->
                target?.let { game.applyDamage(it, 3, player) }
            }
        },
        Card("Onde de Choc", 4, CardType.ACTION, CardCategory.ACTION, "Tous les autres monstres perdent 1 ❤️.", R.drawable.carte_onde_de_choc) { player, game ->
            game.players.filter { it != player }.forEach { game.applyDamage(it, 1, player) }
        },
        Card("Absorption d’Énergie", 3, CardType.ACTION, CardCategory.ACTION, "Gagnez 3 énergie immédiatement.", R.drawable.carte_absorption_energie) { player, game ->
            player.energy += 3
            game.notifyEnergyChange(listOf(player to 3))
        },
        Card("Bond Titanesque", 4, CardType.ACTION, CardCategory.ACTION, "Entrez immédiatement dans Tokyo. Gagnez 1 ⭐.", R.drawable.carte_bond_titanesque) { player, game ->
            game.enterTokyo(player)
        },
        Card("Mutation Express", 5, CardType.ACTION, CardCategory.ACTION, "Changez un symbole d’un dé de votre choix.", R.drawable.carte_mutation_express) { player, _ ->
            // Effet simplifié : bonus polyvalent (gain 1⭐ et 1⚡)
            player.victoryPoints += 1
            player.energy += 1
        },
        Card("Téléportation", 6, CardType.ACTION, CardCategory.ACTION, "Placez votre monstre où vous voulez (dans Tokyo ou dehors).", R.drawable.carte_teleportation) { player, game ->
            game.requestTeleport(
                player,
                onEnterTokyo = { game.enterTokyo(player) },
                onExitTokyo = { player.isInTokyo = false }
            )
        },

        // --- MUTATIONS (PASSIVE) ---
        Card("Rage Primale", 5, CardType.POWER, CardCategory.MUTATION, "+1 👊 à toutes vos attaques. Impossible de quitter Tokyo.", R.drawable.carte_rage_primale) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Sang Corrompu", 4, CardType.POWER, CardCategory.MUTATION, "+2 👊 par attaque, mais perdez 1 ❤️ après chaque attaque.", R.drawable.carte_sang_corrompu) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Mutation Cristalline", 6, CardType.POWER, CardCategory.MUTATION, "Lorsque vous recevez 3+ dégâts, gagnez 3 énergie.", R.drawable.carte_mutation_cristalline) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Parasite Kaiju", 5, CardType.POWER, CardCategory.MUTATION, "Lorsque vous blessez un monstre, volez 1 énergie, mais perdez 1 ❤️.", R.drawable.carte_parasite_kaiju) { _, _ -> /* Effet passif géré par le GameManager */ },

        // --- ULTIMES (PASSIVE) ---
        Card("Mode Apocalypse", 10, CardType.POWER, CardCategory.ULTIME, "Toutes vos attaques infligent +3 👊 jusqu’à la fin de la partie.", R.drawable.carte_mode_apocalypse) { _, _ -> /* Effet passif géré par le GameManager */ },
        Card("Résurrection", 10, CardType.POWER, CardCategory.ULTIME, "Si vous mourrez, revenez avec 6 ❤️.", R.drawable.carte_resurrection) { _, _ -> /* Effet passif géré par le GameManager */ }
    )

    val allCards: List<Card>
        get() {
            val deck = cardModels.map { it.withNewId() } + cardModels.map { it.withNewId() }
            return deck.shuffled()
        }
}
