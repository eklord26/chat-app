package Authentication.Services

import Authentication.DTO.AuthenticationBodyDTO
import Authentication.DTO.AuthenticationDataDTO
import Passwords.Services.PasswordService
import Tokens.Services.TokenService
import Users.DTO.UserFilter
import com.example.Users.Repository.UserRepository
import io.ktor.server.application.ApplicationEnvironment

class AuthenticationService(environment: ApplicationEnvironment? = null) {
    private val repo = UserRepository(environment)
    private val tokenService = TokenService()
    private val passwordService = PasswordService()

    suspend fun authenticate(data: AuthenticationBodyDTO): AuthenticationDataDTO {
        val user = repo.findByFilter(UserFilter(login = data.login, isDeleted = false)).firstOrNull()
            ?: return AuthenticationDataDTO(
                id = null,
                status = "error",
                message = "User with this login does not exist.",
                authToken = null
            )

        if (!passwordService.checkHashPassword(user.passwordHash, data.password)) {
            return AuthenticationDataDTO(
                id = null,
                status = "error",
                message = "Invalid password.",
                authToken = null
            )
        }

        val token = tokenService.generateAuthToken(user.id!!)
        return AuthenticationDataDTO(
            id = user.id,
            status = "success",
            message = "User authenticated successfully.",
            authToken = token
        )
    }
}
