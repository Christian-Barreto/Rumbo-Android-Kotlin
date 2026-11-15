package com.rumbo.app.ui.componentes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.text.KeyboardOptions
import com.rumbo.app.data.Estado
import com.rumbo.app.ui.tema.RumboColores

// ------------------------------------------------------------------ colores por tipo
fun colorRiesgo(riesgo: String): Color = when (riesgo) {
    "bajo" -> RumboColores.VerdeC
    "medio" -> RumboColores.AmbarC
    else -> RumboColores.RojoC
}

fun colorTextoRiesgo(riesgo: String): Color = when (riesgo) {
    "bajo" -> RumboColores.Verde
    "medio" -> RumboColores.Ambar
    else -> RumboColores.Rojo
}

fun colorFondoRiesgo(riesgo: String): Color = when (riesgo) {
    "bajo" -> RumboColores.VerdeBg
    "medio" -> RumboColores.AmbarBg
    else -> RumboColores.RojoBg
}

fun colorCategoria(categoria: String): Color = when (categoria) {
    "hobbies" -> RumboColores.Hobbies
    "fortalezas" -> RumboColores.Fortalezas
    "intereses" -> RumboColores.Intereses
    else -> RumboColores.EstiloTrabajo
}

/** "2026-10-27T01:50:12" -> "27/10/2026 01:50" */
fun formatearFecha(iso: String): String = try {
    val partes = iso.substring(0, 10).split("-")
    "${partes[2]}/${partes[1]}/${partes[0]} ${iso.substring(11, 16)}"
} catch (e: Exception) {
    iso
}

// ------------------------------------------------------------------ estructura de pantalla
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Pantalla(
    titulo: String,
    alVolver: (() -> Unit)? = null,
    acciones: @Composable RowScope.() -> Unit = {},
    barraInferior: @Composable () -> Unit = {},
    contenido: @Composable (PaddingValues) -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titulo, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    if (alVolver != null) {
                        TextButton(onClick = alVolver) { Text("←", color = Color.White, fontSize = 22.sp) }
                    }
                },
                actions = acciones,
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = RumboColores.Navy,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White,
                    actionIconContentColor = Color.White
                )
            )
        },
        bottomBar = barraInferior,
        containerColor = MaterialTheme.colorScheme.background,
        content = contenido
    )
}

/** Muestra carga, error con "Reintentar" o el contenido cuando los datos están listos. */
@Composable
fun <T> Cargable(
    estado: Estado<T>,
    alReintentar: () -> Unit,
    alSesionExpirada: () -> Unit,
    contenido: @Composable (T) -> Unit
) {
    when (estado) {
        is Estado.Cargando -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        }
        is Estado.Falla -> {
            if (estado.codigo == 401) {
                LaunchedEffect(Unit) { alSesionExpirada() }
            } else {
                Column(
                    Modifier.fillMaxSize().padding(24.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(estado.mensaje, textAlign = TextAlign.Center, color = RumboColores.Rojo)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = alReintentar) { Text("Reintentar") }
                }
            }
        }
        is Estado.Listo -> contenido(estado.datos)
    }
}

// ------------------------------------------------------------------ bloques visuales
@Composable
fun Tarjeta(
    modifier: Modifier = Modifier,
    color: Color = RumboColores.Superficie,
    alClic: (() -> Unit)? = null,
    contenido: @Composable ColumnScope.() -> Unit
) {
    val forma = RoundedCornerShape(16.dp)
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = forma,
        color = color,
        border = BorderStroke(1.dp, RumboColores.Borde),
        shadowElevation = 2.dp
    ) {
        Column(
            modifier = Modifier
                .clip(forma)
                .then(if (alClic != null) Modifier.clickable(onClick = alClic) else Modifier)
                .padding(16.dp),
            content = contenido
        )
    }
}

@Composable
fun Etiqueta(texto: String, color: Color = RumboColores.Azul2) {
    Text(texto.uppercase(), color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.2.sp)
}

@Composable
fun TituloSeccion(texto: String, modifier: Modifier = Modifier) {
    Text(
        texto,
        modifier = modifier.padding(top = 20.dp, bottom = 8.dp),
        fontSize = 20.sp,
        fontWeight = FontWeight.Bold,
        color = RumboColores.Navy
    )
}

