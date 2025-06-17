package Registration.Services

import Base.DTO.ResponseDTO
import Passwords.Builders.PasswordHashBuilder
import Registration.RegisterBodyDTO
import com.example.Users.DTO.User
import com.example.Users.Repository.UserRepository
import com.example.Users.Services.UsersService

class RegistrationService {

    private val repo = UserRepository()
    private val service = UsersService()

    public suspend fun register(data: RegisterBodyDTO): ResponseDTO {

        if(!service.checkLogin(data.login)) {
            return ResponseDTO(null,
                "error",
                "Пользователь с таким логином уже существует."
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

        return ResponseDTO(repo.findByLogin(data.login)?.id,
            "success",
            "Пользователь успешно создан."
        )
    }
}