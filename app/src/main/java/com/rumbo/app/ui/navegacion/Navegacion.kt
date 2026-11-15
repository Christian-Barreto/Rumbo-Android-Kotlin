package com.rumbo.app.ui.navegacion

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.rumbo.app.data.Sesion
import com.rumbo.app.ui.admin.AdminAspirantePantalla
import com.rumbo.app.ui.admin.AdminAspirantesPantalla
import com.rumbo.app.ui.admin.AdminCarreraFormPantalla
import com.rumbo.app.ui.admin.AdminCuestionarioDetallePantalla
import com.rumbo.app.ui.admin.AdminCuestionariosPantalla
import com.rumbo.app.ui.admin.AdminInicioPantalla
import com.rumbo.app.ui.admin.AdminPuntajesPantalla
import com.rumbo.app.ui.pantallas.CarreraDetallePantalla
import com.rumbo.app.ui.pantallas.CarrerasPantalla
import com.rumbo.app.ui.pantallas.CuestionarioPantalla
import com.rumbo.app.ui.pantallas.HistorialPantalla
import com.rumbo.app.ui.pantallas.InicioPantalla
import com.rumbo.app.ui.pantallas.LoginPantalla
import com.rumbo.app.ui.pantallas.RegistroPantalla
import com.rumbo.app.ui.pantallas.ResultadoPantalla

object Rutas {
    const val LOGIN = "login"
    const val REGISTRO = "registro"
    const val INICIO = "inicio"
    const val CUESTIONARIO = "cuestionario"
    const val HISTORIAL = "historial"
    const val CARRERAS = "carreras"
    const val ADMIN = "admin"

    const val RESULTADO = "resultado/{id}"
    const val CARRERA = "carrera/{id}"
    fun resultado(id: Int) = "resultado/$id"
    fun carrera(id: Int) = "carrera/$id"

    // ---- administrador
    const val ADMIN_CARRERAS = "admin/carreras"
    const val ADMIN_CARRERA = "admin/carrera/{id}"          // id 0 = carrera nueva
    const val ADMIN_CUESTIONARIOS = "admin/cuestionarios"
    const val ADMIN_CUESTIONARIO = "admin/cuestionario/{id}"
    const val ADMIN_PUNTAJES = "admin/puntajes/{id}"
    const val ADMIN_ASPIRANTES = "admin/aspirantes"
    const val ADMIN_ASPIRANTE = "admin/aspirante/{id}"
    const val ADMIN_RESULTADO = "admin/resultado/{id}"
    fun adminCarrera(id: Int) = "admin/carrera/$id"
    fun adminCuestionario(id: Int) = "admin/cuestionario/$id"
    fun adminPuntajes(id: Int) = "admin/puntajes/$id"
    fun adminAspirante(id: Int) = "admin/aspirante/$id"
    fun adminResultado(id: Int) = "admin/resultado/$id"
}

