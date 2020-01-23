package red.binder.mildom.user.model

data class UserResponse (
    var id: Long,
    var name: String,
    var official: Boolean,
    var fans: Long
)
