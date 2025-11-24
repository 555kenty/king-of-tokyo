package com.example.kingoftokyo

import androidx.annotation.DrawableRes

data class Monster(
    val name: String,
    @DrawableRes val image: Int,
    var healthPoints: Int = 10,
    var victoryPoints: Int = 0,
    var description: String = ""
)
