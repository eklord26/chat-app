package Logger.DTO

data class Log(
    val id: Int,
    val logType: String,
    val event: String,
    val idUser: Int,
    val date: String,
    val description: String,
    val lifeTime: Int,
    val ipAddress: String,
)
