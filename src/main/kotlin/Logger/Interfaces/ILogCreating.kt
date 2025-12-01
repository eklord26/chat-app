package Logger.Interfaces

import Logger.Enums.EventType

interface ILogCreating {
    public suspend fun createLog (
        repository: ILogRepository,
        idUser: Int,
        event: EventType,
        ip: String,
        description: String,
    ): Unit
}