package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Caracteristica(
    val id: Long,
    val idExterno: Long,
    val nombre: String,
    val descripcion: String,
    val dispositivo: Dispositivo
)