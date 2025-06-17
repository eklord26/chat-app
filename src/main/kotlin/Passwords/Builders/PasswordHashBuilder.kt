package Passwords.Builders

import Base.Application.Configs
import Passwords.Interfaces.IPasswordHashBuilder
import at.favre.lib.crypto.bcrypt.BCrypt
import java.util.*


class PasswordHashBuilder(var password: String) : IPasswordHashBuilder  {

    private var hashConfig = Configs("hash").getConfig()

    private var cost: Int = hashConfig["defaultCost"]!!.toInt()

    override fun setHashCost(hashCost: Int) {
        this.cost = hashCost
    }

    override fun build(): String {
        return BCrypt.with(BCrypt.Version.VERSION_2B).hashToString(cost, password.toCharArray())
    }
}