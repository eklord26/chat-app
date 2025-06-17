package Roles.DAO

import Roles.DTO.Role
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object RoleTable : IntIdTable("roles")
{
    val name = text("name")
    val deleted = bool("deleted")
}

class RoleDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RoleDAO>(RoleTable)

    var name by RoleTable.name
    var deleted by RoleTable.deleted
}

fun daoToModel(dao: RoleDAO?): Role? = dao?.let {
    Role (
        it.id.value,
        it.name,
        it.deleted
    )
}