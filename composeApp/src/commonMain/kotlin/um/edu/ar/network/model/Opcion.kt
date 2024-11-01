package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Opcion(
    val id: Long,
    val idExterno: Long,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val precioAdicional: Double,
    val personalizacion: Personalizacion,
    var isSelected: Boolean = false
)
