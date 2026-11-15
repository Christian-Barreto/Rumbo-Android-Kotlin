package com.rumbo.app.ui.admin

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rumbo.app.data.Resp
import kotlinx.coroutines.launch

/**
 * Base de los ViewModel del administrador: ejecuta una operación contra la API,
 * marca "ocupado", guarda el mensaje de error si falla y avisa si la sesión expiró.
 */
open class AdminViewModel : ViewModel() {
    var error by mutableStateOf<String?>(null)
    var ocupado by mutableStateOf(false)
    var sesionExpirada by mutableStateOf(false)

    protected fun <T> ejecutar(
        bloque: suspend () -> Resp<T>,
        alExito: suspend (T) -> Unit = {}
    ) {
        viewModelScope.launch {
            ocupado = true
            error = null
            when (val r = bloque()) {
                is Resp.Ok -> alExito(r.datos)
                is Resp.Err -> if (r.codigo == 401) sesionExpirada = true else error = r.mensaje
            }
            ocupado = false
        }
    }
}

/** Si la sesión expiró (el token venció), regresa al login. */
@Composable
fun AvisoSesion(expirada: Boolean, alExpirar: () -> Unit) {
    LaunchedEffect(expirada) { if (expirada) alExpirar() }
}
