package Base.Interfaces

import com.example.Users.DTO.User

interface IBaseRepository<T> {
    suspend fun findById(id: Int): T?

    suspend fun findAll(): List<T?>
    suspend fun create(entity: T): Unit
    suspend fun updateById(id: Int, entity: T): Unit
}