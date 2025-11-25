package com.example.kingoftokyo

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat

class InventoryPopupManager(
    private val context: Context,
    private val root: ViewGroup,
    private val gameManager: GameManager
) {

    private var popupView: View? = null
    private val cardViews = mutableMapOf<Card, CardView>() // Associe la carte à sa CardView
    private var selectedCard: Card? = null

    fun show() {
        if (popupView == null) {
            val inflater = LayoutInflater.from(context)
            popupView = inflater.inflate(R.layout.inventory_popup, root, false)
            root.addView(popupView)

            popupView?.findViewById<Button>(R.id.useEffectButton)?.setOnClickListener { useSelectedCard() }
            popupView?.findViewById<Button>(R.id.backButton)?.setOnClickListener { hide() }
        }
        refreshInventory()
    }

    private fun refreshInventory() {
        val container = popupView?.findViewById<LinearLayout>(R.id.inventoryCardsContainer) ?: return
        val inflater = LayoutInflater.from(context)
        val player = gameManager.currentPlayer

        container.removeAllViews()
        cardViews.clear()
        selectedCard = null

        val cardsPerRow = 3
        player.cards.chunked(cardsPerRow).forEach { rowCards ->
            val row = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_HORIZONTAL
            }
            rowCards.forEach { card ->
                val cardView = inflater.inflate(R.layout.card_item, row, false) as CardView
                val cardImage = cardView.findViewById<ImageView>(R.id.cardImage)
                
                if (card.image != null) {
                    cardImage.setImageResource(card.image)
                }

                cardView.setOnClickListener {
                    selectCard(card, cardView)
                }

                row.addView(cardView)
                cardViews[card] = cardView
            }
            container.addView(row)
        }

        updateButtons()
    }

    private fun selectCard(card: Card, clickedView: CardView) {
        val isDeselecting = selectedCard == card
        
        // Réinitialise l'apparence de toutes les cartes
        cardViews.values.forEach { it.foreground = null }
        
        if (isDeselecting) {
            selectedCard = null
        } else {
            selectedCard = card
            clickedView.foreground = context.getDrawable(R.drawable.card_selection_border)
        }
        updateButtons()
    }

    private fun useSelectedCard() {
        selectedCard?.let { cardToUse ->
            val shouldRemove = cardToUse.type == CardType.ACTION
            gameManager.useCard(cardToUse, removeFromInventory = shouldRemove)
            showInventoryBanner(cardToUse)
            showEffectOverlay(cardToUse)
            
            refreshInventory()
            selectedCard = null
            updateButtons()
            if (shouldRemove) {
                hide() // On ferme l'inventaire après usage d'une action
            }
        }
    }

    private fun updateButtons() {
        val useEffectButton = popupView?.findViewById<Button>(R.id.useEffectButton)
        useEffectButton?.isEnabled = selectedCard != null
    }

    fun hide() {
        popupView?.let { root.removeView(it) }
        popupView = null
        selectedCard = null
    }

    private fun showInventoryBanner(card: Card) {
        val banner = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            background = ContextCompat.getDrawable(context, R.drawable.button_main_menu)
            val textView = TextView(context).apply {
                text = "${gameManager.currentPlayer.monster.name} utilise ${card.name}"
                setTextColor(ContextCompat.getColor(context, android.R.color.white))
                textSize = 16f
            }
            addView(textView)
            alpha = 0f
        }

        val params = ViewGroup.MarginLayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        ).apply {
            topMargin = 24
            marginStart = 24
            marginEnd = 24
        }

        root.addView(banner, params)
        banner.animate()
            .alpha(1f)
            .setDuration(150)
            .withEndAction {
                banner.animate()
                    .alpha(0f)
                    .setStartDelay(700)
                    .setDuration(250)
                    .withEndAction { root.removeView(banner) }
                    .start()
            }
            .start()
    }

    private fun showEffectOverlay(card: Card) {
        val effect = resolveEffectVisual(card)
        val overlay = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(32, 32, 32, 32)
            setBackgroundColor(effect.color)
            isClickable = true // bloque les interactions pendant l'affichage
            val title = TextView(context).apply {
                text = "Effet activé"
                setTextColor(Color.WHITE)
                textSize = 18f
            }
            val desc = TextView(context).apply {
                text = effect.message
                setTextColor(Color.WHITE)
                textSize = 16f
            }
            val duration = TextView(context).apply {
                text = effect.durationText
                setTextColor(Color.WHITE)
                textSize = 14f
            }
            addView(title)
            addView(desc)
            addView(duration)
            alpha = 0f
        }

        val params = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        root.addView(overlay, params)
        overlay.animate()
            .alpha(0.9f)
            .setDuration(200)
            .withEndAction {
                overlay.animate()
                    .alpha(0f)
                    .setStartDelay(2500) // reste ~2-3s
                    .setDuration(300)
                    .withEndAction { root.removeView(overlay) }
                    .start()
            }
            .start()
    }

    private fun resolveEffectVisual(card: Card): EffectVisual {
        return when (card.name) {
            "Absorption d’Énergie" -> EffectVisual("+3⚡", "Instantané", 0xCC43A047.toInt())
            "Mutation Express" -> EffectVisual("+1⭐ et +1⚡", "Instantané", 0xCC1E88E5.toInt())
            "Bond Titanesque" -> EffectVisual("Entre à Tokyo, +1⭐", "Instantané", 0xCC1E88E5.toInt())
            "Téléportation" -> EffectVisual("Change de zone Tokyo/dehors", "Instantané", 0xCC7B1FA2.toInt())
            "Frappe Orbitale" -> EffectVisual("Inflige 3❤️ à un adversaire", "Instantané", 0xCCF4511E.toInt())
            "Onde de Choc" -> EffectVisual("Tous les autres perdent 1❤️", "Instantané", 0xCCF4511E.toInt())
            "Propulsion" -> EffectVisual("+4⭐ en quittant Tokyo", "Permanent", 0xCC1E88E5.toInt())
            "Griffes Chargées" -> EffectVisual("+1👊 hors Tokyo", "Permanent", 0xCCF4511E.toInt())
            "Cœur Atomique" -> EffectVisual("+2⭐ quand tu attaques Tokyo", "Permanent", 0xCC1E88E5.toInt())
            "Nano-Régénération" -> EffectVisual("+1❤️ fin de tour", "Permanent", 0xCC43A047.toInt())
            "Carapace Adaptative" -> EffectVisual("+1⭐ début de tour en Tokyo", "Permanent", 0xCC1E88E5.toInt())
            "Hurlement Terrifiant" -> EffectVisual("Les autres perdent 1⭐ début de tour", "Permanent", 0xCCF4511E.toInt())
            "Vision Nocturne" -> EffectVisual("Attaques inévitables", "Permanent", 0xCC3949AB.toInt())
            "Batterie Surchargée" -> EffectVisual("+3⚡ si tu gardes 3⚡", "Permanent", 0xCC43A047.toInt())
            "Rage Primale" -> EffectVisual("+1👊 et reste à Tokyo", "Permanent", 0xCCF4511E.toInt())
            "Sang Corrompu" -> EffectVisual("+2👊 mais -1❤️ après attaque", "Permanent", 0xCCF4511E.toInt())
            "Mutation Cristalline" -> EffectVisual("+3⚡ si tu prends 3+ dégâts", "Permanent", 0xCC43A047.toInt())
            "Parasite Kaiju" -> EffectVisual("Vole 1⚡ et -1❤️ pour toi", "Permanent", 0xCCF4511E.toInt())
            "Mode Apocalypse" -> EffectVisual("+3👊", "Permanent", 0xCCF4511E.toInt())
            "Résurrection" -> EffectVisual("Revient à 6❤️ en mourant", "Permanent", 0xCC43A047.toInt())
            else -> EffectVisual("Effet appliqué", "Instantané", 0xCC263238.toInt())
        }
    }

    private data class EffectVisual(val message: String, val durationText: String, val color: Int)
}
