package com.example.kingoftokyo

import android.content.Context
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
        val host = context as? GameActivity
        host?.showBlockingEffect(
            title = "Effet activé",
            desc = "${card.name} – ${card.description}",
            duration = 1200L
        )
    }

}
