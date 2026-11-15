package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

class CarrerasViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<List<CarreraResumen>>>(Estado.Cargando)
    var filtro by mutableStateOf<String?>(null)      // null = todas, o "bajo" / "medio" / "alto"

    fun cargar() {
        viewModelScope.launch {
            // Si ya hay una lista en pantalla se mantiene mientras llega la nueva.
            if (estado !is Estado.Listo) estado = Estado.Cargando
            estado = Repositorio.carreras(filtro).aEstado()
        }
    }

    fun filtrar(riesgo: String?) {
        filtro = riesgo
        cargar()
    }
}

/**
 * Catálogo de carreras. El administrador la reutiliza con otro título y un botón "Nueva carrera".
 */
@Composable
fun CarrerasPantalla(
    alVolver: () -> Unit,
    alAbrir: (Int) -> Unit,
    titulo: String = "Carreras",
    alNueva: (() -> Unit)? = null,
    vm: CarrerasViewModel = viewModel()
) {
    // Se recarga cada vez que se entra a la pantalla (por ejemplo, al volver de editar una carrera).
    LaunchedEffect(Unit) { vm.cargar() }

    Pantalla(
        titulo = titulo,
        alVolver = alVolver,
        barraInferior = {
            if (alNueva != null) BarraAccion("Nueva carrera", alNueva)
        }
    ) { interior ->
        Column(Modifier.padding(interior).fillMaxSize()) {
            Row(
                Modifier.padding(start = 16.dp, end = 16.dp, top = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Pastilla("Todas", vm.filtro == null) { vm.filtrar(null) }
                Pastilla("Riesgo bajo", vm.filtro == "bajo") { vm.filtrar("bajo") }
                Pastilla("Medio", vm.filtro == "medio") { vm.filtrar("medio") }
                Pastilla("Alto", vm.filtro == "alto") { vm.filtrar("alto") }
            }
            // El catálogo es público en la API: nunca responde 401, por eso no hay nada que hacer al expirar la sesión.
            Cargable(vm.estado, { vm.cargar() }, { }) { lista ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(lista, key = { it.id }) { carrera -> FilaCarrera(carrera, alAbrir) }
                }
            }
        }
    }
}

@Composable
private fun FilaCarrera(carrera: CarreraResumen, alAbrir: (Int) -> Unit) {
    Tarjeta(alClic = { alAbrir(carrera.id) }) {
        Text(carrera.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        Spacer(Modifier.height(6.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Insignia(carrera.area, Color(0xFFE6EEF7), RumboColores.Azul)
            MedidorRiesgo(carrera.riesgoIa)
            InsigniaRiesgo(carrera.riesgoIa, carrera.riesgoIaEtiqueta)
        }
    }
}
