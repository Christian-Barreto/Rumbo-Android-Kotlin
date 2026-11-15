package com.rumbo.app.ui.pantallas

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rumbo.app.data.*
import com.rumbo.app.ui.componentes.*
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

/**
 * Si es true, al pulsar "Siguiente" en cada pregunta aparece un aviso
 * "¿Estás seguro de tu respuesta?". Si es false (por defecto), la confirmación se hace
 * una sola vez, al final, después de revisar todas las respuestas.
 */
private const val CONFIRMAR_CADA_RESPUESTA = false

/** Una pregunta con su número dentro del cuestionario (1 a N) y su categoría. */
internal data class PreguntaPlana(
    val numero: Int,
    val pregunta: Pregunta,
    val categoria: String,
    val categoriaEtiqueta: String
)

class CuestionarioViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<Cuestionario>>(Estado.Cargando)
    internal var preguntas by mutableStateOf<List<PreguntaPlana>>(emptyList())
    val elegidas = mutableStateMapOf<Int, Int>()          // preguntaId -> opcionId
    var carreras by mutableStateOf<List<CarreraResumen>>(emptyList())
    var carreraEnMente by mutableStateOf<CarreraResumen?>(null)

    /** 0 = pantalla de inicio, 1..N = pregunta número "paso", N+1 = revisión de respuestas. */
    var paso by mutableStateOf(0)
    /** true cuando se llegó a una pregunta tocando "Cambiar" en la revisión. */
    var desdeRevision by mutableStateOf(false)

    var enviando by mutableStateOf(false)
    var errorEnvio by mutableStateOf<String?>(null)

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            estado = Estado.Cargando
            val r = Repositorio.cuestionario()
            if (r is Resp.Ok) {
                preguntas = aplanar(r.datos)
                elegidas.clear()
                paso = 0
                desdeRevision = false
            }
            estado = r.aEstado()
            val c = Repositorio.carreras()
            if (c is Resp.Ok) carreras = c.datos
        }
    }

    private fun aplanar(c: Cuestionario): List<PreguntaPlana> {
        val lista = mutableListOf<PreguntaPlana>()
        var numero = 1
        for (grupo in c.grupos) {
            for (pregunta in grupo.preguntas) {
                lista.add(PreguntaPlana(numero, pregunta, grupo.categoria, grupo.categoriaEtiqueta))
                numero++
            }
        }
        return lista
    }

    fun elegir(preguntaId: Int, opcionId: Int) {
        elegidas[preguntaId] = opcionId
        errorEnvio = null
    }

    fun siguiente() {
        if (desdeRevision) {
            desdeRevision = false
            paso = preguntas.size + 1
        } else {
            paso = (paso + 1).coerceAtMost(preguntas.size + 1)
        }
    }

    fun anterior() {
        desdeRevision = false
        if (paso > 0) paso -= 1
    }

    /** Desde la revisión: ir a una pregunta (o al inicio con 0) para cambiarla y luego volver. */
    fun cambiar(destino: Int) {
        desdeRevision = true
        paso = destino
    }

    fun enviar(alExito: (Int) -> Unit, alExpirar: () -> Unit) {
        val faltan = preguntas.filter { !elegidas.containsKey(it.pregunta.id) }
        if (faltan.isNotEmpty()) {
            errorEnvio = "Te faltan ${faltan.size} pregunta(s) por responder."
            cambiar(faltan.first().numero)
            return
        }
        viewModelScope.launch {
            enviando = true
            errorEnvio = null
            when (val r = Repositorio.calcular(carreraEnMente?.id, elegidas.values.toList())) {
                is Resp.Ok -> alExito(r.datos.id)
                is Resp.Err -> if (r.codigo == 401) alExpirar() else errorEnvio = r.mensaje
            }
            enviando = false
        }
    }
}

