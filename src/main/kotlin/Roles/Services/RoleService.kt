package Roles.Services

import Roles.Constants.ChatRoleNames
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

    suspend fun findActiveByName(name: String): Role? = roleRepository.findByFilter(
        RoleFilter(name = name, isDeleted = false)
    ).filterNotNull().firstOrNull { it.name == name }

    suspend fun requireActiveByName(name: String): Role {
        findActiveByName(name)?.let { return it }
        roleRepository.create(Role(name = name))
        return findActiveByName(name) ?: error("Required role was not found: $name")
    }

    suspend fun participantRoleId(): Int = requireActiveByName(ChatRoleNames.PARTICIPANT).id
        ?: error("Participant role has no ID")

    suspend fun administratorRoleId(): Int = requireActiveByName(ChatRoleNames.ADMINISTRATOR).id
        ?: error("Administrator role has no ID")
}
