package Registration.Services

import Passwords.Builders.PasswordHashBuilder
import Registration.DTO.RegisterBodyDTO
import Registration.DTO.RegistrationDataDTO
import Tokens.Services.TokenService
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import com.example.Users.Services.UsersService

class RegistrationService {

    private val repo = UserRepository()
    private val service = UsersService()
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

            repo.create(User(
                login = data.login,
                passwordHash = passwordHash,
                name = data.username,
                id = 0,
                isAdmin = false,
                deleted = false
            ))

            val token = tokenService.generateAuthToken()
            val key = tokenService.getEncryptToken(token)

            return RegistrationDataDTO(repo.findByLogin(data.login)?.id,
                "success",
                "Пользователь успешно создан.",
                token,
                tokenService.getEncryptToken(token)
            )
        } catch (e: Exception) {
            throw e
        }

    }
}