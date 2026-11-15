package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

class ResultadoViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<Resultado>>(Estado.Cargando)
    var orden by mutableStateOf("afinidad")
    private var id = 0
    private var esAdmin = false

    fun cargar(idResultado: Int, nuevoOrden: String = orden, admin: Boolean = esAdmin) {
        id = idResultado
        orden = nuevoOrden
        esAdmin = admin
        viewModelScope.launch {
            // Si ya hay datos en pantalla se mantienen mientras llegan los nuevos (al cambiar el orden).
            val r = if (esAdmin) Repositorio.resultadoAdmin(id, orden) else Repositorio.resultado(id, orden)
            estado = r.aEstado()
        }
    }

    fun reintentar() {
        estado = Estado.Cargando
        cargar(id)
    }
}

@Composable
fun ResultadoPantalla(
    id: Int,
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alVerCarrera: (Int) -> Unit,
    alRepetir: (() -> Unit)?,
    alVerHistorial: (() -> Unit)?,
    admin: Boolean = false,
    vm: ResultadoViewModel = viewModel()
) {
    LaunchedEffect(id) { vm.cargar(id, admin = admin) }

    Pantalla(titulo = if (admin) "Resultado del aspirante" else "Tu resultado", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(
                estado = vm.estado,
                alReintentar = { vm.reintentar() },
                alSesionExpirada = alSesionExpirada
            ) { resultado ->
                ContenidoResultado(
                    resultado = resultado,
                    orden = vm.orden,
                    alCambiarOrden = { vm.cargar(id, it) },
                    alVerCarrera = alVerCarrera,
                    alRepetir = alRepetir,
                    alVerHistorial = alVerHistorial
                )
            }
        }
    }
}

@Composable
private fun ContenidoResultado(
    resultado: Resultado,
    orden: String,
    alCambiarOrden: (String) -> Unit,
    alVerCarrera: (Int) -> Unit,
    alRepetir: (() -> Unit)?,
    alVerHistorial: (() -> Unit)?
) {
    LazyColumn(
        Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                "${resultado.usuarioNombre} · ${formatearFecha(resultado.fecha)}",
                color = RumboColores.Suave,
                fontSize = 13.sp
            )
        }

        val ganadora = resultado.ganadora
        if (ganadora == null) {
            item { MensajeError("Ninguna carrera obtuvo puntaje con tus respuestas. Intenta de nuevo eligiendo las opciones que más te representen.") }
        } else {
            item { TarjetaGanadora(ganadora, resultado.carreraDefinida, alVerCarrera) }
        }

        resultado.justificacion?.let { just ->
            item { TarjetaJustificacion(just) }
        }

        if (resultado.ramas.isNotEmpty()) {
            item {
                Column {
                    TituloSeccion("Ramas que mejor encajan contigo", Modifier.padding(top = 4.dp))
                    resultado.carreraDefinida?.let {
                        Text("Dentro de ${it.nombre}", color = RumboColores.Suave, fontSize = 14.sp)
                        Spacer(Modifier.height(8.dp))
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        resultado.ramas.forEach { TarjetaRama(it) }
                    }
                }
            }
        }

        if (resultado.carreras.isNotEmpty()) {
            item {
                Column {
                    TituloSeccion("Tus carreras más afines", Modifier.padding(top = 4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Pastilla("Por afinidad", orden == "afinidad") { alCambiarOrden("afinidad") }
                        Pastilla("Por riesgo de IA", orden == "riesgo") { alCambiarOrden("riesgo") }
                    }
                }
            }
            val maximo = (resultado.carreras.maxOfOrNull { it.puntajeTotal } ?: 1).coerceAtLeast(1)
            resultado.carreras.forEach { recomendada ->
                item(key = "carrera-${recomendada.carrera.id}") {
                    FilaRanking(recomendada, maximo, alVerCarrera)
                }
            }
        }

        item { TarjetaRespuestas(resultado.respuestas) }

        // Estos botones son solo del aspirante; el administrador no los ve.
        if (alRepetir != null && alVerHistorial != null) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)) {
                    BotonPrincipal("Hacer el cuestionario otra vez", alRepetir)
                    BotonSecundario("Ver todos mis resultados", alVerHistorial)
                }
            }
        }
    }
}

