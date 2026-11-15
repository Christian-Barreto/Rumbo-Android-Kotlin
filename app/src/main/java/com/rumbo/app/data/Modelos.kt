package com.rumbo.app.data

// Los nombres de los campos coinciden con el JSON de la API (camelCase).

// ---- autenticación
data class LoginRequest(val usuario: String, val clave: String)
data class RegistroRequest(val nombre: String, val edad: Int, val correo: String, val clave: String)
data class LoginResponse(
    val token: String,
    val rol: String,          // "admin" o "aspirante"
    val usuarioId: Int,
    val nombre: String,
    val expira: String
)

// ---- carreras
data class CarreraResumen(
    val id: Int,
    val nombre: String,
    val area: String,
    val riesgoIa: String,          // "bajo", "medio" o "alto"
    val riesgoIaEtiqueta: String   // "Bajo", "Medio" o "Alto"
)

data class Rama(val id: Int, val nombre: String, val descripcion: String?)

data class CarreraDetalle(
    val id: Int,
    val nombre: String,
    val area: String,
    val riesgoIa: String,
    val riesgoIaEtiqueta: String,
    val notaIa: String?,
    val descripcion: String?,
    val ramas: List<Rama>
)

// ---- cuestionario
data class Opcion(val id: Int, val texto: String)

data class Pregunta(
    val id: Int,
    val enunciado: String,
    val orden: Int,
    val opciones: List<Opcion>
)

data class GrupoPreguntas(
    val categoria: String,          // "hobbies", "fortalezas", "intereses", "estilo_trabajo"
    val categoriaEtiqueta: String,
    val preguntas: List<Pregunta>
)

data class Cuestionario(
    val id: Int,
    val titulo: String,
    val descripcion: String?,
    val totalPreguntas: Int,
    val grupos: List<GrupoPreguntas>
)

// ---- resultados
data class CalcularRequest(val carreraDefinidaId: Int?, val opcionIds: List<Int>)

data class CarreraRecomendada(val posicion: Int, val puntajeTotal: Int, val carrera: CarreraResumen)

data class Aporte(val texto: String, val categoria: String, val categoriaEtiqueta: String, val puntos: Int)

data class Justificacion(val carrera: CarreraResumen, val puntaje: Int, val aportes: List<Aporte>)

data class RamaPuntaje(val rama: Rama, val puntaje: Int)

data class RespuestaDada(
    val preguntaId: Int,
    val pregunta: String,
    val categoria: String,
    val categoriaEtiqueta: String,
    val opcionId: Int,
    val opcion: String
)

data class Resultado(
    val id: Int,
    val fecha: String,
    val usuarioNombre: String,
    val carreraDefinida: CarreraResumen?,
    val orden: String,
    val carreras: List<CarreraRecomendada>,
    val ganadora: CarreraRecomendada?,
    val justificacion: Justificacion?,
    val ramas: List<RamaPuntaje>,
    val respuestas: List<RespuestaDada>
)

data class ResultadoResumen(
    val id: Int,
    val fecha: String,
    val ganadora: CarreraResumen?,
    val puntajeGanadora: Int,
    val carreraDefinida: CarreraResumen?
)

// ---- errores que devuelve la API: { "error": "...", "errores": {campo: mensaje}, "preguntasFaltantes": [..] }
data class ErrorApi(
    val error: String?,
    val errores: Map<String, String>?,
    val preguntasFaltantes: List<Int>?
)
