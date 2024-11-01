package um.edu.ar
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpHeaders
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import um.edu.ar.network.NetworkUtils.httpClient
import um.edu.ar.network.model.*

@Composable
fun App() {
    MaterialTheme {
        var dispositivos by remember { mutableStateOf(listOf<Dispositivo>()) }
        var selectedDispositivo by remember { mutableStateOf<Dispositivo?>(null) }
        var adicionales by remember { mutableStateOf(listOf<Adicional>()) }
        var caracteristicas by remember { mutableStateOf(listOf<Caracteristica>()) }
        var personalizaciones by remember { mutableStateOf(listOf<Personalizacion>()) }
        var opciones by remember { mutableStateOf(listOf<Opcion>()) }
        var totalPrice by remember { mutableStateOf(0.0) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTczMDU4MjkwOCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzMwNDk2NTA4fQ.D4rzgR_rtpZlBCL_xyql8m2LXeKdr8wJnF539Cu6PM_OC8yks89TTaPgIhd0kmIQu0DNR9zluLmzFKRwAlKNcA"

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Permite scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(onClick = {
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        isLoading = true
                        errorMessage = null
                        // Usa fetchData para decodificar directamente la respuesta JSON en dispositivos
                        dispositivos = fetchData("http://10.0.2.2:8080/api/dispositivos", token)
                        isLoading = false
                    } catch (e: Exception) {
                        e.printStackTrace()  // Imprime el error completo
                        errorMessage = "Error cargando dispositivos: ${e}"
                        isLoading = false
                    }
                }
            }) {
                Text("Cargar Dispositivos")
            }

            if (isLoading) {
                CircularProgressIndicator()
            } else {
                errorMessage?.let {
                    Text("Error: $it", color = MaterialTheme.colors.error)
                }

                // Mostrar los dispositivos cargados
                dispositivos.forEach { dispositivo ->
                    DispositivoCard(dispositivo, selectedDispositivo) { selected ->
                        selectedDispositivo = selected
                        totalPrice = selected.precioBase ?: 0.0

                        // Cargar adicionales, características y personalizaciones
                        CoroutineScope(Dispatchers.IO).launch {
                            try {
                                adicionales = fetchData<List<Adicional>>("http://10.0.2.2:8080/api/adicionals", token)
                                    .filter { it.dispositivo.id == selectedDispositivo!!.id }

                                caracteristicas = fetchData<List<Caracteristica>>("http://10.0.2.2:8080/api/caracteristicas", token)
                                    .filter { it.dispositivo.id == selectedDispositivo!!.id }

                                personalizaciones = fetchData<List<Personalizacion>>("http://10.0.2.2:8080/api/personalizacions", token)
                                    .filter { it.dispositivo?.id == selectedDispositivo!!.id }

                                opciones = fetchData<List<Opcion>>("http://10.0.2.2:8080/api/opcions", token)
                                    .filter { opcion -> personalizaciones.any { it.id == opcion.personalizacion.id } }
                            } catch (e: Exception) {
                                errorMessage = "Error cargando datos: ${e.message}"
                            }
                        }
                    }
                }
            }

            if (selectedDispositivo != null) {
                Text("Características:")
                caracteristicas.forEach { caracteristica ->
                    CaracteristicaCard(caracteristica)
                }
                Text("Adicionales disponibles:")
                adicionales.forEach { adicional ->
                    AdicionalCard(adicional) { adicionalSelected, isSelected ->
                        adicionalSelected.isSelected = isSelected
                        totalPrice += if (isSelected) adicionalSelected.precio else -(adicionalSelected.precio)
                    }
                }

                Text("Personalizaciones disponibles:")
                personalizaciones.forEach { personalizacion ->
                    PersonalizacionCard(personalizacion, opciones, onOptionSelected = { opcion, isSelected ->
                        opcion.isSelected = isSelected
                        totalPrice += if (isSelected) opcion.precioAdicional else -(opcion.precioAdicional)
                    })
                }
            }

            // Mostrar el precio total actualizado
            Text(
                "Total: $totalPrice USD",
                fontSize = 24.sp, // Cambia el número por el tamaño deseado
                fontWeight = FontWeight.Bold
            )
            Button(
                onClick = {
                    if (selectedDispositivo != null) {
                        concretarVenta(
                            dispositivo = selectedDispositivo!!,
                            adicionales = adicionales.filter { it.isSelected }, // Filtrar adicionales seleccionados
                            personalizaciones = personalizaciones.map { personalizacion ->
                                personalizacion.copy(opciones = personalizacion.opciones.filter { it.isSelected })
                            }, // Filtrar personalizaciones con opciones seleccionadas
                            precioFinal = totalPrice,
                            token = token
                        )
                    }
                },
                enabled = selectedDispositivo != null,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(), // Ajusta el tamaño del botón
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.primary // Cambia el color si es necesario
                )
            ) {
                Text(
                    text = "Finalizar Compra",
                    fontSize = 18.sp, // Ajusta el tamaño del texto si es necesario
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DispositivoCard(dispositivo: Dispositivo, selectedDispositivo: Dispositivo?, onSelected: (Dispositivo) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onSelected(dispositivo) },
        horizontalAlignment = Alignment.Start
    ) {
        Text("Código: ${dispositivo.codigo}", style = MaterialTheme.typography.h6)
        Text("Nombre: ${dispositivo.nombre ?: "Sin nombre"}", style = MaterialTheme.typography.body1)
        Text("Descripción: ${dispositivo.descripcion ?: "Sin descripción"}", style = MaterialTheme.typography.body2)
        Text("Precio: ${dispositivo.precioBase ?: "N/A"} ${dispositivo.moneda ?: "N/A"}", style = MaterialTheme.typography.body2)
        Divider()
    }
}

