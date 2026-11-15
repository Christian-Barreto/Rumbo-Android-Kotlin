package com.rumbo.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rumbo.app.data.*
import com.rumbo.app.ui.componentes.*
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

class CuestionariosViewModel : AdminViewModel() {
    var estado by mutableStateOf<Estado<List<CuestionarioAdminResumen>>>(Estado.Cargando)

    fun cargar() {
        viewModelScope.launch {
            if (estado !is Estado.Listo) estado = Estado.Cargando
            estado = Repositorio.cuestionariosAdmin().aEstado()
        }
    }

    fun crear(titulo: String, descripcion: String, activo: Boolean, alExito: (Int) -> Unit) {
        if (titulo.isBlank()) {
            error = "El título es obligatorio."
            return
        }
        val cuerpo = CuestionarioGuardar(titulo.trim(), descripcion.trim().ifEmpty { null }, activo)
        ejecutar({ Repositorio.crearCuestionario(cuerpo) }) { alExito(it.id) }
    }
}

@Composable
fun AdminCuestionariosPantalla(
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alAbrir: (Int) -> Unit,
    vm: CuestionariosViewModel = viewModel()
) {
    // Se recarga cada vez que se vuelve a esta pantalla (después de editar un cuestionario).
    LaunchedEffect(Unit) { vm.cargar() }
    AvisoSesion(vm.sesionExpirada, alSesionExpirada)
    var dialogo by remember { mutableStateOf(false) }

    Pantalla(
        titulo = "Cuestionarios",
        alVolver = alVolver,
        barraInferior = {
            BarraAccion("Nuevo cuestionario", { vm.error = null; dialogo = true })
        }
    ) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar() }, alSesionExpirada) { lista ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Text(
                            "Solo un cuestionario está activo a la vez: es el que responden los aspirantes.",
                            color = RumboColores.Suave,
                            fontSize = 14.sp
                        )
                    }
                    items(lista, key = { it.id }) { c ->
                        Tarjeta(alClic = { alAbrir(c.id) }) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    c.titulo,
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = RumboColores.Navy,
                                    modifier = Modifier.weight(1f)
                                )
                                if (c.activo) Insignia("Activo", RumboColores.VerdeBg, RumboColores.Verde)
                                else Insignia("Inactivo", androidx.compose.ui.graphics.Color(0xFFE6EEF7), RumboColores.Suave)
                            }
                            c.descripcion?.let {
                                Spacer(Modifier.height(4.dp))
                                Text(it, color = RumboColores.Suave, fontSize = 14.sp)
                            }
                            Spacer(Modifier.height(6.dp))
                            Text("${c.totalPreguntas} preguntas", color = RumboColores.Azul, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                        }
                    }
                }
            }
        }
    }

    if (dialogo) {
        DialogoNuevoCuestionario(
            error = vm.error,
            alCrear = { titulo, descripcion, activo ->
                vm.crear(titulo, descripcion, activo) { id ->
                    dialogo = false
                    alAbrir(id)
                }
            },
            alCancelar = { dialogo = false }
        )
    }
}

@Composable
private fun DialogoNuevoCuestionario(
    error: String?,
    alCrear: (String, String, Boolean) -> Unit,
    alCancelar: () -> Unit
) {
    var titulo by remember { mutableStateOf("") }
    var descripcion by remember { mutableStateOf("") }
    var activo by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text("Nuevo cuestionario") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoTexto(titulo, { titulo = it }, "Título")
                CampoTexto(descripcion, { descripcion = it }, "Descripción (opcional)", multilinea = true)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Switch(checked = activo, onCheckedChange = { activo = it })
                    Spacer(Modifier.width(10.dp))
                    Text("Activarlo al crearlo")
                }
                if (error != null) {
                    Spacer(Modifier.height(8.dp))
                    MensajeError(error)
                }
            }
        },
        confirmButton = { TextButton(onClick = { alCrear(titulo, descripcion, activo) }) { Text("Crear") } },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
