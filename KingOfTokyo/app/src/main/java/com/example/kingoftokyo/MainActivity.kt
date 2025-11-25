package com.example.kingoftokyo

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private var menuMusicPlayer: MediaPlayer? = null
    private lateinit var rulesPopup: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // ===========================
        //  POPUP RÈGLES
        // ===========================

        rulesPopup = layoutInflater.inflate(R.layout.popup_rules, null)
        rulesPopup.visibility = View.GONE
        rulesPopup.elevation = 9999f   // s'affiche toujours au-dessus

        addContentView(
            rulesPopup,
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
        )

        // Bouton ouvrir RÈGLES
        findViewById<Button>(R.id.btnRules).setOnClickListener {
            rulesPopup.visibility = View.VISIBLE
        }

        // Bouton fermer (croix)
        rulesPopup.findViewById<ImageButton>(R.id.btnCloseRules).setOnClickListener {
            rulesPopup.visibility = View.GONE
        }

        // IMPORTANT : empêche les clics de passer à travers
        rulesPopup.setOnClickListener { /* bloque le clic */ }

        // Texte à l’intérieur du popup
        val rulesText = rulesPopup.findViewById<TextView>(R.id.rulesText)

        rulesText.text = """
🎯 But du jeu

Incarnez un monstre géant et devenez le dernier survivant ou le premier à atteindre 20 Points de Victoire (PV).

🎲 Contenu

6 figurines + 6 fiches Monstre (avec roues de Vie ❤️ et de Victoire ⭐)

6 dés noirs + 2 dés verts (bonus)

1 plateau Tokyo (avec Tokyo City et Tokyo Bay)

66 cartes Énergie

50 cubes d’énergie 🔋

Jetons spéciaux (Poison, Mimétisme, etc.)

⚙️ Mise en place

Chaque joueur choisit un monstre et place ses roues sur :

❤️ 10 Points de Vie
⭐ 0 Points de Victoire

Mélangez les cartes Énergie → formez une pioche.
Révélez 3 cartes Énergie face visible.
Placez le plateau Tokyo au centre.

Créez une banque de cubes d’énergie 🔋.

Utilisez :
- Tokyo City uniquement à 2–4 joueurs.
- Tokyo City + Tokyo Bay à 5–6 joueurs.

Le joueur qui obtient le plus de ⚡ au lancer commence.

🔁 Déroulement d’un tour (5 phases)
1️⃣ Lancer les dés
2️⃣ Résoudre les dés
3️⃣ Entrer dans Tokyo
4️⃣ Acheter des cartes Énergie
5️⃣ Fin du tour

💥 Règles spéciales de Tokyo
Avantages :
+1⭐ quand vous entrez.
+2⭐ si vous commencez un tour dans Tokyo.

Inconvénients :
Vous êtes la cible de tous les monstres à l’extérieur.
Vous ne pouvez pas regagner de ❤️ avec les dés.

🏁 Fin de partie
- 1 joueur atteint 20⭐ → victoire immédiate.
- Il ne reste qu'un monstre vivant → victoire.

🧪 Exemples de cartes
Mimétisme, Opportuniste, Venin, Souffle de Feu, Métamorphose…

""".trimIndent()

        // ===========================
        //  MUSIQUE DU MENU
        // ===========================

        try {
            menuMusicPlayer = MediaPlayer.create(this, R.raw.menu_bgm)
            menuMusicPlayer?.isLooping = true
            menuMusicPlayer?.start()
        } catch (_: Exception) {}

        // ===========================
        //  BOUTONS PLAY / QUIT
        // ===========================

        findViewById<Button>(R.id.playButton).setOnClickListener {
            val intent = Intent(this, CharacterSelectionActivity::class.java)
            startActivity(intent)
        }

        findViewById<Button>(R.id.quitButton).setOnClickListener {
            finish()
        }
    }

    override fun onResume() {
        super.onResume()
        menuMusicPlayer?.start()
    }

    override fun onPause() {
        super.onPause()
        menuMusicPlayer?.pause()
    }

    override fun onDestroy() {
        super.onDestroy()
        menuMusicPlayer?.release()
    }
}
