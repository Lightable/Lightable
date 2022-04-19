package rebase.interfaces.Image

/**
 * Interface for avatar
 * @param size Size of the avatar (In Bytes)
 * @param connectedUser What user id this avatar represents
 */
interface AvatarImpl : ImageImpl {
    val size: Long
    val connectedUser: String
}