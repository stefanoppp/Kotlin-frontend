package um.edu.ar.network.model
import kotlinx.serialization.Serializable

@Serializable
data class Personalizacion(
    val id: Long,
    val idExterno: Long,
    val nombre: String? = null,
    val descripcion: String? = null,
    val dispositivo: Dispositivo? = null,
    var isSelected: Boolean = false,
    var opciones: List<Opcion> = listOf()
)
