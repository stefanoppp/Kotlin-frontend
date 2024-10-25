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
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcyOTg4MjY1NCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzI5Nzk2MjU0fQ.Dm6dSnk9dp4MlSIBeyPRSxLxmG30XLnjQWPWl-XzEOTIzGXgGGvakTGM4N_yWne2l5F7ds6wMe1tqWzB77uOQg"

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()), // Permite scroll
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Botón para cargar dispositivos
            Button(onClick = {
                isLoading = true
                errorMessage = null // Resetear mensaje de error
                CoroutineScope(Dispatchers.IO).launch {
                    try {
                        val urlDispositivos = "http://10.0.2.2:8080/api/dispositivos"
                        val response = httpClient.get(urlDispositivos) {
                            headers {
                                append("Authorization", "Bearer $token")
                            }
                        }
                        dispositivos = Json.decodeFromString(response.bodyAsText())
                    } catch (e: Exception) {
                        dispositivos = listOf()
                        errorMessage = "Error cargando dispositivos: ${e.message}"
                    } finally {
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
                        totalPrice += if (isSelected) adicionalSelected.precio ?: 0.0 else -(adicionalSelected.precio ?: 0.0)
                    }
                }

                Text("Personalizaciones disponibles:")
                personalizaciones.forEach { personalizacion ->
                    PersonalizacionCard(personalizacion, opciones, onOptionSelected = { opcion, isSelected ->
                        totalPrice += if (isSelected) opcion.precioAdicional ?: 0.0 else -(opcion.precioAdicional ?: 0.0)
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
                onClick = { println("Compra finalizada con un total de $totalPrice USD") },
                enabled = selectedDispositivo != null, // Habilitado si hay un dispositivo seleccionado
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
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text("Personalización: ${personalizacion.nombre ?: "Sin nombre"}", style = MaterialTheme.typography.body1)
        opciones.filter { it.personalizacion?.id == personalizacion.id }.forEach { opcion ->
            OpcionCard(opcion, onOptionSelected)
        }
        Divider()
    }
}

@Composable
fun OpcionCard(opcion: Opcion, onOptionSelected: (Opcion, Boolean) -> Unit) {
    var isSelected by remember { mutableStateOf(false) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(checked = isSelected, onCheckedChange = { checked ->
            isSelected = checked
            // Lógica de sumar o restar dependiendo si está seleccionado o no
            onOptionSelected(opcion, checked)
        })
        Text("Opción: ${opcion.nombre ?: "Sin nombre"} - ${opcion.precioAdicional ?: "N/A"} USD", style = MaterialTheme.typography.body2)
    }
}


// Función genérica para obtener datos de la API
suspend inline fun <reified T> fetchData(endpoint: String, token: String): T {
    return try {
        val response = httpClient.get(endpoint) {
            headers {
                append(HttpHeaders.Authorization, "Bearer $token")
            }
        }
        val json = Json { ignoreUnknownKeys = true; coerceInputValues = true }
        json.decodeFromString(response.bodyAsText())
    } catch (e: Exception) {
        throw e
    }
}