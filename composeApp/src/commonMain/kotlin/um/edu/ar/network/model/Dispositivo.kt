package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Dispositivo(
    val id: Int,
    val codigo: String?,
    val nombre: String?,
    val descripcion: String?,
    val precioBase: Double?,
    val moneda: String?
)