package com.rumbo.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.rumbo.app.data.Sesion
import com.rumbo.app.ui.navegacion.RumboNavegacion
import com.rumbo.app.ui.tema.RumboTema

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Sesion.iniciar(applicationContext)
        setContent {
            RumboTema {
                RumboNavegacion()
            }
        }
    }
}
