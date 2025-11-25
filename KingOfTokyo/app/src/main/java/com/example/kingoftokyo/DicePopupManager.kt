package com.example.kingoftokyo

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.PopupWindow
import android.widget.TextView

class DicePopupManager(private val context: Context, private val root: ViewGroup) {

    private var popupWindow: PopupWindow? = null
    private var popupView: View? = null
    private val diceImageViews = mutableListOf<ImageView>()
    private var dice = Array(DICE_COUNT) { Die() }
    private var rollsLeft = MAX_ROLLS
    private val handler = Handler(Looper.getMainLooper())
    private var onDiceResult: ((List<Die>) -> Unit)? = null

    fun showDiceForHuman(onResult: (List<Die>) -> Unit) {
        createPopupView()
        this.onDiceResult = onResult
        popupView?.findViewById<View>(R.id.buttonsRow)?.visibility = View.VISIBLE
        diceImageViews.forEachIndexed { index, dieView ->
            dieView.setOnClickListener { onDieClicked(index) }
        }

        resetAndRoll()
        showPopup()
    }

    fun showDiceForBot(botDice: List<Die>) {
        createPopupView()
        this.dice = botDice.toTypedArray()
        popupView?.findViewById<View>(R.id.buttonsRow)?.visibility = View.GONE
        diceImageViews.forEach { it.setOnClickListener(null) }

        rollsLeft = 0
        updateUI()
        showPopup()

        // Auto-fermeture après affichage du résultat bot.
        handler.postDelayed({ hide() }, 2000)
    }

    private fun createPopupView() {
        val inflater = LayoutInflater.from(context)
        popupView = inflater.inflate(R.layout.layout_dice_popup, null)

        diceImageViews.clear()
        popupView?.let { view ->
            diceImageViews.addAll(listOf(
                view.findViewById(R.id.die1),
                view.findViewById(R.id.die2),
                view.findViewById(R.id.die3),
            ))

            view.findViewById<Button>(R.id.readyButton)?.setOnClickListener { endRollPhase() }
            view.findViewById<Button>(R.id.rerollButton)?.setOnClickListener { rollDice() }
        }
    }

    private fun showPopup() {
        popupView?.let { view ->
            if (popupWindow?.isShowing == true) {
                popupWindow?.dismiss()
            }
            popupWindow = PopupWindow(
                view,
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
            )
            popupWindow?.isFocusable = true
            popupWindow?.showAtLocation(root, Gravity.CENTER, 0, 0)
        }
    }

    private fun onDieClicked(index: Int) {
        if (rollsLeft < MAX_ROLLS) {
            dice[index].isLocked = !dice[index].isLocked
            updateUI()
        }
    }

    private fun resetAndRoll() {
        rollsLeft = MAX_ROLLS
        dice.forEach { it.isLocked = true } // Tout relancer au premier lancer
        rollDice()
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
                            diceImageViews.getOrNull(index)?.setImageResource(DieFace.values().random().drawableId)
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
        if (popupWindow?.isShowing == true) {
            onDiceResult?.invoke(dice.toList())
            hide()
        }
    }

    private fun updateUI() {
        dice.forEachIndexed { index, die ->
            diceImageViews.getOrNull(index)?.setImageResource(die.face.drawableId)
            diceImageViews.getOrNull(index)?.isSelected = die.isLocked
        }

        popupView?.findViewById<TextView>(R.id.rollInfoTextView)?.text = "Lancers restants : $rollsLeft"
        popupView?.findViewById<Button>(R.id.rerollButton)?.isEnabled = rollsLeft > 0 && dice.any { it.isLocked }
        popupView?.findViewById<Button>(R.id.readyButton)?.isEnabled = rollsLeft < MAX_ROLLS
    }

    private fun hide() {
        popupWindow?.dismiss()
        popupWindow = null
        popupView = null
        handler.removeCallbacksAndMessages(null)
    }

    companion object {
        private const val DICE_COUNT = 3
        private const val MAX_ROLLS = 3
    }
}
