package Passwords.Services

import Passwords.Builders.PasswordHashBuilder
import Passwords.Interfaces.IPasswordHashBuilder
import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordService () {
    private lateinit var hashBuilder: IPasswordHashBuilder

    public fun createHashPassword(password: String): String {
        hashBuilder = PasswordHashBuilder(password)
        return hashBuilder.build()
    }

    public fun checkHashPassword(hash: String, password: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), hash.toCharArray()).verified
    }
}