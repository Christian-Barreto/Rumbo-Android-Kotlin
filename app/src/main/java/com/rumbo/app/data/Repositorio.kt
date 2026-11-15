package com.rumbo.app.data

import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.IOException

/** Llama a la API y convierte la respuesta (o el error) en un Resp. */
object Repositorio {
    private val api get() = ApiClient.api
    private val gson = Gson()

    private suspend fun <T> llamar(bloque: suspend () -> Response<T>): Resp<T> =
        withContext(Dispatchers.IO) {
            try {
                val r = bloque()
                if (r.isSuccessful) {
                    val cuerpo = r.body()
                    if (cuerpo != null) Resp.Ok(cuerpo)
                    else Resp.Err("El servidor devolvió una respuesta vacía.", r.code())
                } else {
                    errorDe(r)
                }
            } catch (e: IOException) {
                Resp.Err("No se pudo conectar (${e.javaClass.simpleName}: ${e.message})")
            } catch (e: Exception) {
                Resp.Err("Ocurrió un error inesperado: ${e.message}")
            }
        }

    /** Para las llamadas que responden 204 (sin cuerpo), como los DELETE. */
    private suspend fun llamarSinCuerpo(bloque: suspend () -> Response<Unit>): Resp<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val r = bloque()
                if (r.isSuccessful) Resp.Ok(Unit) else errorDe(r)
            } catch (e: IOException) {
                Resp.Err("No se pudo conectar (${e.javaClass.simpleName}: ${e.message})")
            } catch (e: Exception) {
                Resp.Err("Ocurrió un error inesperado: ${e.message}")
            }
        }

    private fun errorDe(r: Response<*>): Resp.Err {
        val texto = try { r.errorBody()?.string() } catch (e: Exception) { null }
        val api: ErrorApi? = try { gson.fromJson(texto, ErrorApi::class.java) } catch (e: Exception) { null }
        val mensaje = api?.error ?: when (r.code()) {
            400 -> "Los datos enviados no son válidos."
            401 -> "Tu sesión expiró o no está iniciada. Vuelve a iniciar sesión."
            403 -> "No tienes permiso para hacer esto."
            404 -> "No se encontró lo que buscas."
            else -> "El servidor respondió con un error (${r.code()})."
        }
        return Resp.Err(mensaje, r.code(), api?.errores, api?.preguntasFaltantes)
    }

    // ---- autenticación
    suspend fun login(usuario: String, clave: String) = llamar { api.login(LoginRequest(usuario, clave)) }

    suspend fun registro(nombre: String, edad: Int, correo: String, clave: String) =
        llamar { api.registro(RegistroRequest(nombre, edad, correo, clave)) }

    // ---- catálogo
    suspend fun carreras(riesgo: String? = null) = llamar { api.carreras(riesgo) }
    suspend fun carrera(id: Int) = llamar { api.carrera(id) }

    // ---- aspirante
    suspend fun cuestionario() = llamar { api.cuestionario() }

    suspend fun calcular(carreraDefinidaId: Int?, opcionIds: List<Int>) =
        llamar { api.calcular(CalcularRequest(carreraDefinidaId, opcionIds)) }

    suspend fun historial() = llamar { api.historial() }
    suspend fun resultado(id: Int, orden: String?) = llamar { api.resultado(id, orden) }

    // ======================================================== administrador
    suspend fun resultadoAdmin(id: Int, orden: String?) = llamar { api.resultadoAdmin(id, orden) }

    // ---- carreras y ramas
    suspend fun crearCarrera(datos: CarreraGuardar) = llamar { api.crearCarrera(datos) }
    suspend fun actualizarCarrera(id: Int, datos: CarreraGuardar) = llamar { api.actualizarCarrera(id, datos) }
    suspend fun eliminarCarrera(id: Int) = llamarSinCuerpo { api.eliminarCarrera(id) }
    suspend fun agregarRama(carreraId: Int, datos: RamaGuardar) = llamar { api.agregarRama(carreraId, datos) }
    suspend fun eliminarRama(carreraId: Int, ramaId: Int) = llamarSinCuerpo { api.eliminarRama(carreraId, ramaId) }

    // ---- cuestionarios, preguntas y opciones
    suspend fun cuestionariosAdmin() = llamar { api.cuestionariosAdmin() }
    suspend fun cuestionarioAdmin(id: Int) = llamar { api.cuestionarioAdmin(id) }
    suspend fun crearCuestionario(datos: CuestionarioGuardar) = llamar { api.crearCuestionario(datos) }
    suspend fun actualizarCuestionario(id: Int, datos: CuestionarioGuardar) = llamar { api.actualizarCuestionario(id, datos) }
    suspend fun eliminarCuestionario(id: Int) = llamarSinCuerpo { api.eliminarCuestionario(id) }

    suspend fun agregarPregunta(cuestionarioId: Int, datos: PreguntaGuardar) = llamar { api.agregarPregunta(cuestionarioId, datos) }
    suspend fun actualizarPregunta(id: Int, datos: PreguntaGuardar) = llamar { api.actualizarPregunta(id, datos) }
    suspend fun eliminarPregunta(id: Int) = llamarSinCuerpo { api.eliminarPregunta(id) }

    suspend fun agregarOpcion(preguntaId: Int, texto: String) = llamar { api.agregarOpcion(preguntaId, OpcionGuardar(texto)) }
    suspend fun actualizarOpcion(id: Int, texto: String) = llamar { api.actualizarOpcion(id, OpcionGuardar(texto)) }
    suspend fun eliminarOpcion(id: Int) = llamarSinCuerpo { api.eliminarOpcion(id) }

    suspend fun puntajes(opcionId: Int) = llamar { api.puntajes(opcionId) }
    suspend fun guardarPuntajes(opcionId: Int, datos: PuntajesGuardar) = llamar { api.guardarPuntajes(opcionId, datos) }

    // ---- aspirantes
    suspend fun aspirantes() = llamar { api.aspirantes() }
    suspend fun resultadosDeAspirante(id: Int) = llamar { api.resultadosDeAspirante(id) }
    suspend fun eliminarAspirante(id: Int) = llamarSinCuerpo { api.eliminarAspirante(id) }
}
