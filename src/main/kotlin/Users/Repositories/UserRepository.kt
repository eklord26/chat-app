package com.example.Users.Repository

import Base.Interfaces.IBaseRepository
import Users.DTO.UserFilter
import com.example.Base.Helpers.suspendTransaction
import com.example.Users.DAO.UserDAO
import com.example.Users.DAO.UserTable
import com.example.Users.DAO.daoToModel
import com.example.Users.DTO.User
import org.jetbrains.exposed.sql.Op
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.like
import org.jetbrains.exposed.sql.and


class UserRepository: IBaseRepository<User, UserFilter> {
    override suspend fun findById(id: Int): User? = suspendTransaction {
        daoToModel(UserDAO.findById(id))
    }

    override suspend fun findByFilter(filter: UserFilter): List<User?> = suspendTransaction {
        // 1. Создаем список условий
        val conditions = mutableListOf<Op<Boolean>>()

        filter.login?.let {
            conditions.add(UserTable.login eq it)
        }

        filter.name?.let {
            conditions.add(UserTable.name like "%$it%")
        }

        filter.isAdmin?.let {
            conditions.add(UserTable.isAdmin eq it)
        }

        filter.deleted?.let {
            conditions.add(UserTable.deleted eq it)
        }

        if (conditions.isEmpty()) {
            UserDAO.all().map(::daoToModel)
        } else {
            val finalOp = conditions.reduce { acc, op -> acc and op }
            UserDAO.find(finalOp).map(::daoToModel)
        }
    }

    override suspend fun findAll(): List<User?> = suspendTransaction {
        UserDAO.all().map(::daoToModel)
    }

    override suspend fun create(entity: User): Unit = suspendTransaction {
        UserDAO.new {
            name = entity.name
            login = entity.login
            passwordHash = entity.passwordHash
            isAdmin = entity.isAdmin
            deleted = entity.deleted
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