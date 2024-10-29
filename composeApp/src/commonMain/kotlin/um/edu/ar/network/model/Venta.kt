package um.edu.ar.network.model

import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.serialization.Serializable

@Serializable
data class Venta(
    val fechaVenta: String = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).toString(),
    val precioFinal: Double,
    val dispositivo: DispositivoSimple,
    val personalizaciones: List<PersonalizacionConOpciones>,
    val adicionales: List<AdicionalSimple>
)
