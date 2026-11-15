package com.rumbo.app.data

/** Resultado de una llamada a la API. */
sealed class Resp<out T> {
    data class Ok<out T>(val datos: T) : Resp<T>()
    data class Err(
        val mensaje: String,
        val codigo: Int = 0,
        val errores: Map<String, String>? = null,
        val preguntasFaltantes: List<Int>? = null
    ) : Resp<Nothing>()
}

/** Estado de una pantalla que carga datos. */
sealed interface Estado<out T> {
    object Cargando : Estado<Nothing>
    data class Listo<out T>(val datos: T) : Estado<T>
    data class Falla(val mensaje: String, val codigo: Int = 0) : Estado<Nothing>
}

fun <T> Resp<T>.aEstado(): Estado<T> = when (this) {
    is Resp.Ok -> Estado.Listo(datos)
    is Resp.Err -> Estado.Falla(mensaje, codigo)
}
