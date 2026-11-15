package com.rumbo.app.data

// ---- carreras y ramas (lo que se envía al guardar)
data class CarreraGuardar(
    val nombre: String,
    val area: String,
    val riesgoIa: String,       // "bajo", "medio" o "alto"
    val notaIa: String?,
    val descripcion: String?
)

data class RamaGuardar(val nombre: String, val descripcion: String?)

// ---- cuestionarios
data class CuestionarioAdminResumen(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val activo: Boolean,
    val totalPreguntas: Int
)

data class PreguntaAdmin(
    val id: Int,
    val enunciado: String,
    val categoria: String,
    val categoriaEtiqueta: String,
    val orden: Int,
    val opciones: List<Opcion>
)

data class CuestionarioAdmin(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val activo: Boolean,
    val totalPreguntas: Int,
    val preguntas: List<PreguntaAdmin>
)

data class CuestionarioGuardar(val titulo: String, val descripcion: String?, val activo: Boolean)
data class PreguntaGuardar(val enunciado: String, val categoria: String, val orden: Int?)
data class OpcionGuardar(val texto: String)

// ---- puntajes de una opción
data class PuntajeCarrera(val carreraId: Int, val nombre: String, val puntos: Int)
data class PuntajeRama(val ramaId: Int, val nombre: String, val carreraNombre: String, val puntos: Int)

data class PuntajesOpcion(
    val opcionId: Int,
    val texto: String,
    val carreras: List<PuntajeCarrera>,
    val ramas: List<PuntajeRama>
)

data class PuntajeItem(val id: Int, val puntos: Int)
data class PuntajesGuardar(val carreras: List<PuntajeItem>, val ramas: List<PuntajeItem>)

// ---- aspirantes
data class AspiranteAdmin(
    val id: Int,
    val nombre: String,
    val edad: Int,
    val correo: String,
    val fechaRegistro: String,
    val totalResultados: Int
)
