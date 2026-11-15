package com.rumbo.app.ui.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rumbo.app.data.*
import com.rumbo.app.ui.componentes.*
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

class CuestionarioDetalleViewModel : AdminViewModel() {
    var estado by mutableStateOf<Estado<CuestionarioAdmin>>(Estado.Cargando)
    var titulo by mutableStateOf("")
    var descripcion by mutableStateOf("")
    var activo by mutableStateOf(false)
    var aviso by mutableStateOf<String?>(null)
    private var id = 0
    private var formCargado = false

    fun cargar(idCuestionario: Int) {
        id = idCuestionario
        viewModelScope.launch {
            if (estado !is Estado.Listo) estado = Estado.Cargando
            val r = Repositorio.cuestionarioAdmin(id)
            // Los campos del encabezado se llenan solo la primera vez, para no pisar lo que se esté escribiendo.
            if (r is Resp.Ok && !formCargado) {
                titulo = r.datos.titulo
                descripcion = r.datos.descripcion ?: ""
                activo = r.datos.activo
                formCargado = true
            }
            estado = r.aEstado()
        }
    }

    fun guardarCabecera() {
        aviso = null
        if (titulo.isBlank()) {
            error = "El título es obligatorio."
            return
        }
        val cuerpo = CuestionarioGuardar(titulo.trim(), descripcion.trim().ifEmpty { null }, activo)
        ejecutar({ Repositorio.actualizarCuestionario(id, cuerpo) }) {
            aviso = "Cambios guardados."
            cargar(id)
        }
    }

    fun eliminarCuestionario(alExito: () -> Unit) {
        ejecutar({ Repositorio.eliminarCuestionario(id) }) { alExito() }
    }

    fun guardarPregunta(preguntaId: Int?, enunciado: String, categoria: String, orden: Int?, alExito: () -> Unit) {
        if (enunciado.isBlank()) {
            error = "El enunciado es obligatorio."
            return
        }
        val cuerpo = PreguntaGuardar(enunciado.trim(), categoria, orden)
        ejecutar({
            if (preguntaId == null) Repositorio.agregarPregunta(id, cuerpo)
            else Repositorio.actualizarPregunta(preguntaId, cuerpo)
        }) {
            cargar(id)
            alExito()
        }
    }

    fun eliminarPregunta(preguntaId: Int) {
        aviso = null
        ejecutar({ Repositorio.eliminarPregunta(preguntaId) }) { cargar(id) }
    }

    fun agregarOpcion(preguntaId: Int, texto: String, alExito: () -> Unit) {
        if (texto.isBlank()) {
            error = "El texto de la opción es obligatorio."
            return
        }
        ejecutar({ Repositorio.agregarOpcion(preguntaId, texto.trim()) }) {
            cargar(id)
            alExito()
        }
    }

    fun actualizarOpcion(opcionId: Int, texto: String, alExito: () -> Unit) {
        if (texto.isBlank()) {
            error = "El texto de la opción es obligatorio."
            return
        }
        ejecutar({ Repositorio.actualizarOpcion(opcionId, texto.trim()) }) {
            cargar(id)
            alExito()
        }
    }

    fun eliminarOpcion(opcionId: Int) {
        aviso = null
        ejecutar({ Repositorio.eliminarOpcion(opcionId) }) { cargar(id) }
    }
}

private sealed interface Dialogo {
    object NuevaPregunta : Dialogo
    data class EditarPregunta(val pregunta: PreguntaAdmin) : Dialogo
    data class EliminarPregunta(val pregunta: PreguntaAdmin) : Dialogo
    data class NuevaOpcion(val preguntaId: Int) : Dialogo
    data class EditarOpcion(val opcion: Opcion) : Dialogo
    data class EliminarOpcion(val opcion: Opcion) : Dialogo
    object EliminarCuestionario : Dialogo
}

