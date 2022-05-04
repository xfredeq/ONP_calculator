package com.example.onp_calculator

import android.graphics.Color

enum class DisplayColors(val color: Int) {
    WHITE(Color.WHITE),
    YELLOW(Color.YELLOW),
    GREEN(Color.GREEN),
    CYAN(Color.CYAN),
    GRAY(Color.LTGRAY)
}

inline fun <reified T: Enum<T>> T.next(): T {
    val values = enumValues<T>()
    val nextOrdinal = (ordinal + 1) % values.size
    return values[nextOrdinal]
}