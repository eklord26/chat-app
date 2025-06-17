package Roles.Repositories

import Base.Interfaces.IBaseRepository
import Roles.DAO.RoleDAO
import Roles.DAO.daoToModel
import Roles.DTO.Role
import com.example.Base.Helpers.suspendTransaction

class RoleRepository: IBaseRepository<Role> {
    override suspend fun findById(id: Int): Role? = suspendTransaction {
        daoToModel(RoleDAO.findById(id))
    }

    override suspend fun findAll(): List<Role?> = suspendTransaction {
        RoleDAO.all().map(::daoToModel)
    }

    override suspend fun updateById(id: Int, entity: Role): Unit = suspendTransaction {
        RoleDAO.findByIdAndUpdate(
            id,
            {
                    it: RoleDAO ->
                it.name = entity.name
                it.deleted = entity.deleted
            }
        )
    }

    override suspend fun create(entity: Role): Unit = suspendTransaction {
        RoleDAO.new {
            name = entity.name
            deleted = entity.deleted
        }
    }
}