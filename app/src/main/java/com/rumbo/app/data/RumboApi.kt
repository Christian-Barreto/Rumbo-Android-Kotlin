package com.rumbo.app.data

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/** Endpoints de la API de Rumbo que usa el aspirante. */
interface RumboApi {

    @POST("api/auth/login")
    suspend fun login(@Body datos: LoginRequest): Response<LoginResponse>

    @POST("api/auth/registro")
    suspend fun registro(@Body datos: RegistroRequest): Response<LoginResponse>

    @GET("api/carreras")
    suspend fun carreras(@Query("riesgo") riesgo: String?): Response<List<CarreraResumen>>

    @GET("api/carreras/{id}")
    suspend fun carrera(@Path("id") id: Int): Response<CarreraDetalle>

    @GET("api/cuestionario")
    suspend fun cuestionario(): Response<Cuestionario>

    @POST("api/resultados")
    suspend fun calcular(@Body datos: CalcularRequest): Response<Resultado>

    @GET("api/resultados")
    suspend fun historial(): Response<List<ResultadoResumen>>

    @GET("api/resultados/{id}")
    suspend fun resultado(@Path("id") id: Int, @Query("orden") orden: String?): Response<Resultado>

    // ======================================================== administrador
    // ---- carreras y ramas
    @POST("api/admin/carreras")
    suspend fun crearCarrera(@Body datos: CarreraGuardar): Response<CarreraDetalle>

    @PUT("api/admin/carreras/{id}")
    suspend fun actualizarCarrera(@Path("id") id: Int, @Body datos: CarreraGuardar): Response<CarreraDetalle>

    @DELETE("api/admin/carreras/{id}")
    suspend fun eliminarCarrera(@Path("id") id: Int): Response<Unit>

    @POST("api/admin/carreras/{id}/ramas")
    suspend fun agregarRama(@Path("id") id: Int, @Body datos: RamaGuardar): Response<Rama>

    @DELETE("api/admin/carreras/{id}/ramas/{ramaId}")
    suspend fun eliminarRama(@Path("id") id: Int, @Path("ramaId") ramaId: Int): Response<Unit>

    // ---- cuestionarios
    @GET("api/admin/cuestionarios")
    suspend fun cuestionariosAdmin(): Response<List<CuestionarioAdminResumen>>

    @GET("api/admin/cuestionarios/{id}")
    suspend fun cuestionarioAdmin(@Path("id") id: Int): Response<CuestionarioAdmin>

    @POST("api/admin/cuestionarios")
    suspend fun crearCuestionario(@Body datos: CuestionarioGuardar): Response<CuestionarioAdmin>

    @PUT("api/admin/cuestionarios/{id}")
    suspend fun actualizarCuestionario(@Path("id") id: Int, @Body datos: CuestionarioGuardar): Response<CuestionarioAdmin>

    @DELETE("api/admin/cuestionarios/{id}")
    suspend fun eliminarCuestionario(@Path("id") id: Int): Response<Unit>

    // ---- preguntas
    @POST("api/admin/cuestionarios/{id}/preguntas")
    suspend fun agregarPregunta(@Path("id") id: Int, @Body datos: PreguntaGuardar): Response<PreguntaAdmin>

    @PUT("api/admin/preguntas/{id}")
    suspend fun actualizarPregunta(@Path("id") id: Int, @Body datos: PreguntaGuardar): Response<PreguntaAdmin>

    @DELETE("api/admin/preguntas/{id}")
    suspend fun eliminarPregunta(@Path("id") id: Int): Response<Unit>

    // ---- opciones
    @POST("api/admin/preguntas/{id}/opciones")
    suspend fun agregarOpcion(@Path("id") id: Int, @Body datos: OpcionGuardar): Response<Opcion>

    @PUT("api/admin/opciones/{id}")
    suspend fun actualizarOpcion(@Path("id") id: Int, @Body datos: OpcionGuardar): Response<Opcion>

    @DELETE("api/admin/opciones/{id}")
    suspend fun eliminarOpcion(@Path("id") id: Int): Response<Unit>

    // ---- puntajes
    @GET("api/admin/opciones/{id}/puntajes")
    suspend fun puntajes(@Path("id") id: Int): Response<PuntajesOpcion>

    @PUT("api/admin/opciones/{id}/puntajes")
    suspend fun guardarPuntajes(@Path("id") id: Int, @Body datos: PuntajesGuardar): Response<PuntajesOpcion>

    // ---- aspirantes
    @GET("api/admin/aspirantes")
    suspend fun aspirantes(): Response<List<AspiranteAdmin>>

    @GET("api/admin/aspirantes/{id}/resultados")
    suspend fun resultadosDeAspirante(@Path("id") id: Int): Response<List<ResultadoResumen>>

    @GET("api/admin/resultados/{id}")
    suspend fun resultadoAdmin(@Path("id") id: Int, @Query("orden") orden: String?): Response<Resultado>

    @DELETE("api/admin/aspirantes/{id}")
    suspend fun eliminarAspirante(@Path("id") id: Int): Response<Unit>
}
