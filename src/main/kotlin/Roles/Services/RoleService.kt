package Roles.Services

import Roles.DTO.Role
import Roles.DTO.RoleFilter
import Roles.Repositories.RoleRepository
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class RoleService {
    private val roleRepository = RoleRepository()

    suspend fun create(role: Role): Int? {
        roleRepository.create(role)
        val filter = RoleFilter(name = role.name)
        return roleRepository.findByFilter(filter).firstOrNull()?.id
    }

    suspend fun update(id: Int, role: Role): Boolean {
        if (roleRepository.findById(id) != null) {
            roleRepository.updateById(id, role)
            return true
        }
        return false
    }

    suspend fun softDelete(id: Int): Boolean {
        val role = roleRepository.findById(id)
        if (role != null) {
            val deletedRole = role.copy(
                deletedAt = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            )
            roleRepository.updateById(id, deletedRole)
            return true
        }
        return false
    }

    suspend fun findById(id: Int): Role? = roleRepository.findById(id)

    suspend fun findByFilter(filter: RoleFilter): List<Role?> = roleRepository.findByFilter(filter)
}