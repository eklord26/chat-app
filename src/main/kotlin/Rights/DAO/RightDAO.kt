package Rights.DAO

import Rights.DTO.Right
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable

object RightTable : IntIdTable("rights")
{
    val idRole = integer("id_role")
    val name = text("name")
    val deleted = bool("deleted")
}

class RightDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<RightDAO>(RightTable)

    var idRole by RightTable.idRole
    var name by RightTable.name
    var deleted by RightTable.deleted
}

fun daoToModel(dao: RightDAO?): Right? = dao?.let {
    Right (
        it.id.value,
        it.idRole,
        it.name,
        it.deleted
    )
}