package ChatMembers.DAO

import ChatMembers.DTO.ChatMember
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp
import java.time.ZoneId
import java.time.format.DateTimeFormatter

object ChatMembersTable : IntIdTable("chat_members") {
    val idChat = integer("id_chat")
    val idRole = integer("id_role")
    val idUser = integer("id_user")
    val createdAt = timestamp("created_at")
    val deletedAt = timestamp("deleted_at").nullable()
}

class ChatMemberDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<ChatMemberDAO>(ChatMembersTable)

    var idChat by ChatMembersTable.idChat
    var idRole by ChatMembersTable.idRole
    var idUser by ChatMembersTable.idUser
    var createdAt by ChatMembersTable.createdAt
    var deletedAt by ChatMembersTable.deletedAt
}

private val formatter = DateTimeFormatter.ISO_INSTANT.withZone(ZoneId.systemDefault())

fun daoToModel(dao: ChatMemberDAO?): ChatMember? = dao?.let {
    ChatMember(
        it.id.value,
        it.idChat,
        it.idRole,
        it.idUser,
        formatter.format(it.createdAt),
        it.deletedAt?.let { date -> formatter.format(date) }
    )
}