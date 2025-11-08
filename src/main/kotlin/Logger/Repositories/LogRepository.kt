package Logger.Repositories

import Logger.DAO.LogDAO
import Logger.DAO.LogTable
import Logger.DAO.daoToModel
import Logger.DTO.Log
import Logger.Enums.EventType
import Logger.Enums.LogType
import Logger.Interfaces.ILogRepository
import com.example.Base.Helpers.suspendTransaction
import org.jetbrains.exposed.sql.*
import java.time.Instant
import java.time.format.DateTimeFormatter

class LogRepository: ILogRepository {
    override suspend fun findAll(): List<Log?> = suspendTransaction {
        LogDAO.all().map(::daoToModel)
    }

    override suspend fun findByType(type: LogType): List<Log?> = suspendTransaction {
        LogDAO
            .find { (LogTable.logType eq type.name) }
            .map(::daoToModel)
    }

    override suspend fun findByEvent(event: EventType): List<Log?> = suspendTransaction {
        LogDAO
            .find { (LogTable.event eq event.name) }
            .map(::daoToModel)
    }

    override suspend fun findByIdUser(id: Int): List<Log?> = suspendTransaction {
        LogDAO
            .find { (LogTable.idUser eq id) }
            .map(::daoToModel)
    }

    override suspend fun findByIp(ip: String): List<Log?> = suspendTransaction {
        LogDAO
            .find { (LogTable.ipAddress eq ip) }
            .map(::daoToModel)
    }

    override suspend fun findByDatePeriod(dateFrom: String, dateTo: String): List<Log?> = suspendTransaction {
        var formatedDateFrom = Instant.from(
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss").parse(dateFrom)
        )
        var formatedDateTo = Instant.from(
            DateTimeFormatter
                .ofPattern("yyyy-MM-dd HH:mm:ss").parse(dateTo)
        )

        LogDAO
            .find { (LogTable.date greaterEq formatedDateFrom) and (LogTable.date lessEq formatedDateTo) }
            .map(::daoToModel)
    }

    override suspend fun create(log: Log): Unit = suspendTransaction {
        LogDAO.new {
            logType = log.logType
            event = log.event
            ipAddress = log.ipAddress
            date = Instant.now()
            description = log.description
            idUser = log.idUser
            lifeTime = log.lifeTime
        }
    }

    override suspend fun delete(id: Int): Unit = suspendTransaction {
        LogDAO.findById(id)?.delete()
    }
}