package com.example.kingoftokyo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import android.os.Handler
import android.os.Looper

class ShopPopupManager(
    private val context: Context,
    private val root: ViewGroup
) {

    private var popupView: View? = null
    private val cardImageViews = mutableListOf<ImageView>()
    // CORRECTION FINALE : Cette liste DOIT toujours avoir 3 éléments, comme l'interface.
    private val selectedInUI = mutableListOf(false, false, false)
    private var gameManager: GameManager? = null
    private val handler = Handler(Looper.getMainLooper())

    fun showShop(gameManager: GameManager) {
        this.gameManager = gameManager
        if (popupView != null) { // Si la vue existe déjà, on la met juste à jour
            updateShopUI()
            return
        }

        val inflater = LayoutInflater.from(context)
        popupView = inflater.inflate(R.layout.layout_shop_popup, root, false)

        cardImageViews.clear()
        popupView?.findViewById<View>(R.id.card1)?.let { cardImageViews.add(it.findViewById(R.id.cardImage)) }
        popupView?.findViewById<View>(R.id.card2)?.let { cardImageViews.add(it.findViewById(R.id.cardImage)) }
        popupView?.findViewById<View>(R.id.card3)?.let { cardImageViews.add(it.findViewById(R.id.cardImage)) }

        popupView?.findViewById<Button>(R.id.buyButton)?.setOnClickListener { buySelectedCards() }
        popupView?.findViewById<Button>(R.id.refreshButton)?.setOnClickListener { refreshShop() }
        popupView?.findViewById<Button>(R.id.finishButton)?.setOnClickListener { finishShopping() }

        cardImageViews.forEachIndexed { index, imageView ->
            imageView.setOnClickListener { selectCard(index) }
        }

        root.addView(popupView)
        updateShopUI()
    }

    private fun selectCard(uiIndex: Int) {
        // On ne peut sélectionner une carte que si elle existe à cet emplacement.
        if (gameManager?.shop?.getOrNull(uiIndex) != null) {
            selectedInUI[uiIndex] = !selectedInUI[uiIndex]
            updateCardSelectionUI()
            updateButtons()
        }
    }

    private fun updateShopUI() {
        val gm = gameManager ?: return
        val cards = gm.shop
        val player = gm.currentPlayer

        popupView?.findViewById<TextView>(R.id.playerEnergy)?.text = player.energy.toString()

        // On réinitialise la sélection à chaque mise à jour.
        selectedInUI.fill(false)

        for (i in cardImageViews.indices) { // i va de 0 à 2
            val card = cards.getOrNull(i)
            val imageView = cardImageViews[i]
            if (card?.image != null) {
                imageView.setImageResource(card.image)
                imageView.visibility = View.VISIBLE
            } else {
                imageView.visibility = View.INVISIBLE
            }
        }
        updateCardSelectionUI()
        updateButtons()
    }

    private fun updateCardSelectionUI() {
        for (i in cardImageViews.indices) {
            // selectedInUI a toujours 3 éléments, donc pas de crash.
            cardImageViews[i].alpha = if (selectedInUI[i]) 0.6f else 1.0f
        }
    }

    private fun buySelectedCards() {
        val gm = gameManager ?: return
        
        val cardsToBuy = selectedInUI.mapIndexedNotNull { index, isSelected ->
            if (isSelected) gm.shop.getOrNull(index)?.let { card -> index to card } else null
        }

        val totalCost = cardsToBuy.sumOf { it.second.cost }

        if (totalCost > gm.currentPlayer.energy) {
            Toast.makeText(context, "Pas assez d'énergie !", Toast.LENGTH_SHORT).show()
            return
        }
        
        var hasAction = false
        cardsToBuy.forEach { (uiIndex, card) ->
            if (card.type == CardType.ACTION) {
                animateActionActivation(uiIndex, card)
                hasAction = true
            }
            gm.buyCard(card)
        }

        if (hasAction) {
            // Laisser l'animation se jouer puis rafraîchir le shop sans le fermer
            handler.postDelayed({ updateShopUI() }, 1200)
        } else {
            updateShopUI()
        }
    }

    private fun refreshShop() {
        val gm = gameManager ?: return
        if (gm.currentPlayer.energy >= 2) {
            gm.refreshShop()
            updateShopUI()
        } else {
            Toast.makeText(context, "Il faut 2⚡ pour rafraîchir", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateButtons() {
        val gm = gameManager ?: return
        val player = gm.currentPlayer
        val totalCost = selectedInUI.mapIndexedNotNull { index, isSelected ->
            if (isSelected) gm.shop.getOrNull(index) else null
        }.sumOf { it.cost }

        popupView?.findViewById<Button>(R.id.buyButton)?.isEnabled = totalCost > 0 && player.energy >= totalCost
        popupView?.findViewById<Button>(R.id.refreshButton)?.isEnabled = player.energy >= 2
    }

    private fun finishShopping() {
        gameManager?.finishShopping()
    }

    fun hideShop() {
        if (popupView == null) return
        root.removeView(popupView)
        popupView = null
        gameManager = null
    }

    private fun animateActionActivation(uiIndex: Int, card: Card) {
        cardImageViews.getOrNull(uiIndex)?.let { imageView ->
            imageView.animate()
                .scaleX(1.08f)
                .scaleY(1.08f)
                .alpha(0.85f)
                .setDuration(120)
                .withEndAction {
                    imageView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .alpha(1f)
                        .setDuration(200)
                        .start()
                }
                .start()
        }
        showActionBanner(card)
    }

    private fun showActionBanner(card: Card) {
        val playerName = gameManager?.currentPlayer?.monster?.name ?: "Un joueur"
        val banner = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            setPadding(24, 16, 24, 16)
            background = ContextCompat.getDrawable(context, R.drawable.button_main_menu)
            val textView = TextView(context).apply {
                text = "$playerName utilise ${card.name}"
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
                    .setStartDelay(3000)
                    .setDuration(250)
                    .withEndAction { root.removeView(banner) }
                    .start()
            }
            .start()
    }
}
