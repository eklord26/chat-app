package Logger.DAO

import Logger.DTO.Log
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object LogTable : IntIdTable("logs") {
    val logType = varchar("log_type", 50)
    val event = varchar("event", 255)
    val idUser = integer("id_user")
    val date = timestamp("date")
    val lifeTime = integer("life_time")
    val ipAddress = varchar("id_address", 50)
    val description = text("description")
}

class LogDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<LogDAO>(LogTable)

    var logType by LogTable.logType
    var event by LogTable.event
    var idUser by LogTable.idUser
    var date by LogTable.date
    var lifeTime by LogTable.lifeTime
    var ipAddress by LogTable.ipAddress
    var description by LogTable.description
}

fun daoToModel(dao: LogDAO?): Log? = dao?.let {
    Log(
        id = it.id.value,
        logType = it.logType,
        event = it.event,
        idUser = it.idUser,
        date = it.date.toString(),
        description = it.description,
        lifeTime = it.lifeTime,
        ipAddress = it.ipAddress,
    )
}