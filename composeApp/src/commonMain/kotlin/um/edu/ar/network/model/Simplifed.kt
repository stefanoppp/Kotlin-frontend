package um.edu.ar.network.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DispositivoSimple(
    @SerialName("idExterno") val idExterno: Long,
)

@Serializable
data class PersonalizacionConOpciones(
    @SerialName("idExterno") val id: Long,
    val opcion: OpcionSimple
)

@Serializable
data class OpcionSimple(
    @SerialName("idExterno") val id: Long
)

@Serializable
data class AdicionalSimple(
    @SerialName("idExterno") val id: Long
)
