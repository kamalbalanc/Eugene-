package com.example.eugene.ui.components

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.spring

object EugeneAnimationTokens {
    const val Instant = 100
    const val Fast = 150
    const val Standard = 200
    const val Emphasis = 300
    const val Hero = 350
    const val Choreography = 600
    const val Ambient = 3000

    val StandardEasing: Easing = FastOutSlowInEasing
    val DecelerateEasing: Easing = LinearOutSlowInEasing
    val AccelerateEasing: Easing = FastOutLinearInEasing

    fun <T> springDefault(): SpringSpec<T> = spring(dampingRatio = 0.7f, stiffness = 380f)
    fun <T> springHeavy(): SpringSpec<T> = spring(dampingRatio = 0.6f, stiffness = 300f)
    fun <T> springLight(): SpringSpec<T> = spring(dampingRatio = 0.8f, stiffness = 500f)
    fun <T> springSettle(): SpringSpec<T> = spring(dampingRatio = 0.9f, stiffness = 380f)

    const val StaggerDense = 20
    const val StaggerStandard = 30
    const val StaggerLoose = 50
    const val StaggerCascade = 60
}