@Composable
fun CuestionarioPantalla(
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alTerminar: (resultadoId: Int) -> Unit,
    vm: CuestionarioViewModel = viewModel()
) {
    var confirmarSalida by remember { mutableStateOf(false) }
    var confirmarEnvio by remember { mutableStateOf(false) }
    var confirmarRespuesta by remember { mutableStateOf(false) }

    // Si ya contestó algo, preguntar antes de salir para no perder las respuestas.
    val intentarSalir: () -> Unit = {
        if (vm.elegidas.isEmpty()) alVolver() else confirmarSalida = true
    }

    // El botón "atrás" del celular retrocede una pregunta; en el primer paso intenta salir.
    BackHandler { if (vm.paso > 0) vm.anterior() else intentarSalir() }

    Pantalla(
        titulo = "Cuestionario",
        alVolver = intentarSalir,
        barraInferior = {
            if (vm.estado is Estado.Listo) {
                val total = vm.preguntas.size
                val actual = vm.preguntas.getOrNull(vm.paso - 1)
                val respondida = actual != null && vm.elegidas.containsKey(actual.pregunta.id)
                when {
                    vm.paso == 0 -> BarraPasos(
                        textoAtras = null,
                        alAtras = {},
                        textoAdelante = if (vm.desdeRevision) "Volver a la revisión" else "Empezar",
                        activoAdelante = true,
                        alAdelante = { vm.siguiente() },
                        error = null
                    )
                    vm.paso <= total -> BarraPasos(
                        textoAtras = "Anterior",
                        alAtras = { vm.anterior() },
                        textoAdelante = when {
                            vm.desdeRevision -> "Volver a la revisión"
                            vm.paso == total -> "Revisar respuestas"
                            else -> "Siguiente"
                        },
                        activoAdelante = respondida,
                        alAdelante = {
                            if (CONFIRMAR_CADA_RESPUESTA) confirmarRespuesta = true else vm.siguiente()
                        },
                        error = if (respondida) null else "Elige una opción para continuar."
                    )
                    else -> BarraPasos(
                        textoAtras = "Anterior",
                        alAtras = { vm.anterior() },
                        textoAdelante = if (vm.enviando) "Enviando…" else "Enviar respuestas",
                        activoAdelante = !vm.enviando,
                        alAdelante = { confirmarEnvio = true },
                        error = vm.errorEnvio
                    )
                }
            }
        }
    ) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(
                estado = vm.estado,
                alReintentar = { vm.cargar() },
                alSesionExpirada = alSesionExpirada
            ) { cuestionario ->
                val total = vm.preguntas.size
                when {
                    vm.paso == 0 -> PasoInicio(cuestionario, total, vm)
                    vm.paso <= total -> {
                        val p = vm.preguntas[vm.paso - 1]
                        PasoPregunta(p, total, vm.elegidas[p.pregunta.id]) { vm.elegir(p.pregunta.id, it) }
                    }
                    else -> PasoRevision(vm.preguntas, vm)
                }
            }
        }
    }

    // ------------------------------------------------------------ avisos de confirmación
    if (confirmarSalida) {
        DialogoConfirmar(
            titulo = "¿Salir del cuestionario?",
            mensaje = "Perderás las respuestas que llevas hasta ahora.",
            textoConfirmar = "Salir",
            alConfirmar = { confirmarSalida = false; alVolver() },
            alCancelar = { confirmarSalida = false }
        )
    }

    if (confirmarRespuesta) {
        val p = vm.preguntas.getOrNull(vm.paso - 1)
        if (p != null) {
            val elegida = p.pregunta.opciones.firstOrNull { it.id == vm.elegidas[p.pregunta.id] }
            AlertDialog(
                onDismissRequest = { confirmarRespuesta = false },
                title = { Text("¿Estás seguro de tu respuesta?") },
                text = { Text("Elegiste: ${elegida?.texto ?: ""}") },
                confirmButton = {
                    TextButton(onClick = { confirmarRespuesta = false; vm.siguiente() }) {
                        Text("Sí, continuar", fontWeight = FontWeight.Bold)
                    }
                },
                dismissButton = { TextButton(onClick = { confirmarRespuesta = false }) { Text("Cambiar") } }
            )
        }
    }

    if (confirmarEnvio) {
        AlertDialog(
            onDismissRequest = { confirmarEnvio = false },
            title = { Text("¿Enviar tus respuestas?") },
            text = {
                Text(
                    "Con tus ${vm.preguntas.size} respuestas se calcularán tus carreras más afines " +
                        "y el resultado quedará guardado en tu historial."
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    confirmarEnvio = false
                    vm.enviar(alTerminar, alSesionExpirada)
                }) { Text("Sí, enviar", fontWeight = FontWeight.Bold) }
            },
            dismissButton = { TextButton(onClick = { confirmarEnvio = false }) { Text("Revisar otra vez") } }
        )
    }
}