@Composable
fun RumboNavegacion() {
    val nav = rememberNavController()

    // Si ya hay una sesión guardada, se entra directo a la pantalla de su rol.
    val destinoInicial = when {
        !Sesion.estaActiva -> Rutas.LOGIN
        Sesion.rol == "admin" -> Rutas.ADMIN
        else -> Rutas.INICIO
    }

    val cerrarSesion: () -> Unit = {
        Sesion.cerrar()
        nav.navigate(Rutas.LOGIN) { popUpTo(nav.graph.id) { inclusive = true } }
    }
    val volver: () -> Unit = { nav.popBackStack() }

    NavHost(navController = nav, startDestination = destinoInicial) {

        composable(Rutas.LOGIN) {
            LoginPantalla(
                alRegistrar = { nav.navigate(Rutas.REGISTRO) },
                alEntrar = { rol ->
                    val destino = if (rol == "admin") Rutas.ADMIN else Rutas.INICIO
                    nav.navigate(destino) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Rutas.REGISTRO) {
            RegistroPantalla(
                alVolver = volver,
                alRegistrar = {
                    nav.navigate(Rutas.INICIO) { popUpTo(Rutas.LOGIN) { inclusive = true } }
                }
            )
        }

        composable(Rutas.INICIO) {
            InicioPantalla(
                alResponder = { nav.navigate(Rutas.CUESTIONARIO) },
                alVerResultados = { nav.navigate(Rutas.HISTORIAL) },
                alVerCarreras = { nav.navigate(Rutas.CARRERAS) },
                alSalir = cerrarSesion
            )
        }

        composable(Rutas.CUESTIONARIO) {
            CuestionarioPantalla(
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alTerminar = { id ->
                    nav.navigate(Rutas.resultado(id)) { popUpTo(Rutas.CUESTIONARIO) { inclusive = true } }
                }
            )
        }

        composable(
            Rutas.RESULTADO,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ResultadoPantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alVerCarrera = { nav.navigate(Rutas.carrera(it)) },
                alRepetir = { nav.navigate(Rutas.CUESTIONARIO) },
                alVerHistorial = { nav.navigate(Rutas.HISTORIAL) }
            )
        }

        composable(Rutas.HISTORIAL) {
            HistorialPantalla(
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alAbrir = { nav.navigate(Rutas.resultado(it)) },
                alResponder = { nav.navigate(Rutas.CUESTIONARIO) }
            )
        }

        composable(Rutas.CARRERAS) {
            CarrerasPantalla(
                alVolver = volver,
                alAbrir = { nav.navigate(Rutas.carrera(it)) }
            )
        }

        composable(
            Rutas.CARRERA,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            CarreraDetallePantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver
            )
        }

        // ============================================================ administrador
        composable(Rutas.ADMIN) {
            AdminInicioPantalla(
                alCarreras = { nav.navigate(Rutas.ADMIN_CARRERAS) },
                alCuestionarios = { nav.navigate(Rutas.ADMIN_CUESTIONARIOS) },
                alAspirantes = { nav.navigate(Rutas.ADMIN_ASPIRANTES) },
                alSalir = cerrarSesion
            )
        }

        composable(Rutas.ADMIN_CARRERAS) {
            // Es la misma lista del catálogo, con botón para crear y editando al tocar una carrera.
            CarrerasPantalla(
                alVolver = volver,
                alAbrir = { nav.navigate(Rutas.adminCarrera(it)) },
                titulo = "Gestionar carreras",
                alNueva = { nav.navigate(Rutas.adminCarrera(0)) }
            )
        }

        composable(
            Rutas.ADMIN_CARRERA,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            AdminCarreraFormPantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion
            )
        }

        composable(Rutas.ADMIN_CUESTIONARIOS) {
            AdminCuestionariosPantalla(
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alAbrir = { nav.navigate(Rutas.adminCuestionario(it)) }
            )
        }

        composable(
            Rutas.ADMIN_CUESTIONARIO,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            AdminCuestionarioDetallePantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alPuntajes = { nav.navigate(Rutas.adminPuntajes(it)) }
            )
        }

        composable(
            Rutas.ADMIN_PUNTAJES,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            AdminPuntajesPantalla(
                opcionId = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion
            )
        }

        composable(Rutas.ADMIN_ASPIRANTES) {
            AdminAspirantesPantalla(
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alAbrir = { nav.navigate(Rutas.adminAspirante(it)) }
            )
        }

        composable(
            Rutas.ADMIN_ASPIRANTE,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            AdminAspirantePantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alAbrirResultado = { nav.navigate(Rutas.adminResultado(it)) }
            )
        }

        composable(
            Rutas.ADMIN_RESULTADO,
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { entrada ->
            ResultadoPantalla(
                id = entrada.arguments?.getInt("id") ?: 0,
                alVolver = volver,
                alSesionExpirada = cerrarSesion,
                alVerCarrera = { nav.navigate(Rutas.carrera(it)) },
                alRepetir = null,
                alVerHistorial = null,
                admin = true
            )
        }
    }
}
