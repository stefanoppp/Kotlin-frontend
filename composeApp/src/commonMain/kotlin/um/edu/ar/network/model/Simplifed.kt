package um.edu.ar.network.model

import kotlinx.serialization.Serializable
@Serializable
data class DispositivoSimple(val id: Int)

@Serializable
data class PersonalizacionConOpciones(
    val id: Int,
    val opcion: OpcionSimple
)

@Serializable
data class OpcionSimple(val id: Int)

@Serializable
data class AdicionalSimple(val id: Int)
