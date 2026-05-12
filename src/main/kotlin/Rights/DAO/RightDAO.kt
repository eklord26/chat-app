package Rights.DAO

import Roles.DAO.RoleTable
import Rights.DTO.Right
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object RightTable : IntIdTable("rights") {
    val idRole = reference("id_role", RoleTable)
    val name = text("name")
    val deletedAt = timestamp("deleted_at").nullable()
}

class RightDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RightDAO>(RightTable)

    var idRole by RightTable.idRole
    var name by RightTable.name
    var deletedAt by RightTable.deletedAt
}

fun daoToModel(dao: RightDAO?): Right? = dao?.let {
    Right(
        it.id.value,
        it.idRole.value,
        it.name,
        it.deletedAt?.toString()
    )
}