@Composable
fun AdminCuestionarioDetallePantalla(
    id: Int,
    alVolver: () -> Unit,
    alSesionExpirada: () -> Unit,
    alPuntajes: (opcionId: Int) -> Unit,
    vm: CuestionarioDetalleViewModel = viewModel()
) {
    // Al volver de la pantalla de puntajes se recarga la lista.
    LaunchedEffect(id) { vm.cargar(id) }
    AvisoSesion(vm.sesionExpirada, alSesionExpirada)
    var dialogo by remember { mutableStateOf<Dialogo?>(null) }
    fun abrir(d: Dialogo) { vm.error = null; dialogo = d }

    Pantalla(titulo = "Cuestionario", alVolver = alVolver) { interior ->
        Box(Modifier.padding(interior)) {
            Cargable(vm.estado, { vm.cargar(id) }, alSesionExpirada) { cuestionario ->
                Column(
                    Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    if (dialogo == null) vm.error?.let { MensajeError(it) }
                    vm.aviso?.let { Aviso(it) }

                    Tarjeta {
                        CampoTexto(vm.titulo, { vm.titulo = it }, "Título")
                        CampoTexto(vm.descripcion, { vm.descripcion = it }, "Descripción (opcional)", multilinea = true)
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Switch(checked = vm.activo, onCheckedChange = { vm.activo = it })
                            Spacer(Modifier.width(10.dp))
                            Column {
                                Text("Cuestionario activo", fontWeight = FontWeight.SemiBold, color = RumboColores.Navy)
                                Text("Al activarlo se desactivan los demás.", color = RumboColores.Suave, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        BotonPrincipal(
                            if (vm.ocupado) "Guardando…" else "Guardar cambios",
                            { vm.guardarCabecera() },
                            activo = !vm.ocupado
                        )
                    }

                    TituloSeccion("Preguntas (${cuestionario.preguntas.size})", Modifier.padding(top = 0.dp))
                    BotonSecundario("Agregar pregunta", { abrir(Dialogo.NuevaPregunta) })

                    cuestionario.preguntas.forEach { pregunta ->
                        TarjetaPreguntaAdmin(
                            pregunta = pregunta,
                            alEditar = { abrir(Dialogo.EditarPregunta(pregunta)) },
                            alEliminar = { abrir(Dialogo.EliminarPregunta(pregunta)) },
                            alNuevaOpcion = { abrir(Dialogo.NuevaOpcion(pregunta.id)) },
                            alPuntajes = alPuntajes,
                            alEditarOpcion = { abrir(Dialogo.EditarOpcion(it)) },
                            alEliminarOpcion = { abrir(Dialogo.EliminarOpcion(it)) }
                        )
                    }

                    Spacer(Modifier.height(8.dp))
                    BotonPeligro("Eliminar este cuestionario", { abrir(Dialogo.EliminarCuestionario) })
                    Spacer(Modifier.height(16.dp))
                }
            }
        }
    }

    // ------------------------------------------------------------ diálogos
    when (val d = dialogo) {
        null -> {}
        is Dialogo.NuevaPregunta -> DialogoPregunta(
            inicial = null,
            error = vm.error,
            alGuardar = { enunciado, categoria, orden ->
                vm.guardarPregunta(null, enunciado, categoria, orden) { dialogo = null }
            },
            alCancelar = { dialogo = null }
        )
        is Dialogo.EditarPregunta -> DialogoPregunta(
            inicial = d.pregunta,
            error = vm.error,
            alGuardar = { enunciado, categoria, orden ->
                vm.guardarPregunta(d.pregunta.id, enunciado, categoria, orden) { dialogo = null }
            },
            alCancelar = { dialogo = null }
        )
        is Dialogo.EliminarPregunta -> DialogoConfirmar(
            titulo = "¿Eliminar la pregunta?",
            mensaje = "Se borrarán también sus opciones y puntajes. Si ya fue respondida en resultados guardados no se podrá eliminar.",
            textoConfirmar = "Eliminar",
            alConfirmar = { vm.eliminarPregunta(d.pregunta.id); dialogo = null },
            alCancelar = { dialogo = null }
        )
        is Dialogo.NuevaOpcion -> DialogoTexto(
            titulo = "Nueva opción",
            etiqueta = "Texto de la opción",
            inicial = "",
            error = vm.error,
            alGuardar = { texto -> vm.agregarOpcion(d.preguntaId, texto) { dialogo = null } },
            alCancelar = { dialogo = null }
        )
        is Dialogo.EditarOpcion -> DialogoTexto(
            titulo = "Editar opción",
            etiqueta = "Texto de la opción",
            inicial = d.opcion.texto,
            error = vm.error,
            alGuardar = { texto -> vm.actualizarOpcion(d.opcion.id, texto) { dialogo = null } },
            alCancelar = { dialogo = null }
        )
        is Dialogo.EliminarOpcion -> DialogoConfirmar(
            titulo = "¿Quitar la opción?",
            mensaje = "Se borrarán sus puntajes. Si ya fue elegida en resultados guardados no se podrá eliminar.",
            textoConfirmar = "Quitar",
            alConfirmar = { vm.eliminarOpcion(d.opcion.id); dialogo = null },
            alCancelar = { dialogo = null }
        )
        is Dialogo.EliminarCuestionario -> DialogoConfirmar(
            titulo = "¿Eliminar el cuestionario?",
            mensaje = "Se borrarán sus preguntas, opciones y puntajes. No se puede eliminar si ya tiene resultados guardados ni si es el único activo.",
            textoConfirmar = "Eliminar",
            alConfirmar = { dialogo = null; vm.eliminarCuestionario(alVolver) },
            alCancelar = { dialogo = null }
        )
    }
}

@Composable
private fun TarjetaPreguntaAdmin(
    pregunta: PreguntaAdmin,
    alEditar: () -> Unit,
    alEliminar: () -> Unit,
    alNuevaOpcion: () -> Unit,
    alPuntajes: (Int) -> Unit,
    alEditarOpcion: (Opcion) -> Unit,
    alEliminarOpcion: (Opcion) -> Unit
) {
    val color = colorCategoria(pregunta.categoria)
    Tarjeta {
        Row(verticalAlignment = Alignment.Top) {
            Box(
                Modifier.size(30.dp).clip(CircleShape).background(color),
                contentAlignment = Alignment.Center
            ) { Text("${pregunta.orden}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp) }
            Spacer(Modifier.width(10.dp))
            Column {
                Text(pregunta.enunciado, fontWeight = FontWeight.Bold, color = RumboColores.Texto)
                Text(pregunta.categoriaEtiqueta, color = color, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
        Row {
            TextButton(onClick = alEditar) { Text("Editar") }
            TextButton(onClick = alEliminar) { Text("Eliminar", color = RumboColores.Rojo) }
        }

        pregunta.opciones.forEach { opcion ->
            Column(
                Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp)
                    .clip(androidx.compose.foundation.shape.RoundedCornerShape(12.dp))
                    .background(RumboColores.Tarjeta2)
                    .padding(start = 12.dp, end = 4.dp, top = 8.dp, bottom = 2.dp)
            ) {
                Text(opcion.texto, color = RumboColores.Texto)
                Row {
                    TextButton(onClick = { alPuntajes(opcion.id) }) { Text("Puntajes") }
                    TextButton(onClick = { alEditarOpcion(opcion) }) { Text("Editar") }
                    TextButton(onClick = { alEliminarOpcion(opcion) }) { Text("Quitar", color = RumboColores.Rojo) }
                }
            }
        }
        TextButton(onClick = alNuevaOpcion) { Text("+ Agregar opción") }
    }
}

@Composable
private fun DialogoPregunta(
    inicial: PreguntaAdmin?,
    error: String?,
    alGuardar: (String, String, Int?) -> Unit,
    alCancelar: () -> Unit
) {
    var enunciado by remember { mutableStateOf(inicial?.enunciado ?: "") }
    var categoria by remember { mutableStateOf(inicial?.categoria ?: "hobbies") }
    var orden by remember { mutableStateOf(inicial?.orden?.toString() ?: "") }

    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(if (inicial == null) "Nueva pregunta" else "Editar pregunta") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState())) {
                CampoTexto(enunciado, { enunciado = it }, "Enunciado", multilinea = true)
                Spacer(Modifier.height(4.dp))
                Text("Categoría", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pastilla("Hobbies", categoria == "hobbies") { categoria = "hobbies" }
                    Pastilla("Fortalezas", categoria == "fortalezas") { categoria = "fortalezas" }
                }
                Spacer(Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Pastilla("Intereses", categoria == "intereses") { categoria = "intereses" }
                    Pastilla("Estilo de trabajo", categoria == "estilo_trabajo") { categoria = "estilo_trabajo" }
                }
                Spacer(Modifier.height(10.dp))
                CampoTexto(
                    orden,
                    { orden = it.filter { c -> c.isDigit() } },
                    "Orden (1 a 999; vacío = al final)",
                    teclado = KeyboardType.Number
                )
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    MensajeError(error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { alGuardar(enunciado, categoria, orden.toIntOrNull()) }) { Text("Guardar") }
        },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}

@Composable
private fun DialogoTexto(
    titulo: String,
    etiqueta: String,
    inicial: String,
    error: String?,
    alGuardar: (String) -> Unit,
    alCancelar: () -> Unit
) {
    var texto by remember { mutableStateOf(inicial) }
    AlertDialog(
        onDismissRequest = alCancelar,
        title = { Text(titulo) },
        text = {
            Column {
                CampoTexto(texto, { texto = it }, etiqueta, multilinea = true)
                if (error != null) {
                    Spacer(Modifier.height(6.dp))
                    MensajeError(error)
                }
            }
        },
        confirmButton = { TextButton(onClick = { alGuardar(texto) }) { Text("Guardar") } },
        dismissButton = { TextButton(onClick = alCancelar) { Text("Cancelar") } }
    )
}
