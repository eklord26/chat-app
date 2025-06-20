package Base.Interfaces

interface IEncryptionHelper {
    public fun code(data: String, key: String): String
    public fun decode(data: String, key: String): String
}