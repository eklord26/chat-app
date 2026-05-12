package Roles.DAO

import Roles.DTO.Role
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.format.DateTimeFormatter

object RoleTable : IntIdTable("roles") {
    val name = text("name")
    val deletedAt = timestamp("deleted_at").nullable()
}

class RoleDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RoleDAO>(RoleTable)

    var name by RoleTable.name
    var deletedAt by RoleTable.deletedAt
}

fun daoToModel(dao: RoleDAO?): Role? = dao?.let {
    Role(
        it.id.value,
        it.name,
        it.deletedAt?.toString()
    )
}