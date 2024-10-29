package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Adicional(
    val id: Int,
    val nombre: String,
    val descripcion: String,
    val precio: Double,
    val dispositivo: Dispositivo,
    var isSelected: Boolean = false
)