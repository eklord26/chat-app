package Tokens.DAO

import Tokens.DTO.Token
import com.example.Users.DAO.UserTable
import org.jetbrains.exposed.dao.IntEntity
import org.jetbrains.exposed.dao.IntEntityClass
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.timestamp

object TokenTable : IntIdTable("tokens") {
    val idUser = optReference("id_user", UserTable)
    val authToken = text("auth_token")
    val encryptToken = text("encrypt_token")
    val dateExpire = timestamp("date_expire")
    val active = bool("active")
    val deletedAt = timestamp("deleted_at").nullable()
}

class TokenDAO(id: EntityID<Int>) : IntEntity(id) {
    companion object : IntEntityClass<TokenDAO>(TokenTable)

    var idUser by TokenTable.idUser
    var authToken by TokenTable.authToken
    var encryptToken by TokenTable.encryptToken
    var dateExpire by TokenTable.dateExpire
    var active by TokenTable.active
    var deletedAt by TokenTable.deletedAt
}

fun daoToModel(dao: TokenDAO?): Token? = dao?.let {
    Token(
        it.id.value,
        it.idUser?.value,
        it.authToken,
        it.encryptToken,
        it.dateExpire.toString(),
        it.active,
        it.deletedAt?.toString()
    )
}
