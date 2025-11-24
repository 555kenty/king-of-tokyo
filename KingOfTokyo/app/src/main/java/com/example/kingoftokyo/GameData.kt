package com.example.kingoftokyo

object GameData {
    val monsters = listOf(
        Monster(
            name = "Aegis-01",
            image = R.drawable.giant_robot,
            description = "Prototype militaire de dernière génération, conçu pour résister à un siège nucléaire. Ses systèmes d’armement sont scellés… mais quelque chose l’a réveillé.",
            healthPoints = 10,
            victoryPoints = 0
        ),
        Monster(
            name = "Kairyū-Zero",
            image = R.drawable.dino,
            description = "Créature reptilienne cryogénique découverte sous Tokyo. Son souffle glacial peut figer un immeuble instantanément.",
            healthPoints = 10,
            victoryPoints = 0
        ),
        Monster(
            name = "Brutalus Prime",
            image = R.drawable.gorilla,
            description = "Expérimentation génétique interdite. Chaque battement de son cœur amplifie sa force jusqu’à broyer le métal brut.",
            healthPoints = 10,
            victoryPoints = 0
        ),
        Monster(
            name = "Elderion",
            image = R.drawable.lovecraft_monster,
            description = "Anomalie lovecraftienne issue d’un portail dimensionnel. Sa chair mutante absorbe la lumière et corrompt la matière.",
            healthPoints = 10,
            victoryPoints = 0
        ),
        Monster(
            name = "Obsidian Seraph",
            image = R.drawable.ange_dechu,
            description = "Ancien protecteur des cieux, déchu et transformé par une énergie infernale. Ses ailes brûlent de rage et jugent les villes entières.",
            healthPoints = 10,
            victoryPoints = 0
        ),
        Monster(
            name = "Nightfang X",
            image = R.drawable.loup_cyber,
            description = "Prédateur cybernétique furtif conçu pour la chasse urbaine. Ses griffes énergétiques déchirent l’acier comme du papier.",
            healthPoints = 10,
            victoryPoints = 0
        )
    )
}
