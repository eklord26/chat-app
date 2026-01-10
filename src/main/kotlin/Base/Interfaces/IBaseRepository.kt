package Base.Interfaces

// T - Класс Модели сущности K - Класс фильтра
interface IBaseRepository<T, K> {
    suspend fun findById(id: Int): T?

    suspend fun findAll(): List<T?>
    suspend fun create(entity: T): Unit
    suspend fun updateById(id: Int, entity: T): Unit

    suspend fun findByFilter(filter: K): List<T?>
}