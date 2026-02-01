package Rights.DTO

import kotlinx.serialization.Serializable

@Serializable
data class RightFilter(
    var idRole: Int? = null,
    var name: String? = null,
    var deletedAt: String? = null,
)
