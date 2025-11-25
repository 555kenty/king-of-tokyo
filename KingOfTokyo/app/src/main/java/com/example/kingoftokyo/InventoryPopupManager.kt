package com.example.kingoftokyo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.cardview.widget.CardView

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

        for (card in player.cards) {
            val cardView = inflater.inflate(R.layout.card_item, container, false) as CardView
            val cardImage = cardView.findViewById<ImageView>(R.id.cardImage)
            
            if (card.image != null) {
                cardImage.setImageResource(card.image)
            }

            cardView.setOnClickListener {
                selectCard(card, cardView)
            }

            container.addView(cardView)
            cardViews[card] = cardView
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
            // CORRECTION : On applique le contour en "foreground"
            clickedView.foreground = context.getDrawable(R.drawable.card_selection_border)
        }
        updateButtons()
    }

    private fun useSelectedCard() {
        selectedCard?.let { cardToUse ->
            val cardType = cardToUse.type

            gameManager.useCard(cardToUse)
            Toast.makeText(context, "Effet de ${cardToUse.name} utilisé !", Toast.LENGTH_SHORT).show()
            
            // La carte a été supprimée par le GameManager, on rafraîchit l'affichage
            if (cardType == CardType.ACTION) {
                refreshInventory()
            } else {
                // Si c'est un pouvoir, on le désélectionne simplement
                cardViews[cardToUse]?.foreground = null
                selectedCard = null
                updateButtons()
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
}