package com.example.kingoftokyo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView

class DicePopupManager(private val context: Context, private val root: ViewGroup) {

    private var popupView: View? = null
    private val diceImageViews = mutableListOf<ImageView>()
    private var dice = Array(3) { Die() }
    private var rollsLeft = 3

    private val handler = Handler(Looper.getMainLooper())
    private var onDiceResult: ((List<Die>) -> Unit)? = null

    private fun ensurePopupView() {
        if (popupView == null) {
            val inflater = LayoutInflater.from(context)
            popupView = inflater.inflate(R.layout.layout_dice_popup, root, false)

            // Utilisation des bons IDs
            diceImageViews.addAll(listOf(
                popupView!!.findViewById(R.id.die1),
                popupView!!.findViewById(R.id.die2),
                popupView!!.findViewById(R.id.die3),
            ))

            // CORRECTION : Les listeners des boutons étaient manquants. Je les remets ici.
            popupView?.findViewById<Button>(R.id.readyButton)?.setOnClickListener { endRollPhase() }
            popupView?.findViewById<Button>(R.id.rerollButton)?.setOnClickListener { rollDice() }
        }
    }

    fun showDiceForHuman(onResult: (List<Die>) -> Unit) {
        ensurePopupView()
        this.onDiceResult = onResult

        popupView?.findViewById<View>(R.id.buttonsRow)?.visibility = View.VISIBLE
        diceImageViews.forEachIndexed { index, dieView ->
            dieView.setOnClickListener { onDieClicked(index) }
        }

        resetAndRoll()
        if (popupView?.parent == null) {
            root.addView(popupView)
        }
    }

    fun showDiceForBot(botDice: List<Die>) {
        ensurePopupView()
        this.dice = botDice.toTypedArray()
        popupView?.findViewById<View>(R.id.buttonsRow)?.visibility = View.GONE
        diceImageViews.forEach { it.setOnClickListener(null) }

        updateUI()
        if (popupView?.parent == null) {
            root.addView(popupView)
        }

        handler.postDelayed({ hideDicePopup() }, 2000)
    }

    private fun resetAndRoll() {
        rollsLeft = 3
        dice.forEach { it.isLocked = true } // On sélectionne tout pour le 1er lancer
        rollDice()
    }

    private fun onDieClicked(index: Int) {
        if (rollsLeft < 3) {
            dice[index].isLocked = !dice[index].isLocked
            updateUI()
        }
    }

    private fun rollDice() {
        if (rollsLeft > 0 && dice.any { it.isLocked }) {
            rollsLeft--
            animateDiceRoll()
        }
    }

    private fun animateDiceRoll() {
        val animationDuration = 1000L
        val frameRate = 60L
        val rollStartTime = System.currentTimeMillis()

        val updateRunnable = object : Runnable {
            override fun run() {
                val elapsedTime = System.currentTimeMillis() - rollStartTime
                val isAnimationOver = elapsedTime >= animationDuration

                dice.forEachIndexed { index, die ->
                    if (die.isLocked) {
                        if (isAnimationOver) {
                            die.face = DieFace.values().random()
                            die.isLocked = false
                        } else {
                            diceImageViews[index].setImageResource(DieFace.values().random().drawableId)
                        }
                    }
                }

                if (isAnimationOver) {
                    updateUI()
                    if (rollsLeft == 0) {
                        handler.postDelayed({ endRollPhase() }, 1500)
                    }
                } else {
                    handler.postDelayed(this, 1000L / frameRate)
                }
            }
        }

        handler.post(updateRunnable)
    }

    private fun endRollPhase() {
        if (popupView?.parent != null) {
            onDiceResult?.invoke(dice.toList())
            hideDicePopup()
        }
    }

    private fun updateUI() {
        dice.forEachIndexed { index, die ->
            diceImageViews[index].setImageResource(die.face.drawableId)
            diceImageViews[index].isSelected = die.isLocked
        }

        popupView?.findViewById<TextView>(R.id.rollInfoTextView)?.text = "Lancers restants : $rollsLeft"
        popupView?.findViewById<Button>(R.id.rerollButton)?.isEnabled = rollsLeft > 0 && dice.any { it.isLocked }
        popupView?.findViewById<Button>(R.id.readyButton)?.isEnabled = rollsLeft < 3
    }

    private fun hideDicePopup() {
        popupView?.let {
            if (it.parent != null) {
                root.removeView(it)
            }
        }
    }
}
