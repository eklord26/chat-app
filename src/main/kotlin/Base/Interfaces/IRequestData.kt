package Base.Interfaces

import Tokens.DTO.Token

interface IRequestData {
    public suspend fun decode(token: Token): Unit

    public suspend fun encode(token: Token): Unit
}