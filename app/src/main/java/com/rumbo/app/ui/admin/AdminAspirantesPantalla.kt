package com.rumbo.app.ui.admin

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
import com.rumbo.app.ui.pantallas.FilaHistorial
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

// ===================================================================== lista de aspirantes
class AspirantesViewModel : ViewModel() {
    var estado by mutableStateOf<Estado<List<AspiranteAdmin>>>(Estado.Cargando)

    fun cargar() {
        viewModelScope.launch {
            if (estado !is Estado.Listo) estado = Estado.Cargando
            estado = Repositorio.aspirantes().aEstado()
        }
    }
}

@Composable
fun AdminAspirantesPantalla(
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alAbrir: (Int) -> Unit,
    vm: AspirantesViewModel = viewModel()
) {
    // Se recarga al volver (por ejemplo, después de eliminar un aspirante).
    LaunchedEffect(Unit) { vm.cargar() }

    Pantalla(titulo = "Aspirantes", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar() }, alSesionExpirada) { lista ->
                if (lista.isEmpty()) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Todavía no hay aspirantes registrados", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                    }
                } else {
                    LazyColumn(
                        Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        item {
                            Text("${lista.size} aspirante(s) registrados", color = RumboColores.Suave, fontSize = 14.sp)
                        }
                        items(lista, key = { it.id }) { a ->
                            Tarjeta(alClic = { alAbrir(a.id) }) {
                                Text(a.nombre, fontSize = 17.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                                Text(a.correo, color = RumboColores.Suave, fontSize = 14.sp)
                                Spacer(Modifier.height(6.dp))
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                                    Text("${a.edad} años", color = RumboColores.Texto, fontSize = 13.sp)
                                    Insignia(
                                        if (a.totalResultados == 1) "1 resultado" else "${a.totalResultados} resultados",
                                        androidx.compose.ui.graphics.Color(0xFFDBEAFE),
                                        androidx.compose.ui.graphics.Color(0xFF1D4ED8)
                                    )
                                }
                                Spacer(Modifier.height(4.dp))
                                Text("Registrado: ${formatearFecha(a.fechaRegistro)}", color = RumboColores.Suave, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

// ===================================================================== un aspirante
data class DatosAspirante(val aspirante: AspiranteAdmin?, val resultados: List<ResultadoResumen>)

class AspiranteViewModel : AdminViewModel() {
    var estado by mutableStateOf<Estado<DatosAspirante>>(Estado.Cargando)
    private var id = 0

    fun cargar(idAspirante: Int) {
        id = idAspirante
        viewModelScope.launch {
            if (estado !is Estado.Listo) estado = Estado.Cargando
            val resultados = Repositorio.resultadosDeAspirante(id)
            estado = when (resultados) {
                is Resp.Ok -> {
                    val lista = Repositorio.aspirantes()
                    val aspirante = if (lista is Resp.Ok) lista.datos.firstOrNull { it.id == id } else null
                    Estado.Listo(DatosAspirante(aspirante, resultados.datos))
                }
                is Resp.Err -> Estado.Falla(resultados.mensaje, resultados.codigo)
            }
        }
    }

    fun eliminar(alExito: () -> Unit) {
        ejecutar({ Repositorio.eliminarAspirante(id) }) { alExito() }
    }
}

@Composable
fun AdminAspirantePantalla(
    id: Int,
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alAbrirResultado: (Int) -> Unit,
    vm: AspiranteViewModel = viewModel()
) {
    LaunchedEffect(id) { vm.cargar(id) }
    AvisoSesion(vm.sesionExpirada, alSesionExpirada)
    var confirmar by remember { mutableStateOf(false) }

    Pantalla(titulo = "Aspirante", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar(id) }, alSesionExpirada) { datos ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    vm.error?.let { mensaje -> item { MensajeError(mensaje) } }

                    datos.aspirante?.let { a ->
                        item {
                            Tarjeta(color = RumboColores.Navy) {
                                Etiqueta("Aspirante", androidx.compose.ui.graphics.Color(0xFF9FD0FF))
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    a.nombre,
                                    color = androidx.compose.ui.graphics.Color.White,
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(a.correo, color = androidx.compose.ui.graphics.Color(0xFFD6E5F6))
                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "${a.edad} años · registrado el ${formatearFecha(a.fechaRegistro)}",
                                    color = androidx.compose.ui.graphics.Color(0xFFD6E5F6),
                                    fontSize = 13.sp
                                )
                            }
                        }
                    }

                    item { TituloSeccion("Resultados (${datos.resultados.size})", Modifier.padding(top = 0.dp)) }
                    if (datos.resultados.isEmpty()) {
                        item { Text("Este aspirante todavía no ha respondido el cuestionario.", color = RumboColores.Suave) }
                    }
                    items(datos.resultados, key = { it.id }) { r -> FilaHistorial(r, alAbrirResultado) }

                    item {
                        Spacer(Modifier.height(8.dp))
                        BotonPeligro("Eliminar aspirante", { confirmar = true })
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (confirmar) {
        DialogoConfirmar(
            titulo = "¿Eliminar al aspirante?",
            mensaje = "Se eliminarán su cuenta y todos sus resultados y respuestas. Esta acción no se puede deshacer.",
            textoConfirmar = "Eliminar",
            alConfirmar = {
                confirmar = false
                vm.eliminar(alVolver)
            },
            alCancelar = { confirmar = false }
        )
    }
}
