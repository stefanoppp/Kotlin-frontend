package um.edu.ar.network.model

import kotlinx.serialization.Serializable

@Serializable
data class Opcion(
    val id: Int,
    val codigo: String,
    val nombre: String,
    val descripcion: String,
    val precioAdicional: Double,
    val personalizacion: Personalizacion
)
