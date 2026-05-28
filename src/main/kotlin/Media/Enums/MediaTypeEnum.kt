package Media.Enums

enum class MediaTypeEnum(val value: String, val extensions: Set<String>) {
    PHOTO("photo", setOf("jpg", "jpeg", "png", "webp", "gif")),
    VIDEO("video", setOf("mp4", "webm", "mov")),
    DOCUMENT(
        "document",
        setOf("pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "rtf", "odt", "ods", "odp", "zip", "rar", "7z")
    );

    companion object {
        fun byExtension(extension: String): MediaTypeEnum? =
            entries.firstOrNull { extension.lowercase() in it.extensions }
    }
}
