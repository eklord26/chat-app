package Rights.Repositories

import Base.Interfaces.IBaseRepository
import Rights.DAO.RightDAO
import Rights.DAO.daoToModel
import Rights.DTO.Right
import com.example.Base.Helpers.suspendTransaction

class RightRepository: IBaseRepository<Right> {
    override suspend fun findById(id: Int): Right? = suspendTransaction {
        daoToModel(RightDAO.findById(id))
    }

    override suspend fun findAll(): List<Right?> = suspendTransaction {
        RightDAO.all().map(::daoToModel)
    }

    override suspend fun updateById(id: Int, entity: Right): Unit = suspendTransaction {
        RightDAO.findByIdAndUpdate(
            id,
            {
                it: RightDAO->
                it.name = entity.name
                it.idRole = entity.idRole
                it.deleted = entity.deleted
            }
        )
    }

    override suspend fun create(entity: Right): Unit = suspendTransaction {
        RightDAO.new {
            name = entity.name
            idRole = entity.idRole
            deleted = entity.deleted
        }
    }
}