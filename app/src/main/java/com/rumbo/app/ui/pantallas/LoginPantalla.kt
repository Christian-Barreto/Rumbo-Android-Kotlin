package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
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
import com.rumbo.app.ui.componentes.Tarjeta
import com.rumbo.app.ui.tema.RumboColores
import kotlinx.coroutines.launch

class LoginViewModel : ViewModel() {
    var usuario by mutableStateOf("")
    var clave by mutableStateOf("")
    var cargando by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)

    fun entrar(alExito: (String) -> Unit) {
        if (usuario.isBlank() || clave.isEmpty()) {
            error = "Escribe tu correo (o usuario) y tu contraseña."
            return
        }
        viewModelScope.launch {
            cargando = true
            error = null
            when (val r = Repositorio.login(usuario.trim(), clave)) {
                is Resp.Ok -> {
                    Sesion.guardar(r.datos)
                    alExito(r.datos.rol)
                }
                is Resp.Err -> error = r.mensaje
            }
            cargando = false
        }
    }
}

@Composable
fun LoginPantalla(
    alRegistrar: () -> Unit,
    alEntrar: (rol: String) -> Unit,
    vm: LoginViewModel = viewModel()
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(RumboColores.Fondo)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("Rumbo", fontSize = 40.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
        Text(
            "Orientación vocacional en la era de la IA",
            color = RumboColores.Suave,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(28.dp))

        Tarjeta {
            Text("Iniciar sesión", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = RumboColores.Navy)
            Spacer(Modifier.height(14.dp))
            CampoTexto(vm.usuario, { vm.usuario = it }, "Correo (aspirante) o usuario (administrador)", teclado = KeyboardType.Email)
            CampoTexto(vm.clave, { vm.clave = it }, "Contraseña", oculto = true)
            Spacer(Modifier.height(8.dp))
            vm.error?.let {
                MensajeError(it)
                Spacer(Modifier.height(10.dp))
            }
            BotonPrincipal(
                texto = if (vm.cargando) "Entrando…" else "Entrar",
                alClic = { vm.entrar(alEntrar) },
                activo = !vm.cargando
            )
        }

        Spacer(Modifier.height(12.dp))
        Text("¿Aún no tienes cuenta?", color = RumboColores.Suave)
        TextButton(onClick = alRegistrar) { Text("Regístrate como aspirante", fontWeight = FontWeight.SemiBold) }
    }
}
