package Registration.DTO

import Base.Helpers.EncriptionHelper
import Base.Interfaces.IRequestData
import Tokens.DTO.Token
import Tokens.Services.TokenService
import kotlinx.serialization.Serializable

@Serializable
data class RegisterBodyDTO(
    var username: String,
    var password: String,
    var login: String
): IRequestData {

    override suspend fun decode(token: Token) {
        val service = TokenService()
        val helper = EncriptionHelper()
        val key = service.getEncryptToken(token.authToken)

        if(key != null) {
            username = helper.decode(username, key)
            password = helper.decode(password, key)
            login = helper.decode(login, key)
        }
    }

    override suspend fun encode(token: Token) {
        val service = TokenService()
        val helper = EncriptionHelper()
        val key = service.getEncryptToken(token.authToken)

        if(key != null) {
            username = helper.code(username, key)
            password = helper.code(password, key)
            login = helper.code(login, key)
        }
    }
}
