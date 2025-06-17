package Messages.Enum

enum class MessageTypeEnum (val string: String) {
    TEXT("text"),
    PHOTO("photo"),
    VIDEO("video"),
    FILE("file"),
    AUDIO("audio");

    companion object {
        public fun getEnumByType(type: String): MessageTypeEnum? {
            return enumValues<MessageTypeEnum>().find { it.string == type }
        }
    }
}