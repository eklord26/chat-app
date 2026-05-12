package Registration.Services

import Passwords.Builders.PasswordHashBuilder
import Registration.DTO.RegisterBodyDTO
import Registration.DTO.RegistrationDataDTO
import Tokens.Services.TokenService
import Users.DTO.UserFilter
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import com.example.Users.Services.UserService

class RegistrationService {

    private val repo = UserRepository()
    private val service = UserService()
    private val tokenService = TokenService()

    public suspend fun register(data: RegisterBodyDTO): RegistrationDataDTO {
        try {
            if(service.checkLogin(data.login)) {
                return RegistrationDataDTO(null,
                    "error",
                    "Пользователь с таким логином уже существует.",
                    null,
                    null
                )
            }
            val passwordHash = PasswordHashBuilder(data.password).build()

            service.create(
                User(
                    name = data.username,
                    login = data.login,
                    isAdmin = false,
                    passwordHash = passwordHash
                )
            )

            val filter: UserFilter = UserFilter(login = data.login,)
            val userId = repo.findByFilter(filter).first()?.id
                ?: throw IllegalStateException("Created user was not found by login: ${data.login}")
            val token = tokenService.generateAuthToken(userId)

            return RegistrationDataDTO(
                id = userId,
                status = "success",
                message = "Пользователь успешно создан.",
                authToken = token,
                encryptedKey = tokenService.getEncryptToken(token)
            )
        } catch (e: Exception) {
            throw e
        }

    }
}
