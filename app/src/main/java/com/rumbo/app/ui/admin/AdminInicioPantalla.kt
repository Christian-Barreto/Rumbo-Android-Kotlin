package com.rumbo.app.ui.admin

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rumbo.app.data.Sesion
import com.rumbo.app.ui.componentes.Etiqueta
import com.rumbo.app.ui.componentes.Pantalla
import com.rumbo.app.ui.pantallas.TarjetaAccion
import com.rumbo.app.ui.tema.RumboColores

@Composable
fun AdminInicioPantalla(
    alCarreras: () -> Unit,
    alCuestionarios: () -> Unit,
    alAspirantes: () -> Unit,
    alSalir: () -> Unit
) {
    Pantalla(
        titulo = "Administración",
        acciones = { TextButton(onClick = alSalir) { Text("Salir", color = Color.White) } }
    ) { interior ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(interior)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Etiqueta("Panel del administrador", RumboColores.Azul2)
            Text(
                "Hola, ${Sesion.nombre}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RumboColores.Navy
            )
            Text("Aquí gestionas todo el contenido de Rumbo.", color = RumboColores.Suave)

            TarjetaAccion(
                titulo = "Carreras y ramas",
                descripcion = "Crea y edita carreras, su nivel de riesgo de IA, la nota explicativa y sus ramas.",
                alClic = alCarreras,
                destacada = true
            )
            TarjetaAccion(
                titulo = "Cuestionario",
                descripcion = "Edita las preguntas, sus opciones y los puntajes que cada opción aporta a carreras y ramas.",
                alClic = alCuestionarios
            )
            TarjetaAccion(
                titulo = "Aspirantes",
                descripcion = "Consulta quién se registró, revisa sus respuestas y resultados, o elimina una cuenta.",
                alClic = alAspirantes
            )
        }
    }
}
