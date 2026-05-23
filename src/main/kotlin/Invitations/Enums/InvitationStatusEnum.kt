package Invitations.Enums

enum class InvitationStatusEnum(val value: String) {
    PENDING("pending"),
    ACCEPTED("accepted"),
    REJECTED("rejected"),
    CANCELLED("cancelled");

    companion object {
        fun normalize(status: String?): String? = status
            ?.trim()
            ?.lowercase()
            ?.takeIf { candidate -> entries.any { it.value == candidate } }
    }
}
