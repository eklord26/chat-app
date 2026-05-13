package Registration.Services

import Passwords.Builders.PasswordHashBuilder
import Registration.DTO.RegisterBodyDTO
import Registration.DTO.RegistrationDataDTO
import Tokens.Services.TokenService
import Users.DTO.UserFilter
import Users.Validators.UserContactValidator
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import com.example.Users.Services.UserService
import io.ktor.server.application.ApplicationEnvironment

class RegistrationService(environment: ApplicationEnvironment? = null) {
    private val repo = UserRepository(environment)
    private val service = UserService(environment)
    private val tokenService = TokenService()

    suspend fun register(data: RegisterBodyDTO): RegistrationDataDTO {
        require(UserContactValidator.isValidEmail(data.email)) { "Invalid email format" }
        require(UserContactValidator.isValidPhone(data.phone)) { "Invalid phone format" }

        if (service.checkLogin(data.login)) {
            return RegistrationDataDTO(
                id = null,
                status = "error",
                message = "User with this login already exists.",
                authToken = null
            )
        }

        val passwordHash = PasswordHashBuilder(data.password).build()
        service.create(
            User(
                name = data.username,
                login = data.login,
                email = data.email.trim(),
                phone = UserContactValidator.normalizePhone(data.phone),
                fio = data.fio.trim(),
                isAdmin = false,
                passwordHash = passwordHash
            )
        )

        val userId = repo.findByFilter(UserFilter(login = data.login)).first()?.id
            ?: throw IllegalStateException("Created user was not found by login: ${data.login}")
        val token = tokenService.generateAuthToken(userId)

        return RegistrationDataDTO(
            id = userId,
            status = "success",
            message = "User created successfully.",
            authToken = token
        )
    }
}
