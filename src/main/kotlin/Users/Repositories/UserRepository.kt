package com.example.Users.Repository

import Base.Interfaces.IBaseRepository
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserDAO
import com.example.Users.DAO.UserTable
import com.example.Users.DAO.daoToModel
import com.example.Users.DTO.User
import com.example.Users.Interfaces.IUserRepository


class UserRepository: IUserRepository, IBaseRepository<User> {
    override suspend fun findById(id: Int): User? = suspendTransaction {
        daoToModel(UserDAO.findById(id))
    }

    override suspend fun findByLogin(login: String): User? = suspendTransaction {
        UserDAO
            .find { (UserTable.login eq login) }
            .map(::daoToModel)[0]
    }

    override suspend fun findAll(): List<User?> = suspendTransaction {
        UserDAO.all().map(::daoToModel)
    }

    override suspend fun create(user: User): Unit = suspendTransaction {
        UserDAO.new {
            name = user.name
            login = user.login
            passwordHash = user.passwordHash
            isAdmin = user.isAdmin
            deleted = user.deleted
        }
    }

    override suspend fun updateById(id: Int, entity: User): Unit = suspendTransaction {
        UserDAO.findByIdAndUpdate(
            id,
            { it: UserDAO ->
                it.name = entity.name
                it.login = entity.login
                it.passwordHash = entity.passwordHash
                it.isAdmin = entity.isAdmin
                it.deleted = entity.deleted
            }
        )
    }
}