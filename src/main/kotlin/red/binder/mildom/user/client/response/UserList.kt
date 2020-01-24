package red.binder.mildom.user.client.response

import com.google.gson.annotations.SerializedName

data class UserList(
        @SerializedName("users")
        var users: List<User>
)