@Composable
fun Insignia(texto: String, fondo: Color, color: Color) {
    Box(Modifier.clip(CircleShape).background(fondo).padding(horizontal = 10.dp, vertical = 3.dp)) {
        Text(texto, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun InsigniaRiesgo(riesgo: String, etiqueta: String) =
    Insignia("Riesgo IA: $etiqueta", colorFondoRiesgo(riesgo), colorTextoRiesgo(riesgo))

/** Tres segmentos que se llenan según el riesgo: bajo = 1, medio = 2, alto = 3. */
@Composable
fun MedidorRiesgo(riesgo: String, grande: Boolean = false, sobreOscuro: Boolean = false) {
    val nivel = when (riesgo) { "bajo" -> 1; "medio" -> 2; else -> 3 }
    val ancho: Dp = if (grande) 52.dp else 24.dp
    val alto: Dp = if (grande) 16.dp else 9.dp
    val vacio = if (sobreOscuro) Color.White.copy(alpha = 0.25f) else Color(0xFFD4DDE8)
    Row(horizontalArrangement = Arrangement.spacedBy(if (grande) 8.dp else 4.dp)) {
        for (i in 1..3) {
            Box(
                Modifier
                    .width(ancho)
                    .height(alto)
                    .clip(CircleShape)
                    .background(if (i <= nivel) colorRiesgo(riesgo) else vacio)
            )
        }
    }
}

@Composable
fun BarraPuntaje(fraccion: Float, modifier: Modifier = Modifier) {
    Box(modifier.height(10.dp).clip(CircleShape).background(Color(0xFFE3EBF3))) {
        Box(
            Modifier
                .fillMaxHeight()
                .fillMaxWidth(fraccion.coerceIn(0f, 1f))
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(RumboColores.Cian, RumboColores.Azul2)))
        )
    }
}

@Composable
fun Pastilla(texto: String, seleccionada: Boolean, alClic: () -> Unit) {
    Box(
        Modifier
            .clip(CircleShape)
            .background(if (seleccionada) RumboColores.Azul2 else RumboColores.Tarjeta2)
            .clickable(onClick = alClic)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Text(
            texto,
            color = if (seleccionada) Color.White else RumboColores.Azul,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ------------------------------------------------------------------ formularios
@Composable
fun CampoTexto(
    valor: String,
    alCambiar: (String) -> Unit,
    etiqueta: String,
    error: String? = null,
    teclado: KeyboardType = KeyboardType.Text,
    oculto: Boolean = false,
    multilinea: Boolean = false
) {
    OutlinedTextField(
        value = valor,
        onValueChange = alCambiar,
        label = { Text(etiqueta) },
        isError = error != null,
        supportingText = { if (error != null) Text(error) },
        singleLine = !multilinea,
        minLines = if (multilinea) 3 else 1,
        keyboardOptions = KeyboardOptions(keyboardType = teclado),
        visualTransformation = if (oculto) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier.fillMaxWidth()
    )
}

@Composable
fun BotonPrincipal(texto: String, alClic: () -> Unit, activo: Boolean = true, modifier: Modifier = Modifier) {
    Button(
        onClick = alClic,
        enabled = activo,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) { Text(texto, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun BotonSecundario(texto: String, alClic: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = alClic,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp)
    ) { Text(texto, fontWeight = FontWeight.SemiBold) }
}

@Composable
fun MensajeError(texto: String) {
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(RumboColores.RojoBg)
            .padding(12.dp)
    ) { Text(texto, color = RumboColores.Rojo, fontWeight = FontWeight.Medium) }
}

@Composable
fun BotonPeligro(texto: String, alClic: () -> Unit, modifier: Modifier = Modifier) {
    OutlinedButton(
        onClick = alClic,
        modifier = modifier.fillMaxWidth().height(50.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = RumboColores.Rojo),
        border = BorderStroke(1.dp, RumboColores.Rojo)
    ) { Text(texto, fontWeight = FontWeight.SemiBold) }
}

/** Barra fija al pie de la pantalla con un botón principal. */
@Composable
fun BarraAccion(texto: String, alClic: () -> Unit, activo: Boolean = true) {
    Surface(shadowElevation = 8.dp, color = RumboColores.Fondo) {
        Box(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
            BotonPrincipal(texto, alClic, activo)
        }
    }
}

@Composable
fun DialogoConfirmar(
    titulo: String,
    mensaje: String,
    textoConfirmar: String,
    alConfirmar: () -> Unit,
    alCancelar: () -> Unit
) {
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(titulo) },
        text = { Text(mensaje) },
        confirmButton = {
            TextButton(onClick = alConfirmar) {
                Text(textoConfirmar, color = RumboColores.Rojo, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
