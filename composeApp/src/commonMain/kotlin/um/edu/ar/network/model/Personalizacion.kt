package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Personalizacion(
    val id: Int,
    val nombre: String? = null,
    val descripcion: String? = null,
    val dispositivo: Dispositivo? = null
)
