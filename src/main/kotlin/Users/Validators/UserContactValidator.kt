package Users.Validators

object UserContactValidator {
    private val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val phoneRegex = Regex("^\\+?[1-9][0-9]{7,14}$")

    fun isValidEmail(email: String): Boolean = emailRegex.matches(email.trim())

    fun isValidPhone(phone: String): Boolean {
        val normalized = phone.trim().replace(Regex("[\\s()\\-]"), "")
        return phoneRegex.matches(normalized)
    }

    fun normalizePhone(phone: String): String = phone.trim().replace(Regex("[\\s()\\-]"), "")
}
