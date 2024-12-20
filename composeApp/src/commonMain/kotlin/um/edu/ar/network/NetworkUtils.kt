package um.edu.ar.network

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.ContentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object NetworkUtils {
    val httpClient= HttpClient{
        install(ContentNegotiation){
            json(json = Json { ignoreUnknownKeys = true
                                coerceInputValues = true
                                }, contentType = ContentType.Any)
        }
    }
}