@Composable
fun CaracteristicaCard(caracteristica: Caracteristica) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Nombre: ${caracteristica.nombre ?: "Sin nombre"}", style = MaterialTheme.typography.body1)
        Text("Descripción: ${caracteristica.descripcion ?: "Sin descripción"}", style = MaterialTheme.typography.body2)
        Divider()
    }
}

@Composable
fun AdicionalCard(adicional: Adicional, onSelected: (Adicional, Boolean) -> Unit) {
    var isSelected by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isSelected,
            onCheckedChange = { checked ->
                isSelected = checked
                // Lógica de suma o resta dependiendo del estado seleccionado
                onSelected(adicional, checked)
            }
        )
        Text("Nombre: ${adicional.nombre ?: "Sin nombre"}", style = MaterialTheme.typography.body1)
        Text("Precio: ${adicional.precio ?: "N/A"} USD", style = MaterialTheme.typography.body2)
    }
    Divider()
}

@Composable
fun PersonalizacionCard(personalizacion: Personalizacion, opciones: List<Opcion>, onOptionSelected: (Opcion, Boolean) -> Unit) {
    // Mantenemos una lista mutable de las opciones seleccionadas
    var opcionesSeleccionadas by remember { mutableStateOf(personalizacion.opciones.filter { it.isSelected }) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Personalización: ${personalizacion.nombre ?: "Sin nombre"}", style = MaterialTheme.typography.body1)

        // Filtrar solo las opciones que pertenecen a esta personalización
        opciones.filter { it.personalizacion.id == personalizacion.id }.forEach { opcion ->
            OpcionCard(opcion) { opcionSeleccionada, isSelected ->
                // Actualizamos la selección de la opción
                opcionSeleccionada.isSelected = isSelected

                // Añadir o quitar la opción de la lista de opciones seleccionadas
                opcionesSeleccionadas = if (isSelected) {
                    opcionesSeleccionadas + opcionSeleccionada
                } else {
                    opcionesSeleccionadas - opcionSeleccionada
                }

                // Notificar que se seleccionó o deseleccionó una opción
                onOptionSelected(opcionSeleccionada, isSelected)
            }
        }
        Divider()
    }

    // Actualizamos la lista de opciones en la personalización para que refleje solo las seleccionadas
    personalizacion.opciones = opcionesSeleccionadas
}