// ============================================================================ paso 0: inicio
@Composable
private fun PasoInicio(cuestionario: Cuestionario, total: Int, vm: CuestionarioViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Etiqueta("Antes de empezar")
        Text(cuestionario.titulo, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        cuestionario.descripcion?.let { Text(it, color = RumboColores.Suave) }

        Tarjeta {
            Text("¿Cómo funciona?", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
            Spacer(Modifier.height(6.dp))
            Text("•  Son $total preguntas, una a la vez.", color = RumboColores.Texto)
            Text("•  Elige una opción y pulsa Siguiente.", color = RumboColores.Texto)
            Text("•  Con Anterior puedes volver y cambiar una respuesta.", color = RumboColores.Texto)
            Text("•  Al final revisas todo antes de enviar.", color = RumboColores.Texto)
        }

        SelectorCarrera(vm)
    }
}

@Composable
private fun SelectorCarrera(vm: CuestionarioViewModel) {
    var abierto by remember { mutableStateOf(false) }
    Tarjeta(color = RumboColores.Tarjeta2) {
        Text("¿Ya tienes una carrera en mente?", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        Text(
            "Es opcional. Si eliges una, además de tus carreras afines verás qué ramas de esa carrera encajan mejor contigo.",
            color = RumboColores.Suave,
            fontSize = 14.sp
        )
        Spacer(Modifier.height(10.dp))
        Box {
            OutlinedButton(onClick = { abierto = true }, modifier = Modifier.fillMaxWidth()) {
                Text(vm.carreraEnMente?.nombre ?: "Ninguna por ahora")
            }
            DropdownMenu(expanded = abierto, onDismissRequest = { abierto = false }) {
                DropdownMenuItem(
                    text = { Text("Ninguna por ahora") },
                    onClick = { vm.carreraEnMente = null; abierto = false }
                )
                vm.carreras.forEach { carrera ->
                    DropdownMenuItem(
                        text = { Text(carrera.nombre) },
                        onClick = { vm.carreraEnMente = carrera; abierto = false }
                    )
                }
            }
        }
    }
}

// ============================================================================ pasos 1..N: una pregunta
@Composable
private fun PasoPregunta(
    p: PreguntaPlana,
    total: Int,
    elegida: Int?,
    alElegir: (Int) -> Unit
) {
    val color = colorCategoria(p.categoria)
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        BarraPuntaje(p.numero.toFloat() / total, Modifier.fillMaxWidth())

        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Box(Modifier.size(14.dp).clip(CircleShape).background(color))
            Text(
                p.categoriaEtiqueta.uppercase(),
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = RumboColores.Navy,
                letterSpacing = 1.sp
            )
            Spacer(Modifier.weight(1f))
            Text("Pregunta ${p.numero} de $total", color = RumboColores.Suave, fontWeight = FontWeight.SemiBold)
        }

        Text(
            p.pregunta.enunciado,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = RumboColores.Navy,
            lineHeight = 28.sp
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            p.pregunta.opciones.forEach { opcion ->
                OpcionFila(opcion.texto, elegida == opcion.id, color) { alElegir(opcion.id) }
            }
        }
    }
}

@Composable
private fun OpcionFila(texto: String, seleccionada: Boolean, color: Color, alClic: () -> Unit) {
    val forma = RoundedCornerShape(14.dp)
    Box(
        Modifier
            .fillMaxWidth()
            .clip(forma)
            .background(if (seleccionada) color else Color.White)
            .border(2.dp, if (seleccionada) color else RumboColores.Borde, forma)
            .selectable(selected = seleccionada, onClick = alClic, role = Role.RadioButton)
            .padding(horizontal = 16.dp, vertical = 16.dp)
    ) {
        Text(
            texto,
            fontSize = 16.sp,
            color = if (seleccionada) Color.White else RumboColores.Texto,
            fontWeight = if (seleccionada) FontWeight.SemiBold else FontWeight.Normal
        )
    }
}

// ============================================================================ paso N+1: revisión
@Composable
private fun PasoRevision(preguntas: List<PreguntaPlana>, vm: CuestionarioViewModel) {
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Etiqueta("Último paso")
        Text("Revisa tus respuestas", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        Text(
            "Toca Cambiar si quieres modificar alguna. Cuando estés listo, pulsa Enviar respuestas.",
            color = RumboColores.Suave
        )

        Tarjeta(color = RumboColores.Tarjeta2) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("Carrera en mente", color = RumboColores.Suave, fontSize = 13.sp)
                    Text(
                        vm.carreraEnMente?.nombre ?: "Ninguna por ahora",
                        fontWeight = FontWeight.Bold,
                        color = RumboColores.Navy
                    )
                }
                TextButton(onClick = { vm.cambiar(0) }) { Text("Cambiar") }
            }
        }

        preguntas.forEach { p ->
            val color = colorCategoria(p.categoria)
            val textoElegido = p.pregunta.opciones.firstOrNull { it.id == vm.elegidas[p.pregunta.id] }?.texto ?: "Sin responder"
            Tarjeta {
                Row(verticalAlignment = Alignment.Top) {
                    Box(
                        Modifier.size(28.dp).clip(CircleShape).background(color),
                        contentAlignment = Alignment.Center
                    ) { Text("${p.numero}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp) }
                    Spacer(Modifier.width(10.dp))
                    Column(Modifier.weight(1f)) {
                        Text(p.pregunta.enunciado, color = RumboColores.Suave, fontSize = 14.sp)
                        Spacer(Modifier.height(2.dp))
                        Text(textoElegido, fontWeight = FontWeight.Bold, color = RumboColores.Azul2)
                    }
                    TextButton(onClick = { vm.cambiar(p.numero) }) { Text("Cambiar") }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}

// ============================================================================ barra de botones
@Composable
private fun BarraPasos(
    textoAtras: String?,
    alAtras: () -> Unit,
    textoAdelante: String,
    activoAdelante: Boolean,
    alAdelante: () -> Unit,
    error: String?
) {
    Surface(shadowElevation = 8.dp, color = RumboColores.Fondo) {
        Column(Modifier.navigationBarsPadding().padding(horizontal = 16.dp, vertical = 10.dp)) {
            if (error != null) {
                Text(error, color = RumboColores.Rojo, fontWeight = FontWeight.Medium, fontSize = 14.sp)
                Spacer(Modifier.height(8.dp))
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                if (textoAtras != null) {
                    BotonSecundario(textoAtras, alAtras, Modifier.weight(1f))
                }
                BotonPrincipal(textoAdelante, alAdelante, activoAdelante, Modifier.weight(1f))
            }
        }
    }
}
