package Registration

import Base.Interfaces.IRequestData
import kotlinx.serialization.Serializable

@Serializable
data class RegisterBodyDTO(
    var username: String,
    var password: String,
    var login: String
): IRequestData {
    public override fun decode()
    {

    }
}
