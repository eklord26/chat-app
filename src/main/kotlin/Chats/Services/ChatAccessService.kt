package Chats.Services

import ChatMembers.DTO.ChatMember
import ChatMembers.DTO.ChatMemberFilter
import ChatMembers.Services.ChatMemberService
import io.ktor.server.application.ApplicationEnvironment

class ChatAccessService(environment: ApplicationEnvironment? = null) {
    private val chatService = ChatService(environment)
    private val chatMemberService = ChatMemberService(environment)

    suspend fun activeMember(chatId: Int, userId: Int): ChatMember? =
        chatMemberService.findByFilter(
            ChatMemberFilter(idChat = chatId, idUser = userId, isDeleted = false)
        ).filterNotNull().firstOrNull()

    suspend fun activeMembers(chatId: Int): List<ChatMember> =
        chatMemberService.findByFilter(
            ChatMemberFilter(idChat = chatId, isDeleted = false)
        ).filterNotNull()

    suspend fun requireActiveMember(chatId: Int, userId: Int): ChatMember? {
        val chat = chatService.findById(chatId)
        if (chat == null || chat.deletedAt != null) return null
        return activeMember(chatId, userId)
    }

    suspend fun userActiveChatIds(userId: Int): List<Int> =
        chatMemberService.findByFilter(
            ChatMemberFilter(idUser = userId, isDeleted = false)
        ).filterNotNull().map { it.idChat }.distinct()
}
