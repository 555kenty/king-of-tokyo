package com.example.kingoftokyo

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

class TokyoChoicePopupManager(private val context: Context, private val root: ViewGroup) {

    private var popupView: View? = null

    fun showChoicePopup(onChoice: (Boolean) -> Unit) {
        if (popupView == null) {
            val inflater = LayoutInflater.from(context)
            popupView = inflater.inflate(R.layout.layout_tokyo_choice_popup, root, false)
        }

        popupView?.findViewById<Button>(R.id.stayButton)?.setOnClickListener {
            onChoice(true) // Reste à Tokyo
            hidePopup()
        }

        popupView?.findViewById<Button>(R.id.leaveButton)?.setOnClickListener {
            onChoice(false) // Sort de Tokyo
            hidePopup()
        }

        root.addView(popupView)
    }

    private fun hidePopup() {
        popupView?.let {
            root.removeView(it)
        }
    }
}
