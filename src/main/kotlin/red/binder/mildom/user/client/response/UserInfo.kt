package red.binder.mildom.user.client.response

import com.google.gson.annotations.SerializedName

data class UserInfo(

    @SerializedName("user_info")
    var userInfo: User?
)