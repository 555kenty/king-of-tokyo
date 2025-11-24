package com.example.kingoftokyo

import androidx.annotation.DrawableRes

enum class DieFace(@DrawableRes val drawableId: Int) {
    ONE(R.drawable.face_1),
    TWO(R.drawable.face_2),
    THREE(R.drawable.face_3),
    HEART(R.drawable.face_coeur),
    ENERGY(R.drawable.face_eclair),
    SMASH(R.drawable.face_poing)
}
