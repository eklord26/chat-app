package Tokens.DAO

import Messages.DAO.MessageDAO
import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum.Companion.getEnumByType
import Tokens.DTO.Token
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp


object TokenTable : IntIdTable("tokens")
{
    val authToken = text("auth_token")
    val encryptToken = text("encrypt_token")
    val dateExpire = timestamp("date_expire")
    val active = bool("active")
}

class TokenDAO(id: EntityID<Int>): IntEntity(id) {
    companion object : IntEntityClass<TokenDAO>(TokenTable)

    var authToken by TokenTable.authToken
    var encryptToken by TokenTable.encryptToken
    var dateExpire by TokenTable.dateExpire
    var active by TokenTable.active
}

fun daoToModel(dao: TokenDAO?): Token? = dao?.let {
    Token (
        it.id.value,
        it.authToken,
        it.encryptToken,
        it.dateExpire.toString(),
        it.active
    )
}