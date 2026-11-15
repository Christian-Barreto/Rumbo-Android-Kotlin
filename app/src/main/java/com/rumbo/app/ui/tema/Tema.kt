package com.rumbo.app.ui.tema

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * TODO el aspecto de la app sale de este archivo: colores, formas y tipografía.
 * Cambia aquí la paleta y toda la app cambia.
 * (Colores iguales a los de rumbo.css de la versión web.)
 */
object RumboColores {
    val Navy = Color(0xFF0F2747)
    val Azul = Color(0xFF1F4D78)
    val Azul2 = Color(0xFF2563EB)   // color principal (botones, enlaces)
    val Cian = Color(0xFF38BDF8)
    val Fondo = Color(0xFFF3F6FB)
    val Texto = Color(0xFF14212E)
    val Suave = Color(0xFF5A6B7C)
    val Borde = Color(0xFFDBE4EE)
    val Superficie = Color.White
    val Tarjeta2 = Color(0xFFEAF2FD)   // fondo de tarjetas secundarias

    // semáforo del riesgo de IA
    val Verde = Color(0xFF15803D); val VerdeBg = Color(0xFFE3F6EA); val VerdeC = Color(0xFF22C55E)
    val Ambar = Color(0xFFA35A00); val AmbarBg = Color(0xFFFFF2D6); val AmbarC = Color(0xFFF59E0B)
    val Rojo = Color(0xFFB91C1C); val RojoBg = Color(0xFFFDE8E7); val RojoC = Color(0xFFEF4444)

    // color de cada categoría de pregunta
    val Hobbies = Color(0xFFF59E0B)
    val Fortalezas = Color(0xFF10B981)
    val Intereses = Color(0xFF6366F1)
    val EstiloTrabajo = Color(0xFFEC4899)
}

private val Esquema = lightColorScheme(
    primary = RumboColores.Azul2,
    onPrimary = Color.White,
    secondary = RumboColores.Azul,
    onSecondary = Color.White,
    background = RumboColores.Fondo,
    onBackground = RumboColores.Texto,
    surface = RumboColores.Superficie,
    onSurface = RumboColores.Texto,
    surfaceVariant = RumboColores.Tarjeta2,
    onSurfaceVariant = RumboColores.Suave,
    outline = RumboColores.Borde,
    error = RumboColores.Rojo
)

private val Formas = Shapes(
    small = RoundedCornerShape(10.dp),
    medium = RoundedCornerShape(14.dp),
    large = RoundedCornerShape(16.dp)
)

@Composable
fun RumboTema(contenido: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = Esquema,
        typography = Typography(),
        shapes = Formas,
        content = contenido
    )
}
