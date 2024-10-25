package um.edu.ar

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.ktor.client.request.*
import io.ktor.client.statement.bodyAsText
import io.ktor.http.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.jetbrains.compose.ui.tooling.preview.Preview
import um.edu.ar.network.NetworkUtils.httpClient
import um.edu.ar.network.model.*

@Composable
@Preview
fun App() {
    MaterialTheme {
        var dispositivos by remember { mutableStateOf(listOf<Dispositivo>()) }
        var selectedDispositivo by remember { mutableStateOf<Dispositivo?>(null) }
        var adicionales by remember { mutableStateOf(listOf<Adicional>()) }
        var caracteristicas by remember { mutableStateOf(listOf<Caracteristica>()) }
        var personalizaciones by remember { mutableStateOf(listOf<Personalizacion>()) }
        var totalPrice by remember { mutableStateOf(0.0) }
        var isLoading by remember { mutableStateOf(false) }
        var errorMessage by remember { mutableStateOf<String?>(null) }

        // Token de autenticación
        val token = "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImV4cCI6MTcyOTg4MjY1NCwiYXV0aCI6IlJPTEVfQURNSU4gUk9MRV9VU0VSIiwiaWF0IjoxNzI5Nzk2MjU0fQ.Dm6dSnk9dp4MlSIBeyPRSxLxmG30XLnjQWPWl-XzEOTIzGXgGGvakTGM4N_yWne2l5F7ds6wMe1tqWzB77uOQg"

        val scrollState = rememberScrollState() // Scroll state para manejar el desplazamiento vertical

        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(scrollState), // Activamos el scroll vertical
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
                                append(HttpHeaders.Authorization, "Bearer $token")
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
                    DispositivoCard(dispositivo, selectedDispositivo, onSelected = { selected ->
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
                                    .filter { it.dispositivo.id == selectedDispositivo!!.id }
                            } catch (e: Exception) {
                                errorMessage = "Error cargando datos: ${e.message}"
                            }
                        }
                    })
                }
            }

            if (selectedDispositivo != null) {
                Text("Características:")
                caracteristicas.forEach { caracteristica ->
                    CaracteristicaCard(caracteristica)
                }
                Text("Adicionales disponibles:")
                adicionales.forEach { adicional ->
                    AdicionalCard(adicional) {
                        totalPrice += adicional.precio
                    }
                }
                Text("Personalizaciones disponibles:")
                personalizaciones.forEach { personalizacion ->
                    PersonalizacionCard(personalizacion)
                }
            }

            // Mostrar el precio total actualizado
            Text("Total: $totalPrice USD")
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
        Text("Nombre: ${dispositivo.nombre}", style = MaterialTheme.typography.body1)
        Text("Descripción: ${dispositivo.descripcion}", style = MaterialTheme.typography.body2)
        Text("Precio: ${dispositivo.precioBase} ${dispositivo.moneda}", style = MaterialTheme.typography.body2)
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
        Text("Nombre: ${caracteristica.nombre}", style = MaterialTheme.typography.body1)
        Text("Descripción: ${caracteristica.descripcion}", style = MaterialTheme.typography.body2)
        Divider()
    }
}

@Composable
fun AdicionalCard(adicional: Adicional, onSelected: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clickable { onSelected() },
        horizontalAlignment = Alignment.Start
    ) {
        Text("Nombre: ${adicional.nombre}", style = MaterialTheme.typography.body1)
        Text("Precio: ${adicional.precio} USD", style = MaterialTheme.typography.body2)
        Divider()
    }
}

@Composable
fun PersonalizacionCard(personalizacion: Personalizacion) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text("Nombre: ${personalizacion.nombre}", style = MaterialTheme.typography.body1)
        Divider()
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
        // Configuramos el JSON para ignorar las claves desconocidas
        val json = Json { ignoreUnknownKeys = true }
        json.decodeFromString(response.bodyAsText())
    } catch (e: Exception) {
        throw e
    }
}
