package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Adicional(
    val id: Long,
    val idExterno: Long,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val precioGratis: Double? = null,
    val dispositivo: Dispositivo,
    var isSelected: Boolean = false
)