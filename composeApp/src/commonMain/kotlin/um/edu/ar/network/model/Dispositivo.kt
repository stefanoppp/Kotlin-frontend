package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Dispositivo(
    val id: Int,
    val codigo: String? = null,
    val nombre: String? = null,
    val descripcion: String? = null,
    val precioBase: Double? = null,
    val moneda: String? = null
)

