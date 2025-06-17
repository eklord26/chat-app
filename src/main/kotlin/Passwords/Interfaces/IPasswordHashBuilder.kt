package Passwords.Interfaces

import Passwords.Builders.PasswordHashBuilder

interface IPasswordHashBuilder
{
    public fun setHashCost(hashCost: Int)

    public fun build(): String
}