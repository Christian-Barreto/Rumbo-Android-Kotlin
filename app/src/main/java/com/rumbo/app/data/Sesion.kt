package com.rumbo.app.data

import android.content.Context
import android.content.SharedPreferences

/** Guarda en el teléfono quién inició sesión (token, rol y nombre). */
object Sesion {
    private var prefs: SharedPreferences? = null

    fun iniciar(contexto: Context) {
        prefs = contexto.getSharedPreferences("rumbo_sesion", Context.MODE_PRIVATE)
    }

    val token: String? get() = prefs?.getString("token", null)
    val rol: String? get() = prefs?.getString("rol", null)
    val nombre: String get() = prefs?.getString("nombre", "") ?: ""
    val estaActiva: Boolean get() = !token.isNullOrEmpty()

    fun guardar(respuesta: LoginResponse) {
        prefs?.edit()
            ?.putString("token", respuesta.token)
            ?.putString("rol", respuesta.rol)
            ?.putString("nombre", respuesta.nombre)
            ?.apply()
    }

    fun cerrar() {
        prefs?.edit()?.clear()?.apply()
    }
}
