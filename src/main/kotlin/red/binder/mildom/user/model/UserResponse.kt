package red.binder.mildom.user.model

data class UserResponse (
    var id: Long,
    var name: String,
    var official: Boolean,
    var status: Int,
    var fans: Long,
    var level: Long,
    var viewer: Long
)
