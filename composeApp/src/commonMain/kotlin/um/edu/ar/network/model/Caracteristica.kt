package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Caracteristica(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val dispositivo: Dispositivo
)