package com.rumbo.app.ui.admin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
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

class CarreraFormViewModel : AdminViewModel() {
    var id by mutableStateOf(0)                       // 0 = carrera nueva
    var estadoCarga by mutableStateOf<Estado<Unit>>(Estado.Cargando)
    var nombre by mutableStateOf("")
    var area by mutableStateOf("")
    var riesgo by mutableStateOf("bajo")
    var nota by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var ramas by mutableStateOf<List<Rama>>(emptyList())
    var ramaNombre by mutableStateOf("")
    var ramaDescripcion by mutableStateOf("")
    var aviso by mutableStateOf<String?>(null)
    private var iniciado = false

    fun iniciar(idCarrera: Int) {
        if (iniciado) return
        iniciado = true
        id = idCarrera
        if (idCarrera == 0) {
            estadoCarga = Estado.Listo(Unit)
            return
        }
        viewModelScope.launch {
            when (val r = Repositorio.carrera(idCarrera)) {
                is Resp.Ok -> {
                    volcar(r.datos)
                    estadoCarga = Estado.Listo(Unit)
                }
                is Resp.Err -> {
                    iniciado = false
                    estadoCarga = Estado.Falla(r.mensaje, r.codigo)
                }
            }
        }
    }

    private fun volcar(c: CarreraDetalle) {
        id = c.id
        nombre = c.nombre
        area = c.area
        riesgo = c.riesgoIa
        nota = c.notaIa ?: ""
        descripcion = c.descripcion ?: ""
        ramas = c.ramas
    }

    fun guardar() {
        aviso = null
        if (nombre.isBlank() || area.isBlank()) {
            error = "El nombre y el área son obligatorios."
            return
        }
        val cuerpo = CarreraGuardar(
            nombre.trim(), area.trim(), riesgo,
            nota.trim().ifEmpty { null }, descripcion.trim().ifEmpty { null }
        )
        val eraNueva = id == 0
        ejecutar({
            if (id == 0) Repositorio.crearCarrera(cuerpo) else Repositorio.actualizarCarrera(id, cuerpo)
        }) { carrera ->
            volcar(carrera)
            aviso = if (eraNueva) "Carrera creada. Ya puedes agregarle ramas." else "Cambios guardados."
        }
    }

    fun eliminar(alExito: () -> Unit) {
        ejecutar({ Repositorio.eliminarCarrera(id) }) { alExito() }
    }

    fun agregarRama() {
        aviso = null
        if (ramaNombre.isBlank()) {
            error = "Escribe el nombre de la rama."
            return
        }
        val cuerpo = RamaGuardar(ramaNombre.trim(), ramaDescripcion.trim().ifEmpty { null })
        ejecutar({ Repositorio.agregarRama(id, cuerpo) }) { rama ->
            ramas = ramas + rama
            ramaNombre = ""
            ramaDescripcion = ""
        }
    }

    fun quitarRama(ramaId: Int) {
        aviso = null
        ejecutar({ Repositorio.eliminarRama(id, ramaId) }) {
            ramas = ramas.filter { it.id != ramaId }
        }
    }
}

@Composable
fun AdminCarreraFormPantalla(
    id: Int,
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    vm: CarreraFormViewModel = viewModel()
) {
    LaunchedEffect(id) { vm.iniciar(id) }
    AvisoSesion(vm.sesionExpirada, alSesionExpirada)
    var confirmarEliminar by remember { mutableStateOf(false) }

    Pantalla(
        titulo = if (vm.id == 0) "Nueva carrera" else "Editar carrera",
        alVolver = alVolver
    ) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estadoCarga, { vm.iniciar(id) }, alSesionExpirada) {
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    vm.error?.let { MensajeError(it) }
                    vm.aviso?.let { Aviso(it) }

                    Tarjeta {
                        CampoTexto(vm.nombre, { vm.nombre = it }, "Nombre de la carrera")
                        CampoTexto(vm.area, { vm.area = it }, "Área (p. ej. Tecnología, Salud)")
                        Spacer(Modifier.height(6.dp))
                        Text("Riesgo frente a la IA", fontWeight = FontWeight.SemiBold, color = RumboColores.Navy)
                        Spacer(Modifier.height(6.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Pastilla("Bajo", vm.riesgo == "bajo") { vm.riesgo = "bajo" }
                            Pastilla("Medio", vm.riesgo == "medio") { vm.riesgo = "medio" }
                            Pastilla("Alto", vm.riesgo == "alto") { vm.riesgo = "alto" }
                            MedidorRiesgo(vm.riesgo)
                        }
                        Spacer(Modifier.height(12.dp))
                        CampoTexto(vm.nota, { vm.nota = it }, "Nota sobre el riesgo de IA (opcional)", multilinea = true)
                        CampoTexto(vm.descripcion, { vm.descripcion = it }, "Descripción (opcional)", multilinea = true)
                        Spacer(Modifier.height(8.dp))
                        BotonPrincipal(
                            texto = if (vm.ocupado) "Guardando…" else if (vm.id == 0) "Crear carrera" else "Guardar cambios",
                            alClic = { vm.guardar() },
                            activo = !vm.ocupado
                        )
                    }

                    if (vm.id != 0) {
                        TituloSeccion("Ramas (${vm.ramas.size})", Modifier.padding(top = 0.dp))
                        if (vm.ramas.isEmpty()) {
                            Text("Esta carrera todavía no tiene ramas.", color = RumboColores.Suave)
                        }
                        vm.ramas.forEach { rama ->
                            Tarjeta {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Column(Modifier.weight(1f)) {
                                        Text(rama.nombre, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                                        rama.descripcion?.let {
                                            Text(it, color = RumboColores.Suave, fontSize = 14.sp)
                                        }
                                    }
                                    TextButton(onClick = { vm.quitarRama(rama.id) }, enabled = !vm.ocupado) {
                                        Text("Quitar", color = RumboColores.Rojo)
                                    }
                                }
                            }
                        }
                        Tarjeta(color = RumboColores.Tarjeta2) {
                            Text("Agregar una rama", fontWeight = FontWeight.Bold, color = RumboColores.Navy)
                            Spacer(Modifier.height(8.dp))
                            CampoTexto(vm.ramaNombre, { vm.ramaNombre = it }, "Nombre de la rama")
                            CampoTexto(vm.ramaDescripcion, { vm.ramaDescripcion = it }, "Descripción (opcional)", multilinea = true)
                            Spacer(Modifier.height(6.dp))
                            BotonSecundario("Agregar rama", { vm.agregarRama() })
                        }

                        Spacer(Modifier.height(8.dp))
                        BotonPeligro("Eliminar esta carrera", { confirmarEliminar = true })
                        Spacer(Modifier.height(16.dp))
                    }
                }
            }
        }
    }

    if (confirmarEliminar) {
        DialogoConfirmar(
            titulo = "¿Eliminar la carrera?",
            mensaje = "Se borrarán también sus ramas y sus puntajes. Si la carrera ya aparece en resultados guardados no se podrá eliminar.",
            textoConfirmar = "Eliminar",
            alConfirmar = {
                confirmarEliminar = false
                vm.eliminar(alVolver)
            },
            alCancelar = { confirmarEliminar = false }
        )
    }
}

/** Mensaje de éxito (verde). */
@Composable
fun Aviso(texto: String) {
    Surface(
        color = RumboColores.VerdeBg,
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            texto,
            color = RumboColores.Verde,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(12.dp)
        )
    }
}
