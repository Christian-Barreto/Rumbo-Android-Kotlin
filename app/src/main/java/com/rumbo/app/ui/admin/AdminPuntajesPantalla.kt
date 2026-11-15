package com.rumbo.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rumbo.app.data.*
import com.rumbo.app.ui.componentes.*
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

private const val PUNTOS_MAX = 5

class PuntajesViewModel : AdminViewModel() {
    var estado by mutableStateOf<Estado<PuntajesOpcion>>(Estado.Cargando)
    val carreras = mutableStateMapOf<Int, Int>()      // carreraId -> puntos
    val ramas = mutableStateMapOf<Int, Int>()         // ramaId -> puntos
    var aviso by mutableStateOf<String?>(null)
    private var id = 0

    fun cargar(idOpcion: Int) {
        id = idOpcion
        viewModelScope.launch {
            estado = Estado.Cargando
            val r = Repositorio.puntajes(id)
            if (r is Resp.Ok) volcar(r.datos)
            estado = r.aEstado()
        }
    }

    private fun volcar(d: PuntajesOpcion) {
        carreras.clear()
        d.carreras.forEach { carreras[it.carreraId] = it.puntos }
        ramas.clear()
        d.ramas.forEach { ramas[it.ramaId] = it.puntos }
    }

    fun cambiarCarrera(carreraId: Int, delta: Int) {
        carreras[carreraId] = ((carreras[carreraId] ?: 0) + delta).coerceIn(0, PUNTOS_MAX)
        aviso = null
    }

    fun cambiarRama(ramaId: Int, delta: Int) {
        ramas[ramaId] = ((ramas[ramaId] ?: 0) + delta).coerceIn(0, PUNTOS_MAX)
        aviso = null
    }

    fun guardar() {
        aviso = null
        val cuerpo = PuntajesGuardar(
            carreras = carreras.map { PuntajeItem(it.key, it.value) },
            ramas = ramas.map { PuntajeItem(it.key, it.value) }
        )
        ejecutar({ Repositorio.guardarPuntajes(id, cuerpo) }) { datos ->
            volcar(datos)
            aviso = "Puntajes guardados."
        }
    }
}

@Composable
fun AdminPuntajesPantalla(
    opcionId: Int,
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    vm: PuntajesViewModel = viewModel()
) {
    LaunchedEffect(opcionId) { vm.cargar(opcionId) }
    AvisoSesion(vm.sesionExpirada, alSesionExpirada)

    Pantalla(
        titulo = "Puntajes de la opción",
        alVolver = alVolver,
        barraInferior = {
            if (vm.estado is Estado.Listo) {
                BarraAccion(if (vm.ocupado) "Guardando…" else "Guardar puntajes", { vm.guardar() }, activo = !vm.ocupado)
            }
        }
    ) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar(opcionId) }, alSesionExpirada) { datos ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    item {
                        Tarjeta(color = RumboColores.Tarjeta2) {
                            Etiqueta("Opción")
                            Spacer(Modifier.height(4.dp))
                            Text(datos.texto, fontWeight = FontWeight.Bold, fontSize = 17.sp, color = RumboColores.Navy)
                            Spacer(Modifier.height(6.dp))
                            Text(
                                "Cuántos puntos (0 a $PUNTOS_MAX) suma esta opción a cada carrera y rama cuando alguien la elige. Con 0 no aporta.",
                                color = RumboColores.Suave,
                                fontSize = 14.sp
                            )
                        }
                    }
                    item {
                        vm.error?.let { MensajeError(it) }
                        vm.aviso?.let { Aviso(it) }
                    }

                    item { TituloSeccion("Carreras", Modifier.padding(top = 4.dp)) }
                    items(datos.carreras, key = { "c${it.carreraId}" }) { fila ->
                        FilaPuntaje(
                            nombre = fila.nombre,
                            subtitulo = null,
                            puntos = vm.carreras[fila.carreraId] ?: 0,
                            alCambiar = { vm.cambiarCarrera(fila.carreraId, it) }
                        )
                    }

                    if (datos.ramas.isNotEmpty()) {
                        item { TituloSeccion("Ramas", Modifier.padding(top = 8.dp)) }
                        items(datos.ramas, key = { "r${it.ramaId}" }) { fila ->
                            FilaPuntaje(
                                nombre = fila.nombre,
                                subtitulo = fila.carreraNombre,
                                puntos = vm.ramas[fila.ramaId] ?: 0,
                                alCambiar = { vm.cambiarRama(fila.ramaId, it) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilaPuntaje(nombre: String, subtitulo: String?, puntos: Int, alCambiar: (Int) -> Unit) {
    Tarjeta(color = if (puntos > 0) RumboColores.Tarjeta2 else RumboColores.Superficie) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(nombre, fontWeight = FontWeight.SemiBold, color = RumboColores.Navy)
                if (subtitulo != null) Text(subtitulo, color = RumboColores.Suave, fontSize = 12.sp)
            }
            BotonPaso("−", puntos > 0) { alCambiar(-1) }
            Text(
                "$puntos",
                modifier = Modifier.width(34.dp),
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = if (puntos > 0) RumboColores.Azul2 else RumboColores.Suave
            )
            BotonPaso("+", puntos < PUNTOS_MAX) { alCambiar(1) }
        }
    }
}

@Composable
private fun BotonPaso(texto: String, activo: Boolean, alClic: () -> Unit) {
    Box(
        Modifier
            .size(38.dp)
            .clip(CircleShape)
            .background(if (activo) RumboColores.Azul2 else RumboColores.Borde)
            .clickable(enabled = activo, onClick = alClic),
        contentAlignment = Alignment.Center
    ) { Text(texto, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold) }
}
