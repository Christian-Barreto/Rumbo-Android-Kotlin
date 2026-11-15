package com.rumbo.app.ui.pantallas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
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
import com.rumbo.app.ui.componentes.Tarjeta
import com.rumbo.app.ui.tema.RumboColores

@Composable
fun InicioPantalla(
    alResponder: () -> Unit,
    alVerResultados: () -> Unit,
    alVerCarreras: () -> Unit,
    alSalir: () -> Unit
) {
    Pantalla(
        titulo = "Rumbo",
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
            Etiqueta("Orientación vocacional en la era de la IA", RumboColores.Azul2)
            Text(
                "Hola, ${Sesion.nombre}",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = RumboColores.Navy
            )
            Text(
                "Responde 12 preguntas y descubre tus carreras más afines y qué tan expuestas están a la inteligencia artificial.",
                color = RumboColores.Suave
            )
            Spacer(Modifier.height(4.dp))

            TarjetaAccion(
                titulo = "Responder el cuestionario",
                descripcion = "Doce preguntas sobre tus hobbies, fortalezas, intereses y estilo de trabajo.",
                destacada = true,
                alClic = alResponder
            )
            TarjetaAccion(
                titulo = "Mis resultados",
                descripcion = "Revisa tus respuestas y las recomendaciones de evaluaciones anteriores.",
                alClic = alVerResultados
            )
            TarjetaAccion(
                titulo = "Explorar carreras",
                descripcion = "Consulta el catálogo con el nivel de riesgo de IA de cada carrera.",
                alClic = alVerCarreras
            )
        }
    }
}

@Composable
fun TarjetaAccion(titulo: String, descripcion: String, alClic: () -> Unit, destacada: Boolean = false) {
    Tarjeta(color = if (destacada) RumboColores.Navy else RumboColores.Superficie, alClic = alClic) {
        Text(
            titulo,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (destacada) Color.White else RumboColores.Navy
        )
        Spacer(Modifier.height(4.dp))
        Text(descripcion, color = if (destacada) Color(0xFFD6E5F6) else RumboColores.Suave)
    }
}
