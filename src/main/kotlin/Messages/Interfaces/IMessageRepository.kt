package Messages.Interfaces

import Messages.DTO.Message
import Messages.Enum.MessageTypeEnum

interface IMessageRepository {
    suspend fun findByValue(value: String): List<Message?>

    suspend fun findByType(type: MessageTypeEnum): List<Message?>

    suspend fun findByIdChatMember(idChatMember: Int): List<Message?>
}