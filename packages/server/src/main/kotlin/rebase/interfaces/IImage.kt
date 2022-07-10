package rebase.interfaces

interface IImage {
    var height: Int
    var width: Int
    var path: String
    var isNSFW: NSFW
    enum class NSFW {
        YES,
        NO,
        UNKNOWN
    }
}