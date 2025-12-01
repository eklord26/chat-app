package Logger.DTO

import Logger.Enums.EventType

data class Event(
    val type: EventType,
    val description: String,
)
