package Messages.DTO

import ChatMembers.DTO.ChatMember
import Web.DTO.MessageEndpointDTO
import Web.DTO.UserEndpointDTO

data class CreatedMessageResult(
    val message: Message,
    val member: ChatMember,
    val sender: UserEndpointDTO?,
    val endpointMessage: MessageEndpointDTO
)
