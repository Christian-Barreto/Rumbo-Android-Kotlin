package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

class HistorialViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<List<ResultadoResumen>>>(Estado.Cargando)

    init { cargar() }

    fun cargar() {
        viewModelScope.launch {
            estado = Estado.Cargando
            estado = Repositorio.historial().aEstado()
        }
    }
}

@Composable
fun HistorialPantalla(
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alAbrir: (Int) -> Unit,
    alResponder: () -> Unit,
    vm: HistorialViewModel = viewModel()
) {
    Pantalla(titulo = "Mis resultados", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar() }, alSesionExpirada) { lista ->
                if (lista.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Todavía no tienes resultados", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                        Spacer(Modifier.height(6.dp))
                        Text("Responde el cuestionario para ver tus carreras más afines.", color = RumboColores.Suave)
                        Spacer(Modifier.height(18.dp))
                        BotonPrincipal("Responder el cuestionario", alResponder)
                    }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(lista, key = { it.id }) { r -> FilaHistorial(r, alAbrir) }
                    }
                }
            }
        }
    }
}

@Composable
fun FilaHistorial(r: ResultadoResumen, alAbrir: (Int) -> Unit) {
    Tarjeta(alClic = { alAbrir(r.id) }) {
        Text(formatearFecha(r.fecha), color = RumboColores.Suave, fontSize = 13.sp)
        Spacer(Modifier.height(4.dp))
        val ganadora = r.ganadora
        if (ganadora == null) {
            Text("Sin carreras con afinidad", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        } else {
            Text(ganadora.nombre, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Insignia(ganadora.area, androidx.compose.ui.graphics.Color(0xFFE6EEF7), RumboColores.Azul)
                Text("${r.puntajeGanadora} pts", color = RumboColores.Suave, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                MedidorRiesgo(ganadora.riesgoIa)
            }
        }
        r.carreraDefinida?.let {
            Spacer(Modifier.height(6.dp))
            Text("Tenías en mente: ${it.nombre}", color = RumboColores.Suave, fontSize = 13.sp)
        }
    }
}
