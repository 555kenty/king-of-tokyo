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
    private val dice = Array(3) { Die() } // On gère 3 dés
    private var rollsLeft = 3

    private val handler = Handler(Looper.getMainLooper())

    fun showDicePopup() {
        if (popupView == null) {
            val inflater = LayoutInflater.from(context)
            popupView = inflater.inflate(R.layout.layout_dice_popup, root, false)

            // CORRECTION 1 : Utilisation des bons IDs (die1View, etc.)
            diceImageViews.addAll(listOf(
                popupView!!.findViewById(R.id.die1),
                popupView!!.findViewById(R.id.die2),
                popupView!!.findViewById(R.id.die3 ),
            ))

            diceImageViews.forEachIndexed { index, dieView ->
                dieView.setOnClickListener { onDieClicked(index) }
            }

            popupView?.findViewById<Button>(R.id.readyButton)?.setOnClickListener {
                // La logique pour appliquer les résultats ira ici
                hideDicePopup()
            }

            popupView?.findViewById<Button>(R.id.rerollButton)?.setOnClickListener {
                rollDice()
            }
        }

        resetAndRoll()
        root.addView(popupView)
    }

    private fun resetAndRoll() {
        rollsLeft = 3
        // Pour le premier lancer, on "sélectionne" tous les dés pour qu'ils soient tous relancés
        dice.forEach { it.isLocked = true }
        rollDice()
    }

    private fun onDieClicked(index: Int) {
        // On ne peut sélectionner un dé qu'après le premier lancer
        if (rollsLeft < 3) {
            dice[index].isLocked = !dice[index].isLocked
            updateUI()
        }
    }

    private fun rollDice() {
        // On ne peut relancer que s'il reste des lancers ET si au moins un dé est sélectionné
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
                    // CORRECTION 2 : On relance le dé uniquement s'il est sélectionné (isLocked)
                    if (die.isLocked) {
                        if (isAnimationOver) {
                            die.face = DieFace.values().random()
                            die.isLocked = false // Le dé est désélectionné après la relance
                        } else {
                            diceImageViews[index].setImageResource(DieFace.values().random().drawableId)
                        }
                    }
                }

                if (isAnimationOver) {
                    updateUI()
                } else {
                    handler.postDelayed(this, 1000L / frameRate)
                }
            }
        }

        handler.post(updateRunnable)
    }

    private fun updateUI() {
        dice.forEachIndexed { index, die ->
            diceImageViews[index].setImageResource(die.face.drawableId)
            diceImageViews[index].isSelected = die.isLocked
        }

        popupView?.findViewById<TextView>(R.id.rollInfoTextView)?.text = "Lancers restants : $rollsLeft"

        val rerollButton = popupView?.findViewById<Button>(R.id.rerollButton)
        rerollButton?.isEnabled = rollsLeft > 0

        if (rollsLeft == 0) {
            handler.postDelayed({ hideDicePopup() }, 2000)
        }
    }

    fun hideDicePopup() {
        popupView?.let {
            root.removeView(it)
        }
    }
}