@Composable
fun OpcionCard(opcion: Opcion, onOptionSelected: (Opcion, Boolean) -> Unit) {
    var isSelected by remember { mutableStateOf(opcion.isSelected) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { checked ->
            isSelected = checked
            opcion.isSelected = checked // Asegurarse de que la opción tiene el estado actualizado
            // Notificar que se seleccionó o deseleccionó la opción
            onOptionSelected(opcion, checked)
        })
        Text("Opción: ${opcion.nombre ?: "Sin nombre"} - ${opcion.precioAdicional ?: "N/A"} USD", style = MaterialTheme.typography.body2)
    }
}
fun concretarVenta(
    dispositivo: Dispositivo,
    adicionales: List<Adicional>,
    personalizaciones: List<Personalizacion>,
    precioFinal: Double,
    token: String
) {
    val fechaVenta = Clock.System.now().toString()

    val dispositivoSimple = DispositivoSimple(idExterno = dispositivo.idExterno)

    // Verificar y depurar el estado de las opciones seleccionadas
    personalizaciones.forEach { personalizacion ->
        println("Personalización ID Externo: ${personalizacion.idExterno}")
        personalizacion.opciones.forEach { opcion ->
            println("    Opción ID Externo: ${opcion.idExterno}, Seleccionada: ${opcion.isSelected}")
        }
    }

    // Filtrar personalizaciones con al menos una opción seleccionada
    val personalizacionesConOpciones = personalizaciones
        .mapNotNull { personalizacion ->
            val opcionSeleccionada = personalizacion.opciones.find { it.isSelected }
            if (opcionSeleccionada != null) {
                PersonalizacionConOpciones(
                    id = personalizacion.idExterno,
                    opcion = OpcionSimple(id = opcionSeleccionada.idExterno)
                )
            } else {
                null
            }
        }

    // Verificar el resultado de personalizaciones con opciones seleccionadas
    personalizacionesConOpciones.forEach {
        println("Personalización a enviar: ID Externo ${it.id}, Opción ID Externo: ${it.opcion.id}")
    }

    val adicionalesSimple = adicionales
        .filter { it.isSelected }
        .map { AdicionalSimple(id = it.idExterno) }

    // Construir el objeto Venta y enviarlo
    val ventaData = Venta(
        fechaVenta = fechaVenta,
        precioFinal = precioFinal,
        dispositivo = dispositivoSimple,
        personalizaciones = personalizacionesConOpciones,
        adicionales = adicionalesSimple
    )

    CoroutineScope(Dispatchers.IO).launch {
        try {
            val urlVentas = "http://10.0.2.2:8080/api/ventas"
            val response = httpClient.post(urlVentas) {
                headers {
                    append(HttpHeaders.Authorization, "Bearer $token")
                    append(HttpHeaders.ContentType, "application/json")
                }
                setBody(Json.encodeToString(ventaData))
            }

            if (response.status.value in 200..299) {
                println("Venta realizada correctamente")
            } else {
                println("Error al realizar la venta: ${response.status.description}")
            }
        } catch (e: Exception) {
            println("Error al realizar la venta: ${e.message}")
        }
    }
}

// Función para obtener datos de la API con sustitución de valores null en campos numéricos
suspend inline fun <reified T> fetchData(endpoint: String, token: String): T {
    return try {
        val response = httpClient.get(endpoint) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        val responseBody = response.bodyAsText()

        // Reemplaza null en idExterno con un valor predeterminado (ej. 0) antes de la deserialización
        val cleanedJson = responseBody.replace(Regex("""("idExterno"\s*:\s*)null"""), "$10")

        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString(cleanedJson)
    } catch (e: Exception) {
        throw e
    }
}
