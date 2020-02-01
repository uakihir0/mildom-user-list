package red.binder.mildom.user.client.response

import com.google.gson.annotations.SerializedName

data class LiveList(
        @SerializedName("models")
        var models: List<User>
)