@Composable
private fun TarjetaGanadora(ganadora: CarreraRecomendada, definida: CarreraResumen?, alVerCarrera: (Int) -> Unit) {
    val carrera = ganadora.carrera
    Tarjeta(color = RumboColores.Navy, alClic = { alVerCarrera(carrera.id) }) {
        Etiqueta("Tu mejor opción", Color(0xFF9FD0FF))
        Spacer(Modifier.height(4.dp))
        Text(carrera.nombre, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Insignia(carrera.area, Color.White.copy(alpha = 0.16f), Color.White)
            Insignia("${ganadora.puntajeTotal} puntos", Color.White.copy(alpha = 0.16f), Color.White)
        }
        Spacer(Modifier.height(16.dp))
        Text("Riesgo frente a la IA", color = Color(0xFFD6E5F6), fontSize = 13.sp)
        Spacer(Modifier.height(6.dp))
        MedidorRiesgo(carrera.riesgoIa, grande = true, sobreOscuro = true)
        Spacer(Modifier.height(6.dp))
        Text(carrera.riesgoIaEtiqueta, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        if (definida != null) {
            Spacer(Modifier.height(12.dp))
            Text("Carrera que tenías en mente: ${definida.nombre}", color = Color(0xFFD6E5F6), fontSize = 13.sp)
        }
        Spacer(Modifier.height(10.dp))
        Text("Toca para ver el detalle", color = Color(0xFF9FD0FF), fontSize = 12.sp)
    }
}

@Composable
private fun TarjetaJustificacion(just: Justificacion) {
    Tarjeta {
        Text("¿Por qué esta carrera?", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        Spacer(Modifier.height(4.dp))
        Text(
            "${just.carrera.nombre} sumó ${just.puntaje} puntos a partir de tus respuestas. Lo que más pesó:",
            color = RumboColores.Suave
        )
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            just.aportes.forEach { aporte ->
                Row(
                    Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(RumboColores.Tarjeta2)
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        Modifier.clip(RoundedCornerShape(10.dp)).background(RumboColores.Azul2).padding(horizontal = 10.dp, vertical = 4.dp)
                    ) { Text("+${aporte.puntos}", color = Color.White, fontWeight = FontWeight.Bold) }
                    Column {
                        Text(aporte.texto, fontWeight = FontWeight.SemiBold, color = RumboColores.Texto)
                        Text(aporte.categoriaEtiqueta, color = colorCategoria(aporte.categoria), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun TarjetaRama(ramaPuntaje: RamaPuntaje) {
    Tarjeta {
        Row(horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(ramaPuntaje.rama.nombre, fontWeight = FontWeight.Bold, color = RumboColores.Navy, modifier = Modifier.weight(1f))
            Insignia("${ramaPuntaje.puntaje} pts", Color(0xFFDBEAFE), Color(0xFF1D4ED8))
        }
        ramaPuntaje.rama.descripcion?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = RumboColores.Suave, fontSize = 14.sp)
        }
    }
}

@Composable
private fun FilaRanking(recomendada: CarreraRecomendada, maximo: Int, alVerCarrera: (Int) -> Unit) {
    val carrera = recomendada.carrera
    Tarjeta(alClic = { alVerCarrera(carrera.id) }) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(
                Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(if (recomendada.posicion == 1) RumboColores.AmbarC else RumboColores.Navy),
                contentAlignment = Alignment.Center
            ) { Text("${recomendada.posicion}", color = Color.White, fontWeight = FontWeight.Bold) }

            Column(Modifier.weight(1f)) {
                Text(carrera.nombre, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = RumboColores.Navy)
                Spacer(Modifier.height(4.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    Insignia(carrera.area, Color(0xFFE6EEF7), RumboColores.Azul)
                    Text("${recomendada.puntajeTotal} pts", color = RumboColores.Suave, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                }
                Spacer(Modifier.height(8.dp))
                BarraPuntaje(recomendada.puntajeTotal.toFloat() / maximo, Modifier.fillMaxWidth())
                Spacer(Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    MedidorRiesgo(carrera.riesgoIa)
                    InsigniaRiesgo(carrera.riesgoIa, carrera.riesgoIaEtiqueta)
                }
            }
        }
    }
}

/** Las respuestas que dio el aspirante, agrupadas por categoría. Se despliega al tocar. */
@Composable
private fun TarjetaRespuestas(respuestas: List<RespuestaDada>) {
    var abierto by remember { mutableStateOf(false) }
    Tarjeta {
        Row(
            Modifier.fillMaxWidth().clickable { abierto = !abierto },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text("Mis respuestas", fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                Text("${respuestas.size} preguntas respondidas", color = RumboColores.Suave, fontSize = 13.sp)
            }
            Text(if (abierto) "Ocultar" else "Ver", color = RumboColores.Azul2, fontWeight = FontWeight.SemiBold)
        }
        if (abierto) {
            respuestas.groupBy { it.categoriaEtiqueta }.forEach { (etiqueta, lista) ->
                Spacer(Modifier.height(14.dp))
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(Modifier.size(10.dp).clip(CircleShape).background(colorCategoria(lista.first().categoria)))
                    Text(etiqueta.uppercase(), fontWeight = FontWeight.Bold, fontSize = 13.sp, color = RumboColores.Navy, letterSpacing = 1.sp)
                }
                lista.forEach { r ->
                    Spacer(Modifier.height(8.dp))
                    Text(r.pregunta, color = RumboColores.Suave, fontSize = 14.sp)
                    Text(r.opcion, fontWeight = FontWeight.SemiBold, color = RumboColores.Texto)
                }
            }
        }
    }
}
