package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
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

class CarreraDetalleViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<CarreraDetalle>>(Estado.Cargando)
    private var id = 0

    fun cargar(idCarrera: Int) {
        id = idCarrera
        viewModelScope.launch {
            estado = Estado.Cargando
            estado = Repositorio.carrera(id).aEstado()
        }
    }

    fun reintentar() = cargar(id)
}

@Composable
fun CarreraDetallePantalla(
    id: Int,
    alVolver: () -> Unit,
    vm: CarreraDetalleViewModel = viewModel()
) {
    LaunchedEffect(id) { vm.cargar(id) }

    Pantalla(titulo = "Carrera", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.reintentar() }, { }) { carrera ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Tarjeta(color = RumboColores.Navy) {
                        Etiqueta("Carrera", Color(0xFF9FD0FF))
                        Spacer(Modifier.height(4.dp))
                        Text(carrera.nombre, color = Color.White, fontSize = 28.sp, fontWeight = FontWeight.Bold, lineHeight = 32.sp)
                        Spacer(Modifier.height(10.dp))
                        Insignia(carrera.area, Color.White.copy(alpha = 0.16f), Color.White)
                        Spacer(Modifier.height(16.dp))
                        Text("Riesgo frente a la IA", color = Color(0xFFD6E5F6), fontSize = 13.sp)
                        Spacer(Modifier.height(6.dp))
                        MedidorRiesgo(carrera.riesgoIa, grande = true, sobreOscuro = true)
                        Spacer(Modifier.height(6.dp))
                        Text(carrera.riesgoIaEtiqueta, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    }

                    carrera.notaIa?.let { nota ->
                        Tarjeta(color = RumboColores.Tarjeta2) {
                            Text("¿Por qué este nivel de riesgo?", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                            Spacer(Modifier.height(4.dp))
                            Text(nota, color = RumboColores.Texto)
                        }
                    }

                    carrera.descripcion?.let { descripcion ->
                        Tarjeta {
                            Text("Sobre la carrera", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                            Spacer(Modifier.height(4.dp))
                            Text(descripcion, color = RumboColores.Texto)
                        }
                    }

                    if (carrera.ramas.isNotEmpty()) {
                        TituloSeccion("Ramas y áreas de enfoque", Modifier.padding(top = 4.dp))
                        carrera.ramas.forEach { rama ->
                            Tarjeta {
                                Text(rama.nombre, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                                rama.descripcion?.let {
                                    Spacer(Modifier.height(4.dp))
                                    Text(it, color = RumboColores.Suave, fontSize = 14.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
