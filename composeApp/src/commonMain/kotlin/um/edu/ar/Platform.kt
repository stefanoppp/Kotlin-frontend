package um.edu.ar

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform