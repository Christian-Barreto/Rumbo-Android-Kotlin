package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.rumbo.app.data.Repositorio
import com.rumbo.app.data.Resp
import com.rumbo.app.data.Sesion
import com.rumbo.app.ui.componentes.BotonPrincipal
import com.rumbo.app.ui.componentes.CampoTexto
import com.rumbo.app.ui.componentes.MensajeError
import com.rumbo.app.ui.componentes.Pantalla
import com.rumbo.app.ui.componentes.Tarjeta
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

class RegistroViewModel : ViewModel() {
    var nombre by mutableStateOf("")
    var edad by mutableStateOf("")
    var correo by mutableStateOf("")
    var clave by mutableStateOf("")
    var confirmar by mutableStateOf("")
    var cargando by mutableStateOf(false)
    var errorGeneral by mutableStateOf<String?>(null)
    var errores by mutableStateOf<Map<String, String>>(emptyMap())

    fun registrar(alExito: () -> Unit) {
        val locales = mutableMapOf<String, String>()
        val edadNumero = edad.trim().toIntOrNull()
        if (nombre.isBlank()) locales["nombre"] = "Escribe tu nombre."
        if (edadNumero == null) locales["edad"] = "Escribe tu edad (solo números)."
        if (correo.isBlank()) locales["correo"] = "Escribe tu correo."
        if (clave.length < 6) locales["clave"] = "La contraseña debe tener al menos 6 caracteres."
        if (confirmar != clave) locales["confirmar"] = "Las contraseñas no coinciden."
        errores = locales
        errorGeneral = null
        if (locales.isNotEmpty() || edadNumero == null) return

        viewModelScope.launch {
            cargando = true
            when (val r = Repositorio.registro(nombre.trim(), edadNumero, correo.trim(), clave)) {
                is Resp.Ok -> {
                    Sesion.guardar(r.datos)
                    alExito()
                }
                is Resp.Err -> {
                    errores = r.errores ?: emptyMap()
                    errorGeneral = r.mensaje
                }
            }
            cargando = false
        }
    }
}

@Composable
fun RegistroPantalla(
    alVolver: () -> Unit,
    alRegistrar: () -> Unit,
    vm: RegistroViewModel = viewModel()
) {
    Pantalla(titulo = "Crear cuenta", alVolver = alVolver) { interior ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(interior)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            Text("Cuéntanos de ti", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
            Text(
                "Con tu cuenta podrás guardar tus resultados y volver a revisarlos cuando quieras.",
                color = RumboColores.Suave
            )
            Spacer(Modifier.height(16.dp))

            Tarjeta {
                CampoTexto(vm.nombre, { vm.nombre = it }, "Nombre", vm.errores["nombre"])
                CampoTexto(vm.edad, { vm.edad = it }, "Edad", vm.errores["edad"], teclado = KeyboardType.Number)
                CampoTexto(vm.correo, { vm.correo = it }, "Correo electrónico", vm.errores["correo"], teclado = KeyboardType.Email)
                CampoTexto(vm.clave, { vm.clave = it }, "Contraseña (mínimo 6 caracteres)", vm.errores["clave"], oculto = true)
                CampoTexto(vm.confirmar, { vm.confirmar = it }, "Repite la contraseña", vm.errores["confirmar"], oculto = true)
                Spacer(Modifier.height(8.dp))
                vm.errorGeneral?.let {
                    MensajeError(it)
                    Spacer(Modifier.height(10.dp))
                }
                BotonPrincipal(
                    texto = if (vm.cargando) "Creando cuenta…" else "Crear cuenta y continuar",
                    alClic = { vm.registrar(alRegistrar) },
                    activo = !vm.cargando
                )
            }
        }
    }
}
