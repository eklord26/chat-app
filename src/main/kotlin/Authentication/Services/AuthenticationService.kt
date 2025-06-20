package Authentication.Services

import Authentication.DTO.AuthenticationBodyDTO
import Authentication.DTO.AuthenticationDataDTO
import Passwords.Services.PasswordService
import Tokens.Services.TokenService
import com.example.Users.Repository.UserRepository
import com.example.Users.Services.UsersService

class AuthenticationService {

    private val repo = UserRepository()
    private val service = UsersService()
    private val tokenService = TokenService()
    private val passwordService = PasswordService()

    public suspend fun authenticate(data: AuthenticationBodyDTO, authToken: String): AuthenticationDataDTO {
        try {
            if(service.checkLogin(data.login)) {
                if (service.checkDeletedByLogin(data.login)) {

                    val user = repo.findByLogin(data.login)

                    if (!passwordService.checkHashPassword(user!!.passwordHash, data.password)) {
                        return AuthenticationDataDTO(
                            null,
                            "error",
                            "Неверно указан пароль.",
                            null,
                            null
                        )
                    }

                    var token = authToken

                    if (!tokenService.checkToken(authToken)) {
                        token = tokenService.generateAuthToken()
                    }

                    return AuthenticationDataDTO(repo.findByLogin(data.login)?.id,
                        "success",
                        "Пользователь успешно авторизован.",
                        token,
                        tokenService.getEncryptToken(token)
                    )
                } else return AuthenticationDataDTO (
                    null,
                    "error",
                    "Данный пользователь удалён.",
                    null,
                    null
                )

            } else return AuthenticationDataDTO (
                null,
                "error",
                "Пользователя с этим логином не существует.",
                null,
                null
            )
        } catch (e: Exception) {
            throw e
        }
    }
}