package Rights.Services

import Rights.DTO.Right
import Rights.DTO.RightFilter
import Rights.Repositories.RightRepository
import java.time.Instant

class RightService {
    private val repository = RightRepository()

    suspend fun findById(id: Int): Right? = repository.findById(id)

    suspend fun findByFilter(filter: RightFilter): List<Right?> = repository.findByFilter(filter)

    suspend fun create(right: Right): Int? {
        repository.create(right)
        return repository.findByFilter(RightFilter(name = right.name, idRole = right.idRole))
            .firstOrNull()?.id
    }

    suspend fun update(id: Int, right: Right): Boolean {
        if (repository.findById(id) != null) {
            repository.updateById(id, right)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val right = repository.findById(id)
        if (right != null) {
            val deletedRight = right.copy(deletedAt = Instant.now().toString())
            repository.updateById(id, deletedRight)
            return true
        }
        return false
    }
}