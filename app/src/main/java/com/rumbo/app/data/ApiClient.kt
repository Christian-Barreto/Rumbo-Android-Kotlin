package com.rumbo.app.data

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    // 10.0.2.2 es el "localhost" de tu PC visto desde el emulador de Android.
    // En un celular real usa la IP de tu PC, por ejemplo "http://192.168.1.20:5080/".
    const val BASE_URL = "http://10.0.2.2:5080/"

    private val okHttp: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { cadena ->
                val peticion = cadena.request().newBuilder()
                Sesion.token?.let { peticion.addHeader("Authorization", "Bearer $it") }
                cadena.proceed(peticion.build())
            }
            .addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BASIC })
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val api: RumboApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttp)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(RumboApi::class.java)
    }
}
