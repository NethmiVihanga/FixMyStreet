package com.fixmystreet.ui.components

import androidx.compose.foundation.shape.GenericShape

val WavyTopShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.7f)
    cubicTo(
        size.width * 0.75f, size.height * 1.2f,
        size.width * 0.25f, size.height * 0.4f,
        0f, size.height * 0.9f
    )
    close()
}

val WavyBottomShape = GenericShape { size, _ ->
    moveTo(0f, size.height)
    lineTo(size.width, size.height)
    lineTo(size.width, size.height * 0.4f)
    cubicTo(
        size.width * 0.6f, size.height * 0.8f,
        size.width * 0.3f, size.height * 0.1f,
        0f, size.height * 0.3f
    )
    close()
}

// A shape that cuts out a wavy bottom, used for the map to fit above the bottom white card
val WavyMapBottomShape = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width, size.height * 0.9f)
    cubicTo(
        size.width * 0.75f, size.height * 1.1f,
        size.width * 0.25f, size.height * 0.7f,
        0f, size.height * 0.85f
    )
    lineTo(0f, 0f)
    close()
}
